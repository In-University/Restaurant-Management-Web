document.addEventListener('DOMContentLoaded', () => {
    const inventoryModal = new ModalWithForm('inventoryModal', "Inventory", '/inventories/add', '/inventories/edit');
    const btnAdd = document.getElementById('btnAddInventory');
    btnAdd.addEventListener('click', () => {
        inventoryModal.open(null);
    });

    document.querySelectorAll('.btnEditInventory').forEach(button => {
        button.addEventListener('click', () => {
            const data = {
                id: button.getAttribute('data-id') || "",
                itemName: button.getAttribute('data-itemName') || "",
                unit: button.getAttribute('data-unit') || "",
                quantity: button.getAttribute('data-quantity') || "",
                unitPrice: button.getAttribute('data-unitPrice') || "",
                supplier: button.getAttribute('data-supplier') || "",
                description: button.getAttribute('data-description') || ""
            };
            inventoryModal.open(data);
        });
    });
});
