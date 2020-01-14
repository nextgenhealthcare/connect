package com.cloudsolutions.ssl;

import java.io.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.*;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.w3c.dom.Document;

import sun.security.x509.X509CertImpl;

import javax.security.cert.Certificate;
import javax.security.cert.X509Certificate;
import javax.xml.crypto.dsig.*;


public class SSLDocumentSigner {

	public static void main(String[] args) {
		signClassicOfficeDocuments();
		
		
	}
	
	private static String sign() {

        // First, create a DOM XMLSignatureFactory that will be used to
        // generate the XMLSignature and marshal it to DOM.
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        OutputStream os = null;
        String signedDoc = "C:\\Users\\demo\\Desktop\\signdoc_signed.xml";
        try {
            // Create a Reference to an external URI that will be digested
            // using the SHA1 digest algorithm

            Reference ref = fac.newReference("http://www.w3.org/2000/09/xmldsig#rsa-sha1", fac.newDigestMethod(DigestMethod.SHA1, null));
            // Create the SignedInfo
            SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE_WITH_COMMENTS, (C14NMethodParameterSpec) null), fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                    Collections.singletonList(ref));

            // Create the Document that will hold the resulting XMLSignature --> detached
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true); // must be set
            Document doc = dbf.newDocumentBuilder().newDocument();
            char[] password="456321".toCharArray();
            // certificate info
            File file = new File("C:\\HMG\\IHE\\certificate\\3226.jks");
            // extracting private key and certificate
            String alias = "vida";
            java.security.cert.X509Certificate x509 = null;
            // loading the keystore
            KeyStore keystore = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream(file);
            keystore.load(fis, password);
            fis.close();
            x509 = (java.security.cert.X509Certificate) keystore.getCertificate(alias);
            KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) keystore.getEntry(alias, new KeyStore.PasswordProtection(password));
            String pubkey=getDecodedPublicKey(x509);
            // Create the KeyInfo containing the X509Data.
            // ref : http://www.oracle.com/technetwork/articles/javase/dig-signature-api-140772.html
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            List<Object> x509Content = new ArrayList<Object>();
            x509Content.add(x509.getSubjectX500Principal().getName());
            x509Content.add(x509);
            X509Data xd = kif.newX509Data(x509Content);
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));            
            
            // Create a DOMSignContext and specify the DSA PrivateKey and
            // location of the resulting XMLSignature's parent element
            DOMSignContext dsc = new DOMSignContext(keyEntry.getPrivateKey(), doc);
            dsc.setBaseURI("http://www.w3.org/2000/09/xmldsig#rsa-sha1");
            //** TODO : Check URI
            // Create the XMLSignature (but don't sign it yet)
            XMLSignature signature = fac.newXMLSignature(si, ki);

            // Marshal, generate (and sign) the enveloped signature
            signature.sign(dsc);

            // Output the resulting document.

            os = new FileOutputStream(signedDoc);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            trans.transform(new DOMSource(doc), new StreamResult(os));


        } catch (Exception e) {
            e.printStackTrace();
        }
        return signedDoc;
    }
	
	
	 public static void signClassicOfficeDocuments() {

	        InputStream is;
	        try {
	            //sign document
	            String signaturePath = sign();
	            InputStream signatureAsIS = new FileInputStream(signaturePath);

	            is = new FileInputStream("C://Users//demo//desktop//test.doc");
	            POIFSFileSystem poifs = new POIFSFileSystem(is);
	            DirectoryEntry dirEntry =  poifs.createDirectory("_xmlsignatures"); // create a new DirectoryEntry in the root directory
	            dirEntry.createDocument("9149", signatureAsIS);

	            String destPath = "C://Users//demo//desktop//signed.doc";
	            OutputStream os = new FileOutputStream(destPath);
	            poifs.writeFilesystem(os);

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	    }
	 
	 public static String getDecodedPublicKey(java.security.cert.X509Certificate key) {		 
		 PublicKey publicKey = key.getPublicKey();
		 byte[] encodedPublicKey = publicKey.getEncoded();
		 String b64PublicKey = Base64.getEncoder().encodeToString(encodedPublicKey);

		 try (OutputStreamWriter publicKeyWriter =
		         new OutputStreamWriter(
		                 new FileOutputStream("C:\\users\\demo\\desktop\\pk.txt"),
		                 StandardCharsets.US_ASCII.newEncoder())) {
		     publicKeyWriter.write(b64PublicKey);
		 }catch (Exception e) {
			 e.printStackTrace();
		}
		return b64PublicKey;
		 
	 }

}
