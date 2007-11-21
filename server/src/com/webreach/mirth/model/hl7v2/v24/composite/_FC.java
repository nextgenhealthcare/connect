package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _FC extends Composite {
	public _FC(){
		fields = new Class[]{_IS.class, _TS.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Financial Class", "Effective Date (TS)"};
		description = "Financial Class";
		name = "FC";
	}
}
