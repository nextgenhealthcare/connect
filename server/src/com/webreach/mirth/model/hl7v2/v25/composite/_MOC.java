package com.webreach.mirth.model.hl7v2.v25.composite;
import com.webreach.mirth.model.hl7v2.*;

public class _MOC extends Composite {
	public _MOC(){
		fields = new Class[]{_MO.class, _CE.class};
		repeats = new int[]{0, 0};
		required = new boolean[]{false, false};
		fieldDescriptions = new String[]{"Monetary Amount", "Charge Code"};
		description = "Money and Code";
		name = "MOC";
	}
}
