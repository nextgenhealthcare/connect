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
			<jsp:param name="selected" value="Outbound Endpoint" />
			<jsp:param name="lastState" value="${lastState}" />
		</jsp:include>

		<form action="/wizard/?state=outbound" method="post">
			<fieldset>
				<legend>Available endpoints</legend>

				<table>
					<tr>
						<td colspan="2">All Endpoints</td>
						<td colspan="2"><label for="outEndpoints">Selected Endpoints</label></td></tr>						
					<tr>
						<td>
							<select name="outEndpoints_dual" id="outEndpoints_dual" multiple="multiple" size="4" style="width: 200px">	
								<c:forEach var="endpoint" items="${unselectedEndpoints}">			    					
									<option value="${endpoint.id}"><c:out value="${endpoint.name}" /></option>
								</c:forEach>	
							</select>
						</td>
						<td class="verticalButtons">
							<input type="button" name="outEndpoints_rightbtn" class="arrowButton" id="rightButton"/><br />	
							<input type="button" name="outEndpoints_leftbtn" class="arrowButton" id="leftButton"/>
						</td>
						<td>
							<select name="outEndpoints" id="outEndpoints" multiple="multiple" size="4" class="dualSelect" style="width: 200px">										    					
								<c:forEach var="endpoint" items="${selectedEndpoints}">			    					
									<option value="${endpoint.id}"><c:out value="${endpoint.name}" /></option>
								</c:forEach>
							</select>
						</td>
						<td class="verticalButtons">
							<input type="button" name="outEndpoints_upbtn"  class="arrowButton" id="upButton" /><br />	
							<input type="button" name="outEndpoints_downbtn" class="arrowButton" id="downButton" />
						</td>
					</tr>
				</table>
			</fieldset>

			<fieldset>
				<legend>Add new endpoint</legend>

				<a href="/endpoint/?state=new&amp;flag=default&amp;orig=%2Fwizard%2F%3Fstate%3Doutbound%26outEndpoint%3D" id="endpointNew" class="actionButton"><span>New Endpoint</span></a>

			</fieldset>

			<div class="wizardButtonNav">
				<input type="submit" name="next" class="button next" value="Next &gt;" />
				<c:if test="${lastState == 5}">
					<input type="submit" name="preview" class="button next" value="Back to preview" />
				</c:if>
				<input type="submit" name="previous" class="button previous" value="&lt; Previous" />
			</div>
			<div>
				<input type="hidden" name="op" value="outbound" />
				<input type="hidden" name="id" value="${channel.id}" />
			</div>		
		</form>
	</div><!-- innerContent -->
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
