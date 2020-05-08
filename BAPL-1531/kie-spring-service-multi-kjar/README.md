KIE Server with all capabilities
========================================

NOTE: Tests are currently broken.  Skip Them!



How to build
------------------------------

For this example to work properly the following project need to be built first: 
```
mvn clean install
``` 

1. Evaluation KJar 1: https://github.com/kie-springboot/evaluation-process-kjar
2. Evaluation KJar 2: https://github.com/kie-springboot/evaluation-process-kjar-2
3. Search Var Extension: https://github.com/kie-springboot/jbpm-search-var-extension

Finally build the kie-spring-service-multi-kjar project. 

NOTE: 
- Although currently commented out (no process definitions visible with a KIE Base defined :/ ), these projects dynamically name the KIE-Base using the maven resources plugin. 
-- https://github.com/kie-springboot/evaluation-process-kjar/blob/master/src/main/resources/META-INF/kmodule.xml#L2
-- The resources plugin will define the KIE-Base as: ${project.artifactId}-${project.version}-KBase
-- Resources plugin is defined here: https://github.com/kie-springboot/evaluation-process-kjar/blob/master/pom.xml#L70

- The kie-spring-service-multi-kjar defines 3 steps to package the KJars as a single UBer JAR: 

1. The Maven Dependency Plugin is used to retrieve the required artifacts from the Remote local repository as defined in the settings.xml on the building machine: https://github.com/kie-springboot/kie-spring-service-multi-kjar/blob/master/pom.xml#L266

2. The Maven Install Plugin is then used to create a 'local maven repo' which can be packaged in with the Spring Boot Uber Jar: https://github.com/kie-springboot/kie-spring-service-multi-kjar/blob/master/pom.xml#L310
- The Maven install plugin is created in the src/main/resources folder, which is packaged into the UBer Jar without any additional config. 

3. To prevent any filtering of resources, the resources plugin is configured to ignore filtering of JAR files: 
https://github.com/kie-springboot/kie-spring-service-multi-kjar/blob/master/pom.xml#L251

4. Compile the project
 ```
mvn clean install -DskipTests
``` 

5. Once compiled these Artifacts can be seen in the following locations: 

- The target/classes folder as specified by the Maven Install plugin
```
$ ls -la target/classes/m2/repository/com/company/evaluation/
drwxr-xr-x  5 allyjarrett  staff  160 29 Jan 02:50 .
drwxr-xr-x  3 allyjarrett  staff   96 29 Jan 02:50 ..
drwxr-xr-x  6 allyjarrett  staff  192 29 Jan 02:50 1.0.0-SNAPSHOT
drwxr-xr-x  6 allyjarrett  staff  192 29 Jan 02:50 2.0.0-SNAPSHOT
-rw-r--r--  1 allyjarrett  staff  321 29 Jan 03:04 maven-metadata-local.xml
```

- If you unzip the compiled Uber JAR into a temp folder you will see the repository contained in the following dir: 
```
$ ls -la temp/BOOT-INF/classes/m2/repository/com/company/evaluation/
total 8
drwxr-xr-x  5 allyjarrett  staff  160 29 Jan 02:50 .
drwxr-xr-x  3 allyjarrett  staff   96 29 Jan 02:50 ..
drwxr-xr-x  6 allyjarrett  staff  192 29 Jan 02:50 1.0.0-SNAPSHOT
drwxr-xr-x  6 allyjarrett  staff  192 29 Jan 02:50 2.0.0-SNAPSHOT
-rw-r--r--  1 allyjarrett  staff  321 29 Jan 02:50 maven-metadata-local.xml
```

5. We now have an UBER Jar that contains a local maven repository, ready to be extracted into our runtime environment. 

How to run it
------------------------------

Now we have compiled we can test with the locally built maven repository

1. Containers to be deployed are defined in the Spring Boot Yaml file here: https://github.com/kie-springboot/kie-spring-service-multi-kjar/blob/master/src/main/resources/application.yaml#L8

```
deployment:
  kjars:
    - alias: evaluation-business-application-kjar
      artifactId: evaluation
      containerId: evaluation-kjar-1_0-SNAPSHOT
      groupId: com.company
      version: 1.0.0-SNAPSHOT
    - alias: evaluation-business-application-kjar
      artifactId: evaluation
      containerId: evaluation-kjar-2_0-SNAPSHOT
      groupId: com.company
      version: 2.0.0-SNAPSHOT

```

2. Next, update the settings.xml to your relative local repository directory. Once the project has compiled you will see a m2/repository folders created in the src/main/resources dir. 
- https://github.com/kie-springboot/kie-spring-service-multi-kjar/blob/master/src/main/resources/settings.xml

3. Run the Uber Jar pointing at the maven repository created at compile time: 
```
java -jar target/kie-spring-boot-example.jar -Dkie.maven.settings.custom=src/main/resources/settings.xml 

```

4. You should see the following logging during startup: 

 About to install containers '[KieContainerResource [containerId=evaluation-kjar-1_0-SNAPSHOT, releaseId=com.company:evaluation:1.0.0-SNAPSHOT, resolvedReleaseId=null, status=STARTED], KieContainerResource [containerId=evaluation-kjar-2_0-SNAPSHOT, releaseId=com.company:evaluation:2.0.0-SNAPSHOT, resolvedReleaseId=null, status=STARTED]]' on kie server 'KieServer{id='business-application-service'name='business-application-service'version='7.30.0.Final-redhat-00003'location='http://0.0.0.0:8090/rest/server'}'
[main] INFO org.kie.server.springboot.autoconfiguration.KieServerAutoConfiguration - KieServer (id business-application-service) started successfully
[main] INFO org.jbpm.runtime.manager.impl.AbstractRuntimeManager - SingletonRuntimeManager is created for evaluation-kjar-1_0-SNAPSHOT
[main] INFO org.kie.server.services.impl.KieServerImpl - Container evaluation-kjar-1_0-SNAPSHOT (for release id com.company:evaluation:1.0.0-SNAPSHOT) successfully started
[main] INFO org.kie.server.springboot.samples.KieServerDeployer - deploying KJAR [groupId=com.company, artifactId=evaluation, version=2.0.0-SNAPSHOT, containerId=evaluation-kjar-2_0-SNAPSHOT, alias=evaluation-business-application-kjar] using custom deployer
[main] INFO org.jbpm.runtime.manager.impl.AbstractRuntimeManager - SingletonRuntimeManager is created for evaluation-kjar-2_0-SNAPSHOT
[main] INFO org.kie.server.springboot.samples.KieServerApplication - Started KieServerApplication in 15.199 seconds (JVM running for 15.894)
[main] INFO org.kie.server.springboot.samples.KieServerDeployer - 2 containers deployed
