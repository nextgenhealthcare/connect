<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Channel Statistics" />
	<jsp:param name="selected" value="statuspanel" />
</jsp:include>


<div id="primaryContent">

	<!-- Main content -->
	<h1>Channel Statistics</h1>

	<div class="breadcrumb">
		<a href="/main/">Status Panel</a> &gt; Channel Statistics
	</div>

	<div id="innerContent">
		<table cellspacing="0" id="statsTable">
			<tr>
				<th>Statistic</th>
				<th>Value</th>
			</tr>
	
			<c:forEach var="stat" items="${listKeys}">
				<tr>
					<td>${stat}</td>
					<td>${statsList[stat]}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
	
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
