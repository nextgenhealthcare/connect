package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RRAO02 extends Message{	
	public _RRAO02(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _NTE.class, _PID.class, _NTE.class, _ORC.class, _RXA.class, _RXR.class};
		repeats = new int[]{0, 0, 0, -1, 0, -1, 0, -1, 0};
		required = new boolean[]{true, true, false, false, true, false, true, true, true};
		groups = new int[][]{{5, 6, 0, 0}, {8, 9, 0, 0}, {7, 9, 1, 1}, {5, 9, 0, 0}}; 
		description = "Pharmacy Admininstration Acknowledgement";
		name = "RRAO02";
	}
}
