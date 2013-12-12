document.observe("dom:loaded", function() {
	prioritySorterGlobalConfUpdate();
});

Element.prototype.triggerEvent = function(eventName)
{
    if (document.createEvent)
    {
        var evt = document.createEvent('HTMLEvents');
        evt.initEvent(eventName, true, true);

        return this.dispatchEvent(evt);
    }

    if (this.fireEvent)
        return this.fireEvent('on' + eventName);
}

function prioritySorterGlobalConfUpdate()
{
	$('ps_numberOfPriorities').triggerEvent('change')
}