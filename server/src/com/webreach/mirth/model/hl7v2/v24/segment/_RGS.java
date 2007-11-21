package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RGS extends Segment {
	public _RGS(){
		fields = new Class[]{_SI.class, _ID.class, _CE.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Segment Action Code", "Resource Group ID"};
		description = "Resource Group";
		name = "RGS";
	}
}
