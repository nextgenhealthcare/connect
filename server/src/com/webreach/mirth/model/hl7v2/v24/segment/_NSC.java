package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NSC extends Segment {
	public _NSC(){
		fields = new Class[]{_IS.class, _ST.class, _ST.class, _HD.class, _HD.class, _ST.class, _ST.class, _HD.class, _HD.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Application Change Type", "Current CPU", "Current Fileserver", "Current Application", "Current Facility", "New CPU", "New Fileserver", "New Application", "New Facility"};
		description = "Application status change";
		name = "NSC";
	}
}
