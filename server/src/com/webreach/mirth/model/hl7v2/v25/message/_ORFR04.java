package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORFR04 extends Message{	
	public _ORFR04(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _QRD.class, _QRF.class, _PID.class, _NTE.class, _ORC.class, _OBR.class, _NTE.class, _TQ1.class, _TQ2.class, _CTD.class, _OBX.class, _NTE.class, _CTI.class, _ERR.class, _QAK.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, -1, 0, 0, -1, 0, -1, 0, 0, -1, -1, -1, 0, 0};
		required = new boolean[]{true, false, true, true, false, true, false, false, true, false, true, false, false, false, false, false, false, false, false};
		groups = new int[][]{{6, 7, 0, 0}, {11, 12, 0, 1}, {14, 15, 1, 1}, {8, 16, 1, 1}, {6, 16, 1, 1}}; 
		description = "Response to Query; Transmission of Requested Observation";
		name = "ORFR04";
	}
}
