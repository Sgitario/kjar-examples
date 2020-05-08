package org.kie.server.springboot.samples.listener;

import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.HumanTaskNodeInstance;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.runtime.process.NodeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class JMSAuditProcessEventListener extends DefaultProcessEventListener {

	Logger logger = LoggerFactory.getLogger(JMSAuditProcessEventListener.class);
	@Autowired
	private JmsTemplate jmsTemplate;

	private String varPayload;

	@Value("${kieserver.auditQueueName}")
	private String queueName;

	@Override
	public void afterNodeLeft(ProcessNodeLeftEvent event) {

		NodeInstance node = event.getNodeInstance();

		if (node instanceof HumanTaskNodeInstance) {

			logger.info("Sending JMS Message");

			varPayload = "";

			jmsTemplate.setDefaultDestinationName("bpmqueue");

			WorkflowProcessInstanceImpl wpi = (WorkflowProcessInstanceImpl) event.getProcessInstance();
			wpi.getVariables().keySet().forEach(k -> {

				String tmp = k + "=" + wpi.getVariables().get(k) + ",";

				varPayload += tmp;
			});

			jmsTemplate.convertAndSend("id:" + event.getProcessInstance().getProcessId() + " ,instance:"
					+ event.getProcessInstance().getId() + ", variables:\n" + varPayload);

		} else {

			logger.info("Skipping JMS audit because node isn't a human task");
		}

	}

}
