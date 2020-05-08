package org.kie.server.springboot.samples;

import java.util.List;

import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.client.KieServicesClient;
import org.kie.server.services.api.KieServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConfigurationProperties(prefix = "deployment")
@Profile("!test")
public class KieServerDeployer {

    private static final Logger logger = LoggerFactory.getLogger(KieServerDeployer.class);

    @Autowired
    @Qualifier("kieServerClient")
    KieServicesClient kieServerClient;

    @Autowired
    KieServer kieServer;

    private List<KJAR> kjars;

    @Bean
    CommandLineRunner deployAndValidate() {
        return new CommandLineRunner() {

            @Override
            public void run(String... strings) throws Exception {

                List<KieContainerResource> result = kieServer.listContainers(KieContainerResourceFilter.ACCEPT_ALL)
                        .getResult().getContainers();

                logger.info("{} containers deployed", result.size());

                result.forEach(r -> {

                    logger.info("already deployed {}", r);
                });

                kjars.forEach(k -> {
                    KieContainerResource resource = new KieContainerResource(k.getContainerId(), k.getReleaseId());
                    resource.setResolvedReleaseId(k.getReleaseId());
                    resource.setContainerAlias(k.getAlias());

                    if (!isDeployed(resource, result)) {
                        logger.info("deploying {} using custom deployer", k);

                        resource.setResolvedReleaseId(null);
                        kieServer.createContainer(k.getContainerId(), resource);
                    } else {

                        logger.info("skipping deployment of {} because it's already deployed", k);
                    }
                });

            }
        }

                ;
    }

    private boolean isDeployed(KieContainerResource resource, List<KieContainerResource> result) {

        return (result.contains(resource)) ? true : false;
    }

    public List<KJAR> getKjars() {
        return kjars;
    }

    public void setKjars(List<KJAR> kjars) {
        this.kjars = kjars;
    }

}
