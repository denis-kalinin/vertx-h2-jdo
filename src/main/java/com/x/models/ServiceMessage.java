package com.x.models;

import java.io.Serializable;

public class ServiceMessage implements Serializable{
	
	private static final long serialVersionUID = 6841546448203376080L;
	private String defaultMessage;
	private String code;
	/**
	 * 
	 * @param code i18n code for message text
	 * @param defaultMessage default text
	 */
	public ServiceMessage(String code, String defaultMessage){
		this.code = code;
		this.defaultMessage = defaultMessage;
	}

	public String getDefaultMessage() {
		return defaultMessage;
	}

	public void setDefaultMessage(String defaultMessage) {
		this.defaultMessage = defaultMessage;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
