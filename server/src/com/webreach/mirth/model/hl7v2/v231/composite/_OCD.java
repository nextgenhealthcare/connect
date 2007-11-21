package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _OCD extends Composite {
	public _OCD(){
		fields = new Class[]{_ID.class, _DT.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Occurrence Code", "Occurrence Date"};
		description = "Occurence";
		name = "OCD";
	}
}
