This example demonstrates how the behavior of the marshalling process 
can be customized.

In this example, an <?xml-stylesheet ... ?> processing instruction is 
inserted into the marshalled document. This is done by using a custom 
XMLFilter and have it receive marshalled XML document, before it's 
actually formatted into text.

This technique is useful when the change you need to make to the 
marshalled document is relatively small. To do a full-blown 
transformation, you should consider XSLT.

Note: This sample application requires that you add xalan.jar to your
classpath.  Please use the version of Xalan released with the version
of JAXP bundled with the JWSDP.