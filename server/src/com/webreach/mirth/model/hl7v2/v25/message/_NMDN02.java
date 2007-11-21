package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NMDN02 extends Message{	
	public _NMDN02(){
		segments = new Class[]{_MSH.class, _SFT.class, _NCK.class, _NTE.class, _NST.class, _NTE.class, _NSC.class, _NTE.class};
		repeats = new int[]{0, -1, 0, -1, 0, -1, 0, -1};
		required = new boolean[]{true, false, true, false, true, false, true, false};
		groups = new int[][]{{3, 4, 0, 0}, {5, 6, 0, 0}, {7, 8, 0, 0}, {3, 8, 1, 1}}; 
		description = "Application Management Data Message (Unsolicited)";
		name = "NMDN02";
	}
}
