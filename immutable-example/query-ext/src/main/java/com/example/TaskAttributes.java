package com.example;

import java.util.HashSet;
import java.util.Set;

public class TaskAttributes {

	private String name;
	private String actualOwner;
	private Long taskId;
	private String processId;
	private String correlationKeyName;
	private Set<String> groups;

	public TaskAttributes(Object[] sql) {
		this.taskId = Long.valueOf(sql[0].toString());
		this.actualOwner = sql[3] == null ? null : sql[3].toString();
		this.name = sql[6].toString();
		this.processId = sql[4].toString();
		this.correlationKeyName = sql[5] == null ? null : sql[5].toString();
		groups = new HashSet<String>();
	}

	public void addGroup(String group) {
		groups.add(group);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getActualOwner() {
		return actualOwner;
	}

	public void setActualOwner(String owner) {
		this.actualOwner = owner;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	@Override
	public String toString() {
		return "TaskAttributes [name=" + name + ", owner=" + actualOwner + ", taskId=" + taskId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((actualOwner == null) ? 0 : actualOwner.hashCode());
		result = prime * result + ((taskId == null) ? 0 : taskId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskAttributes other = (TaskAttributes) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (actualOwner == null) {
			if (other.actualOwner != null)
				return false;
		} else if (!actualOwner.equals(other.actualOwner))
			return false;
		if (taskId == null) {
			if (other.taskId != null)
				return false;
		} else if (!taskId.equals(other.taskId))
			return false;
		return true;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getCorrelationKeyName() {
		return correlationKeyName;
	}

	public void setCorrelationKeyName(String correlationKeyName) {
		this.correlationKeyName = correlationKeyName;
	}

	public Set<String> getGroups() {
		return groups;
	}

}
