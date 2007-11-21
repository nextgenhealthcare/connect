package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFDM04 extends Message{	
	public _MFDM04(){
		segments = new Class[]{_MSH.class, _MFI.class, _MFA.class};
		repeats = new int[]{0, 0, -1};
		required = new boolean[]{true, true, false};
		groups = new int[][]{}; 
		description = "Master Files Delayed Application Acknowlegement";
		name = "MFDM04";
	}
}
