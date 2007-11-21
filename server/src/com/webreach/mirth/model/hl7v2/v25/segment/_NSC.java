package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NSC extends Segment {
	public _NSC(){
		fields = new Class[]{_IS.class, _ST.class, _ST.class, _HD.class, _HD.class, _ST.class, _ST.class, _HD.class, _HD.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Application Change Type", "Current Cpu", "Current Fileserver", "Current Application", "Current Facility", "New Cpu", "New Fileserver", "New Application", "New Facility"};
		description = "Application Status Change";
		name = "NSC";
	}
}
