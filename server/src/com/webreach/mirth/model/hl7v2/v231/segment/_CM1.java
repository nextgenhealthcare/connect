package com.webreach.mirth.model.hl7v2.v231.segment;
import com.webreach.mirth.model.hl7v2.v231.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CM1 extends Segment {
	public _CM1(){
		fields = new Class[]{_SI.class, _CE.class, _ST.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Study Phase Identifier", "Description of Study Phase"};
		description = "Clinical Study Phase Master";
		name = "CM1";
	}
}
