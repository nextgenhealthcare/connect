package com.mirth.connect.client.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.mirth.connect.model.ApiProvider;
import com.mirth.connect.model.MetaData;

public class MetaDataUtil {

    public static List<ApiProvider> getApiProviders(MetaData metaData, Version version) {
        List<ApiProvider> list = new ArrayList<ApiProvider>();

        if (CollectionUtils.isNotEmpty(metaData.getApiProviders())) {
            for (ApiProvider provider : metaData.getApiProviders()) {
                boolean valid = true;
                Version minVersion = Version.fromString(provider.getMinVersion());
                Version maxVersion = Version.fromString(provider.getMaxVersion());

                if (minVersion != null && minVersion.ordinal() > version.ordinal()) {
                    valid = false;
                }
                if (maxVersion != null && maxVersion.ordinal() < version.ordinal()) {
                    valid = false;
                }

                if (valid) {
                    list.add(provider);
                }
            }
        }

        return list;
    }
}
