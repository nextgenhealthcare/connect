<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Endpoints" />
	<jsp:param name="selected" value="endpoint" />
</jsp:include>
<div id="primaryContent">
	<!-- Main content -->
	<a href="/endpoint/?state=new&amp;flag=default" class="actionButton" id="endpointNew"><span>New Endpoint</span></a>
	<form action="/endpoint/?state=search" id="listingForm" method="post">
		<div><label>Search: <input name="term" size="20" /><input type="submit" value="GO" class="button" /></label>
		</div>
	</form>

	<h1>All Endpoints</h1>

	<div class="breadcrumb">
		Endpoints &gt; All Endpoints
	</div>

	<div id="innerContent">
			<table cellspacing="0" id="endpointsTable">
			<tr>
				<th>Name</th>
				<th class="actionCol">Edit</th>
				<th class="actionCol">Delete</th>
			</tr>
					
			<!-- data -->
		<c:forEach var="endpoint" items="${endpoints}">
			<c:set var="highlight" value="" />
			<c:if test="${endpoint.id==highlightId}">
				<c:set var="highlight" value="highlight" />
			</c:if>
			<c:choose>
				<c:when test="${fn:contains(usedEndpoints, endpoint.id)}">
					<c:set var="link" value="/endpoint/?state=index" />
					<c:set var="class" value="noDeleteButton" />
				</c:when>
				<c:otherwise>
					<c:set var="link" value="/endpoint/?op=delete&amp;id=${endpoint.id}" />
					<c:set var="class" value="deleteButton" />
				</c:otherwise>	
			</c:choose>		
			<tr class="${highlight}">
				<td><a href="/endpoint/?state=edit&amp;id=${endpoint.id}"><c:out value="${endpoint.name}" /></a></td>
				<td class="actionCol"><a href="/endpoint/?state=edit&amp;id=${endpoint.id}" class="editButton" title="Edit"><span>Edit</span></a></td>
				<td class="actionCol"><a href="${link}" class="${class}" title="Delete"><span>Delete</span></a></td>
			</tr>
		</c:forEach>
		
		<c:if test="${fn:length(endpoints) == 0}">
			<tr><td colspan="3">There are no endpoints.</td></tr>
		</c:if>	
		
		</table>

	</div>

</div><!-- primaryContent -->
<jsp:include page="/_jsp/footer.jsp" />
