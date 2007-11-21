package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _AD extends Composite {
	public _AD(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ID.class, _ID.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Street Address", "Other Designation", "City", "State or Province", "Zip or Postal Code", "Country ", "Address Type", "Other Geographic Designation"};
		description = "Address";
		name = "AD";
	}
}
