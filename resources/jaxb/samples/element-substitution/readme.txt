This sample app illustrates the use of element substitution (via substitution
groups).  The schema, folder.xsd, defines an element named "folder" that can
contain a sequence of "document" elements.  The schema also allows the "document" 
elements to contain interchangeable "header" elements defined by a substitution
group.  In the JAXB client code, you can see logic that iterates over all of
the "document" objects contained in the root "folder" and performs custom 
processing depending on the type of the "header".

Please note:

  o This example DOES NOT have anything to do with W3C XML Schema type substitution.
    Element substitution and type substitution are commonly mistaken features in
    schema - for a complete discussion of the differences, please refer to the
    JAXB interest archive:
    
    https://jaxb.dev.java.net/servlets/ReadMsg?list=users&msgId=12061
    
    Support for type substitution will be available in the next public release of
    the JAXB RI v1.0.2, which will be part of JWSDP v1.3.
    
  o Support for element substitution in the JAXB RI is only available in extension
    mode which is enabled via the xjc commandline switch "-extension" or the xjc
    ant task "extension" attribute.  Please refer to the JAXB RI Release Notes
    for complete information about running xjc:
    
    http://java.sun.com/webservices/docs/1.2/jaxb/xjc.html
    
  o Element substitution is currently supported in JAXB v1.0.1 (JWSDP v1.2), but
    this sample application did not make it into the release.
    
  o JAXB v1.0.1 had a bug that would cause the marshaller to hang if the
    substitution group had minOccurs > 1, but this has been fixed in JAXB v1.0.2
    
    http://developer.java.sun.com/developer/bugParade/bugs/4889239.html