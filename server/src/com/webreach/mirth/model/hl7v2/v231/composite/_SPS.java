package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _SPS extends Composite {
	public _SPS(){
		fields = new Class[]{_CE.class, _TX.class, _TX.class, _CE.class, _CE.class, _CE.class, _CE.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Specimen Source Name or Code", "Additives", "Freetext", "Body Site", "Site Modifier", "Collection Modifier Method Code", "Specimen Role"};
		description = "Specimen Source";
		name = "SPS";
	}
}
