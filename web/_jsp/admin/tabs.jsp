<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="tabs" value="Monitor,Users,Settings,XML" />
<div id="tabNav">
	<ul><%-- for current tab, it can be <li><span>Logs</span></li> or  <li><a href="" class="current">Logs</a></li> if we need a link --%>
		<c:forEach var="tab" items="${fn:split(tabs,',')}">
			<li>
				<c:choose>
					<c:when test="${param.selected==fn:toLowerCase(tab)}">
						<c:set var="tabIsSet" value="true" />
						<span><c:out value="${tab}" /></span>
					</c:when>
					<c:otherwise>
						<a href="?state=${fn:toLowerCase(tab)}"><c:out value="${tab}" /></a>
					</c:otherwise>
				</c:choose>
			</li>
		</c:forEach>
		<c:if test="${!tabIsSet && param.displayExtraTab}">
			<li>
				<span><c:out value="${param.selected}" /></span>
			</li>
		</c:if>

	</ul>
</div>
