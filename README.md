This project's issue tracker has been disabled, if you wish to [create an issue or bug please follow these directions](/CONTRIBUTING.md#issue-tracker).

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

```shell
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
        classpath 'com.gemnasium:gradle-plugin:1.0.2'  // needs to refer to a specific version, no wildcards or meta-versions allowed
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

```shell
./gradlew --init-script gl-gemnasium-init.gradle gemnasiumDumpDependencies
```

## How to use it?

The plugin adds a single task to your Gradle project:

```shell
./gradlew gemnasiumDumpDependencies
```

This will create a JSON file containing information about your Gradle project's dependencies.

You can customize the name/location of the JSON file and configure the behavior of the plugin (see [Configuration](#configuration) section).

## Configuration

The plugin can be configured via a Gradle extension block:

```groovy
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

## How to develop new features

1. Clone this repo on your local machine:

   ```shell
   user@local $ git clone https://gitlab.com/gitlab-org/security-products/analyzers/gemnasium-gradle-plugin.git /path/to/local/gemnasium-gradle-plugin
   ```

1. Run the `gemnasium-maven` Docker image on your local machine, mounting the path to the `gemnasium-gradle-plugin` repo from step `1.` inside the Docker container.

   ```shell
   user@local $ docker run -it --rm -v "/path/to/local/gemnasium-gradle-plugin:/gemnasium-gradle-plugin" \
                  registry.gitlab.com/gitlab-org/security-products/analyzers/gemnasium-maven:2 /bin/bash
   ```

   We use the `gemnasium-maven` image because it satisfies the dependencies needed to build and run the `gemnasium-gradle-plugin`.

1. Make changes to the `gemnasium-gradle-plugin` source code on your local development machine which you cloned in step `1.` to `/path/to/local/gemnasium-gradle-plugin`.

   For example, we're going to change the behaviour of the plugin when a project has unresolved dependencies so that instead of outputting:

   ```shell
   Project has unresolved dependencies
   ```

   we include the number of unresolved dependencies:

   ```shell
   Project has 5 unresolved dependencies
   ```

   Modify the [walk function](https://gitlab.com/gitlab-org/security-products/analyzers/gemnasium-gradle-plugin.git/blob/0bb16fa/src/main/kotlin/com/gemnasium/tasks/DumpDependenciesTask.kt#L123-123) of the `gemnasium-gradle-plugin` to implement the desired behaviour:

      ```diff
      --- a/src/main/kotlin/com/gemnasium/tasks/DumpDependenciesTask.kt
      +++ b/src/main/kotlin/com/gemnasium/tasks/DumpDependenciesTask.kt
      @@ -137,7 +137,8 @@ open class DumpDependenciesTask : DefaultTask() {
                   val root = resolutionResult.root

                   if (root.dependencies.filterIsInstance<UnresolvedDependencyResult>().isNotEmpty()) {
      -                throw GradleException("Project has unresolved dependencies")
      +                val numUnresolvedDeps = root.dependencies.filterIsInstance<UnresolvedDependencyResult>().size
      +                throw GradleException("Project has ${numUnresolvedDeps} unresolved dependencies")
      ```

1. Create the `gradle wrapper` in the `/gemnasium-gradle-plugin` project on the Docker container:

   ```shell
   root@docker:~# cd /gemnasium-gradle-plugin && gradle wrapper
   ```

1. Run the unit tests for the new code changes on the Docker container:

   ```shell
   root@docker:/gemnasium-gradle-plugin# ./gradlew check
   ```

   If a failure occurs, you can view the details by opening the kotlin report file in your web browser on your local machine:

   ```shell
   file:///path/to/local/gemnasium-gradle-plugin/build/reports/tests/functionalTest/index.html
   ```

1. Add new unit tests or update existing broken tests for the new features.

1. Bump the version number and publish a new version of the plugin (see [Publishing](#publishing) for details).

1. (Optional) Manually check the new code changes against a test project:

   1. Install the `patch` commandline tool on the Docker container so we can modify the `gemnasium-gradle-plugin-init.gradle` init script used by `gemnasium-maven` to use the local maven repo:

      ```shell
      root@docker:/gemnasium-gradle-plugin# apt update && apt install -y patch
      ```

   1. Use the `patch` commandline tool installed in the Docker container to apply [this patch](https://gitlab.com/gitlab-org/security-products/analyzers/gemnasium-gradle-plugin/-/raw/master/add-maven-local.diff) to the `/gemnasium-gradle-plugin-init.gradle` init script. This patch will update the init script to include `mavenCentral()` and `mavenLocal()`, so that the `gemnasium-gradle-plugin` can find its dependencies:

      ```shell
      root@docker:/gemnasium-gradle-plugin# patch -d/ -N -p0 -i /gemnasium-gradle-plugin/add-maven-local.diff
      ```

   1. Build and publish the updated plugin code to the local maven repository on the Docker container:

      ```shell
      root@docker:/gemnasium-gradle-plugin# /gemnasium-gradle-plugin/gradlew -p /gemnasium-gradle-plugin/ publishToMavenLocal
      ```

       **Note:** In order for the modified plugin which has been published to the local maven repository in the above step to take precedence over the remote [gemnasium-gradle-plugin](https://plugins.gradle.org/plugin/com.gemnasium.gradle-plugin), the version value in [build.gradle.kts](build.gradle.kts#L10) must match the one specified in the `dependencies { classpath 'com.gemnasium:gradle-plugin:<VERSION>' }` block of the `/gemnasium-gradle-plugin-init.gradle` file in the `gemnasium-maven` Docker image that you're currently running.

       If you change the `version` value in `build.gradle.kts`, then _the remote_ [gemnasium-gradle-plugin](https://plugins.gradle.org/plugin/com.gemnasium.gradle-plugin) will be used in step `6. Execute the plugin against the...` below instead of the modified _local plugin_.

   1. Create the `gradle wrapper` in the `/gradle-plugin-builder` directory on the Docker container:

      ```shell
      root@docker:/gemnasium-gradle-plugin# cd /gradle-plugin-builder/ && gradle wrapper
      ```

   1. Create a new invalid project on the Docker container:

      ```shell
      root@docker:/gradle-plugin-builder# mkdir /invalid-dep-project && cd /invalid-dep-project
      root@docker:/invalid-dep-project# echo $'plugins {\n  id("java")\n}\nrepositories {\n  maven(url = "http://invalid.com")\n}\ndependencies {\n  implementation("junit:junit:4.13")\n}\n' > build.gradle.kts
      ```

   1. Execute the plugin against the new invalid project created above:

      ```shell
      root@docker:/invalid-dep-project# /gradle-plugin-builder/gradlew --init-script /gemnasium-gradle-plugin-init.gradle gemnasiumDumpDependencies
      ```

      Output:

      ```shell
      > Task :gemnasiumDumpDependencies FAILED

      FAILURE: Build failed with an exception.

      * What went wrong:
      Execution failed for task ':gemnasiumDumpDependencies'.
      > Project has 1 unresolved dependencies
      ```

      The output contains the new error message we implemented, as expected.

   1. If you make further changes to the source code in `/gemnasium-gradle-plugin` and want to execute the modified plugin against the local test project, you'll need to run the `publishToMavenlocal` command as explained in step `3. Build and publish the updated plugin code...` above to re-compile and publish the updated plugin to the local maven repository.

## Publishing

Before publishing a new version of this plugin, please make sure to bump the version number in the following blocks of code:

- [manual-test/maven/pom.xml](manual-test/maven/pom.xml)

   ```xml
   <build>
     <plugins>
       <plugin>
         <groupId>com.gemnasium</groupId>
         <artifactId>gemnasium-maven-plugin</artifactId>
         <!-- change the following version -->
         <version>1.0.2</version>
   ```

- [README.md](README.md)

   ```
   initscript {
       repositories {
           jcenter()
           mavenCentral()
           maven { url "https://plugins.gradle.org/m2/" }
       }
       dependencies {
           // change the following version
           classpath 'com.gemnasium:gradle-plugin:1.0.2'
       }
   }
   ```

- [build.gradle.kts](build.gradle.kts)

   ```
   group = "com.gemnasium"
   // change the following version
   version = "1.0.2"
   ```

Publishing to `plugins.gradle.org` is done via the [publish job](.gitlab-ci.yml#L77) and is triggered manually in the merged pipeline.
