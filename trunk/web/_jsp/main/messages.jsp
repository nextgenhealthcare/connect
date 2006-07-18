<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Channel Messages" />
	<jsp:param name="selected" value="statuspanel" />
</jsp:include>


<div id="primaryContent">

	<!-- Main content -->
	<h1>Channel Messages</h1>

	<div class="breadcrumb">
		<a href="/main/">Status Panel</a> &gt; Channel Messages
	</div>

	<div id="innerContent">
		<table cellspacing="0" id="messagesTable">
			<tr>
				<th>Index</th>
				<th>Date</th>
				<th>Sending Facility</th>
				<th>Event</th>
				<th>Control ID</th>
				<th>Size (Bytes)</th>
				<th class="actionCol">View</th>
			</tr>
			
			<c:forEach var="message" items="${messages}" varStatus="status">
			
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
					<td>${message.date}</td>
					<td>${message.sendingFacility}</td>
					<td>${message.event}</td>
					<td>${message.controlId}</td>					
					<td>${message.size}</td>
					<td class="actionCol"><a href="/main/?state=message&amp;id=${message.id}&amp;name=${name}" class="messagesButton" title="View Message"><span>View Message</span></a></td>
				</tr>
			</c:forEach>
			
			<c:if test="${fn:length(messages) == 0}">
				<tr><td colspan="7">This channel has no messages.</td></tr>
			</c:if>
		</table>
		
		<div align="right"><a href="/main/?op=clearMessages&name=${name}">Clear Messages</a></div>
	</div>
	
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
