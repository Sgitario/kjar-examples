package com.example;

import java.util.HashMap;
import java.util.Map;

import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.helper.JBPMServicesClientBuilder;
import org.kie.server.client.helper.KieServicesClientBuilder;

public class KieClientBuilder extends JBPMServicesClientBuilder implements KieServicesClientBuilder {

	
	
	@Override
	public String getImplementedCapability() {
		return "COMMONS_QUERY";
	}
	@Override

    public Map<Class<?>, Object> build(KieServicesConfiguration configuration, ClassLoader classLoader) {
    	
    	Map<Class<?>, Object> services = new HashMap();
    	services.put(KieQueryServicesClient.class, new KieQueryServicesClientImpl(configuration,classLoader));
    	return services;
    	
    }

}
