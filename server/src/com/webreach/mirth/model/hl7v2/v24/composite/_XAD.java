package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _XAD extends Composite {
	public _XAD(){
		fields = new Class[]{_SAD.class, _ST.class, _ST.class, _ST.class, _ST.class, _ID.class, _ID.class, _ST.class, _IS.class, _IS.class, _ID.class, _DR.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Street Address (SAD)", "Other Designation", "City", "State or Province", "Zip or Postal Code", "Country", "Address Type", "Other Geographic Designation", "County/Parish Code", "Census Tract", "Address Representation Code", "Address Validity Range"};
		description = "Extended Address";
		name = "XAD";
	}
}
