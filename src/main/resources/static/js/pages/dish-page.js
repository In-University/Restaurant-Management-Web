document.addEventListener('DOMContentLoaded', () => {
    const dishModal = new ModalWithForm('dishModal', 'Dish', '/dishes/add', '/dishes/edit');

    document.getElementById('btnAddDish')
        .addEventListener('click', () => dishModal.open(null));

    document.querySelectorAll('.btnEditDish').forEach(btn => {
        btn.addEventListener('click', () => {
            const data = {
                id: btn.getAttribute('data-id') || '',
                name: btn.getAttribute('data-name') || '',
                description: btn.getAttribute('data-description') || '',
                price: btn.getAttribute('data-price') || '',
                'category.id': btn.getAttribute('data-category-id') || '',
                imageUrl: btn.getAttribute('data-image-url') || ''
            };
            dishModal.open(data);
        });
    });

    document.querySelectorAll('.btnDeleteDish').forEach(link => {
        link.addEventListener('click', e => {
            if (!confirm('Are you sure you want to delete this dish?')) {
                e.preventDefault();
            }
        });
    });
});
