# BAPL-1531: Immutable SpringBoot Deployment

The root issue is that the customer infrastructure does not have any artifactory/nexus to download the KJAR from
So we need to provide a way to deploy KJAR with all the dependencies inside
There are two approaches to do so:
- Using Maven Dependency Plugin and Maven Install plugin
- Using the offliner http://release-engineering.github.io/offliner/ which has been wrapped as a Maven plugin

## Objective

I need to test each approach and deploy it **locally** and also **in Openshift** (as the customer is using it this way for production)

## Source Projects

- Evaluation KJar 1: https://github.com/kie-springboot/evaluation-process-kjar
- Evaluation KJar 2: https://github.com/kie-springboot/evaluation-process-kjar-2
- Search Var Extension (query-ext): https://github.com/kie-springboot/jbpm-search-var-extension