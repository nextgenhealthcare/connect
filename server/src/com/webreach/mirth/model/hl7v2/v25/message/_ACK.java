package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _ACK extends Message{	
	public _ACK(){
		segments = new Class[]{_MSH.class, _SFT.class, _MSA.class, _ERR.class};
		repeats = new int[]{0, -1, 0, -1};
		required = new boolean[]{true, false, true, false};
		groups = new int[][]{}; 
		description = "Acknowledgement";
		name = "ACK";
	}
}
