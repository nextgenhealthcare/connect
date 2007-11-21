package com.webreach.mirth.model.hl7v2.v21.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _AD extends Composite {
	public _AD(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Street Address", "Other designation", "City", "State or province", "Zip or Postal Code", "Country "};
		description = "Address";
		name = "AD";
	}
}
