# Gemnasium Gradle Plugin - initscript tests

This directory contains Gradle projects that test the application
of the Gemnasium Gradle Plugin via the Gradle initscript mechanism.
That is, the Gradle project do not state an explicit dependency
on the Gemnasium Gradle Plugin, instead, the plugin is applied
using a Gradle initialization script 
(https://docs.gradle.org/current/userguide/init_scripts.html).

## Execution

To run the plugin on the projects here you need to enter the
project directory (singleproject or multiproject) and run the
following command:

`./gradlew --initscript ../gl-gemnasium-init.gradle gemnasiumDumpDependencies`

## Test Projects

This directory contains two test project. One (singleproject) is 
a standard Gradle project with only the root project. The other
one (multiproject) is a multi-project 
(https://docs.gradle.org/current/userguide/multi_project_builds.html)
build to test the behavior of the plugin in such situations. The
multiproject contains sub-projects, some of which are java projects
but one which is not. It also has external dependencies as well as
inter-project dependencies.