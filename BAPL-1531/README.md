# BAPL-1531: Immutable SpringBoot Deployment

The root issue is that the customer infrastructure does not have any artifactory/nexus to download the KJAR from
So we need to provide a way to deploy KJAR with all the dependencies inside
There are two approaches to do so:
- Using Maven Dependency Plugin and Maven Install plugin
- Using the offliner http://release-engineering.github.io/offliner/ which has been wrapped as a Maven plugin

## Objective

I need to test each approach and deploy it **locally** and also **in Openshift** (as the customer is using it this way for production)

## Requirements
- Maven
- Java 8
- offliner-maven-plugin:

```sh
git clone https://github.com/ippul/offliner-maven-plugin
cd offliner-maven-plugin
mvn clean install
```

## Steps 

- Compile and package artifacts
- Run in Integration Test
- Run into a standalone kie server (TODO)
- Run into Openshift (TODO)

### Compile:

There are two approaches to create the FatJar output:

**- Using a project Maven repository**

```sh
mvn clean install -PprojectRepository
```

This will compile the **KJAR** child projects and then the **kie-spring-boot-example** build will retrieve its dependencies and install them inside the final FatJar at _BOOT-INFO/classes/m2/repository_. This is done by using this maven plugins:

- maven-dependency-plugin: to copy the KJar dependency
- maven-install-plugin: to install the KJar dependency into a Maven repository

**- Using a plugin Maven:**

```sh
mvn clean install -PusingPlugin
```

Not working...

### Sanity Checks

Let's see the content of our FatJar **kie-spring-boot-example.jar**:

```sh
jar tf kie-spring-service-multi-kjar/target/kie-spring-boot-example.jar | grep kjar-examples
```

Expected output (depending on the KJAR you deployed):

```sh
BOOT-INF/classes/m2/repository/com/sgitario/kjar-examples/xxx/
...
```

In order to continue, let's delete the child KJar dependencies from our local instance to ensure we're using the right location packaged inside **kie-spring-boot-example-jar**:

```sh
rm -rf ${HOME}/.m2/repository/com/sgitario/kjar-examples
```

### Run in Integration Test

Extract the Maven repository from the JAR:

```
rm -rf BOOT-INF
jar xf kie-spring-service-multi-kjar/target/kie-spring-boot-example.jar BOOT-INF/classes/m2
```

Prepare the Maven settings pointing to the BOOT-INF folder, _settings.xml_:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.1.0 http://maven.apache.org/xsd/settings-1.1.0.xsd"
          xmlns="http://maven.apache.org/SETTINGS/1.1.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <offline>true</offline>
    <localRepository>/home/jcarvaja/sources/kjar-examples/kjar-examples/BAPL-1531/BOOT-INF/classes/m2/repository</localRepository>
</settings>
```

When doing the previous step, we can quickly check everything is well packaged by simply doing:

```sh
java -Dkie.maven.settings.custom=/home/jcarvaja/sources/kjar-examples/kjar-examples/BAPL-1531/settings.xml -jar kie-spring-service-multi-kjar/target/kie-spring-boot-example.jar
```

| kie.maven.settings.custom must point to a full path!

| Note that the kie-spring-service-multi-kjar/target/classes/settings.xml is the local Maven repository settings which is auto generated in the previous step.

This will start a Kie Server, the expected output is:

```sh
...
[main] INFO org.kie.server.springboot.samples.KieServerDeployer - already deployed KieContainerResource [containerId=evaluation-kjar-2_0-SNAPSHOT, releaseId=com.sgitario.kjar-examples:evaluation:2.0-SNAPSHOT, resolvedReleaseId=com.sgitario.kjar-examples:evaluation:2.0-SNAPSHOT, status=STARTED]
...
[main] INFO org.kie.server.springboot.samples.KieServerDeployer - already deployed KieContainerResource [containerId=evaluation-kjar-1_0-SNAPSHOT, releaseId=com.sgitario.kjar-examples:evaluation:1.0-SNAPSHOT, resolvedReleaseId=com.sgitario.kjar-examples:evaluation:1.0-SNAPSHOT, status=STARTED]
...
```

### Run into a standalone kie server

Start a local Kie Server instance:

```sh
git clone https://github.com/kiegroup/droolsjbpm-integration
cd droolsjbpm-integration/kie-spring-boot/kie-spring-boot-samples/kie-server-spring-boot-sample
mvn clean spring-boot:run
```

TODO 

## Issues

- The plugin "kie-maven-plugin" is not configured to be run in Eclipse (see similar in Kogito maven plugin: https://issues.redhat.com/browse/KOGITO-1786)

## Source Projects

- Evaluation KJar 1: https://github.com/kie-springboot/evaluation-process-kjar
- Evaluation KJar 2: https://github.com/kie-springboot/evaluation-process-kjar-2
- Search Var Extension (query-ext): https://github.com/kie-springboot/jbpm-search-var-extension
