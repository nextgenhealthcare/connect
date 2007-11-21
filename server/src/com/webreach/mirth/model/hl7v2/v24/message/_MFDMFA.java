package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFDMFA extends Message{	
	public _MFDMFA(){
		segments = new Class[]{_MSH.class, _MFI.class, _MFA.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{true, true, false};
		groups = new int[][]{}; 
		description = "None";
		name = "MFDMFA";
	}
}
