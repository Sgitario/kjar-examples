package org.kie.server.springboot.samples;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.example.Task;


@Service
public class KieServerClientProducer {

	private String user = "kieserver";
	private String password = "kieserver1!";
	private static Integer port = 8090;

	@Bean(name = "kieServerClient")
	public KieServicesClient clientProducer() {
		String serverUrl = "http://localhost:" + port + "/rest/server";
		KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(serverUrl, user, password);
		configuration.setTimeout(60000);
		configuration.setMarshallingFormat(MarshallingFormat.JSON);

		List<String> cap = new ArrayList<String>();
		cap.add("COMMONS_QUERY");
		cap.add("BPM");
		cap.add("BRM");
		cap.add("CaseMgmt");

		configuration.setCapabilities(cap);

		Set<Class<?>> classes = new HashSet<Class<?>>();
		classes.add(Task.class);
		configuration.addExtraClasses(classes);

		return KieServicesFactory.newKieServicesClient(configuration);

	}

}
