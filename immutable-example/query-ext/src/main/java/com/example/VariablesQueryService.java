package com.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariablesQueryService {

	private static final Logger logger = LoggerFactory.getLogger(VariablesQueryService.class);

	private VariablesQueryRequest request;
	private EntityManagerFactory emf;
	private List<IDWrapper> tasksByPotentialOwner;
	private List<IDWrapper> tasksByProcessVars;
	private List<IDWrapper> tasksByTasksVar;
	private Map<Long, List<Variable>> taskVariables;
	private Map<Long, TaskAttributes> taskAttributes;
	private Map<Long, ProcessAttributes> processAttributes;
	private Map<Long, List<Variable>> processVariables;

	private Boolean printVerbose;

	private Boolean haveResults;
	private static final String POTENTIAL_OWNER = " Potential Owner ";
	private static final String PROCESS_VAR = " Process variable ";
	private static final String TASK_VAR = " Task Variable ";

	private static final String INTERSECT = " Intersect ";

	public VariablesQueryService() {
		init();
	}

	public void init() {
		this.tasksByPotentialOwner = new ArrayList<IDWrapper>();
		this.tasksByProcessVars = new ArrayList<IDWrapper>();
		this.tasksByTasksVar = new ArrayList<IDWrapper>();
		this.haveResults = true;
		this.taskVariables = new HashMap<Long, List<Variable>>();
		this.taskAttributes = new HashMap<Long, TaskAttributes>();
		this.processVariables = new HashMap<Long, List<Variable>>();
		this.processAttributes = new HashMap<Long, ProcessAttributes>();

	}

	public VariablesQueryService(EntityManagerFactory emf, Boolean printVerbose) {
		this.emf = emf;
		this.printVerbose = printVerbose;
		init();

	}

	public VariablesQueryService(VariablesQueryRequest request, EntityManagerFactory emf) {

		this.request = request;
		this.emf = emf;
		init();

	}

	public VariablesQueryRequest getRequest() {
		return request;
	}

	public void setRequest(VariablesQueryRequest request) {
		this.request = request;
	}

	public EntityManagerFactory getEmf() {
		return emf;
	}

	public void setEmf(EntityManagerFactory emf) {
		this.emf = emf;
	}

	public void filter() {

		filterByPotentialOwner();
		filterByProcessVars();
		filterByTasksVars();
	}

	public void filterByPotentialOwner() {
		if (request.getAttributesCriterias().containsKey(Attribute.POTENTIAL_OWNER)) {
			request.setHavePotentialOwner(true);

			String sql = buildGetTasksByPotentialOwnerSQL(
					request.getAttributesCriterias().get(Attribute.POTENTIAL_OWNER).toString());
			logger.info("filterByPotentialOwner sql {}", sql);
			EntityManager em = emf.createEntityManager();
			Query query = em.createNativeQuery(sql);
			List<Object[]> sqlResult = query.getResultList();
			List<IDWrapper> pojoResult = transformSQLToIdWrapper(sqlResult);
			if (pojoResult.isEmpty() && request.getHavePotentialOwner()) {
				this.haveResults = false;

			}
			em.close();

			if (printVerbose) {
				printList(pojoResult, POTENTIAL_OWNER);
			}

			this.tasksByPotentialOwner = pojoResult;

		}

	}

	private void printList(List<IDWrapper> pojoResult, String filterType) {
		pojoResult.forEach(p -> logger.info("filtered by {} {} ", filterType, p));

	}

	private List<IDWrapper> transformSQLToIdWrapper(List<Object[]> sqlResult) {
		List<IDWrapper> result = new ArrayList<IDWrapper>();

		sqlResult.forEach(s -> {
			result.add(new IDWrapper(s));
		});

		return result;
	}

	private String buildGetTasksByPotentialOwnerSQL(String groups) {
		String sql = "";

		sql += SQLConstants.SELECT_TASKS_BY_POTENTIAL_OWNERS_START;
		List<String> listGroups = new ArrayList<>(Arrays.asList(groups.split(",")));
		String strGroups = "";
		for (String group : listGroups) {

			strGroups += SQLConstants.SINGLE_QUOTE;
			strGroups += group;
			strGroups += SQLConstants.SINGLE_QUOTE;
			strGroups += SQLConstants.COMMA;

		}

		strGroups = removeLastOccurence(strGroups, SQLConstants.COMMA);

		sql += strGroups;
		sql += SQLConstants.SELECT_TASKS_BY_POTENTIAL_OWNERS_END;
		return sql;
	}

	private String removeLastOccurence(String source, String toRemove) {
		if (!source.isEmpty() && source.contains(toRemove)) {
			StringBuilder builder = new StringBuilder();
			int start = source.lastIndexOf(toRemove);
			builder.append(source.substring(0, start));
			return builder.toString();
		} else
			return source;

	}

	public void filterByProcessVars() {
		if (haveResults && request.getHaveProcessVar()) {
			String sql = buildSearchByProcessVarQuery();
			logger.info("filterByProcessVars sql \n {}", sql);

			EntityManager em = emf.createEntityManager();
			Query query = em.createNativeQuery(sql);
			List<Object[]> sqlResult = query.getResultList();
			List<IDWrapper> pojoResult = transformSQLToIdWrapper(sqlResult);

			em.close();

			this.tasksByProcessVars = pojoResult;
			if (tasksByProcessVars.isEmpty()) {
				haveResults = false;
			}
		}

		if (printVerbose) {
			printList(tasksByProcessVars, PROCESS_VAR);
		}
	}

	private String applyProcessAttributes(Map<Attribute, Object> attributes) {
		AtomicReference<String> sql = new AtomicReference<String>();
		//@formatter:off

		attributes.keySet().forEach(a -> {

			switch (a) {

			case BUSINESS_KEY: {

				String local = sql.get() != null ? sql.get() : "";
				String tmp = SQLConstants.CORRELATION_KEY_NAME 
						+ SQLConstants.EQUAL_TO
						+ SQLConstants.SINGLE_QUOTE
						+ attributes.get(Attribute.BUSINESS_KEY)
						+ SQLConstants.SINGLE_QUOTE
						+ SQLConstants.AND;
				sql.set(local + "\n" + tmp);
				
				break;
			}
			
			case PROCESS_INSTANCE_ID: {
				String local = sql.get() != null ? sql.get() : "";
				String tmp = SQLConstants.PROCESS_INSTANCE_ID 
						+ SQLConstants.EQUAL_TO
						+ Long.valueOf(attributes.get(Attribute.PROCESS_INSTANCE_ID).toString())
						+ SQLConstants.AND;
				sql.set(local + "\n" + tmp);
				break;
				
			}
			
			case PROCESS_ID: {
				String local = sql.get() != null ? sql.get() : "";
				String tmp = SQLConstants.PROCESS_ID 
						+ SQLConstants.EQUAL_TO
						+ SQLConstants.SINGLE_QUOTE
						+ attributes.get(Attribute.PROCESS_ID)
						+ SQLConstants.SINGLE_QUOTE
						+ SQLConstants.AND;
				sql.set(local + "\n" + tmp);				
				
				break;
			}
			
			default: break;

			}

		});
		
		//@formatter:on

		return sql.get();

	}

	private String buildSearchByProcessVarQuery() {
		String variableColumns = "";
		String whereClause = "";
		for (String var : request.getSearchProcessVars().keySet()) {
			variableColumns += String.format(SQLConstants.PROCESS_VAR_MAX,
					var.substring(SQLConstants.PROCESS_VAR_PREFIX.length()),
					var.substring(SQLConstants.PROCESS_VAR_PREFIX.length()));
			whereClause += SQLConstants.VAR_PREFIX + var.substring(SQLConstants.PROCESS_VAR_PREFIX.length()) + " = "
					+ "'" + request.getSearchProcessVars().get(var).toString() + "' " + SQLConstants.AND;
		}

		whereClause += applyProcessAttributes(request.getAttributesCriterias());

		String sql = "";
		sql += SQLConstants.SELECT_PROCESS + variableColumns + SQLConstants.FROM_PROCESSVARLOG;

		whereClause = removeLastOccurence(whereClause, SQLConstants.AND);

		sql += SQLConstants.WHERE + " " + whereClause;

		return sql;
	}

	public void filterByTasksVars() {

		if (haveResults && request.getHaveTaskVar()) {

			String sql = buildSearchByTaskVarQuery();
			logger.info("filterByTasksVars sql \n {}", sql);

			EntityManager em = emf.createEntityManager();
			Query query = em.createNativeQuery(sql);
			List<Object[]> sqlResult = query.getResultList();
			List<IDWrapper> pojoResult = transformSQLToIdWrapper(sqlResult);
			tasksByTasksVar = pojoResult;
			if (tasksByTasksVar.isEmpty()) {
				haveResults = false;
			}

			em.close();
		}

		if (printVerbose) {
			printList(tasksByTasksVar, TASK_VAR);
		}
	}

	private String buildSearchByTaskVarQuery() {
		String variableColumns = "";
		String whereClause = "";
		for (String var : request.getSearchTaskVars().keySet()) {
			variableColumns += String.format(SQLConstants.TASK_VAR_MAX,
					var.substring(SQLConstants.TASK_VAR_PREFIX.length()),
					var.substring(SQLConstants.TASK_VAR_PREFIX.length()));
			whereClause += SQLConstants.VAR_PREFIX + var.substring(SQLConstants.TASK_VAR_PREFIX.length()) + " = " + "'"
					+ request.getSearchTaskVars().get(var).toString() + "' " + SQLConstants.AND;
		}

		whereClause += applyTaskAttributes(request.getAttributesCriterias());

		String sql = "";
		sql += SQLConstants.SELECT_TASK + variableColumns + SQLConstants.FROM_TASKVARLOG;
		whereClause = removeLastOccurence(whereClause, SQLConstants.AND);
		sql += SQLConstants.WHERE + " " + whereClause;

		return sql;
	}

	private String applyTaskAttributes(Map<Attribute, Object> attributesCriterias) {
		AtomicReference<String> sql = new AtomicReference<String>();
		//@formatter:off

		attributesCriterias.keySet().forEach(a -> {

			switch (a) {

			case ACTUAL_OWNER: {

				String local = sql.get() != null ? sql.get() : "";
				String tmp = SQLConstants.ACTUALOWNER_ID
						+ SQLConstants.EQUAL_TO
						+ SQLConstants.SINGLE_QUOTE
						+ attributesCriterias.get(Attribute.ACTUAL_OWNER)
						+ SQLConstants.SINGLE_QUOTE
						+ SQLConstants.AND;
				sql.set(local + "\n" + tmp);

				break;
			}
			
			case TASK_NAME: {
				
				String local = sql.get() != null ? sql.get() : "";
				String tmp = SQLConstants.TASK_NAME
						+ SQLConstants.EQUAL_TO
						+ SQLConstants.SINGLE_QUOTE
						+ attributesCriterias.get(Attribute.TASK_NAME)
						+ SQLConstants.SINGLE_QUOTE
						+ SQLConstants.AND;
				sql.set(local + "\n" + tmp);

				break;
				
			}

			default:
				break;
			}
		});
		
		//@formatter:on

		return sql.get();
	}

	public Boolean getHaveResults() {

		if (!haveResults
				|| (tasksByPotentialOwner.isEmpty() && tasksByProcessVars.isEmpty() && tasksByTasksVar.isEmpty())) {
			return false;
		}
		return true;
	}

	public void setHaveResults(Boolean haveResults) {
		this.haveResults = haveResults;
	}

	public Set<IDWrapper> intersectResults() {
		List<Set<IDWrapper>> tmpResult = new ArrayList<>();
		if (request.getHaveProcessVar())
			tmpResult.add(new HashSet<>(tasksByProcessVars));
		if (request.getHaveTaskVar())
			tmpResult.add(new HashSet<>(tasksByTasksVar));
		if (request.getHavePotentialOwner())
			tmpResult.add(new HashSet<>(tasksByPotentialOwner));
		Set<IDWrapper> src = tmpResult.get(0);
		Set<IDWrapper> result = new HashSet<IDWrapper>();
		for (int i = 1; i < tmpResult.size(); i++) {
			src.retainAll(tmpResult.get(i));
		}
		for (IDWrapper address : src) {
			result.add(address);
		}

		if (printVerbose) {

			printSet(result, INTERSECT);
		}

		return result;

	}

	private void printSet(Set<IDWrapper> result, String setType) {
		result.forEach(t -> logger.info("{} {} ", setType, t));

	}

	public Map<Long, List<Variable>> getTaskVariables() {
		return taskVariables;
	}

	public void setTaskVariables(Map<Long, List<Variable>> taskVariables) {
		this.taskVariables = taskVariables;
	}

	public void fetchTaskVariables(Set<IDWrapper> intersect) {
		Set<Long> tids = new HashSet<Long>();
		intersect.forEach(id -> tids.add(id.getTaskid()));
		List<Variable> variables = new ArrayList<Variable>();
		if (!tids.isEmpty()) {
			variables = executeTaskVariablesSQL(tids);
		}
		this.taskVariables = variables.stream().collect(Collectors.groupingBy(Variable::getParentId));
	}

	@SuppressWarnings("unchecked")
	private List<Variable> executeTaskVariablesSQL(Set<Long> tids) {
		String sql = buildGetTaskVarsQuery(tids);
		logger.info("executeTaskVariablesSQL sql {}", sql);
		EntityManager em = emf.createEntityManager();
		Query query = em.createNativeQuery(sql);
		List<Object[]> sqlResult = query.getResultList();
		extractTaskAttributes(sqlResult);
		List<Variable> pojoResult = transformToVariable(sqlResult, SQLConstants.TASK_TYPE);
		em.close();

		return pojoResult;
	}

	private List<Variable> transformToVariable(List<Object[]> sqlResult, String varType) {

		List<Variable> result = new ArrayList<Variable>();

		sqlResult.forEach(s -> {

			result.add(new Variable(s, varType));
		});
		return result;
	}

	private void extractTaskAttributes(List<Object[]> sqlResult) {

		sqlResult.forEach(sql -> {

			TaskAttributes attribute = new TaskAttributes(sql);
			taskAttributes.put(attribute.getTaskId(), attribute);
		});

	}

	private String buildGetTaskVarsQuery(Set<Long> tids) {
		String sql = "";
		sql += SQLConstants.SELECT_TASK_VARS;

		String idList = "";
		for (Long id : tids) {
			idList += id + " , ";
		}

		idList = removeLastOccurence(idList, SQLConstants.COMMA);
		sql += idList;
		sql += SQLConstants.END_TASK_VARS;

		return sql;
	}

	public void fetchProcessVariables(Set<IDWrapper> intersect) {
		if (request.getAppendVar() != null && request.getAppendVar()) {

			Set<Long> pids = new HashSet<Long>();
			intersect.forEach(id -> pids.add(id.getProcessinstanceid()));

			List<Variable> variables = new ArrayList<Variable>();
			if (!pids.isEmpty()) {
				variables = executeProcessVariablesSQL(pids);
			}
			this.processVariables = variables.stream().collect(Collectors.groupingBy(Variable::getParentId));

			if (printVerbose) {

				// TODO

			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<Variable> executeProcessVariablesSQL(Set<Long> pids) {
		String sql = buildGetProcessVarsQuery(pids);
		logger.info("executeProcessVariablesSQL sql \n {}", sql);
		EntityManager em = emf.createEntityManager();
		Query query = em.createNativeQuery(sql);
		List<Object[]> sqlResult = query.getResultList();
		List<Variable> pojoResult = transformToVariable(sqlResult, SQLConstants.PROCESS_TYPE);
		em.close();

		return pojoResult;

	}

	private String buildGetProcessVarsQuery(Set<Long> pids) {
		String sql = "";
		sql += SQLConstants.SELECT_PROCESS_VARS;

		String idList = "";
		for (Long id : pids) {
			idList += id + " , ";
		}

		idList = removeLastOccurence(idList, SQLConstants.COMMA);
		sql += idList;
		sql += SQLConstants.END_PROCESS_VARS;

		return sql;
	}

	public Map<Long, List<Variable>> getProcessVariables() {
		return processVariables;
	}

	public void setProcessVariables(Map<Long, List<Variable>> processVariables) {
		this.processVariables = processVariables;
	}

	public List<Task> generateTaskResult(Set<IDWrapper> intersect) {

		List<Task> result = new ArrayList<Task>();

		intersect.forEach(id -> {

			result.add(generateBPMTask(id));
		});

		return result;
	}

	private Task generateBPMTask(IDWrapper id) {
		Task task = new Task();
		task.setProcessInstanceId(id.getProcessinstanceid());
		task.setTaskId(id.getTaskid());

		if (taskVariables.containsKey(id.getTaskid())) {
			task.addTaskVariables(taskVariables.get(id.getTaskid()));
		}
		if (processVariables.containsKey(id.getProcessinstanceid())) {
			task.addProcessVariables(processVariables.get(id.getProcessinstanceid()));
		}
		task.setActualOwner(taskAttributes.get(id.getTaskid()).getActualOwner());
		task.setName(taskAttributes.get(id.getTaskid()).getName());
		task.setProcessId(taskAttributes.get(id.getTaskid()).getProcessId());
		task.setCorrelationKeyName(taskAttributes.get(id.getTaskid()).getCorrelationKeyName());
		task.setGroups(taskAttributes.get(id.getTaskid()).getGroups());

		return task;
	}

	public void fetchTaskGroups(Set<IDWrapper> intersect) {
		Set<Long> tids = new HashSet<Long>();
		intersect.forEach(id -> tids.add(id.getTaskid()));
		if (!tids.isEmpty()) {
			executeTaskGroupsSQL(tids);
		}

	}

	private void executeTaskGroupsSQL(Set<Long> tids) {
		String sql = buildSelectTaskGroupsQuery(tids);
		logger.info("executeTaskGroupsSQL sql \n {}", sql);
		EntityManager em = emf.createEntityManager();
		Query query = em.createNativeQuery(sql);
		List<Object[]> sqlResult = query.getResultList();
		addToTaskAttributes(sqlResult);
		em.close();
	}

	private void addToTaskAttributes(List<Object[]> sqlResult) {
		sqlResult.forEach(r -> {

			Long taskId = Long.valueOf(r[0].toString());
			taskAttributes.get(taskId).addGroup(r[1].toString());
		});

	}

	private String buildSelectTaskGroupsQuery(Set<Long> tids) {
		String sql = "";
		sql += SQLConstants.SELECT_TASK_GROUPS;

		String idList = "";
		for (Long id : tids) {
			idList += id + " , ";
		}

		idList = removeLastOccurence(idList, SQLConstants.COMMA);
		sql += idList;
		sql += SQLConstants.RIGHT_BRACKET;

		return sql;
	}

	public List<Task> taskResult() {
		Set<IDWrapper> intersect = intersectResults();
		fetchTaskVariables(intersect);
		fetchTaskGroups(intersect);
		fetchProcessVariables(intersect);

		return generateTaskResult(intersect);
	}

	public List<Process> bpmProcessResult() {
		Set<IDWrapper> intersect = intersectResults();
		fetchProcessVariables(intersect);
		return generateProcessResult(intersect);

	}

	private List<Process> generateProcessResult(Set<IDWrapper> intersect) {
		List<Process> result = new ArrayList<Process>();

		intersect.forEach(id -> {

			result.add(generateBPMProcess(id));
		});

		return result;
	}

	private Process generateBPMProcess(IDWrapper id) {
		Process p = new Process();
		p.setInstanceId(id.getProcessinstanceid());
		if (processVariables.containsKey(id.getProcessinstanceid())) {
			p.addProcessVariables(processVariables.get(id.getProcessinstanceid()));
		}
		p.setCorrelationKeyName(id.getCorrelationKeyName());
		p.setId(id.getProcessId());
		return p;
	}

}
