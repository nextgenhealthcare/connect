package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _TX_CHALLENGE extends Composite {
	public _TX_CHALLENGE(){
		fields = new Class[]{_TX.class, _TX.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Challenge Information", "Challenge Information"};
		description = "Challenge Information";
		name = "TX_CHALLENGE";
	}
}
