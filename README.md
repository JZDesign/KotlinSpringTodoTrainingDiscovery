# KotlinSpringTodoTrainingDiscovery

## Running the application

**Prerequisites**

This uses Amazon Corretto 17. Visit [this page](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html) and install the version you need for your machine. (M1 Macs are aarch64, intel Macs are x64). Ensure it's installed in your JavaVirtualMachines directory then run `$ export JAVA_HOME=/Library/Java/JavaVirtualMachines/amazon-corretto-17.jdk/Contents/Home`

### Gradle

This project doesn't use Maven, instead, it's using Gradle. Open the Todo directory in IntelliJ and wait a bit. You'll see the Gradle job get kicked off right away (indicated by a progress bar at the bottom of the screen).

> If you open the project and see an error like this: `Incompatible because this component declares an API of a component compatible with Java 17 and the consumer needed a runtime of a component compatible with Java 11` You just need to change your [gradle settings](https://stackoverflow.com/a/75355554/9333764) a bit.

After Gradle has run successfully, navigate to the `TodoApplication.kt` file and click the play button to run the app.

## Overview

In this class, we're going to be building a Todo management API. Yes, it's not a hard problem that most of us have already used to learn software, but—for the sake of learning—we're going to approach this as if we don't really know much about how Todo management should work, how the payloads should look, etc., 

We're also going to pretend as if our company doesn't already have a lot of things standardized already. For example, at work, we use Postgres for our databases, JOOQ in our Spring Services, and Redis for caching… I'm sure you could list many others. Part of what this class is intending to cover is how to draw clear boundaries in our code so that we can test things in isolation and quickly pivot to new solutions. We will follow SOLID principles to separate concerns and write modular code, how Kotlin can support some of those separations with interfaces and extensions, and how to think/develop with a TDD approach.

Test Driven Development is a bit awkward at first. I agree. But if you look at it like taking the APIs you're writing for a _Test Drive_, it becomes pretty natural. It's essentially what we usually do when we start writing code, get it working and get it to be usable in a desirable/readable way. It's the method that is different. Failing tests are written first, get it to pass, then refactor until it's nice. (Red, Green, Refactor).

Even if you don't choose to continue the TDD habits after this class is over, it's a good experience that I believe will have a positive impact on how you approach the code you write.


## Modules

### TDD and Simple Storage

**Scenario:**
User research has discovered the way people would expect to use Todos and now the business has uncovered the requirements. Since they plan on supporting a mobile app—which could send old data up or a user could accidentally send the same data multiple times because of network connectivity—we need to make sure the storage layer insulates the user from accidental data regressions.


- Creating and Testing the store 
- Using a Domain Transfer Object

Write the store interface and implementation next to the tests.

Tests:
- Fetches an empty object
- Creates the object (effectively tests the read functionality)
- Creating a duplicate with different information throws conflict
- Updates only part of the object (Update DTO)
- Does not update when the `updatedAt` time of the DTO coming in is less than the time in the database
- Deletes matching object
- Shares state between multiple instances ( introduce writing to files here )

When finished, move the protocol and implementation to production.

### Benefits of the SOLID principles — Testing

- SOLID Principles
- Creating and testing the service
    - Hand-rolling mocks and spies
    - Using Mockk
- Translating the DTO to a client-friendly object

### Initial User Test - Logging, 

- Creating and testing the Controller
- Postman IT tests

### TodoStoreSpecs - Scale - Migrate to Database

- Migrate the tests for the FileStore into a Test Spec
- Create a Database Store that adheres to the protocol and the spec
    - This is to prove that the service, controller, and client would not need to change at all