/*
 * NewClass.java
 *
 * Created on June 28, 2007, 12:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.components;

import com.webreach.mirth.client.ui.UIConstants;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;

public class MirthTimePicker extends JSpinner
{
    DateFormatter formatter;
    
    public MirthTimePicker()
    {
        init("hh:mm aa", Calendar.MINUTE);
    }
    
    public MirthTimePicker(String format, int accuracy)
    {
        init(format, accuracy);
    }
    
    public void init(String format, int accuracy)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        GregorianCalendar calendar = new GregorianCalendar();
        Date now = calendar.getTime();
        SpinnerDateModel dateModel = new SpinnerDateModel(now, null, null, accuracy);
        getEditor().setFont(UIConstants.TEXTFIELD_PLAIN_FONT);
        setModel(dateModel);
        JFormattedTextField tf = ((JSpinner.DefaultEditor)getEditor()).getTextField();
        DefaultFormatterFactory factory = (DefaultFormatterFactory)tf.getFormatterFactory();
        formatter = (DateFormatter)factory.getDefaultFormatter();
        formatter.setFormat(dateFormat);
        fireStateChanged();
    }
    
    public void setDate(String date)
    {
        try
        {
            this.setValue(formatter.stringToValue(date));
        }
        catch (ParseException e)
        {
        }
    }
    
    public String getDate()
    {
        Date date = (Date) this.getValue();
        String formattedDate = "";
        try
        {
            formattedDate = formatter.valueToString(date);
        }
        catch(ParseException pe)
        {
        }
        return formattedDate;
    }
}
