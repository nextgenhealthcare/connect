package com.mirth.connect.model.hl7v2;

public class Composite extends Component{
	protected Class[] fields;
	protected int[] repeats;
	protected boolean[] required;
	protected String[] fieldDescriptions;
	public Component getComponent(int index){
		Class clazz = fields[index];
		try {
			return getCachedComponent(clazz);
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public String getComponentDescription(int index){
		return fieldDescriptions[index];
	}
}
