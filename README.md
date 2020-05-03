<!--
The MIT License

Copyright (c) 2013, Cisco Systems, Inc., a California corporation

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
-->

# Jenkins Priority Sorter Plugin

This plugin adds the ability to assign different priorities to Jobs, the lower priority the job has the sooner the Job will run.

This can be very helpful when one wants to add low priority jobs but wants to have higher-priority jobs run first when hardware is limited or when there are 
different groups of Jobs that should share resources (equally).

The plugin both contains ways to select a Sorter Strategy and one or more Priority Strategies.

The Sorter Strategies will allow you to select how you want the queue to be sorted. This allows you not only to run higher priority Jobs before lower priority Jobs, but also to use algorithms such as Fair Weighed Queueing.

The Priority Strategies will allow you to have different priorities based on how the Job is started. This enables you to give Jobs started directly by a user higher priority than Jobs started by cron or by a commit.

See Jenkins Wiki @ [Priority Sorter Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Priority+Sorter+Plugin) for more information.

## Dependencies

* [job-restrictions](https://plugins.jenkins.io/job-restrictions/)
