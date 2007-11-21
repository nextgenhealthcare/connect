package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _IAM extends Segment {
	public _IAM(){
		fields = new Class[]{_SI.class, _CE.class, _CE.class, _CE.class, _ST.class, _CNE.class, _EI.class, _ST.class, _CE.class, _CE.class, _DT.class, _ST.class, _TS.class, _XPN.class, _CE.class, _CE.class, _CE.class, _XCN.class, _XON.class, _TS.class};
		repeats = new int[]{0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Set ID", "Allergen Type Code", "Allergen Code/Mnemonic/Description", "Allergy Severity Code", "Allergy Reaction Code", "Allergy Action Code", "Allergy Unique Identifier", "Action Reason", "Sensitivity to Causative Agent Code", "Allergen Group Code/Mnemonic/Description", "Onset Date", "Onset Date Text", "Reported Date/Time", "Reported By", "Relationship to Patient Code", "Alert Device Code", "Allergy Clinical Status Code", "Statused by Person", "Statused by Organization", "Statused At Date/Time"};
		description = "Patient Adverse Reaction Information";
		name = "IAM";
	}
}
