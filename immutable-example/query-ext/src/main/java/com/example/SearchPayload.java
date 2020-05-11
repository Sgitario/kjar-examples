package com.example;

import java.io.Serializable;
import java.util.Map;

public class SearchPayload implements Serializable {

	private static final long serialVersionUID = -7326371481550573278L;

	private Map<String, Object> searchCriteria;

	private Boolean appendProcessVars;

	public Boolean getAppendProcessVars() {
		return appendProcessVars;
	}

	public void setAppendProcessVars(Boolean appendProcessVars) {
		this.appendProcessVars = appendProcessVars;
	}

	public Map<String, Object> getSearchCriteria() {
		return searchCriteria;
	}

	public void setSearchCriteria(Map<String, Object> searchCriteria) {
		this.searchCriteria = searchCriteria;
	}

	@Override
	public String toString() {
		return "SearchPayload [searchCriteria=" + searchCriteria + ", appendProcessVars=" + appendProcessVars + "]";
	}

}
