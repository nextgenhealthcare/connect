package com.webreach.mirth.model.hl7v2.v25.segment;
import com.webreach.mirth.model.hl7v2.v25.composite.*;
import com.webreach.mirth.model.hl7v2.*;

public class _PDC extends Segment {
	public _PDC(){
		fields = new Class[]{_XON.class, _CE.class, _ST.class, _ST.class, _CE.class, _ST.class, _ST.class, _ST.class, _CE.class, _ID.class, _ST.class, _CQ.class, _CQ.class, _TS.class, _TS.class};
		repeats = new int[]{-1, 0, 0, 0, 0, -1, 0, -1, 0, 0, 0, 0, 0, 0, 0};
		required = new boolean[]{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false};
		fieldDescriptions = new String[]{"Manufacturer/Distributor", "Country", "Brand Name", "Device Family Name", "Generic Name", "Model Identifier", "Catalogue Identifier", "Other Identifier", "Product Code", "Marketing Basis", "Marketing Approval ID", "Labeled Shelf Life", "Expected Shelf Life", "Date First Marketed", "Date Last Marketed"};
		description = "Product Detail Country";
		name = "PDC";
	}
}
