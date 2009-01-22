package com.thoughtworks.xstream.converters.collections;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class DateConverter implements Converter {

	private String pattern;

	public DateConverter(String pattern) {
		super();
		this.pattern = pattern;
	}

	public boolean canConvert(Class clazz) {
		return Date.class.isAssignableFrom(clazz);
	}

	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
		Date date = (Date) value;
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		writer.setValue(formatter.format(date));
	}

	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		Date date = new Date();
		
		 try {
             date = formatter.parse(reader.getValue());
	     } catch (ParseException e) {
             throw new ConversionException(e.getMessage(), e);
	     }
		
		return date;
	}

}