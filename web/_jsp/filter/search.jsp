<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Filters" />
	<jsp:param name="selected" value="filter" />
</jsp:include>

<div id="primaryContent">
	<!-- Main content -->
	<form action="/filter/?state=search" id="listingForm" method="post">
		<div><label>Search: <input name="term" size="20" /><input type="submit" value="GO" class="button" /></label>
		</div>
	</form>
	
	<h1>Search Results</h1>

	<div class="breadcrumb">
		<a href="/filter/">Filters</a> &gt; Search Results
	</div>

	<div id="innerContent">
		<table cellspacing="0" id="filtersTable">
			<tr>
				<th>Name</th>
				<th class="actionCol">Edit</th>
				<th class="actionCol">Delete</th>
			</tr>

			<!-- data -->
			<c:forEach var="filter" items="${filters}">
				<c:set var="highlight" value="" />
				<c:if test="${filter.id==highlightId}">
					<c:set var="highlight" value="highlight" />
				</c:if>
				<tr class="${highlight}">
					<td><a href="/filter/?state=edit&amp;id=${filter.id}"><c:out value="${filter.name}" /></a></td>
					<td class="actionCol"><a href="/filter/?state=edit&amp;id=${filter.id}" class="editButton" title="Edit"><span>Edit</span></a></td>
					<td class="actionCol"><a href="/filter/?op=delete&amp;id=${filter.id}" class="deleteButton" title="Delete"><span>Delete</span></a></td>
				</tr>
			</c:forEach>
			<c:if test="${fn:length(filters) == 0}">
				<tr><td colspan="3"> There are no filters. </td></tr>
			</c:if>			
		</table>

	</div>
</div><!-- primaryContent -->
<jsp:include page="/_jsp/footer.jsp" />
