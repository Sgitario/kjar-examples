package com.example;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.kie.server.client.QueryServicesClient;

public interface KieQueryServicesClient extends QueryServicesClient {

	List<Task> queryTasks(Map<String, Object> queryCriteria, boolean includeProcessVariables) throws IOException;

	void queryProcesses(Map<String, Object> queryCriteria, boolean includeProcessVariables);

	void queryCases(Map<String, Object> queryCriteria, boolean includeProcessVariables);

}