package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ORUW01 extends Message{	
	public _ORUW01(){
		segments = new Class[]{_MSH.class, _PID.class, _PD1.class, _NTE.class, _PV1.class, _PV2.class, _ORC.class, _OBR.class, _NTE.class, _OBX.class, _NTE.class, _CTI.class, _DSC.class};
		repeats = new int[]{0, 0, 0, -1, 0, 0, 0, 0, -1, 0, -1, -1, 0};
		required = new boolean[]{true, true, false, false, true, false, false, true, false, false, false, false, false};
		groups = new int[][]{{5, 6, 0, 0}, {2, 6, 0, 0}, {10, 11, 1, 1}, {7, 12, 1, 1}, {2, 12, 1, 1}}; 
		description = "Waveform Result, Unsolicited Transmission of Requested Information";
		name = "ORUW01";
	}
}
