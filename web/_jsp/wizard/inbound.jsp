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
			<jsp:param name="selected" value="Inbound Endpoint" />
			<jsp:param name="lastState" value="${lastState}" />
		</jsp:include>

		<form action="/wizard/?state=inbound" method="post">
			<fieldset>
				<legend>Available endpoints</legend>

				<select name="inEndpoint" id="inEndpoint" size="4" style="width: 200px">
					<c:forEach var="endpoint" items="${endpoints}">			    					
						<c:choose>
							<c:when test="${(param.inEndpoint != null && endpoint.id==param.inEndpoint) || endpoint.id == channel.sourceEndpointId}">
								<option value="${endpoint.id}" selected="selected"><c:out value="${endpoint.name}" /></option>
							</c:when>
							<c:otherwise>
								<option value="${endpoint.id}"><c:out value="${endpoint.name}" /></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>						
				</select>				
			</fieldset>

			<fieldset>
				<legend>Add new endpoint</legend>

				<a href="/endpoint/?state=new&amp;flag=default&amp;orig=%2Fwizard%2F%3Fstate%3Dinbound%26inEndpoint%3D" id="endpointNew" class="actionButton"><span>New Endpoint</span></a>

			</fieldset>

			<div class="wizardButtonNav">
				<input type="submit" name="next" class="button next" value="Next &gt;" />
				<c:if test="${lastState == 5}">
					<input type="submit" name="preview" class="button next" value="Back to preview" />
				</c:if>
				<input type="submit" name="previous" class="button previous" value="&lt; Previous" />
			</div>
			<div>
				<input type="hidden" name="op" value="inbound" />
			</div>
		</form>
	</div><!-- innerContent -->
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
