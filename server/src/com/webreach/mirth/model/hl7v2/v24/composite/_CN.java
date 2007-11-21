package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CN extends Composite {
	public _CN(){
		fields = new Class[]{_ST.class, _FN.class, _ST.class, _ST.class, _ST.class, _ST.class, _IS.class, _IS.class, _HD.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"ID Number (ST)", "Family Name", "Given Name", "Second and Further Given Names or Initials Thereof", "Suffix", "Prefix", "Degree", "Source Table", "Assigning Authority"};
		description = "Composite ID Number and Name";
		name = "CN";
	}
}
