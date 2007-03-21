/*
 * responseAck.java
 *
 * Created on 14 de noviembre de 2006, 22:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.server.mule.providers.mllp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.webreach.mirth.model.converters.ER7Serializer;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.ByteArrayInputStream;
/**
 *
 * @author ast
 */
public class ResponseAck {
    String ackMessageString="";
    Document ackMessageDocument=null;
    String errorMessage=null;
    boolean responseType=false;
    
    /**
	 * logger used by this class
 */
    protected static transient Log logger = LogFactory.getLog(MllpMessageDispatcher.class);
    
    /** Creates a new instance of responseAck */
    public ResponseAck(String ackMessageString) {
        //for demo purposes, we just declare a literal message string 
        this.ackMessageString=ackMessageString;
        ER7Serializer serializer =new ER7Serializer();
        String xmlAck=null;
        try {        
            xmlAck=serializer.toXML(ackMessageString);            
            logger.debug("ACK: "+xmlAck);            
            this.ackMessageDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xmlAck.getBytes("UTF-8")));            
        } catch (Exception e) {
                errorMessage=" Message is not a valid ACK";
                errorMessage+="\n"+e+"\n"+ackMessageString;
        }          
    }
    public boolean getTypeOfAck(){
        if (ackMessageDocument==null){
            return false;
        }
        try{
            NodeList nl=ackMessageDocument.getElementsByTagName("MSA.1");
            if ((nl==null) || (nl.getLength()==0)){
                errorMessage=" Message is not a valid ACK";
                return false;
            }           
            String msa1=((Element) nl.item(0)).getTextContent();
            String msa3="";
                nl=ackMessageDocument.getElementsByTagName("MSA.3");
            if ((nl!=null)&&(nl.getLength()>0)){
                msa3=((Element) nl.item(0)).getTextContent();
            }
            String errorSegment="";
            nl=ackMessageDocument.getElementsByTagName("ERR");
            if ((nl!=null)&&(nl.getLength()>0)){
                errorSegment=((Element) nl.item(0)).getTextContent();                
            }
            if (errorSegment==null) errorSegment="";
            if ( msa1.equals("AA") || msa1.equals("CA") ){
                responseType=true;
                errorMessage="";
            }else if (msa1.equals("AR") || msa1.equals("CR")){
                responseType=false;
                errorMessage=" [Application Reject]"+"\n"+msa3+"\n"+errorSegment;
            }else if (msa1.equals("AE") || msa1.equals("CE") ){
                responseType=false;
                errorMessage=" [Application Error]"+"\n"+msa3+"\n"+errorSegment;
            }else{
                responseType=false;
                errorMessage="Unknown response type"+"\n"+msa3+"\n"+errorSegment+"\n\n"+ackMessageString;
            }
        }catch(Throwable t){
            errorMessage="Exception reviewing the ACK\n"+t;
            responseType=false;
        }
            return responseType; 
    }
    
    public String getErrorDescription(){
        if (errorMessage==null) getTypeOfAck();
        return errorMessage;
    }
}