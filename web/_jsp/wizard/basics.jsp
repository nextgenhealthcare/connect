<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Channel Wizard" />
	<jsp:param name="selected" value="channel" />
</jsp:include>

<div id="primaryContent">
	<!-- Main content -->
	<h1>Channel Wizard</h1>
	
	<div class="breadcrumb">
		<a href="/channel/">Channels</a> &gt; <a href="/channel/?state=new">New Channel</a> &gt; Channel Wizard
	</div>
	
	<div id="innerContent">

		<jsp:include page="/_jsp/wizard/navbar.jsp">
			<jsp:param name="selected" value="Basics" />
			<jsp:param name="lastState" value="${lastState}" />
		</jsp:include>

		<form action="/wizard/?state=basics" method="post">
			<table>
				<c:set var="channelName" value="${channel.name}" />
				<c:if test="${param.name != null}">
					<c:set var="channelName" value="${param.name}" />
				</c:if>
				<tr><td class="fieldLabel"><label for="name">Name</label></td><td><input name="name" id="name" size="20" value="${fn:escapeXml(channelName)}" /></td></tr>
				<tr><td class="fieldLabel"><label for="hl7">HL7 Version:</label></td>
					<td>
						<select name="hl7" id="hl7">

							<c:forEach var="hl7" items="${hl7Versions}">
								<c:choose>
									<c:when test="${(param.hl7 != null && hl7==param.hl7)}"> <%-- || hl7==channel.encoding --%>
										<option value="${hl7}" selected="selected">${hl7}</option>
									</c:when>
									<c:otherwise>
										<option value="${hl7}">${hl7}</option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
					<td class="fieldLabel"><label for="deployed">Deployed</label></td>
					<c:choose>
						<c:when test="${(param.deployed != null && param.deployed==1) || channel.enabled }">
							<td><input type="checkbox" name="deployed"  id="deployed"  value="1" checked="checked" /></td>
						</c:when>
						<c:otherwise>
							<td><input type="checkbox" name="deployed"  id="deployed"  value="1" /></td>
						</c:otherwise>
					</c:choose>
				</tr>	
				<tr>
					<td class="fieldLabel"><label for="direction">Direction</label></td>
					<td>
							<select name="direction" id="direction">
								<option value="outbound" <c:if test="${param.outbound}"> selected="selected"</c:if>>Outbound</option>
								<option value="inbound"  <c:if test="${!param.outbound}"> selected="selected"</c:if>>Inbound</option>
							</select>			
					</td>
				</tr>					
			</table>


			<div class="wizardButtonNav">
				<input type="submit" name="next" class="button next" value="Next &gt;" />
				<c:if test="${lastState == 5}">
					<input type="submit" name="preview" class="button next" value="Back to preview" />
				</c:if>
			</div>
			<!-- any extra hidden input -->
			<div>
				<input type="hidden" name="op" value="basics" />
				<input type="hidden" name="id" value="${channel.id}" />
			</div>
		</form>
	</div><!-- innerContent -->
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
