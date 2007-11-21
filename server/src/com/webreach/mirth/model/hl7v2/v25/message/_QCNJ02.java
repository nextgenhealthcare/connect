package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QCNJ02 extends Message{	
	public _QCNJ02(){
		segments = new Class[]{_MSH.class, _SFT.class, _QID.class};
		repeats = new int[]{0, -1, 0};
		required = new boolean[]{true, false, true};
		groups = new int[][]{}; 
		description = "Cancel Subscription/Acknowledge Message";
		name = "QCNJ02";
	}
}
