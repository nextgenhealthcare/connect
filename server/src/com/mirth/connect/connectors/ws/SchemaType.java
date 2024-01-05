package com.mirth.connect.connectors.ws;

import java.util.ArrayList;
import java.util.List;

public class SchemaType {
	
	private String name;
	private boolean complex = false;;
	
	// sequence and attributes are only used if complex==true
	private List<SchemaTypeElement> sequence = new ArrayList<>();
	private List<String> attributes = new ArrayList<>();
	
	public SchemaType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public boolean isComplex() {
		return complex;
	}

	public void setComplex(boolean complex) {
		this.complex = complex;
	}

	public List<SchemaTypeElement> getSequence() {
		return sequence;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public class SchemaTypeElement {
		private String name;
		private boolean optional = false;
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public boolean isOptional() {
			return optional;
		}
		
		public void setOptional(boolean optional) {
			this.optional = optional;
		}
		
	}

}
