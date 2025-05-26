function handleShiftTypeChange() {
    const shiftType = document.getElementById('shiftType').value;

    document.getElementById('openShiftInputs').classList.add('hidden');
    document.getElementById('fixedShiftInputs').classList.add('hidden');

    if (shiftType === 'OPEN') {
        document.getElementById('openShiftInputs').classList.remove('hidden');
    } else if (shiftType === 'FIXED') {
        document.getElementById('fixedShiftInputs').classList.remove('hidden');
    }
}
document.getElementById('closeModalBtn').addEventListener('click', () => {
    document.getElementById('shiftModal').classList.add('hidden');
})
handleShiftTypeChange();
document.getElementById('addShiftBtn').onclick = () =>
    document.getElementById('shiftModal').classList.remove('hidden');