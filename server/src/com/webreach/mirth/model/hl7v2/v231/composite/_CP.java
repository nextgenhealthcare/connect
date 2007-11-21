package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _CP extends Composite {
	public _CP(){
		fields = new Class[]{_MO.class, _ID.class, _NM.class, _NM.class, _CE.class, _ID.class};
		repeats = new int[]{0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Price", "Price Type", "From Value", "To Value", "Range Units", "Range Type"};
		description = "Composite Price";
		name = "CP";
	}
}
