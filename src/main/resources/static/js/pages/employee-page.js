// employee-page.js

document.addEventListener('DOMContentLoaded', () => {
    const employeeModal = new ModalWithForm('employeeModal', 'Employee', '/employees/add', '/employees/edit');

    // Nút Add Employee
    document.getElementById('btnAddEmployee')
        .addEventListener('click', () => employeeModal.open(null));

    // Các nút Edit Employee
    document.querySelectorAll('.btnEditEmployee').forEach(btn => {
        btn.addEventListener('click', () => {
            const data = {
                id: btn.getAttribute('data-id') || '',
                name: btn.getAttribute('data-name') || '',
                phone: btn.getAttribute('data-phone') || '',
                email: btn.getAttribute('data-email') || '',
                imageUrl: btn.getAttribute('data-image') || '',
                position: btn.getAttribute('data-position') || ''
            };
            employeeModal.open(data);
        });
    });

    // Confirm xóa
    document.querySelectorAll('.btnDeleteEmployee').forEach(link => {
        link.addEventListener('click', e => {
            if (!confirm('Are you sure you want to delete this employee?')) {
                e.preventDefault();
            }
        });
    });
});
