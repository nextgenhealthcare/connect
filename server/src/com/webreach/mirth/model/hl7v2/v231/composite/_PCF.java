package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _PCF extends Composite {
	public _PCF(){
		fields = new Class[]{_IS.class, _ID.class, _TS.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Pre-certification Patient Type", "Pre-certification Required", "Pre-certification Window"};
		description = "Pre-certification Required";
		name = "PCF";
	}
}
