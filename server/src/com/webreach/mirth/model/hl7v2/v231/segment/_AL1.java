package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _AL1 extends Segment {
	public _AL1(){
		fields = new Class[]{_SI.class, _IS.class, _CE.class, _IS.class, _ST.class, _DT.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Allergy Type", "Allergy Code/Mnemonic/Description", "Allergy Severity", "Allergy Reaction", "Identification Date"};
		description = "Patient Allergy Information";
		name = "AL1";
	}
}
