# Changelog

## Version 3.6.0 (2018-01-12)

-   [PR
    \#42](https://github.com/jenkinsci/priority-sorter-plugin/pull/42) -
    Make plugin compatible with class serialization whitelists in
    Jenkins Core 2.102 and
    above ([JEP-200](https://github.com/jenkinsci/jep/tree/master/jep/200))
    -   Warning: the change fixes the base class of
        the SorterStrategy extension point. If you implement it in
        plugins, it is recommended to update the dependency
-   [PR
    \#40](https://github.com/jenkinsci/priority-sorter-plugin/pull/40) -
    Null safety in some plugin methods (e.g. Strategy\#getPriority()

## Version 3.5.1 (2017-06-18)

-   [JENKINS-41419](https://issues.jenkins-ci.org/browse/JENKINS-41419) -
    Fix sorting of queue items when \`sortAsInQueueSince\` is set
-   [JENKINS-42584](https://issues.jenkins-ci.org/browse/JENKINS-42584) -
    Take priorities of upstream jobs into account when scheduling
    downstream jobs
-   [PR
    \#38](https://github.com/jenkinsci/priority-sorter-plugin/pull/38) -
    Prevent NullPointerException in
    PriorityConfigurationPlaceholderTaskHelper when restarting Jenkins
    with pending jobs
-   [JENKINS-44014](https://issues.jenkins-ci.org/projects/JENKINS/issues/JENKINS-44014) -
    Prevent NullPointerException in `AdvancedQueueSorter#onLeft()` when
    item is missing in the queue
    -   Instead of exceptions, there will be warning messages in Jenkins
        logs with diagnostics info
-   [JENKINS-44014](https://issues.jenkins-ci.org/projects/JENKINS/issues/JENKINS-44014) -
    Prevent ClassCastException if `owner` of the Pipeline placeholder
    task is not a Job
    -   In such case a default priority will be set. The issue will be
        logged on the FINE level

## Version 3.5.0 (2017-01-18)

Fixes :
[JENKINS-40284](https://issues.jenkins-ci.org/browse/JENKINS-40284) Fix
blocked weights with Fair Queuing strategies  
Fixes :
[JENKINS-36570](https://issues.jenkins-ci.org/browse/JENKINS-36570) Add
Pipeline support

## Version 3.4.1 (2015-09-29)

Fixes :
[JENKINS-37644](https://issues.jenkins-ci.org/browse/JENKINS-37644)
Update Folders Plugin dependency to fix the compatibility issue

## Version 3.4 (2015-06-06)

Fixes :
[JENKINS-28621](https://issues.jenkins-ci.org/browse/JENKINS-28621)
Jenkins jobs get held up in queue waiting for available executors, even
though all are available

## Version 3.3 (2015-05-26)

Fixes :
[JENKINS-28462](https://issues.jenkins-ci.org/browse/JENKINS-28462)
Priority selection on Job is "always" shown  
Fixes :
[JENKINS-28461](https://issues.jenkins-ci.org/browse/JENKINS-28461)
Control Node usage based on Priority  
Relates :
[JENKINS-23640](https://issues.jenkins-ci.org/browse/JENKINS-23640)
Validate regular expression and report matching jobs on Job Priorities
page

## Version 3.2 (2015-05-18)

Fixes :
[JENKINS-24962](https://issues.jenkins-ci.org/browse/JENKINS-24962)
Cannot assign a JobGroup to a Nested (sub) View  
Fixes :
[JENKINS-28280](https://issues.jenkins-ci.org/browse/JENKINS-28280)
Wrong Job Group is shown in the selector (one more place) (Thanks
to [Kyrremann](https://github.com/Kyrremann))

## Version 3.1 (2015-05-12)

Fixes :
[JENKINS-28195](https://issues.jenkins-ci.org/browse/JENKINS-28195)
Jenkins not scheduling any jobs after upgrade to Priority Sorter 3.0 but
giving exception  
Fixes :
[JENKINS-28280](https://issues.jenkins-ci.org/browse/JENKINS-28280)
Wrong Job Group is shown in the selector (Thanks
to [Kyrremann](https://github.com/Kyrremann))  
Fixes :
[JENKINS-28359](https://issues.jenkins-ci.org/browse/JENKINS-28359)
Remove support and conversion from Legacy Mode

## Version 3.0 (2015-05-02)

Fixes :
[JENKINS-21337](https://issues.jenkins-ci.org/browse/JENKINS-21337) Add
support for cloudbees-folders  
Fixes :
[JENKINS-21356](https://issues.jenkins-ci.org/browse/JENKINS-21356) Add
support for using JobProperty to join a Job to a JobGroup  
Fixes :
[JENKINS-23538](https://issues.jenkins-ci.org/browse/JENKINS-23538)
Remove support for Legacy Sorter  
Fixes :
[JENKINS-23552](https://issues.jenkins-ci.org/browse/JENKINS-23552)
Replace "Allow priorities directly on Jobs"  
Fixes :
[JENKINS-23557](https://issues.jenkins-ci.org/browse/JENKINS-23557) Add
matrix child jobs to the front of the queue  
Fixes :
[JENKINS-27966](https://issues.jenkins-ci.org/browse/JENKINS-27966)
Priority Sorter must not require matrix-project

## Version 2.12 (2015-04-15)

Fixes :
[JENKINS-27957](https://issues.jenkins-ci.org/browse/JENKINS-27957)
NullPointerException in AdvancedQueueSorter

## Version 2.11 (2015-04-14)

Fixes :
[JENKINS-27770](https://issues.jenkins-ci.org/browse/JENKINS-27770)
AdvancedQueueSorter call to sort violates the comparison contract

## Version 2.10 (2015-04-14)

Never released - maven hickup

## Version 2.9 (2014-10-08)

-   Fixed NPE happening at startup
    \[[X](https://github.com/jenkinsci/priority-sorter-plugin/commit/72272f430d6aa45a3c48b7b339dbd14f1a70d5c7)\]
    (Thanks to [christ66](https://github.com/christ66))
-   Added description field to JobGroups
    \[[X](https://github.com/jenkinsci/priority-sorter-plugin/commit/d394479d589736d3c7f25d0fa3d69b60f6c0dd7c)\]
    (Thanks to [olivergondza](https://github.com/olivergondza))

## Version 2.8 (2014-06-17)

Fixes :
[JENKINS-23462](https://issues.jenkins-ci.org/browse/JENKINS-23462)
Sectioned views are not evaluated for job priority

## Version 2.7 (2014-06-16)

-   Adds some more logging about queue contents, see Troubleshooting
    above
-   Performance enhancement when updating the configuration
    \[[X](https://github.com/jenkinsci/priority-sorter-plugin/commit/e46b2b1fbc4396f441c69692eb328fb982325572)\]
    (Thanks to [ndeloof](https://github.com/ndeloof))

Fixes :
[JENKINS-23428](https://issues.jenkins-ci.org/browse/JENKINS-23428) Jobs
in NestedView (ViewGroup) are not correctly found

## Version 2.6 (2014-01-11)

Fixes :
[JENKINS-21310](https://issues.jenkins-ci.org/browse/JENKINS-21310) CCE
when a Queue.Task was not a Job  
Fixes :
[JENKINS-21316](https://issues.jenkins-ci.org/browse/JENKINS-21316)
PrioritySorter wrongly assumes Queue.Task is Job  
Relates:
[JENKINS-21314](https://issues.jenkins-ci.org/browse/JENKINS-21314)
Stack trace displayed on web page when attempting to configure
PrioritySorter plugin

## Version 2.5 (2014-01-08)

Fixes:
[JENKINS-21289](https://issues.jenkins-ci.org/browse/JENKINS-21289) Item
Logging causing NPE  
Fixes:
[JENKINS-21284](https://issues.jenkins-ci.org/browse/JENKINS-21284) Add
some logging to show what the sorter is doing  
Fixes:
[JENKINS-21204](https://issues.jenkins-ci.org/browse/JENKINS-21204) Add
ability to boost recently failed Jobs

## Version 2.4 (2014-01-02)

Fixes:
[JENKINS-21173](https://issues.jenkins-ci.org/browse/JENKINS-21173)
Anonymous Users Can Configure Priorities  
Fixes:
[JENKINS-21119](https://issues.jenkins-ci.org/browse/JENKINS-21119)
Extend loggning to show assigned priority  
Fixes:
[JENKINS-21103](https://issues.jenkins-ci.org/browse/JENKINS-21103)
"Priorities are assigned top down by first match" does not work
correctly (Thanks to [Adam Gabryś](http://www.adam.gabrys.biz/) for
debugging help)

## Version 2.3 (2013-12-20)

-   introduces the Run Exclusive Mode to deal with
    [JENKINS-11997](https://issues.jenkins-ci.org/browse/JENKINS-11997)
-   adds a new Priority Strategy to give Jobs the same priority as a
    UpstreamJob
-   some performance enhancements
-   hopefully fixes the deadlock issue in
    [JENKINS-21034](https://issues.jenkins-ci.org/browse/JENKINS-21034)
-   fixes the priority assignment issue on main configuration page

Fixes:
[JENKINS-11997](https://issues.jenkins-ci.org/browse/JENKINS-11997)
Consider jobs in the Executors for priority sorter  
Fixes:
[JENKINS-21034](https://issues.jenkins-ci.org/browse/JENKINS-21034)
Jenkins Startup Deadlock - QueueSorter.installDefaultQueueSorter and
Queue.init  
Fixes:
[JENKINS-20995](https://issues.jenkins-ci.org/browse/JENKINS-20995)
Default Priority always shows 1-5

## Version 2.2 (2013-12-05)

Fixing bug that made the plugin switch to Advanced mode even though
Legacy (1.3) data was present in the system. (Thanks to help from
[Matthew Webber](http://www.diamond.ac.uk/))

Fixes:
[\[JENKINS-8597\]](https://issues.jenkins-ci.org/browse/JENKINS-8597)
Deal with matrix builds better

## Version 2.1 (2013-12-04)

Fixes bug that mapped all Jobs to all Views/JobGroups.

## Version 2.0 (2013-12-02)

Introducing advanced queueing features with possibility to selected
different strategies for how priorities are assigned and how the queue
is sorted.

Thanks [Oleg](https://github.com/oleg-nenashev) for testing,
reviewing and helping out.

## Version 1.3

Removed view column from default view (Thanks to work
from [larrys](http://github.com/larrys/Hudson-Priority-Sorter-Plugin))

## Version 1.2

Added View column to easily compare priorities between jobs (Thanks to
work from
[cjo9900](http://github.com/cjo9900/Hudson-Priority-Sorter-Plugin))

## Version 1.1

Fixed a potential NPE when using the plugin on existing jobs without
setting a default priority.

## Version 1.0

Initial Release
