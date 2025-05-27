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

    public static final int MAX_ATTEMPT_PER_IP = 2; // Ví dụ: 5 lần thử sai tối đa cho một IP
    public static final int IP_LOCK_DURATION_MINUTES = 1; // Ví dụ: Khóa IP trong 15 phút

    private final LoadingCache<String, Integer> ipAttemptsCache;
    private final LoadingCache<String, Long> ipLockoutCache;

    public LoginAttemptService() {
        super();
        ipAttemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(IP_LOCK_DURATION_MINUTES * 2, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });

        ipLockoutCache = CacheBuilder.newBuilder()
                .expireAfterWrite(IP_LOCK_DURATION_MINUTES, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Long>() {
                    @Override
                    public Long load(String key) {
                        return 0L;
                    }
                });
    }

    private String getClientIP() {
        if (RequestContextHolder.getRequestAttributes() == null) {
            System.err.println("WARNING: RequestContextHolder.getRequestAttributes() is null. Cannot determine client IP for LoginAttemptService.");
            return "UNKNOWN_IP_CONTEXT";
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }


    public void loginSucceeded() {
        String clientIP = getClientIP();
        if ("UNKNOWN_IP_CONTEXT".equals(clientIP)) {
            return;
        }
        ipAttemptsCache.invalidate(clientIP);
        ipLockoutCache.invalidate(clientIP);
        System.out.println("Login successful from IP: " + clientIP + ". IP attempts and lockout status reset.");
    }

    public void loginFailed() {
        String clientIP = getClientIP();
        if ("UNKNOWN_IP_CONTEXT".equals(clientIP)) {
            return;
        }

        int attempts;
        try {
            attempts = ipAttemptsCache.get(clientIP);
        } catch (ExecutionException e) {
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

    public boolean isIpBlocked() {
        String clientIP = getClientIP();
        if ("UNKNOWN_IP_CONTEXT".equals(clientIP)) {
            return false;
        }

        try {
            Long lockoutEndTime = ipLockoutCache.get(clientIP);
            if (lockoutEndTime == null || lockoutEndTime == 0L) {
                return false;
            }

            boolean isStillLocked = System.currentTimeMillis() < lockoutEndTime;

            if (!isStillLocked) {
                ipLockoutCache.invalidate(clientIP);
                ipAttemptsCache.invalidate(clientIP);
                System.out.println("IP " + clientIP + " auto-unlocked as lockout time expired (checked during isIpBlocked).");
                return false;
            }
            System.out.println("IP " + clientIP + " is still locked. Lockout ends at: " + new java.util.Date(lockoutEndTime));
            return true;
        } catch (ExecutionException e) {
            System.err.println("Error checking if IP " + clientIP + " is blocked: " + e.getMessage());
            return false;
        }
    }

    public String getFormattedIpLockoutDurationRemaining() {
        String clientIP = getClientIP();
        if ("UNKNOWN_IP_CONTEXT".equals(clientIP)) {
            return "an unspecified period";
        }

        try {
            Long lockoutEndTime = ipLockoutCache.get(clientIP);

            if (lockoutEndTime == null || lockoutEndTime == 0L) {
                return "0 seconds";
            }

            long remainingMillis = lockoutEndTime - System.currentTimeMillis();

            if (remainingMillis <= 0) {
                if (ipLockoutCache.asMap().containsKey(clientIP)) {
                    ipLockoutCache.invalidate(clientIP);
                    ipAttemptsCache.invalidate(clientIP);
                    System.out.println("IP " + clientIP + " explicitly unlocked (checked during getFormattedIpLockoutDurationRemaining).");
                }
                return "0 seconds";
            }

            long totalSecondsRemaining = TimeUnit.MILLISECONDS.toSeconds(remainingMillis);
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
                if (remainingMillis > 0) return "less than a second";
                return "0 seconds";
            }

            return sb.toString();

        } catch (ExecutionException e) {
            System.err.println("ExecutionException while getting formatted IP lockout duration for " + clientIP + ": " + e.getMessage());
            return "an unspecified period";
        }
    }
}
