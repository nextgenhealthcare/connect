package com.webreach.mirth.model.hl7v2.v22.message;
import com.webreach.mirth.model.hl7v2.v22.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BARP02 extends Message{	
	public _BARP02(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PV1.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, true, false};
		groups = new int[][]{{3, 4, 1, 1}}; 
		description = "Purge Patient Account";
		name = "BARP02";
	}
}
