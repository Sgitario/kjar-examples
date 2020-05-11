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
- Run into a standalone kie server using Docker
- Run into Openshift (TODO)

## Solutions

###Approach 1: Using a project Maven repository

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

**- Issues: **
- It requires more manual configuration to specify artifacts (even more complicated when multi-module project
- Build must be executed in the same node

###Approach 2: Using a plugin Maven

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

**- Issues: **
- We need to build and deploy the KJAR to a Maven repository, and then build the Kie Server to create the final Docker image.

###Approach 3: Using the dependency plugin

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

**- Issues: **
- Build must be executed in the same node
- It retrieves all the repository which might be a good thing

## Bugs

- The plugin "kie-maven-plugin" is not configured to be run in Eclipse (see similar in Kogito maven plugin: https://issues.redhat.com/browse/KOGITO-1786)

## Source Projects

- Evaluation KJar 1: https://github.com/kie-springboot/evaluation-process-kjar
- Evaluation KJar 2: https://github.com/kie-springboot/evaluation-process-kjar-2
- Search Var Extension (query-ext): https://github.com/kie-springboot/jbpm-search-var-extension

## Conclusion

If the build of KJAR and the Kie Server image will occur at the same time and instance, it's easier to do this using the **approach 3** (rather than **approach 1** which requires more configuration).
If we need to build first the KJAR and then the Kie Server separately, the only way is via the **approach 2**. 
