<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Administrative Panel" />
	<jsp:param name="selected" value="admin" />
</jsp:include>

<jsp:include page="/_jsp/admin/tabs.jsp">
	<jsp:param name="selected" value="monitor" />
</jsp:include>

<div id="primaryContent">
	<h1>Monitor</h1>

	<div class="breadcrumb">
		Administrative &gt; Monitor
	</div>

	<div id="innerContent">

		<div style="width: 500px; border: 1px solid #aaa; background: #fafaf0; text-align: center; padding: 200px 5px; color: #777">
			
		</div>

	</div>
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
