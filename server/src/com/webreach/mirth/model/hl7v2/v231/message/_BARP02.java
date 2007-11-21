package com.webreach.mirth.model.hl7v2.v231.message;
import com.webreach.mirth.model.hl7v2.v231.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BARP02 extends Message{	
	public _BARP02(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PD1.class, _PV1.class, _DB1.class};
		repeats = new int[]{0, 0, 0, 0, 0, -1};
		required = new boolean[]{true, true, true, false, false, false};
		groups = new int[][]{{3, 6, 1, 1}}; 
		description = "Purge Patient Accounts";
		name = "BARP02";
	}
}
