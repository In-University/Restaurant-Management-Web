function openModal(button) {
    const modal = new ModalWithForm('customerModal', "Customers", '/customers/add', '/customers/edit');
    let data = null;
    if (button) {
        data = {
            id: button.getAttribute('data-id') || "",
            firstName: button.getAttribute('data-firstname') || "",
            lastName: button.getAttribute('data-lastname') || "",
            email: button.getAttribute('data-email') || "",
            phone: button.getAttribute('data-phone') || "",
            address: button.getAttribute('data-address') || "",
            password: button.getAttribute('data-password') || ""
        };
    }
    modal.open(data);
}

function closeModal() {
    const modal = new ModalWithForm('customerModal', "Customers", '/customers/add', '/customers/edit');
    modal.close();
}

document.addEventListener('DOMContentLoaded', function () {
    const addBtn = document.getElementById('addCustomerBtn');
    if (addBtn) {
        addBtn.addEventListener('click', function () {
            openModal(null);
        });
    }

    document.querySelectorAll('.customer-edit-btn').forEach(button => {
        button.addEventListener('click', function () {
            openModal(this);
        });
    });

    document.querySelectorAll('.delete-customer-form').forEach(form => {
        form.addEventListener('submit', function (e) {
            if (!confirm('Are you sure you want to delete this customer?')) {
                e.preventDefault();
            }
        });
    });
});
