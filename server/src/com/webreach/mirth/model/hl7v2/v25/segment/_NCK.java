package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NCK extends Segment {
	public _NCK(){
		fields = new Class[]{_TS.class};
		repeats = new int[]{0};
		required = new boolean[]{false};
		fieldDescriptions = new String[]{"System Date/Time"};
		description = "System Clock";
		name = "NCK";
	}
}
