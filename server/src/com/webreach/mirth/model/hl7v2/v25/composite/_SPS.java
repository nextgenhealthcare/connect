package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _SPS extends Composite {
	public _SPS(){
		fields = new Class[]{_CWE.class, _CWE.class, _TX.class, _CWE.class, _CWE.class, _CWE.class, _CWE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Specimen Source Name or Code", "Additives", "Specimen Collection Method", "Body Site", "Site Modifier", "Collection Method Modifier Code", "Specimen Role"};
		description = "Specimen Source";
		name = "SPS";
	}
}
