document.observe("dom:loaded", function() {
	prioritySorterGlobalConfUpdate();
});

function prioritySorterGlobalConfUpdate()
{
	strategy=document.getElementById('ps_strategy').value;
	allowPriorityOnJobs=document.getElementById('ps_allowPriorityOnJobs')
	defaultPriority=document.getElementById('ps_defaultPriority')
	numberOfPriorities=document.getElementById('ps_numberOfPriorities')
	if(strategy == 'FIFO') {
		allowPriorityOnJobs.disabled=true
		numberOfPriorities.disabled=true
		defaultPriority.disabled=true
	} else  {
		allowPriorityOnJobs.disabled=false
		numberOfPriorities.disabled=false
		defaultPriority.disabled=false
	}
}