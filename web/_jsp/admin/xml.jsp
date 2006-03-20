<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="java.io.*" %>
<%
	File mconf = new File( "configuration" + System.getProperty( "file.separator" ) + "mirth-config.xml" );
	BufferedReader br = new BufferedReader( new FileReader( mconf ) );
	String text = "";
	String formatted;
	String line;

	try {
		while( (line = br.readLine()) != null ) {
			text += line + '\n';
		}
	} catch( IOException ioe ) {}

	char curr;
	formatted = "";
	for( int i=0; i<text.length(); i++ ) {
		curr = text.charAt( i );
		if( curr == '<' ) {
			formatted += "&lt;";
		} else if( curr == '>' ) {
			formatted += "&gt;";
		} else if( curr == '\n' ) {
			formatted += "<br />";
		} else if( curr == '\t' ) {
			formatted += "&nbsp;&nbsp;&nbsp;&nbsp;";
		} else if( curr == ' ' ) {
			formatted += "&nbsp;";
		} else {
			formatted += curr;
		}
	}
%>
<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Administrative Panel" />
	<jsp:param name="selected" value="admin" />
</jsp:include>

<jsp:include page="/_jsp/admin/tabs.jsp">
	<jsp:param name="selected" value="xml" />
	<jsp:param name="displayExtraTab" value="true" />
</jsp:include>

<div id="primaryContent">
	<h1>View XML (debugging)</h1>

	<div class="breadcrumb">
		Administrative &gt; View XML
	</div>



	<div id="innerContent">
		<%= formatted %>
	</div>
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
