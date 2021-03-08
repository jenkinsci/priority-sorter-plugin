# Jenkins Priority Sorter Plugin

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins%2Fpriority-sorter-plugin%2Fmaster)](https://ci.jenkins.io/job/Plugins/job/priority-sorter-plugin/job/master/)

Adds the ability to assign different priorities to jobs,
the lower the priority a job has, the sooner the job will run.

This can be very helpful, when one wants to add low priority jobs, but
wants to have higher-priority jobs run first. This is especially true
when hardware is limited, or when there are different groups of jobs
that should share resources (equally).

## Fundamentals

The plugin is build around some basic concepts:

-   Queue Strategy
-   Job Group
-   Job Inclusion Strategy, and
-   Priority Strategy.

First, the *Queue Strategy* describes how the priority of a job is
interpreted. It will translate the job's assigned priority to a
corresponding weight in the queue. The queue is, then, sorted based on
the assigned weight.

With the *Priority* Sorter, you will need to group your jobs into *Job
Groups*. A *Job Group*'s *Job Inclusion* Strategy devides whether a
specific job should be included in the group.

The priority of the job is then set by the *Priority Strategy*, and each
*Job Group* can have any number priority strategies.

When a new job is queued, the following will happen:

-   The *Priority Sorter* will go though the configured *Job Groups*
    from top to bottom.
    -   When it finds a Job Group where the job is to be included, it
        will look though the *Priority Strategies* from top to bottom
        -   When it finds a *Priority Strategy* that matches its
            criteria, it will use this strategy to assign a priority the
            the Job.
-   The *Priority Sorter* will then consult the *Queue Strategy* to
    translate the priority to a weight, i.e. a position in the queue.

## Queue Strategies

There are three included Queue Strategies, each of which will sort the
Queue differently.

### Absolute

Each job will be assigned a priority, and, the lower the priority, the
sooner the job will be run.

### Fair Queuing

Each Job will be assigned a priority, but the queue will try to share
the resources equally over different priorities in a round-robin
fashion.

### Weighted Fair Queuing

The same as *Fair Queuing*, but jobs with a lower priority will be run
more frequently those with higher a priority. Remember, that the lower
the priority assignment, the higher the importance.

## Job Inclusion Strategies

This setting describes which jobs are included in a Job Group, and is
relatively self explanatory. It is worth mentioning the "Jobs Marked for
Inclusion" setting. When this is selected, you are requested to enter a
name of the *Job Group*. This name will then be an available option on
each job, providing an alternative "bottom-up" way to group jobs, rather
than the default "top-down" approach of the other strategies.

## Priority Strategies

It is possible to assign the priority, based on different *Priority
Strategies.*

This setting describes what priority a job should have, and is self
explanatory. It is worth mentioning the the "Take the Priority from
Property on the Job" setting. When selected, a drop-down - where you can
select a priority - appears on all projects that would generate jobs to
be included in this group.

## Main Configuration

The main configuration options are available on the Jenkins
configuration page. Configuration options for *Priority Strategy* are
available on its own page, accessible from the root actions menu.

On the main configuration page, you can select the number of priorities
you would like to use, as well as a default priority to use where no
other priority could be assigned.

![(warning)](docs/images/warning.svg)
** Please note that lower number means higher priority! See details on
the screen, for the specifics on each strategy. **

### *Assigning Priorities*

On the main menu, you will find the link to a page where you can assign
priorities to each Job

![](docs/images/JobPriorities.png)

On this page you will be able to assign jobs to groups, and priorities
to jobs.

*![(warning)](docs/images/warning.svg)*
** Please note that all matching is done top to bottom by first match**

You can limit access to this functionality to administrators, by
checking the appropriate check-box on the main configuration page.

### *Run Exclusive*

If a job from a *Run Exclusive* job group gets started, jobs from other
groups will remain blocked, and will not get executed until all jobs
from the *Run Exclusive* job group are complete, regardless of priority.

## *Using the View Column*

Since a job can get a different priority each time it is started, the
view column cannot show the "correct" priority for the Job.

The column will show the priority used the last time the job was
launched, and, if the job has not been started yet, the column will show
*Pending.*

## Notable changes and upgrading

### Upgrading from 2.x

In version **3.x**, the option "Allow priorities directly on Jobs" has
been removed in favor of the Priority Strategy "Take the priority from
Property on the Job" (see above). Legacy Mode is removed (see above
regarding upgrading from version **1.x**).

### Upgrading from 1.x

Version **2.x** is a complete rewrite of the plugin, but still supports
running in version **1.x** compatibility mode. However if you are
satisfied with the functionality of **1.x**, there is little point in
upgrading.  
Upgrading from version **1.x** to version **3.x** will remove all
**1.x** configurations. Therefore, if you need to keep configured
values, upgrade to **2.x**, first, switch to *Advanced Mode*, and then
upgrade from **2.x** to **3.x**.

## Troubleshooting

To get some inside information on how, and why, a certain job gets a
certain priority, you can turn on some extra logging.

-   Logger: ***PrioritySorter.Queue.Items***
    -   To get more info on the assigned priorities, and state
        transition of the items in the queue, set the log level to
        ***FINE*.**
    -   To get more info on how the jobs are matched to job groups, and
        rules to get the priority, set the log level to ***FINER*.**

To get logging on when the *Queue Sorter* is active log, use:

-   Logger: ***PrioritySorter.Queue.Sorter****,* with the level set to
    ***FINE***.
    -   To see all items sorted (the queue) by the Queue Sorter, set the
        log level to ***FINER*.**

## Release Notes

* For recent versions, see [GitHub Releases](https://github.com/jenkinsci/priority-sorter-plugin/releases)
* For versions 3.6 and older, see the [changelog](./CHANGELOG.md)
