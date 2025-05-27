package com.restaurant.management.config.security.prevent_fuzz;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    // Cấu hình số lần thử và thời gian khóa IP
    public static final int MAX_ATTEMPT_PER_IP = 2; // Ví dụ: 5 lần thử sai tối đa cho một IP
    public static final int IP_LOCK_DURATION_MINUTES = 1; // Ví dụ: Khóa IP trong 15 phút

    // Cache cho số lần thử sai của IP
    // Key: Địa chỉ IP (String), Value: Số lần thử sai (Integer)
    private final LoadingCache<String, Integer> ipAttemptsCache;

    // Cache cho trạng thái khóa của IP
    // Key: Địa chỉ IP (String), Value: Thời điểm hết khóa (Long - timestamp)
    private final LoadingCache<String, Long> ipLockoutCache;

    public LoginAttemptService() {
        super();
        // Khởi tạo cache số lần thử, tự động xóa entry sau một khoảng thời gian (lâu hơn thời gian khóa)
        ipAttemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(IP_LOCK_DURATION_MINUTES * 2, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) {
                        return 0; // Giá trị mặc định nếu IP chưa có trong cache
                    }
                });

        // Khởi tạo cache thời gian khóa, tự động xóa entry sau khi hết thời gian khóa
        ipLockoutCache = CacheBuilder.newBuilder()
                .expireAfterWrite(IP_LOCK_DURATION_MINUTES, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Long>() {
                    @Override
                    public Long load(String key) {
                        return 0L; // 0L nghĩa là IP không bị khóa hoặc cache entry đã hết hạn
                    }
                });
    }

    /**
     * Lấy địa chỉ IP của client từ HttpServletRequest hiện tại.
     * Ưu tiên header 'X-Forwarded-For' nếu có (cho các trường hợp đi qua proxy).
     * @return Địa chỉ IP của client, hoặc "UNKNOWN_IP_CONTEXT" nếu không thể xác định.
     */
    private String getClientIP() {
        if (RequestContextHolder.getRequestAttributes() == null) {
            System.err.println("WARNING: RequestContextHolder.getRequestAttributes() is null. Cannot determine client IP for LoginAttemptService.");
            return "UNKNOWN_IP_CONTEXT"; // Trả về giá trị đặc biệt để xử lý
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        // Nếu X-Forwarded-For có nhiều IP (client, proxy1, proxy2), lấy IP đầu tiên (client IP)
        return xfHeader.split(",")[0].trim();
    }

    /**
     * Được gọi khi đăng nhập thành công. Reset số lần thử sai và trạng thái khóa cho IP hiện tại.
     */
    public void loginSucceeded() {
        String clientIP = getClientIP();
        if ("UNKNOWN_IP_CONTEXT".equals(clientIP)) {
            return; // Không làm gì nếu không xác định được IP
        }
        ipAttemptsCache.invalidate(clientIP);
        ipLockoutCache.invalidate(clientIP); // Cũng xóa khỏi cache khóa nếu IP này từng bị khóa
        System.out.println("Login successful from IP: " + clientIP + ". IP attempts and lockout status reset.");
    }

    /**
     * Được gọi khi đăng nhập thất bại. Tăng số lần thử sai cho IP hiện tại.
     * Nếu vượt ngưỡng, khóa IP.
     */
    public void loginFailed() {
        String clientIP = getClientIP();
        if ("UNKNOWN_IP_CONTEXT".equals(clientIP)) {
            return; // Không làm gì nếu không xác định được IP
        }

        int attempts;
        try {
            attempts = ipAttemptsCache.get(clientIP);
        } catch (ExecutionException e) {
            // Lỗi khi lấy từ cache, coi như là lần thử đầu
            attempts = 0;
        }
        attempts++;
        System.out.println("Login failed from IP: " + clientIP + ". Attempt count for this IP: " + attempts);
        ipAttemptsCache.put(clientIP, attempts);

        if (attempts >= MAX_ATTEMPT_PER_IP) {
            long lockoutEndTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(IP_LOCK_DURATION_MINUTES);
            ipLockoutCache.put(clientIP, lockoutEndTime);
            System.out.println("IP address " + clientIP + " locked for " + IP_LOCK_DURATION_MINUTES + " minutes. Lockout ends at: " + new java.util.Date(lockoutEndTime));
        }
    }

    /**
     * Kiểm tra xem địa chỉ IP hiện tại có đang bị khóa không.
     * @return true nếu IP bị khóa, false nếu không.
     */
    public boolean isIpBlocked() {
        String clientIP = getClientIP();
        if ("UNKNOWN_IP_CONTEXT".equals(clientIP)) {
            return false; // Nếu không xác định được IP, coi như không bị khóa để tránh chặn oan
        }

        try {
            Long lockoutEndTime = ipLockoutCache.get(clientIP); // Sẽ gọi loader nếu key không có hoặc đã expire
            if (lockoutEndTime == null || lockoutEndTime == 0L) { // Loader trả về 0L
                return false; // IP không bị khóa hoặc cache đã hết hạn
            }

            // lockoutEndTime là thời điểm IP được mở khóa
            boolean isStillLocked = System.currentTimeMillis() < lockoutEndTime;

            if (!isStillLocked) {
                // Thời gian khóa đã hết. Invalidate cache entry để lần kiểm tra sau không cần tính toán lại.
                ipLockoutCache.invalidate(clientIP);
                ipAttemptsCache.invalidate(clientIP); // Reset cả số lần thử
                System.out.println("IP " + clientIP + " auto-unlocked as lockout time expired (checked during isIpBlocked).");
                return false;
            }
            System.out.println("IP " + clientIP + " is still locked. Lockout ends at: " + new java.util.Date(lockoutEndTime));
            return true;
        } catch (ExecutionException e) {
            // Lỗi khi cố gắng load từ cache, coi như không bị khóa để tránh chặn oan
            System.err.println("Error checking if IP " + clientIP + " is blocked: " + e.getMessage());
            return false;
        }
    }

    /**
     * Trả về chuỗi định dạng thời gian khóa còn lại cho IP hiện tại (ví dụ: "1 minute and 30 seconds").
     * @return Chuỗi thời gian, hoặc "0 seconds" nếu không bị khóa/đã hết hạn.
     */
    public String getFormattedIpLockoutDurationRemaining() {
        String clientIP = getClientIP();
        if ("UNKNOWN_IP_CONTEXT".equals(clientIP)) {
            return "an unspecified period"; // Thông báo chung nếu không xác định được IP
        }

        try {
            Long lockoutEndTime = ipLockoutCache.get(clientIP);

            if (lockoutEndTime == null || lockoutEndTime == 0L) {
                return "0 seconds"; // Không bị khóa hoặc cache đã dọn dẹp
            }

            long remainingMillis = lockoutEndTime - System.currentTimeMillis();

            if (remainingMillis <= 0) {
                // Thời gian đã hết. `isIpBlocked()` nên đã invalidate cache.
                // Invalidate lại ở đây để chắc chắn.
                if (ipLockoutCache.asMap().containsKey(clientIP)) {
                    ipLockoutCache.invalidate(clientIP);
                    ipAttemptsCache.invalidate(clientIP);
                    System.out.println("IP " + clientIP + " explicitly unlocked (checked during getFormattedIpLockoutDurationRemaining).");
                }
                return "0 seconds";
            }

            long totalSecondsRemaining = TimeUnit.MILLISECONDS.toSeconds(remainingMillis);
            // Nếu còn vài trăm ms, làm tròn lên 1 giây để hiển thị thân thiện hơn
            if (totalSecondsRemaining == 0 && remainingMillis > 0) {
                totalSecondsRemaining = 1;
            }

            long minutes = totalSecondsRemaining / 60;
            long seconds = totalSecondsRemaining % 60;

            StringBuilder sb = new StringBuilder();
            if (minutes > 0) {
                sb.append(minutes).append(minutes == 1 ? " minute" : " minutes");
            }

            if (seconds > 0) {
                if (sb.length() > 0) {
                    sb.append(" and ");
                }
                sb.append(seconds).append(seconds == 1 ? " second" : " seconds");
            }

            if (sb.length() == 0) {
                // Điều này xảy ra nếu remainingMillis <= 0 và đã được xử lý ở trên trả về "0 seconds"
                // Hoặc nếu remainingMillis rất nhỏ (ví dụ 10ms) khiến totalSecondsRemaining = 0
                if (remainingMillis > 0) return "less than a second";
                return "0 seconds"; // Trường hợp an toàn
            }

            return sb.toString();

        } catch (ExecutionException e) {
            System.err.println("ExecutionException while getting formatted IP lockout duration for " + clientIP + ": " + e.getMessage());
            return "an unspecified period"; // Hoặc một thông báo lỗi khác
        }
    }
}