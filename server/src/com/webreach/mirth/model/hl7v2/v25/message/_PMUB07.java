package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PMUB07 extends Message{	
	public _PMUB07(){
		segments = new Class[]{_MSH.class, _SFT.class, _EVN.class, _STF.class, _PRA.class, _CER.class, _ROL.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, -1};
		required = new boolean[]{true, false, true, true, false, true, false};
		groups = new int[][]{{6, 7, 0, 1}}; 
		description = "Grant Certificate/Permission";
		name = "PMUB07";
	}
}
