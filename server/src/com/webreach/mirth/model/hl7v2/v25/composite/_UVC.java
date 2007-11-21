package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _UVC extends Composite {
	public _UVC(){
		fields = new Class[]{_CNE.class, _MO.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Value Code", "Value Amount"};
		description = "UB Value Code and Amount";
		name = "UVC";
	}
}
