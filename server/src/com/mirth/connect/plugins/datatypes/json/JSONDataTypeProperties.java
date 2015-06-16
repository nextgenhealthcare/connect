package com.mirth.connect.plugins.datatypes.json;

import java.util.HashMap;
import java.util.Map;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.model.datatype.DataTypeProperties;

public class JSONDataTypeProperties extends DataTypeProperties {

    public JSONDataTypeProperties() {
        batchProperties = new JSONBatchProperties();
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {}

    @Override
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public void migrate3_3_0(DonkeyElement element) {}

    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("batchProperties", batchProperties.getPurgedProperties());
        return purgedProperties;
    }
}
