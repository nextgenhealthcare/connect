package com.webreach.mirth.model.hl7v2.v24.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _XPN extends Composite {
	public _XPN(){
		fields = new Class[]{_FN.class, _ST.class, _ST.class, _ST.class, _ST.class, _IS.class, _ID.class, _ID.class, _CE.class, _DR.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Family Name", "Given Name", "Second and Further Given Names or Initials Thereof", "Suffix", "Prefix", "Degree", "Name Type Code", "Name Representation Code", "Name Context", "Name Validity Range", "Name Assembly Order"};
		description = "Extended Person Name";
		name = "XPN";
	}
}
