package com.example.notification.listener;

import java.io.IOException;
import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.NamingException;

import org.jbpm.services.task.deadlines.NotificationListener;
import org.kie.internal.task.api.UserInfo;
import org.kie.internal.task.api.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMSNotificationListener implements NotificationListener {

	Logger logger = LoggerFactory.getLogger(JMSNotificationListener.class);

	private ConnectionFactory factory;
	private Destination destination;

	public JMSNotificationListener() throws NamingException {

		logger.info("JMS Notification Listener was initialized");
		Hashtable<Object, Object> env = new Hashtable<Object, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
		env.put("connectionfactory.bpmFactory", "amqp://127.0.0.1:5672");
		env.put("queue.bpmLookup", "bpmqueue");
		javax.naming.Context context = new javax.naming.InitialContext(env);
		factory = (ConnectionFactory) context.lookup("bpmFactory");
		destination = (Destination) context.lookup("bpmLookup");

	}

	@Override
	public void onNotification(NotificationEvent event, UserInfo userinfo) {

		logger.info("notification send for {} to {}", event, userinfo);
		Connection connection = null;
		Session session = null;
		MessageProducer producer = null;

		try {
			connection = factory.createConnection();
			connection.setExceptionListener(new MyExceptionListener());
			connection.start();

			session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
			producer = session.createProducer(destination);

			TextMessage message = session.createTextMessage("Notification sent, pid="
					+ event.getTask().getTaskData().getProcessInstanceId() + ",tid=" + event.getTask().getId());
			producer.send(message);

			connection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static class MyExceptionListener implements ExceptionListener {
		@Override
		public void onException(JMSException exception) {
			System.out.println("Connection ExceptionListener fired, exiting.");
			exception.printStackTrace(System.out);
		}
	}

}
