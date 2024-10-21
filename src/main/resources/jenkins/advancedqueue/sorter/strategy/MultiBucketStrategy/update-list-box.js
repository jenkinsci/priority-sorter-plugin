Behaviour.specify(".ps_numberOfPriorities", "MultiBucketStrategy_updateListBox", 0, function (element) {
    element.addEventListener("change", function (event) {
        const target = event.target;
        const descriptorKey = target.dataset.descriptorKey;
        const descriptorUrl = target.dataset.descriptorUrl;
        const defaultPriority = document.querySelector(`#ps_defaultPriority-${descriptorKey}`);

        updateListBox(defaultPriority, `${descriptorUrl}/updateDefaultPriorityItems?value=${target.value}`);
    });
});
