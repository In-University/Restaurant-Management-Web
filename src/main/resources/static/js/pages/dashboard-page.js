// bieu do reservation the hien cac trang thai theo ngay
let reservationData = /*[[${reservationData}]]*/ [];
// let reservationData = [
//     ["2024-12-05", "CONFIRMED", 3],
//     ["2024-12-06", "CANCELLED", 2],
//     ["2024-12-06", "PENDING", 7],
//     ["2024-12-07", "PENDING", 1]
// ];

// Chuẩn bị dữ liệu cho biểu đồ
let dates = [];
let confirmedCounts = [];
let cancelledCounts = [];
let pendingCounts = [];
let finishedCounts = [];

// Tạo một đối tượng chứa số lượng trạng thái cho từng ngày
let statusMap = {};

// Xử lý dữ liệu từ database
reservationData.forEach(function (entry) {
    let date = entry[0]; // Ngày
    let status = entry[1]; // Trạng thái
    let count = entry[2]; // Số lượng

    // Đảm bảo ngày có trong statusMap
    if (!statusMap[date]) {
        statusMap[date] = {
            CONFIRMED: 0,
            CANCELLED: 0,
            PENDING: 0,
            FINISHED: 0
        };
    }

    // Cập nhật số lượng theo trạng thái
    statusMap[date][status] = count;
});

// Lấy các ngày duy nhất và sắp xếp theo thứ tự
let allDates = Object.keys(statusMap);
allDates.sort();

// Cập nhật dữ liệu biểu đồ
allDates.forEach(function (date) {
    dates.push(date);
    confirmedCounts.push(statusMap[date].CONFIRMED);
    cancelledCounts.push(statusMap[date].CANCELLED);
    pendingCounts.push(statusMap[date].PENDING);
    finishedCounts.push(statusMap[date].FINISHED);
});

// Vẽ biểu đồ
let ctxa = document.getElementById('reservationsChart').getContext('2d');
let statusChart = new Chart(ctxa, {
    type: 'line',
    data: {
        labels: dates, // Các ngày làm trục hoành
        datasets: [{
            label: 'CONFIRMED',
            data: confirmedCounts, // Số lượng 'CONFIRMED'
            borderColor: '#4CAF50',
            fill: false,
            tension: 0.1
        }, {
            label: 'CANCELLED',
            data: cancelledCounts, // Số lượng 'CANCELLED'
            borderColor: '#FF5252',
            fill: false,
            tension: 0.1
        }, {
            label: 'PENDING',
            data: pendingCounts, // Số lượng 'PENDING'
            borderColor: '#FF9800',
            fill: false,
            tension: 0.1
        }, {
            label: 'FINISHED',
            data: finishedCounts, // Số lượng 'FINISHED'
            borderColor: '#673AB7',
            fill: false,
            tension: 0.1
        }]
    },
    options: {
        scales: {
            x: {
                title: {
                    display: true,
                    text: 'Date'
                }
            },
            y: {
                title: {
                    display: true,
                    text: 'Count'
                }
            }
        }
    }
});

// bieu do doanh thu , loi nhuan
const revenueAndProfit = /*[[${revenueAndProfit}]]*/ {};
// // Dữ liệu ví dụ
// const labels2 = ['2024-12-01', '2024-12-02', '2024-12-03', '2024-12-04', '2024-12-05']; // Ngày
// const costData = [1500, 1600, 1700, 1800, 2000]; // Tổng chi phí nguyên liệu
// const revenueData = [500, 600, 700, 800, 1000]; // Lợi nhuận

// Dữ liệu ví dụ
const labels2 = revenueAndProfit.dates; // Ngày
const costData = revenueAndProfit.totalCosts; // Tổng chi phí
const revenueData = revenueAndProfit.totalRevenues; // Tổng doanh thu

// Lấy đối tượng canvas
const ctx2 = document.getElementById('revenueProfitChart').getContext('2d');

// Vẽ biểu đồ đường
const revenueProfitChart = new Chart(ctx2, {
    type: 'line', // Chọn loại biểu đồ đường
    data: {
        labels: labels2, // Các nhãn (ngày)
        datasets: [
            {
                label: 'Tổng chi phí', // Nhãn cho đường chi phí nguyên liệu
                data: costData, // Dữ liệu chi phí nguyên liệu
                borderColor: 'rgba(255, 99, 132, 1)', // Màu đường chi phí nguyên liệu
                backgroundColor: 'rgba(255, 99, 132, 0.2)', // Màu nền
                fill: false, // Không điền màu vào giữa các điểm
                tension: 0.1 // Độ cong của đường
            },
            {
                label: 'Lợi nhuận', // Nhãn cho đường lợi nhuận
                data: revenueData, // Dữ liệu lợi nhuận
                borderColor: 'rgba(54, 162, 235, 1)', // Màu đường lợi nhuận
                backgroundColor: 'rgba(54, 162, 235, 0.2)', // Màu nền
                fill: false, // Không điền màu vào giữa các điểm
                tension: 0.1 // Độ cong của đường
            }
        ]
    },
    options: {
        responsive: true, // Đảm bảo biểu đồ đáp ứng với kích thước màn hình
        plugins: {
            legend: {
                position: 'top', // Vị trí của legend
            },
            tooltip: {
                mode: 'index', // Chế độ hiển thị tooltip
                intersect: false, // Tooltip xuất hiện khi di chuột trên một đường
            }
        },
        scales: {
            y: {
                beginAtZero: true, // Bắt đầu trục y từ 0
                title: {
                    display: true,
                    text: 'Giá trị' // Tiêu đề trục y
                }
            },
            x: {
                title: {
                    display: true,
                    text: 'Ngày' // Tiêu đề trục x
                }
            }
        }
    }
});

// bieu do orderStats
const orderStats = /*[[${orderStats}]]*/ {};
// Chuyển đổi dữ liệu để vẽ biểu đồ
const labels = Object.keys(orderStats); // Các ngày
const data = Object.values(orderStats); // Số lượng đơn hàng

// Vẽ biểu đồ
const ctx = document.getElementById('orderChart').getContext('2d');
const orderChart = new Chart(ctx, {
    type: 'line',
    data: {
        labels: labels,
        datasets: [{
            label: 'Số lượng đơn hàng',
            data: data,
            borderColor: 'rgba(75, 192, 192, 1)',
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            tension: 0.1
        }]
    }
});

//bieu do dish ban chay nhat

function generateRandomColors(num) {
    const colors = [];
    for (let i = 0; i < num; i++) {
        // Tạo các màu có độ sáng khác nhau và phân bố rộng hơn
        const r = Math.floor(Math.random() * 256);  // Đỏ
        const g = Math.floor(Math.random() * 256);  // Xanh lá
        const b = Math.floor(Math.random() * 256);  // Xanh dương

        // Tăng độ tương phản bằng cách đảm bảo rằng màu sắc có sự phân biệt rõ ràng
        const rgba = `rgba(${r}, ${g}, ${b}, 0.7)`;  // Giảm độ trong suốt xuống một chút để dễ nhìn hơn
        colors.push(rgba);
    }
    return colors;
}

const dishStats = /*[[${dishStats}]]*/ {};
const ctx1 = document.getElementById('dishChart').getContext('2d');
const orderChart1 = new Chart(ctx1, {
    type: 'pie',
    data: {
        labels:  Object.keys(dishStats),
        datasets: [{
            label: 'Số lượng đơn hàng',
            data: Object.values(dishStats),
            borderColor: 'rgba(230, 230, 250, 1)',
            backgroundColor: generateRandomColors(Object.keys(dishStats).length),
            tension: 0.1
        }]
    }
});

// Inventory Chart
let inventoryData = /*[[${inventoryData}]]*/ [];

// Tách dữ liệu thành các mảng riêng
let itemNames = inventoryData.map(item => item[0]); // Lấy tên mặt hàng
let quantities = inventoryData.map(item => item[1]); // Lấy số lượng hàng tồn kho

// Vẽ biểu đồ
let ctxInventory = document.getElementById('inventoryChart').getContext('2d');
new Chart(ctxInventory, {
    type: 'bar',
    data: {
        labels: itemNames, // Tên mặt hàng
        datasets: [{
            label: 'Số lượng hàng tồn kho',
            data: quantities, // Số lượng
            backgroundColor: 'rgba(54, 162, 235, 0.6)', // Màu cột
            borderColor: 'rgba(54, 162, 235, 1)', // Màu viền
            borderWidth: 1
        }]
    },
    options: {
        responsive: true,
        scales: {
            x: {
                title: {
                    display: true,
                    text: 'Tên mặt hàng'
                }
            },
            y: {
                beginAtZero: true,
                title: {
                    display: true,
                    text: 'Số lượng'
                }
            }
        },
        plugins: {
            legend: {
                display: true
            }
        }
    }
});