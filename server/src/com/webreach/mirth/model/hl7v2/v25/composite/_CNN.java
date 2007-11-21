package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CNN extends Composite {
	public _CNN(){
		fields = new Class[]{_ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _ST.class, _IS.class, _IS.class, _IS.class, _ST.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"ID Number", "Family Name", "Given Name", "Second and Further Given Names or Initials Thereof", "Suffix", "Prefix", "Degree", "Source Table", "Assigning Authority - Namespace ID", "Assigning Authority - Universal ID", "Assigning Authority - Universal ID Type"};
		description = "Composite ID Number and Name Simplified";
		name = "CNN";
	}
}
