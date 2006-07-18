<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Channel Wizard" />
	<jsp:param name="selected" value="basics" />
</jsp:include>

<div id="primaryContent">
	<!-- Main content -->
	<h1>Channel Wizard</h1>
	
	<div class="breadcrumb">
		<a href="/channel/">Channels</a> &gt; <a href="/channel/?state=new">New Channel</a> &gt; Channel Wizard
	</div>
	
	<div id="innerContent">
		
		<div id="wizardNav" class="block">
			<span class="wizardNavItem visited">Basics</span>
			<span class="wizardNavItem visited">Inbound Endpoint</span>
			<span class="wizardNavItem visited">Filters</span>
			<span class="wizardNavItem visited">Transformers</span>
			<span class="wizardNavItem visited">Outbound Endpoint</span>
			<span class="wizardNavItem visited">Preview</span>
		</div>
		
		<form action="#" method="post">
			<!-- Content Here -->
		
		<div class="wizardButtonNav"><input type="submit" name="next" class="button next" value="Next &gt;" /><input type="submit" name="next" class="button next" value="Back to preview" /><input type="submit" name="previous" class="button previous" value="&lt; Previous" /></div>
		
		</form>
	</div><!-- innerContent -->
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
