package com.webreach.mirth.model.hl7v2.v24.segment;
import com.webreach.mirth.model.hl7v2.v24.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _CM2 extends Segment {
	public _CM2(){
		fields = new Class[]{_SI.class, _CE.class, _ST.class, _CE.class};
		repeats = new int[]{0, 0, 0, 200};
		required = new boolean[]{false, false, false, false};
		fieldDescriptions = new String[]{"Set ID- CM2", "Scheduled Time Point", "Description of Time Point", "Events Scheduled This Time Point"};
		description = "Clinical Study Schedule Master";
		name = "CM2";
	}
}
