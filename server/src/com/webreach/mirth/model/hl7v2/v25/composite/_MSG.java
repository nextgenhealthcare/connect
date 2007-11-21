package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _MSG extends Composite {
	public _MSG(){
		fields = new Class[]{_ID.class, _ID.class, _ID.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Message Code", "Trigger Event", "Message Structure"};
		description = "Message Type";
		name = "MSG";
	}
}
