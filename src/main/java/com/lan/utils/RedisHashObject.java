package com.lan.utils;

import java.io.Serializable;

public class RedisHashObject implements Serializable{

	private static final long serialVersionUID = 6478533647755905534L;

	private String field;
	
	private Object value;

	public RedisHashObject(String field, Object obj) {
		this.field = field;
		this.value = obj;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
