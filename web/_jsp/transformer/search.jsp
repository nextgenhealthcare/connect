<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Transformers" />
	<jsp:param name="selected" value="transformer" />
</jsp:include>
<div id="primaryContent">
	<!-- Main content -->

	<form action="/transformer/?state=search" id="listingForm" method="post">
		<div><label>Search: <input name="term" size="20" /><input type="submit" value="GO" class="button" /></label>
		</div>
	</form>
	
	<h1>Search Results</h1>

	<div class="breadcrumb">
		<a href="/transformer/">Transformers</a> &gt; Search Results
	</div>

	<div id="innerContent">
		<table cellspacing="0" id="transformersTable">
			<tr>
				<th>Name</th>
				<th class="actionCol">Edit</th>
				<th class="actionCol">Delete</th>
			</tr>

			<!-- data -->
			<c:forEach var="transformer" items="${transformers}">
				<c:set var="highlight" value="" />
				<c:if test="${transformer.id==highlightId}">
					<c:set var="highlight" value="highlight" />
				</c:if>
				<tr class="${highlight}">
					<td><a href="/transformer/?state=edit&amp;id=${transformer.id}"><c:out value="${transformer.name}" /></a></td>
					<td class="actionCol"><a href="/transformer/?state=edit&amp;id=${transformer.id}" class="editButton" title="Edit"><span>Edit</span></a></td>
					<td class="actionCol"><a href="/transformer/?op=delete&amp;id=${transformer.id}" class="deleteButton" title="Delete"><span>Delete</span></a></td>
				</tr>
			</c:forEach>
			<c:if test="${fn:length(transformers) == 0}">
				<tr><td colspan="3"> There are no transformers. </td></tr>
			</c:if>			
		</table>

	</div>

</div><!-- primaryContent -->
<jsp:include page="/_jsp/footer.jsp" />
