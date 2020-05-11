package com.example;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariablesQueryRequest {

	private static final String TASK_TYPE = "task";
	private static final String PROCESS_TYPE = "process";
	private static final String ATTRIBUTE_TYPE = "attribute";

	private Map<String, Object> searchTaskVars;
	private Map<String, Object> searchProcessVars;
	private Map<Attribute, Object> attributesCriterias;
	private Map<Long, TaskAttributes> taskAttributes;
	private Boolean haveResults;
	private Boolean haveTaskVar;
	private Boolean haveProcessVar;
	private Boolean havePotentialOwner;
	private SearchPayload payload;
	private static final Logger logger = LoggerFactory.getLogger(VariablesQueryRequest.class);
	private Boolean printVerbose;
	
	public void processSearchCriteria()  {
		printSearchCriteria();
		filterTaskVariables(SQLConstants.TASK_VAR_PREFIX);
		filterProcessVariables(SQLConstants.PROCESS_VAR_PREFIX);
		filterAttributes();
	}

	public VariablesQueryRequest(SearchPayload payload, Boolean printVerbose) {

		this.searchTaskVars = new HashMap<String, Object>();
		this.searchProcessVars = new HashMap<String, Object>();
		this.attributesCriterias = new HashMap<Attribute, Object>();
		this.taskAttributes = new HashMap<Long, TaskAttributes>();
		this.haveResults = true;
		this.haveTaskVar = false;
		this.haveProcessVar = false;
		this.havePotentialOwner = false;
		this.payload = payload;
		this.printVerbose = printVerbose;
	}
	
	public Boolean getAppendVar() {
		return payload.getAppendProcessVars();
	}

	public Map<String, Object> getSearchTaskVars() {
		return searchTaskVars;
	}

	public void setSearchTaskVars(Map<String, Object> searchTaskVars) {
		this.searchTaskVars = searchTaskVars;
	}

	public Map<String, Object> getSearchProcessVars() {
		return searchProcessVars;
	}

	public void setSearchProcessVars(Map<String, Object> searchProcessVars) {
		this.searchProcessVars = searchProcessVars;
	}



	public Map<Attribute, Object> getAttributesCriterias() {
		return attributesCriterias;
	}

	public void setAttributesCriterias(Map<Attribute, Object> attributesCriterias) {
		this.attributesCriterias = attributesCriterias;
	}

	public Map<Long, TaskAttributes> getTaskAttributes() {
		return taskAttributes;
	}

	public void setTaskAttributes(Map<Long, TaskAttributes> taskAttributes) {
		this.taskAttributes = taskAttributes;
	}

	public void filterTaskVariables(String prefix) {

		Map<String, Object> result = filterByPrefix(prefix);
		if (!result.isEmpty()) {
			haveTaskVar = true;
		}

		this.searchTaskVars = result;

		if (printVerbose) {
			printMap(this.searchTaskVars, TASK_TYPE);
		}

	}

	public void filterProcessVariables(String prefix) {

		Map<String, Object> result = filterByPrefix(prefix);
		if (!result.isEmpty()) {
			haveProcessVar = true;
		}

		this.searchProcessVars = result;
		if (printVerbose) {
			printMap(searchProcessVars, PROCESS_TYPE);
		}

	}

	public Map<String, Object> filterByPrefix(String prefix) {
		Map<String, Object> result = payload.getSearchCriteria().entrySet().stream()
				.filter(k -> k.getKey().toLowerCase().startsWith(prefix))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		return result;
	}

	public void printSearchCriteria() {
		logger.info("Received following search criterias");
		payload.getSearchCriteria().keySet().forEach(k -> {
			logger.info("{} = {}", k, payload.getSearchCriteria().get(k));
		});
		logger.info("appendProcessVars {}", payload.getAppendProcessVars());

	}

	public void printMap(Map<String, Object> map, String objectType) {

		map.keySet().forEach(k -> {
			logger.info("{} var name {}, {} var value {}", objectType, k, objectType, map.get(k));
		});
	}

	public void filterAttributes() {
		Map<Attribute, Object> result = payload.getSearchCriteria().keySet().stream()
				.filter(k -> EnumUtils.isValidEnum(Attribute.class,k))
				.collect(Collectors.toMap(k -> Attribute.valueOf(k.toUpperCase()),
						k -> payload.getSearchCriteria().get(k)));
		checkAttributes(result);
		this.attributesCriterias = result;
		if (printVerbose) {
			printAttributeMap(this.attributesCriterias, ATTRIBUTE_TYPE);
		}
	}

	private void printAttributeMap(Map<Attribute, Object> attributesCriterias, String objectType) {
		attributesCriterias.keySet().forEach(t -> {
			logger.info("{}  name {}, {} value {}", objectType, t, objectType, attributesCriterias.get(t));
		});
	}

	private void checkAttributes(Map<Attribute, Object> attributesCriterias) {

		attributesCriterias.keySet().forEach(a -> {

			switch (a) {

			case ACTUAL_OWNER:
			case TASK_NAME: {
				haveTaskVar = true;

				break;
			}

			case PROCESS_ID:
			case PROCESS_INSTANCE_ID:
			case BUSINESS_KEY:

			{
				haveProcessVar = true;
				break;
			}

			default:
				break;
			}

		});

	}

	public Boolean getHaveResults() {
		return haveResults;
	}

	public void setHaveResults(Boolean haveResults) {
		this.haveResults = haveResults;
	}

	public Boolean getHaveTaskVar() {
		return haveTaskVar;
	}

	public void setHaveTaskVar(Boolean haveTaskVar) {
		this.haveTaskVar = haveTaskVar;
	}

	public Boolean getHaveProcessVar() {
		return haveProcessVar;
	}

	public void setHaveProcessVar(Boolean haveProcessVar) {
		this.haveProcessVar = haveProcessVar;
	}

	public Boolean getHavePotentialOwner() {
		return havePotentialOwner;
	}

	public void setHavePotentialOwner(Boolean havePotentialOwner) {
		this.havePotentialOwner = havePotentialOwner;
	}

}
