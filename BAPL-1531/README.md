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
- Docker
- offliner-maven-plugin:

```sh
git clone https://github.com/ippul/offliner-maven-plugin
cd offliner-maven-plugin
mvn clean install
```

## Steps 

- Compile and package artifacts
- Run in Integration Test (PENDING)
- Run into a standalone kie server (TODO)
- Run into Openshift (TODO)

## Solutions

There are two approaches to create the FatJar output:

###Using a project Maven repository

This approach will make use of the next Maven plugins to create the Maven repository in offline mode:

- maven-dependency-plugin: to copy the KJar dependency
- maven-install-plugin: to install the KJar dependency into a Maven repository

To compile the project using this solution, run:

```sh
mvn clean install -PprojectRepository
```

This command will prepare the offline Maven repository with the **KJAR** child project(s) and also the Kie Server application in Spring Boot. 

| Note that the offline Maven repository will include the KJAR child project, not all the dependencies so this solution requires network connection at runtime.

In order to create the Docker image:

```sh
sh utils/prepare_repository.sh kie-spring-service-multi-kjar/target/classes/m2/repository
docker build -t kie_server_project_repository -f Dockerfile.projectRepository .
```

And run the image:

```sh
docker run kie_server_project_repository
```

Expected output:

```sh
[main] INFO org.kie.server.services.impl.KieServerImpl - Container kjar-without-parent-1.0-SNAPSHOT (for release id com.sgitario.kjar-examples:kjar-without-parent:1.0-SNAPSHOT) successfully started
```

###Using a plugin Maven

This approach needs an external Maven repository to access the KJAR from. For testing purposes, we deploy a Nexus maven repository by doing:

```sh
docker run -d -p 8081:8081 --name nexus sonatype/nexus:oss
```

Add the nexus credentials to our _settings.xml_ maven configuration file:

```xml
<server>
    <id>nexus-docker</id>
    <username>admin</username>
    <password>admin123</password>
</server>
```

Then, let's deploy our KJAR module:

```sh
cd kjar-without-parent
kjar-without-parent> mvn clean deploy -PusingPlugin
```

And then we can build our Kie Server instance:

```sh
cd .. (root parent)
mvn clean install -PusingPlugin
```

| This approach only works whether the KJAR child module is published in a public Maven repository.

In order to create the Docker image:

```sh
sh utils/prepare_repository.sh repository
docker build -t kie_server_with_plugin -f Dockerfile.offline .
```

And run the image:

```sh
docker run kie_server_with_plugin
```

Expected output:

```sh
[main] INFO org.kie.server.services.impl.KieServerImpl - Container kjar-without-parent-1.0-SNAPSHOT (for release id com.sgitario.kjar-examples:kjar-without-parent:1.0-SNAPSHOT) successfully started
```

###Using the dependency plugin

This approach will use the dependency plugin in order to first retrieve the dependencies in offline:

```sh
mvn -Dmaven.repo.local=repository -Poffline clean install
```

In order to create the Docker image:

```sh
sh utils/prepare_repository.sh repository
docker build -t kie_server_offline -f Dockerfile.offline .
```

And run the image:

```sh
docker run kie_server_offline
```

Expected output:

```sh
[main] INFO org.kie.server.services.impl.KieServerImpl - Container kjar-without-parent-1.0-SNAPSHOT (for release id com.sgitario.kjar-examples:kjar-without-parent:1.0-SNAPSHOT) successfully started
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
- The "Using a plugin Maven" approach didn't work for me

## Source Projects

- Evaluation KJar 1: https://github.com/kie-springboot/evaluation-process-kjar
- Evaluation KJar 2: https://github.com/kie-springboot/evaluation-process-kjar-2
- Search Var Extension (query-ext): https://github.com/kie-springboot/jbpm-search-var-extension
