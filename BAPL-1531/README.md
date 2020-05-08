# BAPL-1531: Immutable SpringBoot Deployment

The root issue is that the customer infrastructure does not have any artifactory/nexus to download the KJAR from
So we need to provide a way to deploy KJAR with all the dependencies inside
There are two approaches to do so:
- Using Maven Dependency Plugin and Maven Install plugin
- Using the offliner http://release-engineering.github.io/offliner/ which has been wrapped as a Maven plugin

## Objective

I need to test each approach and deploy it **locally** and also **in Openshift** (as the customer is using it this way for production)

## Source Projects

- The **business-application-kjar** has been cloned from (https://github.com/ba-spring/business-application-kjar)[https://github.com/ba-spring/business-application-kjar]
- The **query-ext** has been cloned from (https://github.com/ba-spring/business-application-kjar)[https://github.com/ba-spring/business-application-kjar]