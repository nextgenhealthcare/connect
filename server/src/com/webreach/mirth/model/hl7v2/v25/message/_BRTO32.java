package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _BRTO32 extends Message{	
	public _BRTO32(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _SFT.class, _NTE.class, _PID.class, _ORC.class, _TQ1.class, _TQ2.class, _BPO.class, _BTX.class};
		repeats = new int[]{0, 0, -1, -1, -1, 0, 0, 0, -1, 0, -1};
		required = new boolean[]{true, true, false, false, false, false, true, true, false, false, false};
		groups = new int[][]{{8, 9, 0, 1}, {7, 11, 0, 1}, {6, 11, 0, 0}}; 
		description = "Blood Product Transfusion/Disposition Acknowledgment";
		name = "BRTO32";
	}
}
