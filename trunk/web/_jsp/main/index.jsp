<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Status Panel" />
	<jsp:param name="selected" value="statuspanel" />
</jsp:include>

<%-- param pageTitle: default title if none is chosen --%>
<div id="primaryContent">
	<!-- Main content -->
	<a href="/main/?op=deploy" class="actionButton" id="deployChannels"><span>Deploy Channels</span></a>
	<h1>Status Panel</h1>

	<div id="innerContent">
		<c:set var="isEmpty" value="yes"/>
		
		<table cellspacing="0" id="statusPanelTable">
			<tr>
				<th class="firstCol">Status</th>
				<th>Name</th>
				<th class="statCol">Transformed</th>
				<th class="statCol">Received</th>
				<th class="statCol">Errors</th>				
				<th class="actionCol">Start/Stop</th>
				<th class="actionCol">Stats</th>
				<th class="actionCol">Logs</th>
				<th class="actionCol">Messages</th>	
			</tr>
			
			<c:forEach var="status" items="${statusList}">
				<tr>
					<c:set var="isEmpty" value="no"/>

					<c:choose>
						<c:when test="${status.running}">
							<td><span class="statusRun">Running</span></td>
						</c:when>
						<c:otherwise>
							<td><span class="statusStop">Stopped</span></td>
						</c:otherwise>
					</c:choose>
								
					<td id="channelName">
						${status.displayName}
						<c:if test="${status.changed}">
							<span class="changed">*</span>
						</c:if>
					</td>

					<td class="statCol">${status.sent}</td>
					<td class="statCol">${status.received}</td>
					
					<c:choose>
						<c:when test="${status.error > 0}">
							<td class="errorCol">${status.error}</td>
						</c:when>
						<c:otherwise>
							<td class="statCol">${status.error}</td>
						</c:otherwise>
					</c:choose>

					<c:choose>
						<c:when test="${status.running}">
							<td class="actionCol"><a href="/main/?op=stop&amp;name=${status.name}" class="stopButton" title="Stop"><span>Stop</span></a></td>
						</c:when>
						<c:otherwise>
							<td class="actionCol"><a href="/main/?op=start&amp;name=${status.name}" class="startButton" title="Start"><span>Start</span></a></td>
						</c:otherwise>
					</c:choose>

					<td class="actionCol"><a href="/main/?state=stats&amp;name=${status.name}" class="statsButton" title="View Stats"><span>View Stats</span></a></td>
					<td class="actionCol"><a href="/main/?state=logs&amp;name=${status.name}" class="logsButton" title="View Logs"><span>View Logs</span></a></td>
					<td class="actionCol"><a href="/main/?state=messages&amp;name=${status.name}" class="messagesButton" title="View Messages"><span>View Messages</span></a></td>
				</tr>
			</c:forEach>
			
			<c:if test="${fn:length(statusList) == 0 || isEmpty == 'yes' }">
				<tr><td colspan="9">There are no deployed channels.</td></tr>
			</c:if>
			
		</table>
	</div><!-- innerContent -->			
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
