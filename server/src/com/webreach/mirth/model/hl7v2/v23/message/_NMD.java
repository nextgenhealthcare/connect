package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NMD extends Message{	
	public _NMD(){
		segments = new Class[]{_MSH.class, _NCK.class, _NTE.class, _NST.class, _NTE.class, _NSC.class, _NTE.class};
		repeats = new int[]{0, 0, -1, 0, -1, 0, -1};
		required = new boolean[]{true, true, false, true, false, true, false};
		groups = new int[][]{{2, 3, 0, 0}, {4, 5, 0, 0}, {6, 7, 0, 0}, {2, 7, 1, 1}}; 
		description = "Network Management Data";
		name = "NMD";
	}
}
