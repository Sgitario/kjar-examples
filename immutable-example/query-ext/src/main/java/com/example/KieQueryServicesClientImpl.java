package com.example;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.impl.QueryServicesClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieQueryServicesClientImpl extends QueryServicesClientImpl implements KieQueryServicesClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(KieQueryServicesClientImpl.class);

	private static final String QUERY_TASKS_URI = "/queries/variables";

	public KieQueryServicesClientImpl(KieServicesConfiguration config) {
		super(config);
	}

	public KieQueryServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
		super(config, classLoader);
	}

	/**
	 * @param queryCriteria
	 * @param includeProcessVariables
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Task> queryTasks(Map<String, Object> queryCriteria, boolean includeProcessVariables)
			throws IOException {

		// Obtain KIE Api Endpoint details
		String endpoint = this.loadBalancer.getUrl() + QUERY_TASKS_URI;
		LOGGER.info("Executing 'queryTasks' KIE-Extensions endpoint with URL {} ", endpoint);

		// Build Payload Obj for Extension
		SearchPayload payload = new SearchPayload();
		payload.setSearchCriteria(queryCriteria);
		payload.setAppendProcessVars(includeProcessVariables);

		// Perform Request
		List<Task> tasks = this.makeHttpPostRequestAndCreateCustomResponse(endpoint, payload, List.class);
		LOGGER.info("KIE Query Extension Request for url: {} returned {} task instances", QUERY_TASKS_URI,
				tasks.size());

		return tasks;
	}

	@Override
	public void queryProcesses(Map<String, Object> queryCriteria, boolean includeProcessVariables) {
		// TODO - Implement
	}

	@Override
	public void queryCases(Map<String, Object> queryCriteria, boolean includeProcessVariables) {
		// TODO - Implement
	}
}