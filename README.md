# KotlinSpringTodoTrainingDiscovery

## Running the application

**Prerequisites**

This uses Amazon Corretto 17. Visit [this page](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html) and install the version you need for your machine. (M1 Macs are aarch64, intel Macs are x64). Ensure it's installed in your JavaVirtualMachines directory then run `$ export JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home`

### Gradle

This project doesn't use Maven, instead, it's using Gradle. Open the Todo directory in IntelliJ and wait a bit. You'll see the Gradle job get kicked off right away (indicated by a progress bar at the bottom of the screen).

> If you open the project and see an error like this: `Incompatible because this component declares an API of a component compatible with Java 17 and the consumer needed a runtime of a component compatible with Java 11` You just need to change your [gradle settings](https://stackoverflow.com/a/75355554/9333764) a bit.

After Gradle has run successfully, navigate to the `TodoApplication.kt` file and click the play button to run the app.
