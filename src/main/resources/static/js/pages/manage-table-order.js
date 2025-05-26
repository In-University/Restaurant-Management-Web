document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.table-card').forEach(card => {
        card.addEventListener('click', () => {
            const tableId = card.dataset.tableId;
            const status  = card.dataset.tableStatus;

            document.querySelectorAll('.border-orange-500')
                .forEach(el => el.classList.remove('border-orange-500'));
            card.classList.add('border-orange-500');

            window.location.href = `/orders/manage?tableId=${tableId}&status=${status}`;
        });
    });
});
