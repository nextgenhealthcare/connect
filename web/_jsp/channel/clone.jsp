<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Clone Channel" />
	<jsp:param name="selected" value="channel" />
</jsp:include>

<div id="primaryContent">
	<!-- Main content -->

	<h1>Clone Channel</h1>

	<div class="breadcrumb">
		<a href="/channel/">Channels</a> &gt; Clone Channel
	</div>

	<div id="innerContent">

		<form action="/channel/" method="post">
			<table>

				<tr><td class="fieldLabel"><label for="name">Name</label></td><td><input name="name" id="name" size="20" value="Copy of ${channel.name}" /></td></tr>
				<tr>
					<td class="fieldLabel"><label for="description">Description</label></td>
					<td><textarea name="description" id="description" rows="6" cols="40">${channel.description}</textarea></td>
				</tr>				
				<tr><td class="fieldLabel"><label for="hl7">HL7 Version</label></td>
					<td>
						<select name="hl7" id="hl7">	
							<c:forEach var="hl7" items="${hl7Versions}">
								<c:choose>
									<c:when test="${param.hl7 != null && hl7==param.hl7}">
										<option value="${hl7}" selected="selected">${hl7}</option>
									</c:when>
									<c:when test="${param.hl7 == null && hl7==channel.encoding}">
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
					<td class="fieldLabel"><label for="deployed">Deploy to Status Panel?</label></td>
					<td>
						<c:choose>
							<c:when test="${(param.enabled==null && channel.enabled) || (param.enabled!=null && param.enabled=='1')}">
								<input type="checkbox" name="deployed"  id="deployed"  value="1" checked="checked"  />
							</c:when>
							<c:otherwise>
								<input type="checkbox" name="deployed"  id="deployed"  value="1" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>	
				<tr><td class="fieldLabel"><label for="direction">Direction</label></td>
					<td>
						<select name="direction" id="direction">
							<c:choose>
								<c:when test="${channel.outbound}">								
									<option value="outbound" selected="selected">Outbound</option>
									<option value="inbound">Inbound</option>
								</c:when>
								<c:otherwise>
									<option value="outbound">Outbound</option>
									<option value="inbound" selected="selected">Inbound</option>								
								</c:otherwise>
							</c:choose>
						</select>
					</td>
				</tr>					
				<tr><td class="fieldLabel"><label for="inEndpoint">Inbound Endpoint</label></td>
					<td>
						<select name="inEndpoint" id="inEndpoint">	
							<c:forEach var="endpoint" items="${endpoints}">			
								<c:choose>
									<c:when test="${channel.sourceEndpointId == endpoint.id}">
										<option selected="selected" value="${endpoint.id}"><c:out value="${endpoint.name}" /></option>
									</c:when>
									<c:otherwise>
										<option value="${endpoint.id}"><c:out value="${endpoint.name}" /></option>
									</c:otherwise>
								</c:choose>
							</c:forEach>						
						</select>
					</td>
				</tr>
	<tr>
			<td class="fieldLabel">Filters</td>
			<td>
				<table>
					<tr>
						<td colspan="2"><label for="filters_dual">Available Filters</label></td>
						<td colspan="2"><label for="filters">Selected Filters</label></td></tr>
					<tr>
						<td>
							<select name="filters_dual" id="filters_dual" multiple="multiple" size="4" style="width: 200px">	
								<c:forEach var="filter" items="${filters}">
									<c:set var="checkFilter" value="false" />
									<c:forEach var="currentFilter" items="${currentFilters}">
										<c:if test="${filter.id == currentFilter.id}">
											<c:set var="checkFilter" value="true" />																						
										</c:if>
									</c:forEach>
									<c:if test="${checkFilter == 'false'}">
										<option value="${filter.id}"><c:out value="${filter.name}" /></option>
									</c:if>
								</c:forEach>	
							</select>
						</td>
						<td class="verticalButtons">
							<input type="button" name="filters_rightbtn" class="arrowButton" id="rightButton"/><br />	
							<input type="button" name="filters_leftbtn" class="arrowButton" id="leftButton"/>
						</td>
						<td>
							<select name="filters" id="filters" multiple="multiple" size="4" class="dualSelect" style="width: 200px">
								<c:forEach var="currentFilter" items="${currentFilters}">
									<option value="${currentFilter.id}"><c:out value="${currentFilter.name}" /></option>
								</c:forEach>									    					
							</select>
						</td>
						<td class="verticalButtons">
							<input type="button" name="filters_upbtn"  class="arrowButton" id="upButton" /><br />	
							<input type="button" name="filters_downbtn" class="arrowButton" id="downButton" />
						</td>
					</tr>
				</table>	
			</td>
		</tr>				

		<tr>
			<td class="fieldLabel">Transformers</td>
			<td>
				<table>
					<tr>
						<td colspan="2"><label for="transformers_dual">Available Transformers</label></td>
						<td colspan="2"><label for="transformers">Selected Transformers</label></td></tr>
					<tr>
						<td>
							<select name="transformers_dual" id="transformers_dual" multiple="multiple" size="4" style="width: 200px">	
								<c:forEach var="transformer" items="${transformers}">
									<c:set var="checkTransformer" value="false" />
									<c:forEach var="currentTransformer" items="${currentTransformers}">
										<c:if test="${transformer.id == currentTransformer.id}">
											<c:set var="checkTransformer" value="true" />																						
										</c:if>
									</c:forEach>
									<c:if test="${checkTransformer == 'false'}">
										<option value="${transformer.id}"><c:out value="${transformer.name}" /></option>
									</c:if>
								</c:forEach>	
							</select>
						</td>
						<td class="verticalButtons">
							<input type="button" name="transformers_rightbtn" class="arrowButton" id="rightButton"/><br />	
							<input type="button" name="transformers_leftbtn" class="arrowButton" id="leftButton"/>
						</td>
						<td>
							<select name="transformers" id="transformers" multiple="multiple" size="4" class="dualSelect" style="width: 200px">
								<c:forEach var="currentTransformer" items="${currentTransformers}">
									<option value="${currentTransformer.id}"><c:out value="${currentTransformer.name}" /></option>
								</c:forEach>									    					
							</select>			    					
						</td>
						<td class="verticalButtons">
							<input type="button" name="transformers_upbtn"  class="arrowButton" id="upButton" /><br />	
							<input type="button" name="transformers_downbtn" class="arrowButton" id="downButton" />
						</td>
					</tr>
				</table>	
			</td>
		</tr>

		<tr><td class="fieldLabel"><label for="outEndpoint">Outbound Endpoint</label></td>
			<td>
				<select name="outEndpoint" id="outEndpoint" tabindex="7">
					<c:forEach var="endpoint" items="${endpoints}">			
						<c:choose>
							<c:when test="${channel.destinationEndpointId == endpoint.id}">
								<option selected="selected" value="${endpoint.id}"><c:out value="${endpoint.name}" /></option>
							</c:when>
							<c:otherwise>
								<option value="${endpoint.id}"><c:out value="${endpoint.name}" /></option>
							</c:otherwise>
						</c:choose>
					</c:forEach>						
				</select>
			</td>
		</tr>
	</table>

			<div class="buttonNav">					
				<input type="reset" value="reset" class="button" />
				<input type="submit" name="submit" value="submit" class="button"  />
			</div>		

			<!-- any extra hidden input -->
			<div>
				<input type="hidden" name="op" value="clone" />
				<input type="hidden" name="id" value="${channel.id}" />
			</div>
		</form>


	</div>

</div><!-- primaryContent -->
<jsp:include page="/_jsp/footer.jsp" />
