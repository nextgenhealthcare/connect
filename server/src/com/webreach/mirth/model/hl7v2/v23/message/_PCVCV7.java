package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCVCV7 extends Message{	
	public _PCVCV7(){
		segments = new Class[]{_MSH.class, _EVN.class, _PCI.class, _PCO.class, _PCV.class, _PCW.class, _PCV.class, _PCT.class, _PCV.class, _PCB.class, _PCV.class, _PCL.class, _PCV.class, _PCM.class, _PCV.class, _PCR.class, _PCV.class, _PCH.class, _PCV.class, _PCD.class, _PCV.class, _PCS.class, _PCV.class, _PCC.class, _PCV.class, _PCA.class, _PCV.class};
		repeats = new int[]{0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
		required = new boolean[]{true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		groups = new int[][]{}; 
		description = "Learned Provider Information";
		name = "PCVCV7";
	}
}
