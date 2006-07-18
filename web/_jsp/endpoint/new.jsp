<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="New Endpoint" />
	<jsp:param name="selected" value="endpoint" />
</jsp:include>

<div id="primaryContent">

	<!-- Main content -->
	<h1>New Endpoint</h1>
	<div class="breadcrumb">
		<c:choose>
			<c:when test="${!empty param.orig && fn:contains(param.orig, 'inbound')}">
				<a href="/channel/">Channels</a> &gt; <a href="/channel/?state=new">New Channel</a> &gt; <a href="/wizard/?state=inbound">Channel Wizard</a> &gt; New Endpoint
			</c:when>
			<c:when test="${!empty param.orig && fn:contains(param.orig, 'outbound')}">
				<a href="/channel/">Channels</a> &gt; <a href="/channel/?state=new">New Channel</a> &gt; <a href="/wizard/?state=outbound">Channel Wizard</a> &gt; New Endpoint
			</c:when>
			<c:otherwise>
				<a href="/endpoint/">Endpoints</a> &gt; New Endpoint
			</c:otherwise>
		</c:choose>
	</div>

	<div id="innerContent">

		<form action="/endpoint/" method="post">
			<table id="endpointForm">		
				<tr>
					<td class="fieldLabel"><label for="name">Name</label></td>
					<td><input name="name" id="name" size="20" value="${fn:escapeXml(param.name)}" /></td>
				</tr>
				<tr>
					<td class="fieldLabel"><label for="description">Description</label></td>
					<td><textarea name="description" id="description" rows="6" cols="40"><c:out value="${fn:escapeXml(param.description)}" /></textarea></td>
				</tr>
				<tr>
					<td class="fieldLabel"><label for="type">Type</label></td>
					<td>
						<select name="type" id="type" class="showOptions">					
							<c:forEach var="curtype" items="${types}">
								<c:choose>
									<c:when test="${fn:escapeXml(param.type == curtype.name)}">
										<option selected="selected" value="${curtype.name}">${curtype.displayName}</option>
									</c:when>
									<c:otherwise>
										<option  value="${curtype.name}">${curtype.displayName}</option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>
					</td>
				</tr>
			</table>
			
			<c:if test="${fn:escapeXml(param.flag == 'set')}">
				<c:set var="endpoint" value="nonnull" />
				<c:set var="endpointType" value="${param.type}" />
			</c:if>
			
			<%@ include file="form.jsp" %>			

			<div class="buttonNav">					
				<input type="reset" value="reset" class="button" />
				<input type="submit" name="submit" value="submit" class="button"  />
			</div>		

			<!-- any extra hidden input -->
			<div>
				<input type="hidden" name="flag" value="set" />
				<input type="hidden" name="op" value="new" />
				<c:if test="${param.orig!=null}">
					<input type="hidden" name="orig" value="${fn:escapeXml(param.orig)}" />
				</c:if>
			</div>
		</form>


	</div>
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
