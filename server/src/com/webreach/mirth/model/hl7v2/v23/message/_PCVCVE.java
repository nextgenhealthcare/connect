package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PCVCVE extends Message{	
	public _PCVCVE(){
		segments = new Class[]{_MSH.class, _EVN.class, _PCI.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{true, true, true};
		groups = new int[][]{}; 
		description = "Status Point Notification";
		name = "PCVCVE";
	}
}
