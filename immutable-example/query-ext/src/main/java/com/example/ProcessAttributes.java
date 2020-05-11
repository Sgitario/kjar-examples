package com.example;

import java.io.Serializable;

public class ProcessAttributes implements Serializable {

	private static final long serialVersionUID = -6223181532173694915L;
	private String id;
	private String correlationKeyName;

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

}
