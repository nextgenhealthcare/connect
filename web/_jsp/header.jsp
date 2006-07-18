<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
	<head>
		<link rel="shortcut icon" href="/assets/images/favicon.ico" >
		<c:choose>
			<c:when test="${pageTitle != null}">
				<title>Mirth: <c:out value="${pageTitle}" /></title>
			</c:when>
			<c:otherwise>
				<title>Mirth: <c:out value="${param.pageTitle}" /></title>
			</c:otherwise>
		</c:choose>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
		<link href="/assets/styles/main.css" rel="stylesheet" type="text/css" />
		<c:if test="${!empty param.stylesheets}">
			<c:forEach var="file" items="${fn:split(param.stylesheets,' ')}">
				<link href="/assets/styles/${file}" rel="stylesheet" type="text/css" />
			</c:forEach>
		</c:if>
		
		<script type="text/javascript" src="/assets/scripts/lists.js"></script>
		<script type="text/javascript" src="/assets/scripts/libraries.js"></script>
		<script type="text/javascript" src="/assets/scripts/behaviors.js"></script>	
		<script type="text/javascript" src="/assets/scripts/editor.js"></script>			
	</head>
	<body>

		<!-- navigation -->
		<ul id="navigation">
			<li id="logoNav"><span>Mirth</span></li>

			<%-- Display menu if logged in --%>
			<c:if test="${!param.hideNavbar}">
				<c:choose>
					<c:when test="${param.selected=='statuspanel'}">
						<c:set var="statusClass" value="selected" />
					</c:when>
					<c:when test="${param.selected=='channel'}">
						<c:set var="channelClass" value="selected" />
					</c:when>
					<c:when test="${param.selected=='endpoint'}">
						<c:set var="endpointClass" value="selected" />
					</c:when>
					<c:when test="${param.selected=='filter'}">
						<c:set var="filterClass" value="selected" />
					</c:when>
					<c:when test="${param.selected=='transformer'}">
						<c:set var="transformerClass" value="selected" />
					</c:when>
					<c:when test="${param.selected=='admin'}">
						<c:set var="adminClass" value="selected" />
					</c:when>
				</c:choose>

				<li><a id="statusNav" href="/" class="${statusClass}"><span>Status panel</span></a></li>
				<li><a id="channelsNav" href="/channel/" class="${channelClass}"><span>Channels</span></a></li>
				<li><a id="endpointsNav" href="/endpoint/" class="${endpointClass}"><span>Endpoints</span></a></li>
				<li><a id="filtersNav" href="/filter/" class="${filterClass}"><span>Filters</span></a></li>
				<li><a id="transformersNav" href="/transformer/" class="${transformerClass}"><span>Transformers</span></a></li>
				<li><a id="administrativeNav" href="/admin/" class="${adminClass}"><span>Administrative</span></a></li>
				<li><a id="logoutNav" href="/main/?state=login&amp;op=logout"><span>Log out</span></a></li>
			</c:if>
		</ul>
		<c:if test="${!param.hideNavbar}">
			<div id="loginNotice">Logged in as <c:if test="${user==null}">DefaultAdmin</c:if><c:out value="${user.login}" /></div>
		</c:if>
		<div id="container">
			<!-- messages -->
			<c:if test="${fn:length(errorMessages) > 0}">
				<ul class="error">
					<c:forEach var="msg" items="${errorMessages}">
						<li>
							<c:forEach var="line" items="${fn:split(msg, newline)}">
								<c:out value="${fn:replace(fn:escapeXml(line), '  ', '&nbsp;&nbsp;')}" escapeXml="false" /><br />
							</c:forEach>
						</li>
					</c:forEach>
				</ul>
			</c:if>
			<c:if test="${fn:length(infoMessages) > 0}">
				<ul class="success">
					<c:forEach var="msg" items="${infoMessages}">
						<li>
							<c:forEach var="line" items="${fn:split(msg, newline)}">
								<c:out value="${fn:replace(fn:escapeXml(line), '  ', '&nbsp;&nbsp;')}" escapeXml="false" /><br />
							</c:forEach>
						</li>
					</c:forEach>
				</ul>
			</c:if>
