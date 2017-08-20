package org.dynamicConfig.client.dto;

import java.io.Serializable;

public class CodeConfigDTO implements Serializable{

	private static final long serialVersionUID = 1713397459678191437L;
	
	private String groupId;
	
	private String key;
	
	private String defauleValue;
	
	private String value;
	
	private String ip;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDefauleValue() {
		return defauleValue;
	}

	public void setDefauleValue(String defauleValue) {
		this.defauleValue = defauleValue;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "CodeConfigDTO [groupId=" + groupId + ", key=" + key + ", defauleValue=" + defauleValue + ", value="
				+ value + "]";
	}
}
