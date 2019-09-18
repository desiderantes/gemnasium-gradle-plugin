# Gemnasium Gradle Plugin

The Gemnasium Gradle plugin facilitates support for using Gemnasium to scan Gradle projects by providing a Gradle task that dumps Gradle project dependencies into a JSON file formatted in a way that Gemnasium can process.

This plugin was developed in order to add dependency scanning support for Gradle projects to GitLab and will be used by the GitLab CI system for that purpose. As such, it will probably not be of much use to anyone else.

The implementation borrows heavily from the following projects:
 * https://github.com/jeremylong/dependency-check-gradle
 * https://github.com/gemnasium/gemnasium-maven-plugin

## How to install it?

TODO - deployment / adding the plugin to your gradle project / using Gradle init scripts?

```
apply plugin: 'com.gemnasium.gradle-plugin'
```

## How to use it?

```
./gradlew gemnasiumDumpDependencies
```

This will create a JSON file containing information about your Gradle project's dependencies.

You can customize the name/location of the JSON file and configure the behavior of the plugin (see [Configuration](#configuration) section).

## Configuration

The plugin can be configured via a Gradle extension block:

```
gemnasiumGradlePlugin {
    // Configure the output file name/location
    // Defaults to: <build_dir>/reports/gradle-dependencies.json
    outputDir = file('.')
    outputFileName = 'foo.json'

    // Should test configurations be skipped?
    // Defaults to: true
    skipTestGroups = false

    // A list of configurations that should be skipped
    // Defaults to: []
    // Mutually exclusive with scanConfigurations
    //skipConfigurations = ['compile']


    // A list of configurations that should be scanned
    // Defaults to: []
    // Mutually exclusive with skipConfigurations
    //scanConfigurations = ['compileClasspath']

    // You can only specify at most one of:
    //  * skipConfigurations
    //  * scanConfigurations
    //
    // Specifying neither of them will result in all 
    // configurations being processed (whether or not
    // that includes test configurations depends on
    // the value of skipTestGroups). 
}
```