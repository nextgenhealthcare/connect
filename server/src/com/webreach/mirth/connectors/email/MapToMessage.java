/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package  com.webreach.mirth.connectors.email;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
*
* @author Administrator
*/
public class MapToMessage {
   
   SmtpConnector connector;
   
   /** Creates a new instance of MapToMessage */
   public MapToMessage() {
   }
   
   public Message mapToMessage(Session session, Map map)
   throws AddressException, MessagingException, UnsupportedEncodingException {
	   return null;
	   /*
       String cc = getField(connector.getCcAddresses(), map);
       String bcc = getField(connector.getBccAddresses(), map);
       String subject = getField(connector.getSubject(), map);
       String defaultSubject = getField(connector.getDefaultSubject(), map);
       String from = getField(connector.getFromAddress(), map);
       String replyTo = getField(connector.getReplyToAddresses(), map);
       String to = getField(connector.getToAddresses(), map);
       String body = getField(connector.getBody(), map);
       String fromName = getField(connector.getFromName(), map);
       
       Address[] ccs = getAddresses(cc);
       Address[] bccs = getAddresses(bcc);
       Address[] tos = getAddresses(to);
       Address[] fromAddr = getAddresses(from);
       Address[] replyToAddr = getAddresses(replyTo);
       
       InternetAddress ia = (InternetAddress)fromAddr[0];
       ia.setPersonal(fromName);
       
       MimeMessage msg = new MimeMessage(session);
       
       if (tos != null) {
           msg.addRecipients(Message.RecipientType.TO, tos);
       }
       if (ccs != null) {
           msg.addRecipients(Message.RecipientType.CC, ccs);
       }
       if (bccs != null) {
           msg.addRecipients(Message.RecipientType.BCC, bccs);
       }
      
       msg.addFrom(fromAddr);
       msg.setReplyTo(replyToAddr);
       
       msg.setText(body);
       
       if (subject != null)
    	   msg.setSubject(subject);
       else
    	   msg.setSubject(defaultSubject);
       
       return msg;
       */
   }
   
   private String getField(String tag, Map map) {
       String result = null;
       if (tag.startsWith("$")) {
           tag = tag.substring(1);
           Object o = map.get(tag);
           if (o != null)
        	   result = (String)map.get(tag);
           else
        	   result = null;
       } else {
           result = tag;
       }
       return result;
   }
   
   private Address[] getAddresses(String address)
   throws AddressException {
       String elements[] = address.split(",");
       List l = new ArrayList(elements.length);
       for(int i = 0; i < elements.length; i++) {
           String s = stripSpaces(elements[i]);
           if (s.length() > 0) {
               l.add(new InternetAddress(stripSpaces(elements[i])));
           }
       }
       Address[] result = null;
       if (l.size() > 0) {
           result = (Address[])l.toArray(new Address[l.size()]);
       }
       return result;
   }
   
   private String stripSpaces(String s) {
       char[] chars = s.toCharArray();
       int begin, end;
       int i;
       String result = null;
       for (i = 0; i < chars.length; i++) {
           if (!Character.isWhitespace(chars[i])) {
               break;
           }
       }
       begin = i;
       for (i = chars.length; i >= 0; i++) {
           if (!Character.isWhitespace(chars[i])) {
               break;
           }
       }
       end = i;
       if (begin > end) {
           result = "";
       } else {
           result = new String(chars, begin, end - begin);
       }
       return result;
   }
}
