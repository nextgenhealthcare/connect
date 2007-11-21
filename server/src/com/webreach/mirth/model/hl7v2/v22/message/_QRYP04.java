package com.webreach.mirth.model.hl7v2.v22.message;
import com.webreach.mirth.model.hl7v2.v22.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _QRYP04 extends Message{	
	public _QRYP04(){
		segments = new Class[]{_MSH.class};
		repeats = new int[]{0};
		required = new boolean[]{true};
		groups = new int[][]{}; 
		description = "Generate Bill and A/R Statements";
		name = "QRYP04";
	}
}
