<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="pages" value="Basics,Inbound Endpoint,Filters,Transformers,Outbound Endpoint,Preview" />
<c:set var="pagesList" value="${fn:split(pages,',')}" />

<div id="wizardNav" class="block">
	<c:set var="currentItem" value="0" />
	<c:forEach var="pageTitle" items="${pagesList}">
		<c:choose>
			<c:when test="${param.selected == pageTitle}">
				<span class="wizardNavItem current"><c:out value="${pageTitle}" /></span>
			</c:when>
			<c:when test="${currentItem <= param.lastState}">
				<span class="wizardNavItem visited"><c:out value="${pageTitle}" /></span>
			</c:when>
			<c:otherwise>
				<span class="wizardNavItem"><c:out value="${pageTitle}" /></span>
			</c:otherwise>
		</c:choose>
		<c:set var="currentItem" value="${currentItem + 1}" />
	</c:forEach>
</div>

