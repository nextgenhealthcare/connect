<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Login" />
	<jsp:param name="hideNavbar" value="true" />
	<jsp:param name="stylesheets" value="login.css" />
</jsp:include>

<%-- param pageTitle: default title if none is chosen --%>
<div id="primaryContent">

	<!-- Main content -->
	<h1>Login</h1>
	<div id="innerContent">
		<div id="login">
			<c:set var="nextUri" value="/main/" />
			<c:if test="${fn:trim(param.uri) != ''}">
				<c:set var="nextUri" value="${param.uri}" />
			</c:if>
			<form action="${nextUri}" method="post">
				<input type="hidden" name="uri" value="${param.uri}" />
				<div>
					<label id="usernameLabel">Username:	<input type="text" name="username" id="username" maxlength="40" value="${fn:escapeXml(param.username)}"  /></label>
					<label id="passwordLabel">Password:	<input type="password" name="password" id="password" maxlength="40" value="" /></label>							
					<input type="submit" name="login" id="submit" value="Login" class="button" />
				</div>
				<input type="hidden" name="op" value="login" />
			</form>
		</div>
		Default login is admin/password
	</div>
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
