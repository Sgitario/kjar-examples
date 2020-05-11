package org.kie.server.springboot.samples.listener;

import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.SLAViolatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SLAAwareListener extends DefaultProcessEventListener {

	Logger logger = LoggerFactory.getLogger(JMSAuditProcessEventListener.class);

	@Override
	public void afterSLAViolated(SLAViolatedEvent event) {

		logger.info("SLA Violated");

	}

}
