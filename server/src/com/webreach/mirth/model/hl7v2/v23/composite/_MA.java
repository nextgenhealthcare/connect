package com.webreach.mirth.model.hl7v2.v23.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _MA extends Composite {
	public _MA(){
		fields = new Class[]{_NM.class, _NM.class, _NM.class};
		repeats = new int[]{0, 0, 0};
		required = new boolean[]{false, false, false};
		fieldDescriptions = new String[]{"Channel 1 Sample", "Channel 2 Sample", "Channel 3 Sample"};
		description = "Multi-Plexed Array";
		name = "MA";
	}
}
