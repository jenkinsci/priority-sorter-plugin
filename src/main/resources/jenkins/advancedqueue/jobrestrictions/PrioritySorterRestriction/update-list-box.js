Behaviour.specify("#fromPriority", "PrioritySorterRestriction_updateListBox", 0, function (element) {
    element.addEventListener("change", function (event) {
        const target = event.target;
        const descriptorUrl = target.dataset.descriptorUrl;
        const toPriority = document.querySelector("#toPriority");

        updateListBox(toPriority, `${descriptorUrl}/updateFromPriorityItems?value=${target.value}`);
    });
});
