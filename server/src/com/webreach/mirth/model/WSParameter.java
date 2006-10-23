package com.webreach.mirth.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wsif.schema.SchemaType;

import com.l2fprod.common.beans.BaseBeanInfo;

public class WSParameter {
	private String name;
	private String type;
	private String value = new String();
	private SchemaType schemaType;
	//Used for array types
	private String length;
	private boolean array;
	private boolean Null;
	private List<WSParameter> subParameters;
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public SchemaType getSchemaType() {
		return schemaType;
	}

	public void setSchemaType(SchemaType schemaType) {
		this.schemaType = schemaType;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}
	public String toString(){
		if (this.getSchemaType() != null)
			return this.getType() + " " + this.getName();
		else 
			return this.getType() + " " + this.getName() + " = " + this.getValue();
	}
	
	public boolean isArray() {
		return array;
	}

	public void setArray(boolean array) {
		this.array = array;
	}

	public boolean isNull() {
		return Null;
	}

	public void setNull(boolean null1) {
		Null = null1;
	}

	public List<WSParameter> getSubParameters() {
		if (subParameters == null){
			subParameters = new ArrayList<WSParameter>();
		}
		return subParameters;
	}

	public void setSubParameters(List<WSParameter> subParameters) {
		this.subParameters = subParameters;
	}

}
