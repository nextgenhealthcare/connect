package com.webreach.mirth.model.hl7v2.v23.segment;
import com.webreach.mirth.model.hl7v2.v23.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NSC extends Segment {
	public _NSC(){
		fields = new Class[]{_IS.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Network Change Type", "Current CPU", "Current Fileserver", "Current Application", "Current Facility", "New CPU", "New Fileserver", "New Application", "New Facility"};
		description = "Status Change";
		name = "NSC";
	}
}
