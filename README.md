
# Priority Sorter

This plugin adds the ability to assign different priorities to Jobs, the lower priority the job has
the soon the Job will run. 

This can be very helpful when one wants to add low priority jobs but wants to have higher-priority jobs run first when hardware is limited or when there are different groups of Jobs that should share resources (equally).

## Queue Strategies

There are four included Queue Strategies, the different Queue Strategies will sort the Queue
differently.

### First In First Out

This the normal way Jenkins sorts the Queue, the Queue is sorted based on when the Jobs is added to the Queue, Jobs will be run in the same order as they enter the Queue.

### Absolute

Each Jobs will be assigned a Priority, the lower the Priority the sooner the Jobs will be run.

### Fair Queuing

Each Jobs will be assigned a Priority, the Queue will try to share the resources equally over the different prioritos in a round-robin fashion.

### Weighted Fair Queuing

The same as Fair Queuing but Jobs with lower Priority will be run more frequently than Jobs with higher Priority.

## Assigning Priorities

Priorities can either be assigned directly on Jobs or, preferably, by assigning them by View.

## Priority Strategies

It is possibly to assign the Priority based on different Priority Strategies

 * Default Priority
 * Jobs Started By User
 * Jobs Started By CLI
 * By using Job Parameter

 * * * 

Maintainer

Magnus Sandberg <emsa@switchbeat.com>

