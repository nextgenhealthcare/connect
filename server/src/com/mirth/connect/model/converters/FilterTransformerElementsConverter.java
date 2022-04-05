package com.mirth.connect.model.converters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import com.mirth.connect.model.FilterTransformerElement;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

import edu.emory.mathcs.backport.java.util.Collections;

public class FilterTransformerElementsConverter extends CollectionConverter {
    private Logger logger = Logger.getLogger(getClass());

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
        
        if (list != null && !list.isEmpty()) {
            Object firstElement = list.get(0);
            
            if (FilterTransformerElement.class.isAssignableFrom(firstElement.getClass())) {
                Collections.sort(list, new Comparator<FilterTransformerElement>() {
                    public int compare(FilterTransformerElement e1, FilterTransformerElement e2) {
                        String sequenceNumber1 = e1.getSequenceNumber();
                        String sequenceNumber2 = e2.getSequenceNumber();
                        
                        // if sequenceNumberN is a child of an iterator step (i.e., "n-n") convert to "n.n" for comparison
                        if (sequenceNumber1.contains("-")) {
                            sequenceNumber1 = sequenceNumber1.replace("-", ".");
                        }
                        if (sequenceNumber2.contains("-")) {
                            sequenceNumber2 = sequenceNumber2.replace("-", ".");
                        }
                        
                        try {
                            Double convertedSequenceNumber1 = Double.parseDouble(sequenceNumber1);
                            Double convertedSequenceNumber2 = Double.parseDouble(sequenceNumber2);
                            Double difference = convertedSequenceNumber1 - convertedSequenceNumber2;
                            
                            // -1 = convertedSequenceNumber1 < convertedSequenceNumber2
                            //  0 = convertedSequenceNumber1 == convertedSequenceNumber2
                            //  1 = convertedSequenceNumber1 > convertedSequenceNumber2
                            if (difference < 0) {
                                return -1;
                            } else if (difference == 0) {
                                return 0;
                            } else {
                                return 1;
                            }
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
