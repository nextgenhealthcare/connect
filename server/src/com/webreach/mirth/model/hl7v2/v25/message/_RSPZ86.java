package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RSPZ86 extends Message{	
	public _RSPZ86(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _ERR.class, _QAK.class, _QPD.class, _PID.class, _PD1.class, _NTE.class, _AL1.class, _ORC.class, _TQ1.class, _TQ2.class, _RXO.class, _RXR.class, _RXC.class, _RXE.class, _TQ1.class, _TQ2.class, _RXR.class, _RXC.class, _RXD.class, _RXR.class, _RXC.class, _RXG.class, _RXR.class, _RXC.class, _RXA.class, _RXR.class, _RXC.class, _OBX.class, _NTE.class, _DSC.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, -1, 0, -1, -1, 0, 0, -1, -1, -1, 0, -1, -1, 0, -1, -1, 0, -1, -1, 0, -1, 0};
		required = new boolean[]{true, false, true, false, true, true, true, false, false, false, true, true, false, true, true, false, true, true, false, true, false, true, true, false, true, true, false, true, true, false, false, false, false};
		groups = new int[][]{{7, 10, 0, 0}, {12, 13, 0, 1}, {14, 16, 0, 0}, {18, 19, 0, 1}, {17, 21, 0, 0}, {22, 24, 0, 0}, {25, 27, 0, 0}, {28, 30, 0, 0}, {31, 32, 1, 1}, {11, 32, 1, 1}, {7, 32, 1, 1}}; 
		description = "Pharmacy Information Comprehensive (response)";
		name = "RSPZ86";
	}
}
