package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BPSO29 extends Message{	
	public _BPSO29(){
		segments = new Class[]{_MSH.class, _SFT.class, _NTE.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _ORC.class, _TQ1.class, _TQ2.class, _BPO.class, _NTE.class, _BPX.class, _NTE.class};
		repeats = new int[]{0, -1, -1, 0, 0, -1, 0, 0, 0, 0, -1, 0, -1, 0, -1};
		required = new boolean[]{true, false, false, true, false, false, true, false, true, true, false, true, false, true, false};
		groups = new int[][]{{7, 8, 0, 0}, {4, 8, 0, 0}, {10, 11, 0, 1}, {14, 15, 0, 1}, {9, 15, 1, 1}}; 
		description = "Blood Product Dispense Status";
		name = "BPSO29";
	}
}
