package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CD extends Composite {
	public _CD(){
		fields = new Class[]{_ST.class, _CD_INFO.class, _ST.class, _ST.class, _ST.class, _NM.class, _ST.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Channel Identifier", "Channel Info", "Electrode Names", "Channel Sensitivity/Units", "Calibration Parameters", "Sampling Frequency", "Minimum/Maximum Data Values"};
		description = "Channel Definition";
		name = "CD";
	}
}
