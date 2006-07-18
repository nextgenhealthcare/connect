** UNSUPPORTED ** THIS FUNCTIONALITY MAY BE REMOVED IN FUTURE.

This example shows how to use the JAXB RI locator extension.

This extension changes the generated code in the following way:

  - Each generated impl class will now implement "com.sun.xml.bind.Locatable" 
    (contained in jaxb-impl.jar)
  
  - This interface allows you to obtain an "org.xml.sax.Locator" object,
    which indicates the source location where an object is unmarshalled from.

To enable this extension, you have to specify the "-Xlocator" switch on
the command line.
