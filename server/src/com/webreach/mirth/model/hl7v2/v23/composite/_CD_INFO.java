package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CD_INFO extends Composite {
	public _CD_INFO(){
		fields = new Class[]{_NM.class, _ST.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Channel Number", "Channel Name"};
		description = "Channel Info - Used As Part of CD Composite";
		name = "CD_INFO";
	}
}
