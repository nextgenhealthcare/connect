package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCVCV1 extends Message{	
	public _PCVCV1(){
		segments = new Class[]{_MSH.class, _EVN.class, _PCI.class, _PCO.class, _PCW.class, _PCT.class, _PCB.class, _PCL.class, _PCM.class, _PCR.class, _PCH.class, _PCD.class, _PCS.class, _PCC.class, _PCA.class};
		repeats = new int[]{0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
		required = new boolean[]{true, true, true, false, false, false, false, false, false, false, false, false, false, false, false};
		groups = new int[][]{}; 
		description = "Request to Perform Verification ";
		name = "PCVCV1";
	}
}
