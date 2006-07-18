<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Channel Messages" />
	<jsp:param name="selected" value="statuspanel" />
</jsp:include>


<div id="primaryContent">

	<!-- Main content -->
	<h1>View Message</h1>

	<div class="breadcrumb">
		<a href="/main/">Status Panel</a> &gt; <a href="/main/?state=messages&name=${name}">Channel Messages</a> &gt; View Message
	</div>

	<div id="innerContent">
		<div class="message">${content}</div>
		<div class="message">${contentxml}</div>
	</div>
	
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
