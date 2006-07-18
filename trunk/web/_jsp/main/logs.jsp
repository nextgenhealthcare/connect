<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Channel Logs" />
	<jsp:param name="selected" value="statuspanel" />
</jsp:include>


<div id="primaryContent">

	<!-- Main content -->
	<h1>Channel Logs</h1>

	<div class="breadcrumb">
		<a href="/main/">Status Panel</a> &gt; Channel Logs
	</div>

	<div id="innerContent">
		<table cellspacing="0" id="messagesTable">
			<tr>
				<th>Index</th>
				<th>Date</th>
				<th>Message</th>
			</tr>
			<c:forEach var="log" items="${logs}" varStatus="status">
			
				<c:choose>
					<c:when test="${status.index % 2 == 0}">
						<c:set var="class" value="rowLight"/>
					</c:when>
					<c:otherwise>
						<c:set var="class" value="rowDark"/>				
					</c:otherwise>
				</c:choose>
			
				<tr class="${class}">
					<td>${status.index + 1}</td>
					<td>${log.date}</td>
					<td>${log.message}</td>
				</tr>
			</c:forEach>
			
			<c:if test="${fn:length(logs) == 0}">
				<tr><td colspan="3">This channel has no log messages.</td></tr>
			</c:if>
		</table>

		<div align="right"><a href="/main/?op=clearLogs&name=${name}">Clear Logs</a></div>
	</div>
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
