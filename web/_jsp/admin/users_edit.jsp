<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Edit User" />
	<jsp:param name="selected" value="admin" />
</jsp:include>

<jsp:include page="/_jsp/admin/tabs.jsp">
	<jsp:param name="selected" value="users" />
</jsp:include>

<div id="primaryContent">
	<!-- Main content -->
	<h1>Edit User</h1>

	<div class="breadcrumb">
		<a href="/admin/?state=index&amp;sect=users">Users</a> &gt; Edit User
	</div>

	<div id="innerContent">

		<form action="/admin/?state=index&amp;sect=users" method="post">
			<table>

				<tr>
					<td class="fieldLabel"><label for="username">Username</label></td>
					<td><c:out value="${editUser.login}" /></td>
				</tr>
				<tr>
					<td class="fieldLabel"><label for="password">New Password</label></td>
					<td><input type="password" name="password" id="password" size="20" value="" /></td>
				</tr>

				<tr>
					<td class="fieldLabel"><label for="password_check">Re-enter Password</label></td>
					<td><input type="password" name="password_check" id="password_check" size="20" value="" /></td>
				</tr>

				<tr>
					<td class="fieldLabel"><label for="description">Description</label></td>
					<c:set var="userDescription" value="${editUser.description}" />
					<c:if test="${param.description!=null}">
						<c:set var="userDescription" value="${param.description}" />
					</c:if>
					<td><textarea name="description" id="description" rows="6" cols="40"><c:out value="${userDescription}" /></textarea></td>
				</tr>
			</table>

			<div class="buttonNav">					
				<input type="reset" value="reset" class="button" />
				<input type="button" value="delete" class="button deleteButton" />
				<input type="submit" value="submit" class="button" />
			</div>		

			<!-- any extra hidden input -->
			<input type="hidden" name="op" value="edit" />
			<input type="hidden" name="sect" value="users" />
			<input type="hidden" name="id" value="${editUser.id}" />
		</form>


	</div>
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
