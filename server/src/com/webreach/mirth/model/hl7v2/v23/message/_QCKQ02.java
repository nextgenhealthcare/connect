package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QCKQ02 extends Message{	
	public _QCKQ02(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class, _QAK.class};
		repeats = new int[]{0, 0, 0, 0};
		required = new boolean[]{true, true, false, false};
		groups = new int[][]{}; 
		description = "Query For Deferred Response -  Acknowledgement (Response B to A)";
		name = "QCKQ02";
	}
}
