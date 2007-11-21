package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _AL1 extends Segment {
	public _AL1(){
		fields = new Class[]{_SI.class, _CE.class, _CE.class, _CE.class, _ST.class, _DT.class};
		repeats = new int[]{0, 0, 0, 0, -1, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Allergen Type Code", "Allergen Code/Mnemonic/Description", "Allergy Severity Code", "Allergy Reaction Code", "Identification Date"};
		description = "Patient Allergy Information";
		name = "AL1";
	}
}
