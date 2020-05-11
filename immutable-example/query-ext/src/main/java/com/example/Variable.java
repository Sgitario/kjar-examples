package com.example;

import java.io.Serializable;

public class Variable implements Serializable {

	private static final long serialVersionUID = 2890059094210175167L;

	private String name;
	private String value;
	private String type;
	private Long parentId;
	private Boolean isAttribute;

	public Variable() {
	}

	public Variable(Object[] sql, String varType) {

		this.type = varType;
		parentId = Long.valueOf(sql[0].toString());
		value = sql[1] == null ? null : sql[1].toString() ;
		name = sql[2] == null ? null : sql[2].toString();

	}

	public String getValue() {

		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	@Override
	public String toString() {
		return "Variable [name=" + name + ", value=" + value + ", type=" + type + ", parentId=" + parentId + "]";
	}

	public Boolean getIsAttribute() {
		return isAttribute;
	}

	public void setIsAttribute(Boolean isAttribute) {
		this.isAttribute = isAttribute;
	}

}
