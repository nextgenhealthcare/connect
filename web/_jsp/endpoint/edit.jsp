<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Edit Endpoint" />
	<jsp:param name="selected" value="endpoint" />
</jsp:include>
<div id="primaryContent">
	<!-- Main content -->

	<h1>Edit Endpoint</h1>
	<div class="breadcrumb">
		<a href="/endpoint/">Endpoints</a> &gt; Edit Endpoint
	</div>

	<div id="innerContent">

		<form action="/endpoint/" method="post">
			<table>
				<c:set var="name" value="${endpoint.name}" />
				<c:if test="${param.name != null}">
					<c:set var="name" value="${param.name}" />
				</c:if>
				<tr><td class="fieldLabel"><label for="name">Name</label></td><td><input name="name" id="name" size="20" value="${fn:escapeXml(name)}" /></td></tr>
				<tr>
					<td class="fieldLabel"><label for="description">Description</label></td>
					<td>
						<c:set var="description" value="${endpoint.description}" />
						<c:if test="${param.description != null}">
							<c:set var="description" value="${param.description}" />
						</c:if>
						<textarea name="description" id="description" rows="6" cols="40"><c:out value="${description}" /></textarea>
					</td>
				</tr>
				<tr>
					<td class="fieldLabel"><label for="type">Type</label></td>
					<td>
						<select name="type" id="type" class="showOptions">
							<c:forEach var="type" items="${types}">
								<c:choose>
									<c:when test="${fn:escapeXml(param.type==type.name)}">
										<option selected value="${type.name}">${type.displayName}</option>
									</c:when>
									<c:otherwise>
										<c:choose>
											<c:when test="${fn:escapeXml(endpoint.type == type.name && param.flag != 'set')}">
												<option selected value="${type.name}">${type.displayName}</option>
											</c:when>
											<c:otherwise>
												<option  value="${type.name}">${type.displayName}</option>
											</c:otherwise>
										</c:choose>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>
					</td>
				</tr>
			</table>
			<c:choose>
				<c:when test="${fn:escapeXml(param.flag=='set')}">
					<c:set var="endpointType" value="${param.type}" />
				</c:when>
				<c:otherwise>
					<c:set var="endpointType" value="${endpoint.type}" />
				</c:otherwise>
			</c:choose>
			
			<%@ include file="form.jsp" %>
			
			<div class="buttonNav">					
				<input type="reset" value="reset" class="button" />
				<input type="button" value="delete" class="button deleteButton" />
				<input type="submit" value="submit" class="button"  />
			</div>	
			<!-- any extra hidden input -->
			<div>
				<input type="hidden" name="flag" value="set" />			
				<input type="hidden" name="op" value="edit" />
				<input type="hidden" name="id" value="${endpoint.id}" />
			</div>
		</form>


	</div>
</div><!-- primaryContent -->
<jsp:include page="/_jsp/footer.jsp" />
