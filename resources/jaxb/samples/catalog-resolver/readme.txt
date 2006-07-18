Schemas, especially those which are developed by a standard body, often
contain references to external resources, such as
"http://www.w3.org/2001/xml.xsd" or "XMLSchema.dtd".

To compile those schemas by XJC, you have to either modify a schema and 
fix the references, or you have to make sure that the network connection
is available when you compile it. But both approaches are problematic.

XJC has the "-catalog" option which turns on the use of catalog-based
entity resolver. This will allow you to essentially "redirect" references
to your local copies of resources without touching the schema files.

Try this example without the internet connection to make sure
that the redirection is indeed happening.

A brief description of the catalog file format can be found in
"catalog.cat" itself. For more detailed discussion about the entity
resolution and the format of the catalog files, see 
http://wwws.sun.com/software/xml/developers/resolver/article/


DEBUG TIPS
==========

If you suspect that the catlog resolution is not happening
(for example, you see errors like UnknownHostException), then 
use the -debug option of XJC.