document.addEventListener('DOMContentLoaded', () => {
    const tableModal = new ModalWithForm('tableModal', 'Table', '/tables/add', '/tables/edit');

    document.getElementById('btnAddTable').addEventListener('click', () => {
        tableModal.open(null);
    });

    document.querySelectorAll('.btnEditTable').forEach(btn => {
        btn.addEventListener('click', () => {
            const data = {
                id: btn.getAttribute('data-id') || '',
                tableNumber: btn.getAttribute('data-number') || '',
                capacity: btn.getAttribute('data-capacity') || '',
                status: btn.getAttribute('data-status') || '',
                imageUrl: btn.getAttribute('data-imageUrl') || ''
            };
            tableModal.open(data);
        });
    });

    document.querySelectorAll('.btnDeleteTable').forEach(link => {
        link.addEventListener('click', e => {
            if (!confirm('Are you sure you want to delete this table?')) {
                e.preventDefault();
            }
        });
    });
});
