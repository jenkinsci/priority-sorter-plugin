# Contributing

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [Newcomers](#newcomers)
  - [Source code contribution ways of working](#source-code-contribution-ways-of-working)
- [Run Locally](#run-locally)
  - [IDE configuration](#ide-configuration)
- [Preparing a pull request](#preparing-a-pull-request)
  - [Compiling and testing the plugin](#compiling-and-testing-the-plugin)
  - [Code coverage reporting](#code-coverage-reporting)
  - [Spotbugs checks](#spotbugs-checks)
- [Reviewing Pull Requests](#reviewing-pull-requests)
  - [Testing a Pull Request Build](#testing-a-pull-request-build)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

**Never report security issues on GitHub, public Jira issues or other public channels (Gitter/Twitter/etc.),
follow the instruction from [Jenkins Security](https://www.jenkins.io/security/#reporting-vulnerabilities) to
report it on [Jenkins Jira](https://www.jenkins.io/participate/report-issue/redirect/#15771)**

In the Jenkins project we appreciate any kind of contributions: code, documentation, design, etc.
Any contribution counts, and the size does not matter!
Check out [this page](https://jenkins.io/participate/) for more information and links!

Many plugins and components also define their own contributing guidelines and communication channels.
There is also a big number of [mailing lists](https://jenkins.io/mailing-lists/) and [chats](https://jenkins.io/chat/).

## Newcomers

If you are a newcomer contributor and have any questions, please do not hesitate to ask in the [Newcomers Gitter channel](https://gitter.im/jenkinsci/newcomer-contributors).

### Source code contribution ways of working

- For larger contributions create an [issue](https://issues.jenkins.io/issues/?jql=resolution%20is%20EMPTY%20and%20component%3D15771) for any required discussion
- Implement solution on a branch in your fork
- Make sure to include issue ID (if created) in commit message, and make the message speak for itself
- Once you're done create a pull request and ask at least one of the maintainers for review
  - Remember to title your pull request properly as it is used for release notes

## Run Locally

Prerequisites: _Java_ and _Maven_, (some plugins use Gradle, you will just need Java if you're building a Gradle plugin).

- Ensure Java 8, 11, or 17 is available.

  ```console
  $ java -version
openjdk version "11.0.16.1" 2022-08-12
OpenJDK Runtime Environment Temurin-11.0.16.1+1 (build 11.0.16.1+1)
OpenJDK 64-Bit Server VM Temurin-11.0.16.1+1 (build 11.0.16.1+1, mixed mode)
  ```

- Ensure Maven > 3.8.5 is installed and included in the PATH environment variable.

  ```console
  $ mvn --version
  Apache Maven 3.8.6 (84538c9988a25aec085021c365c560670ad80f63)
  Maven home: /home/mwaite/tools/apache-maven-3.8.6
  Java version: 11.0.16.1, vendor: Eclipse Adoptium, runtime: /home/mwaite/tools/jdk-11.0.16.1+1
  Default locale: en_US, platform encoding: UTF-8
  OS name: "linux", version: "4.18.0-372.19.1.el8_6.x86_64", arch: "amd64", family: "unix"
  ```

### IDE configuration

See [IDE configuration](https://jenkins.io/doc/developer/development-environment/ide-configuration/)

## Preparing a pull request

Plugin source code is hosted on [GitHub](https://github.com/jenkinsci/priority-sorter-plugin).
New feature proposals and bug fix proposals should be submitted as [GitHub pull requests](https://help.github.com/articles/creating-a-pull-request).
Your pull request will be evaluated by the [Jenkins job](https://ci.jenkins.io/job/Plugins/job/priority-sorter-plugin/).

Before submitting your change, please assure that you've added tests which verify your change.
There have been many developers involved in the priority sorter plugin and there are many users who depend on the priority sorter plugin.
Tests help us assure that we're delivering a reliable plugin, and that we've communicated our intent to other developers as executable descriptions of plugin behavior.

### Compiling and testing the plugin

Compile and run the plugin automated tests on Java 8, Java 11, or Java 17 with:

* `mvn clean verify`

Run the plugin inside a Jenkins environment with the [maven hpi plugin](https://jenkinsci.github.io/maven-hpi-plugin/run-mojo.html):

* `mvn -Dport=8080 hpi:run`

### Code coverage reporting

Code coverage reporting is available as a maven target.
Please improve code coverage with tests when you submit.

* `mvn -P enable-jacoco clean install jacoco:report` to report code coverage

### Spotbugs checks

Please don't introduce new spotbugs output.

* `mvn spotbugs:check` to analyze project using [Spotbugs](https://spotbugs.github.io/).
* `mvn spotbugs:gui` to review Spotbugs report using GUI

## Reviewing Pull Requests

Maintainers triage pull requests by reviewing them and by assigning labels.
Release drafter uses the labels to automate [release notes](https://github.com/jenkinsci/priority-sorter-plugin/releases).

Others are encouraged to review pull requests, test pull request builds, and report their results in the pull request.

### Testing a Pull Request Build

Pull request builds merge the most recent changes from their target branch with the change proposed in the pull request.
They can be downloaded from ci.jenkins.io and used to test the pull request.
Steps to test a pull request build are:

. *Find the pull request on [GitHub](https://github.com/jenkinsci/priority-sorter-plugin/pulls)*
. *Find the [ci.jenkins.io](https://ci.jenkins.io/job/Plugins/job/priority-sorter-plugin/view/change-requests/) artifacts for that pull request* from the artifacts link in the specific Jenkins job
. *Paste the link to the  `hpi` file* into the URL field of the Advanced page of the Jenkins Plugin Manager
. *Restart your Jenkins* and you're ready to test
