package com.webreach.mirth.model.hl7v2.v231.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _MOC extends Composite {
	public _MOC(){
		fields = new Class[]{_MO.class, _CE.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Dollar Amount", "Charge Code"};
		description = "Charge to Practise";
		name = "MOC";
	}
}
