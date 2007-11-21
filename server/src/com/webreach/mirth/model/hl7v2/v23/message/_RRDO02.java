package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RRDO02 extends Message{	
	public _RRDO02(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _NTE.class, _PID.class, _NTE.class, _ORC.class, _RXD.class, _RXR.class, _RXC.class};
		repeats = new int[]{0, 0, 0, -1, 0, -1, 0, 0, -1, -1};
		required = new boolean[]{true, true, false, false, true, false, true, true, true, false};
		groups = new int[][]{{5, 6, 0, 0}, {8, 10, 0, 0}, {7, 10, 1, 1}, {5, 10, 0, 0}}; 
		description = "Pharmacy Dispense Acknowledgement";
		name = "RRDO02";
	}
}
