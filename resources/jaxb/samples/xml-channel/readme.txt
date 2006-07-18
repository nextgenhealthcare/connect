This example demonstrates how one can use one communication channel
(such as a socket) to send multiple XML messages, and how it can be
combined with JAXB.

XML1.0 requires a conforming parser to read the entire data till end
of the stream (because a parser needs to handle documents like
<root/><!-- post root comment -->). As a result, a naive attempt to
keep one OutputStream open and marshal objects multiple times fails.

In this example, two classes (OutputStreamMultiplexer and 
InputStreamDemultiplexer) are used to split one stream into multiple
sub-streams, and each sub-stream will carry one XML message.

The client application uses JAXB to build an XML message, and send it
to the socket. The server application then receives it, unmarshal it,
and just print the contents.

This example can help you if you want to set up a persistent connection
that transports XML. The downside of this multiplexing technique is that
the actual data on the wire is not XML because it includes some
out-of-band information.
