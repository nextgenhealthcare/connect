package com.webreach.mirth.model.hl7v2.v21.message;
import com.webreach.mirth.model.hl7v2.v21.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BARP01 extends Message{	
	public _BARP01(){
		segments = new Class[]{_MSH.class, _EVN.class, _PID.class, _PV1.class, _DG1.class, _PR1.class, _GT1.class, _NK1.class, _IN1.class, _ACC.class, _UB1.class};
		repeats = new int[]{0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0};
		required = new boolean[]{true, true, true, false, false, false, false, false, false, false, false};
		groups = new int[][]{{4, 11, 1, 1}}; 
		description = "Add and Update Patient Account";
		name = "BARP01";
	}
}
