package com.webreach.mirth.model.hl7v2.v24.message;
import com.webreach.mirth.model.hl7v2.v24.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _RQQQ09 extends Message{	
	public _RQQQ09(){
		segments = new Class[]{_MSH.class, _ERQ.class, _DSC.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{true, true, false};
		groups = new int[][]{}; 
		description = "Event Replay Query";
		name = "RQQQ09";
	}
}
