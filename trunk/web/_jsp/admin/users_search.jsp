<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Administrative Panel" />
	<jsp:param name="selected" value="admin" />
</jsp:include>

<jsp:include page="/_jsp/admin/tabs.jsp">
	<jsp:param name="selected" value="users" />
</jsp:include>

<div id="primaryContent">
	<!-- Main content -->
	<form action="/admin/?state=search&amp;sect=users" id="listingForm" method="post">
		<div>
			<label>Search: <input name="term" size="20" /><input type="submit" value="GO" class="button" /></label>
		</div>
	</form>

	<h1>Search Results</h1>

	<div class="breadcrumb">
		Administrative &gt; <a href="/admin/?state=users">Users</a> &gt; Search Results
	</div>


	<div id="innerContent">

		<c:choose>
			<c:when test="${fn:length(users) > 0}">
				<table cellspacing="0" id="usersTable">    
					<tr>
						<th class="firstCol">Username</th>
						<th class="actionCol">Edit</th>
						<th class="actionCol">Delete</th>
					</tr>
					<!-- data -->
					<c:forEach var="cuser" items="${users}">
						<c:set var="highlight" value="" />
						<c:if test="${cuser.id==highlightId}">
							<c:set var="highlight" value="highlight" />
						</c:if>
						<tr class="${highlight}">
							<td><a href="/admin/?state=edit&amp;sect=users&amp;id=${cuser.id}"><c:out value="${cuser.login}" /></a></td>
							<td class="actionCol"><a href="/admin/?state=edit&amp;sect=users&amp;id=${cuser.id}" class="editButton" title="Edit"><span>Edit</span></a></td>
							<td class="actionCol"><a href="/admin/?state=index&amp;sect=users&amp;op=delete&amp;id=${cuser.id}" class="deleteButton" title="Delete"><span>Delete</span></a></td>
						</tr>
					</c:forEach>
				</table>
			</c:when>
			<c:otherwise>
				<div class="note"><p>There are no users.</p></div>
			</c:otherwise>
		</c:choose>

	</div>
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
