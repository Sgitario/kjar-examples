package org.kie.server.springboot.samples;

import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JMSRestController {

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	@Qualifier("kieServerClient")
	KieServicesClient kieServerClient;

	@GetMapping("/sendJms")
	public String test() {
		jmsTemplate.setDefaultDestinationName("bpmqueue");
		jmsTemplate.convertAndSend("coming from bpm");
		return "hi";
	}

	@GetMapping("/startProcess")
	public String startProcess() {

		Long pid = kieServerClient.getServicesClient(ProcessServicesClient.class)
				.startProcess("business-application-kjar-1_0-SNAPSHOT", "Sample.SampleProcess");

		return String.valueOf(pid);

	}

}
