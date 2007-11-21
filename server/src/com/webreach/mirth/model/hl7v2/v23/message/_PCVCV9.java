package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCVCV9 extends Message{	
	public _PCVCV9(){
		segments = new Class[]{_MSH.class, _PCT.class, _PCB.class, _PCL.class, _PCM.class, _PCH.class, _PCC.class, _PCA.class};
		repeats = new int[]{0, -1, -1, -1, -1, -1, -1, -1};
		required = new boolean[]{true, false, false, false, false, false, false, false};
		groups = new int[][]{}; 
		description = "Entity Definition Transfer";
		name = "PCVCV9";
	}
}
