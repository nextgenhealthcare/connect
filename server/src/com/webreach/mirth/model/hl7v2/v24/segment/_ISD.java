package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ISD extends Segment {
	public _ISD(){
		fields = new Class[]{_NM.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Reference Interaction Number (unique identifier)", "Interaction Type Identifier", "Interaction Active State"};
		description = "Interaction Status Detail";
		name = "ISD";
	}
}
