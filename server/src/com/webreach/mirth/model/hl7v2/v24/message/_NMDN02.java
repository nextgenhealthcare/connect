package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NMDN02 extends Message{	
	public _NMDN02(){
		segments = new Class[]{_MSH.class, _NCK.class, _NTE.class, _NST.class, _NTE.class, _NSC.class, _NTE.class};
		repeats = new int[]{0, 0, -1, 0, -1, 0, -1};
		required = new boolean[]{true, true, false, true, false, true, false};
		groups = new int[][]{{2, 3, 0, 0}, {4, 5, 0, 0}, {6, 7, 0, 0}, {2, 7, 1, 1}}; 
		description = "Application Management Data Message (unsolicited)";
		name = "NMDN02";
	}
}
