document.addEventListener('DOMContentLoaded', () => {
    const salaryModal = new ModalWithForm('salaryModal', 'Salary', null, '/salary/update');

    document.querySelectorAll('.btnEditSalary').forEach(btn => {
        btn.addEventListener('click', () => {
            const data = {
                id: btn.getAttribute('data-id') || '',
                name: btn.getAttribute('data-name') || '',
                totalHoursWorked: btn.getAttribute('data-totalHoursWorked') || '',
                hourlyRate: btn.getAttribute('data-hourlyRate') || '',
                bonus: btn.getAttribute('data-bonus') || '',
                totalSalary: btn.getAttribute('data-totalSalary') || '',
                employeeId: btn.getAttribute('data-employeeId') || ''
            };
            salaryModal.open(data);
        });
    });
});
