package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _SRT extends Composite {
	public _SRT(){
		fields = new Class[]{_ST.class, _ID.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Sort-by Field", "Sequencing"};
		description = "Sort Order";
		name = "SRT";
	}
}
