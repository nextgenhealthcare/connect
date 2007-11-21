package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _SAD extends Composite {
	public _SAD(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Street or Mailing Address", "Street Name", "Dwelling Number"};
		description = "Street Address";
		name = "SAD";
	}
}
