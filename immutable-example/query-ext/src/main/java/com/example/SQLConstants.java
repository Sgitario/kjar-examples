package com.example;

public class SQLConstants {

	public static final String SELECT_TASKS_BY_POTENTIAL_OWNERS_START = " SELECT "
			+ " DISTINCT taskimpl0_.id AS taskid, " + " taskimpl0_.processInstanceId AS processinstanceid,cki.NAME correlationKeyName, pi.PROCESSID " + " FROM "
			+ " Task taskimpl0_ " + " INNER JOIN PeopleAssignments_PotOwners potentialo1_ ON "
			+ " 	taskimpl0_.id = potentialo1_.task_id " + " INNER JOIN OrganizationalEntity organizati2_ ON "
			+ "	potentialo1_.entity_id = organizati2_.id"
			+ " JOIN PROCESSINSTANCEINFO pi ON pi.INSTANCEID = taskimpl0_.PROCESSINSTANCEID"
			+ " LEFT JOIN CORRELATIONKEYINFO cki ON pi.INSTANCEID = cki.PROCESSINSTANCEID"
			+ " WHERE" + "	taskimpl0_.archived = 0"
			+ "	AND (taskimpl0_.status IN ('Created' ," + "	'Ready' ," + "	'Reserved' ," + "	'InProgress' ,"
			+ "	'Suspended'))" + "	AND (organizati2_.id = null" + "	OR organizati2_.id IN (";
	public static final String SELECT_TASKS_BY_POTENTIAL_OWNERS_END = "	))" + " AND (null NOT IN ( " + " SELECT "
			+ " excludedow3_.entity_id" + " FROM" + " 	PeopleAssignments_ExclOwners excludedow3_" + " WHERE"
			+ " taskimpl0_.id = excludedow3_.task_id))" + " ORDER BY" + " taskimpl0_.id DESC";

	public static final String SELECT_TASK = "SELECT taskid,processinstanceid,correlationKeyName,PROCESSID,ACTUALOWNER_ID,TASKNAME FROM ( SELECT T.taskId,t.processinstanceid, task.ACTUALOWNER_ID,cki.NAME correlationKeyName, pi.PROCESSID, task.name taskname ";

	public static final String SELECT_PROCESS = "SELECT taskid,processinstanceid,correlationKeyName,PROCESSID FROM ( SELECT T.taskId,t.processinstanceid,ck.NAME correlationKeyName, pi.PROCESSID ";
	public static final String FROM_PROCESSVARLOG = " FROM VARIABLEINSTANCELOG V "
			+ "	LEFT JOIN VARIABLEINSTANCELOG  V2 ON ( V.VARIABLEINSTANCEID = V2.VARIABLEINSTANCEID  AND V.PROCESSINSTANCEID=V2.PROCESSINSTANCEID AND V.ID < V2.ID )"
			+ "	INNER JOIN AUDITTASKIMPL  T ON T.PROCESSINSTANCEID = V.PROCESSINSTANCEID"
			+ " JOIN CORRELATIONKEYINFO ck ON ck.PROCESSINSTANCEID = T.PROCESSINSTANCEID "
			+ "	JOIN PROCESSINSTANCEINFO pi ON pi.instanceid = t.PROCESSINSTANCEID "
			+ "	WHERE V2.ID IS NULL AND t.STATUS != 'Completed' GROUP BY T.TASKID,t.processinstanceid,ck.name, pi.PROCESSID ) resultAlias ";

	public static final String FROM_TASKVARLOG = "  FROM TASKVARIABLEIMPL V  "
			+ " LEFT JOIN TASKVARIABLEIMPL  V2 ON ( V.NAME = V2.NAME AND V.TASKID=V2.TASKID AND V.ID < V2.ID )	"
			+ " INNER JOIN AUDITTASKIMPL  T ON T.PROCESSINSTANCEID = V.PROCESSINSTANCEID	 "
			+ " JOIN PROCESSINSTANCEINFO pi ON pi.instanceid = t.PROCESSINSTANCEID "
			+ "  LEFT JOIN CORRELATIONKEYINFO cki ON pi.INSTANCEID = cki.PROCESSINSTANCEID"
			+ " JOIN Task task ON task.ID = v.TASKID"
			+ " WHERE V2.ID IS NULL GROUP BY T.TASKID,t.processinstanceid,task.ACTUALOWNER_ID, task.name,pi.PROCESSID, cki.NAME ) resultAlias ";

	public static final String PROCESS_VAR_MAX = ", MAX ( CASE V.VARIABLEINSTANCEID WHEN '%s' THEN V.VALUE END )  VAR_%s";
	public static final String TASK_VAR_MAX = ", MAX ( CASE V.name WHEN '%s' THEN V.VALUE END )  VAR_%s";

	public static final String WHERE = "WHERE ";
	public static final String AND = " AND ";
	public static final String VAR_PREFIX = "VAR_";
	public static final String CORRELATION_KEY_NAME = "correlationKeyName";
	public static final String ACTUALOWNER_ID = "ACTUALOWNER_ID";
	public static final String PROCESS_INSTANCE_ID = "processinstanceid";
	public static final String PROCESS_ID = "PROCESSID";
	public static final String EQUAL_TO = " = ";
	public static final String SINGLE_QUOTE = "'";
	public static final String TASK_VAR_PREFIX = "t_";
	public static final String PROCESS_VAR_PREFIX = "p_";
	public static final String COMMA = ",";
	public static final String TASK_TYPE = "task";
	public static final String PROCESS_TYPE = "process";
	public static final String TASK_NAME = "TASKNAME";

	public static final String SELECT_PROCESS_VARS = "select " + " v.processinstanceid," + " v.value,"
			+ " v.variableid from variableinstancelog v " + " inner join ( " + " select  max(v.id) myId "
			+ "from variableinstancelog v where v.processinstanceid in ( ";
	public static final String END_PROCESS_VARS = ") group by v.processinstanceid,v.variableid ) resultAlias on v.id = resultAlias.myId";

	public static final String SELECT_TASK_VARS = " select t.taskid,t.value,t.name,task.ACTUALOWNER_ID,pi.PROCESSID,ck.name ckname,task.NAME taskname from taskvariableimpl t "
			+ "	inner join ( " + "			select max(tv.id) myId from taskvariableimpl tv  "
			+ "			where tv.taskid in ( ";
	public static final String END_TASK_VARS = " ) " + "	group by tv.taskid,tv.name "
			+ "	) resultAlias on t.id = resultAlias.myId " + " JOIN Task task ON t.TASKID = task.ID"
			+ " JOIN PROCESSINSTANCEINFO pi ON pi.INSTANCEID = task.PROCESSINSTANCEID"
			+ " LEFT JOIN CORRELATIONKEYINFO ck ON pi.INSTANCEID = ck.PROCESSINSTANCEID";

	public static final String SELECT_TASK_GROUPS = " SELECT distinct" + " taskimpl0_.id AS taskid, "
			+ " organizati2_.ID groupId" + " FROM" + "	Task taskimpl0_"
			+ " INNER JOIN PeopleAssignments_PotOwners potentialo1_ ON" + "	taskimpl0_.id = potentialo1_.task_id"
			+ " INNER JOIN OrganizationalEntity organizati2_ ON" + "	potentialo1_.entity_id = organizati2_.id"
			+ " WHERE" + "	taskimpl0_.archived = 0" + "	AND (taskimpl0_.status IN ('Created' ," + "	'Ready' ,"
			+ "	'Reserved' ," + "	'InProgress' ," + "	'Suspended'))" + "	AND organizati2_.DTYPE = 'Group'"
			+ "	AND taskimpl0_.id IN (";
	public static final String RIGHT_BRACKET = ")";

}
