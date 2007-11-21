package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _LA2 extends Composite {
	public _LA2(){
		fields = new Class[]{_IS.class, _IS.class, _IS.class, _HD.class, _IS.class, _IS.class, _IS.class, _IS.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ID.class, _ID.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Point of Care (IS)", "Room", "Bed", "Facility (HD)", "Location Status", "Person Location Type", "Building", "Floor", "Street Address (ST)", "Other Designation", "City", "State or Province", "Zip or Postal Code", "Country", "Address Type", "Other Geographic Designation"};
		description = "Location with Address Information (variant 2)";
		name = "LA2";
	}
}
