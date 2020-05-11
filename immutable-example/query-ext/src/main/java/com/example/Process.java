package com.example;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Process implements Serializable {

	private static final long serialVersionUID = -8436832201475966008L;
	private Long instanceId;
	private String id;
	private String correlationKeyName;
	private Map<String, Object> processVariables;

	public Long getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(Long instanceId) {
		this.instanceId = instanceId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCorrelationKeyName() {
		return correlationKeyName;
	}

	public void setCorrelationKeyName(String correlationKeyName) {
		this.correlationKeyName = correlationKeyName;
	}

	public Map<String, Object> getProcessVariables() {
		return processVariables;
	}

	public void setProcessVariables(Map<String, Object> processVariables) {
		this.processVariables = processVariables;
	}

	public void addProcessVariables(List<Variable> vars) {
		if (processVariables == null)
			processVariables = new HashMap<String, Object>();
		vars.forEach(v -> {

			processVariables.put(v.getName(), v.getValue());

		});
	}

}
