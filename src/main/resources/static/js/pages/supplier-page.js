document.addEventListener('DOMContentLoaded', () => {
    const supplierModal = new ModalWithForm('supplierModal', 'Supplier', '/suppliers/add', '/suppliers/edit');

    document.getElementById('btnAddSupplier')
        .addEventListener('click', () => supplierModal.open(null));

    document.querySelectorAll('.btnEditSupplier').forEach(btn => {
        btn.addEventListener('click', () => {
            const data = {
                id: btn.getAttribute('data-id') || '',
                name: btn.getAttribute('data-name') || '',
                phone: btn.getAttribute('data-phone') || '',
                email: btn.getAttribute('data-email') || '',
                address: btn.getAttribute('data-address') || '',
                notes: btn.getAttribute('data-notes') || ''
            };
            supplierModal.open(data);
        });
    });

    document.querySelectorAll('.formDeleteSupplier').forEach(form => {
        form.addEventListener('submit', e => {
            if (!confirm('Are you sure you want to delete this supplier?')) {
                e.preventDefault();
            }
        });
    });
});