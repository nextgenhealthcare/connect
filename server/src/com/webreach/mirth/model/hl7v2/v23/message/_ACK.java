package com.webreach.mirth.model.hl7v2.v23.message;
import com.webreach.mirth.model.hl7v2.v23.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ACK extends Message{	
	public _ACK(){
		segments = new Class[]{_MSH.class, _MSA.class, _ERR.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{true, true, false};
		groups = new int[][]{}; 
		description = "Acknowledgment";
		name = "ACK";
	}
}
