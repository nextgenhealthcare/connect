package com.mirth.connect.model.converters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mirth.connect.model.FilterTransformerElement;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

public class FilterTransformerElementsConverter extends CollectionConverter {
    private Logger logger = LogManager.getLogger(getClass());

    public FilterTransformerElementsConverter(Mapper mapper) {
        super(mapper);
    }
    
    @Override
    public boolean canConvert(Class type) {
        return type.equals(ArrayList.class);
    }
    
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        List list = (List) super.unmarshal(reader, context);
        
        if (list != null && !list.isEmpty() && list.get(0) != null) {
            Object firstElement = list.get(0);
            
            if (FilterTransformerElement.class.isAssignableFrom(firstElement.getClass())) {
                Collections.sort(list, new Comparator<FilterTransformerElement>() {
                    public int compare(FilterTransformerElement e1, FilterTransformerElement e2) {
                        try {
                            return Integer.parseInt(e1.getSequenceNumber().replaceAll("[\\D]", "")) - Integer.parseInt(e2.getSequenceNumber().replaceAll("[\\D]", ""));
                        } catch (Exception e) {
                            logger.error(e);
                            throw e;
                        }
                    }
                });
            }
        }
        
        return list;
    }
}
