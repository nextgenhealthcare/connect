package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ADTA44 extends Message{	
	public _ADTA44(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _MRG.class};
		repeats = new int[]{0, 0, 0, 0, 0};
		required = new boolean[]{true, true, true, false, true};
		groups = new int[][]{{3, 5, 1, 1}}; 
		description = "Move Account Information - Patient Account Number";
		name = "ADTA44";
	}
}
