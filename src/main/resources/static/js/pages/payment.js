document.addEventListener('DOMContentLoaded', () => {
    let selectedPaymentMethod = null;
    let orderIdForCashRedirect = null;
    const paymentOptionsContainer = document.getElementById('paymentOptions');
    const payButton = document.getElementById('payButton'); // Đây là thẻ <a>

    function handleCashSelection(element) {
        const action = element.getAttribute('data-staff-action');
        const orderIdAttr = element.getAttribute('data-order-id');

        if (action === 'alert') {
            console.log('Cash selected by customer. Alert will be shown on submit if needed.');
        } else if (action === 'redirect' && orderIdAttr) {
            orderIdForCashRedirect = orderIdAttr;
            window.location.href = `/orders/completed/${orderIdForCashRedirect}`;
        }
    }

    if (paymentOptionsContainer) {
        paymentOptionsContainer.addEventListener('click', function(event) {
            const optionElement = event.target.closest('.payment-option[data-method]');

            if (optionElement) {
                const allOptions = paymentOptionsContainer.querySelectorAll('.payment-option[data-method]');
                allOptions.forEach(opt => {
                    opt.classList.remove('border-blue-500', 'bg-blue-50');
                });

                optionElement.classList.add('border-blue-500', 'bg-blue-50');
                selectedPaymentMethod = optionElement.getAttribute('data-method');
                orderIdForCashRedirect = null;
                if (selectedPaymentMethod === 'CASH') {
                    handleCashSelection(optionElement);
                }
            }
        });
    }

    if (payButton) {
        payButton.addEventListener('click', function(event) {
            if (selectedPaymentMethod == null || selectedPaymentMethod === "") {
                event.preventDefault(); // Ngăn thẻ <a> điều hướng
                alert('Please select a payment method.');
                return;
            }

            const customerCashOption = document.querySelector('.payment-option[data-method="CASH"][data-staff-action="alert"]');
            if (selectedPaymentMethod === 'CASH' && customerCashOption && customerCashOption.classList.contains('border-blue-500')) {
                event.preventDefault();
                alert('Please call the staff to complete the payment. Payment will be processed upon confirmation by staff.');
                return;
            }

            if (selectedPaymentMethod === 'VNPAY') {
                const currentHref = payButton.getAttribute('href');
                if (currentHref) {
                    const url = new URL(currentHref, window.location.origin);
                    url.searchParams.set('method', 'VNPAY');
                    payButton.setAttribute('href', url.pathname + url.search);
                }
            }
        });
    }
});