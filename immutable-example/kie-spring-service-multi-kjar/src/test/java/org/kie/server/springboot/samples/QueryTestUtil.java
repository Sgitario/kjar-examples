package org.kie.server.springboot.samples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.client.ProcessServicesClient;

import com.example.Attribute;

public class QueryTestUtil {

	// Process and Task
	public static final String KEY_ASSIGNEE = "assigneeIdentifier";
	public static final String KEY_REVIEWER = "assigneeReviewer";
	public static final String KEY_GROUP = "groupOwnership";
	public static final String KEY_PROCESS_ASSIGNEE = "processAssigneeIdentifier";
	public static final String VAL_EMAIL = "@db.com";
	public static final String GROUP_ROLES = "REGISTRATION_ANALYST,REGISTRATION_MANAGER,REGIONAL_HEAD_OF_EC,SUPPORT_ANALYST";
	public static final String TASK_NAME = "Case Create Task In Progress";
	public static final String PROCESS_ID = "bpm-commons-test";
	public Map<String, Object> createPayload(int i) {
		String user = "USER_" + i;
		Map<String, Object> payload = new HashMap<>();
		payload.put(KEY_ASSIGNEE, user);
		payload.put(KEY_REVIEWER, "REVIEWER_" + i);
		payload.put(KEY_GROUP, GROUP_ROLES);
		payload.put(KEY_PROCESS_ASSIGNEE, user + VAL_EMAIL);
		payload.put(Attribute.TASK_NAME.name(), TASK_NAME);
		payload.put(Attribute.PROCESS_ID.name(), PROCESS_ID);

		return payload;
	}

	public List<Long> startProcess(int amount, String containerId, String processId,
			ProcessServicesClient processClient, Map<String, Object> params) {
		List<Long> pids = new ArrayList<Long>();
		for (int i = 0; i < amount; i++) {

			Long processInstanceId = processClient.startProcess(containerId, processId, params);
			pids.add(processInstanceId);

		}

		return pids;
	}

}
