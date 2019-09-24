# Gemnasium Gradle Plugin

The Gemnasium Gradle plugin facilitates support for using Gemnasium to scan Gradle projects by providing a Gradle task that dumps Gradle project dependencies into a JSON file formatted in a way that Gemnasium can process.

This plugin was developed in order to add dependency scanning support for Gradle projects to GitLab and will be used by the GitLab CI system for that purpose. As such, it will probably not be of much use to anyone else.

The implementation borrows heavily from the following projects:
 * https://github.com/jeremylong/dependency-check-gradle
 * https://github.com/gemnasium/gemnasium-maven-plugin

## How to install it?

The plugin is published to the Gradle Plugin Repository
https://plugins.gradle.org/plugin/com.gemnasium.gradle-plugin

### Direct dependency

The plugin can be directly applied as a plugin to your Gradle
project:

```
apply plugin: 'com.gemnasium.gradle-plugin'
```

This adds the `gemnasiumDumpDependencies` task to your project.

### Using an init-script

The plugin can also be applied using the Gradle init-script 
mechanism:
https://docs.gradle.org/current/userguide/init_scripts.html

Here is an example of an init-script that can be used:

```groovy
initscript {
    repositories {
        jcenter()
        mavenCentral()
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        classpath 'com.gemnasium:gradle-plugin:0.1'
    }
}

allprojects {
  // We must refer to the plugin by the fully qualified
  // class name in init-scripts:
  // https://github.com/gradle/gradle/issues/1322
  apply plugin: com.gemnasium.GemnasiumGradlePlugin
  
  gemnasiumGradlePlugin {
    //outputDir = file('.')
    //outputFileName = 'gradle-dependencies.json'
    //skipTestGroups = false
    //skipConfigurations = ['compile']
    //scanConfigurations = ['compileClasspath']
  }
}
```

To use that you would use the `--init-script` parameter in your
`gradle` command. Assuming you save the above init script as `gl-gemnasium-init.gradle` in the root directory of your
Gradle project, you could execute the plugin task like this:

```
./gradlew --init-script gl-gemnasium-init.gradle gemnasiumDumpDependencies
```

## How to use it?

The plugin adds a single task to your Gradle project:

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