document.addEventListener('DOMContentLoaded', function() {
    const autoSubmitSelects = document.querySelectorAll('.auto-submit-select');
    autoSubmitSelects.forEach(selectElement => {
        if (!selectElement.disabled) {
            selectElement.addEventListener('change', function() {
                if (this.form) {
                    this.form.submit();
                }
            });
        }
    });
});
