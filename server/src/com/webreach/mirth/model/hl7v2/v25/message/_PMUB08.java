package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PMUB08 extends Message{	
	public _PMUB08(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _STF.class, _PRA.class, _CER.class};
		repeats = new int[]{0, -1, 0, 0, 0, -1};
		required = new boolean[]{true, false, true, true, false, false};
		groups = new int[][]{}; 
		description = "Revoke Certificate/Permission";
		name = "PMUB08";
	}
}
