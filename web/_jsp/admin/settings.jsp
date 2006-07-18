<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Administrative Panel" />
	<jsp:param name="selected" value="admin" />
</jsp:include>

<jsp:include page="/_jsp/admin/tabs.jsp">
	<jsp:param name="selected" value="settings" />
</jsp:include>

<div id="primaryContent">
	<h1>View Settings</h1>

	<div class="breadcrumb">
		Administrative &gt; Settings
	</div>


	<div id="innerContent">


	</div>
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
