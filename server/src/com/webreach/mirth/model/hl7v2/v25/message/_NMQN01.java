package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _NMQN01 extends Message{	
	public _NMQN01(){
		segments = new Class[]{_MSH.class, _SFT.class, _QRD.class, _QRF.class, _NCK.class, _NST.class, _NSC.class};
		repeats = new int[]{0, -1, 0, 0, 0, 0, 0};
		required = new boolean[]{true, false, true, false, false, false, false};
		groups = new int[][]{{3, 4, 0, 0}, {5, 7, 1, 1}}; 
		description = "Application Management Query Message";
		name = "NMQN01";
	}
}
