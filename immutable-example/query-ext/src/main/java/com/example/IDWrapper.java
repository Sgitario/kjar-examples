package com.example;

public class IDWrapper {

	public IDWrapper(Object[] sql) {

		this.taskid = Long.parseLong(sql[0].toString());
		this.processinstanceid = Long.parseLong(sql[1].toString());
		this.correlationKeyName = sql[2] == null ? null :  sql[2].toString();
		this.processId = sql[3].toString();
	}

	private Long taskid;
	private Long processinstanceid;
	private String correlationKeyName;
	private String processId;

	public Long getTaskid() {
		return taskid;
	}

	public void setTaskid(Long taskid) {
		this.taskid = taskid;
	}

	public Long getProcessinstanceid() {
		return processinstanceid;
	}

	public void setProcessinstanceid(Long processId) {
		this.processinstanceid = processId;
	}

	@Override
	public String toString() {
		return "IDWrapper [taskid=" + taskid + ", processinstanceid=" + processinstanceid + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((processinstanceid == null) ? 0 : processinstanceid.hashCode());
		result = prime * result + ((taskid == null) ? 0 : taskid.hashCode());
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
		IDWrapper other = (IDWrapper) obj;
		if (processinstanceid == null) {
			if (other.processinstanceid != null)
				return false;
		} else if (!processinstanceid.equals(other.processinstanceid))
			return false;
		if (taskid == null) {
			if (other.taskid != null)
				return false;
		} else if (!taskid.equals(other.taskid))
			return false;
		return true;
	}

	public String getCorrelationKeyName() {
		return correlationKeyName;
	}

	public void setCorrelationKeyName(String correlationKeyName) {
		this.correlationKeyName = correlationKeyName;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

}
