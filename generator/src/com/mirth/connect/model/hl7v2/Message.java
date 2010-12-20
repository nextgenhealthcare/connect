package com.mirth.connect.model.hl7v2;

public class Message extends Component{
	protected Class[] segments;
	protected int[] repeats;
	protected boolean[] required;
	protected int[][] groups; //start index, end index, repeats, required (1 == true, 0 == false)

	public Component getComponent(int index){
		Class clazz = segments[index];
		try {
			return getCachedComponent(clazz);
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public String getComponentDescription(int index){
		return getComponent(index).getDescription();
	}
	
}
