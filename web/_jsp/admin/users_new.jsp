<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="New User" />
	<jsp:param name="selected" value="admin" />
</jsp:include>

<jsp:include page="/_jsp/admin/tabs.jsp">
	<jsp:param name="selected" value="users" />
</jsp:include>

<div id="primaryContent">
	<!-- Main content -->
	<h1>New User</h1>

	<div class="breadcrumb">
		<a href="/admin/?state=index&amp;sect=users">Users</a> &gt; New User
	</div>

	<div id="innerContent">

		<form action="/admin/?state=index&amp;sect=users" method="post">
			<table>

				<tr>
					<td class="fieldLabel"><label for="username">Username</label></td>
					<td><input name="username" id="username" size="20" value="${fn:escapeXml(param.username)}" /></td>
				</tr>
				<tr>
					<td class="fieldLabel"><label for="password">Password</label></td>
					<td><input type="password" name="password" id="password" size="20" /></td>
				</tr>

				<tr>
					<td class="fieldLabel"><label for="password_check">Re-enter Password</label></td>
					<td><input type="password" name="password_check" id="password_check" size="20" /></td>
				</tr>

				<tr>
					<td class="fieldLabel"><label for="description">Description</label></td>
					<td><textarea name="description" id="description" rows="6" cols="40"></textarea></td>
				</tr>
			</table>

			<div class="buttonNav">					
				<input type="reset" value="reset" class="button" />
				<input type="submit" value="submit" class="button" />
			</div>		

			<!-- any extra hidden input -->
			<div><input type="hidden" name="op" value="new" /></div>
			<div><input type="hidden" name="sect" value="users" /></div>
		</form>


	</div>
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
