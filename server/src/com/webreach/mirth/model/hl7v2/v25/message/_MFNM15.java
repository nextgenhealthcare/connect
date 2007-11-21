package com.webreach.mirth.model.hl7v2.v25.message;
import com.webreach.mirth.model.hl7v2.v25.segment.*;
import com.webreach.mirth.model.hl7v2.*;

public class _MFNM15 extends Message{	
	public _MFNM15(){
		segments = new Class[]{_MSH.class, _SFT.class, _MFI.class, _MFE.class, _IIM.class};
		repeats = new int[]{0, -1, 0, 0, 0};
		required = new boolean[]{true, false, true, true, true};
		groups = new int[][]{{4, 5, 1, 1}}; 
		description = "Inventory Item Master File Notification";
		name = "MFNM15";
	}
}
