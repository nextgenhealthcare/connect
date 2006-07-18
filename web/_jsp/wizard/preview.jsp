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
			<jsp:param name="selected" value="Preview" />
			<jsp:param name="lastState" value="${lastState}" />
		</jsp:include>

		<!-- TODO: Populate the preview lists -->
		<div class="section"><a href="/wizard/?state=basics" class="editButtonBig actionButton"><span>Edit basics</span></a>
			<h3>Basics</h3>
			<ul>
				<li>${channel.name}</li>
				<li>HL7 Version: ${param.hl7} </li>
				<c:if test="${param.deployed != null && param.deployed==1}">
					<li>Deployed</li>
				</c:if>
				<li>Direction:	
				<c:choose>
						<c:when test="${(param.outbound) }">Outbound</c:when>
						<c:otherwise>Inbound</c:otherwise>
				</c:choose></li>

			</ul>
		</div>

		<div class="section"><a href="/wizard/?state=inbound" class="editButtonBig actionButton"><span>Edit Inbound endpoint</span></a>
			<h3>Inbound endpoint</h3>
			<ul>
				<li><c:out value="${inboundEndpoint.name}" /></li>
			</ul>

		</div>

		<div class="section"><a href="/wizard/?state=filters" class="editButtonBig actionButton"><span>Edit Filters</span></a>
			<h3>Filters</h3>
			<ul>
				<c:forEach var="filter" items="${selectedFilters}">
					<li><c:out value="${filter.name}" /></li>
				</c:forEach>
			</ul>

		</div>

		<div class="section"><a href="/wizard/?state=transformers" class="editButtonBig actionButton"><span>Edit Transformers</span></a>
			<h3>Transformers</h3>
			<ul>
				<c:forEach var="transformer" items="${selectedTransformers}">
					<li><c:out value="${transformer.name}" /></li>
				</c:forEach>
			</ul>

		</div>

		<div class="section"><a href="/wizard/?state=outbound" class="editButtonBig actionButton"><span>Edit Outbound Endpoint</span></a>
			<h3>Outbound Endpoints</h3>
			<ul>
				<c:forEach var="endpoint" items="${selectedEndpoints}">
					<li><c:out value="${endpoint.name}" /></li>
				</c:forEach>
			</ul>
		</div>

		<form action="/wizard/?state=preview" method="post">
			<div class="wizardButtonNav"><input type="submit" name="next" class="button next" value="Save Channel" /></div>
			<div>
				<input type="hidden" name="op" value="preview" />
				<input type="hidden" name="id" value="${channel.id}" />
			</div>	
		</form>


	</div><!-- innerContent -->
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
