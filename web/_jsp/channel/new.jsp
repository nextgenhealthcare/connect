<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="New Channel" />
	<jsp:param name="selected" value="channel" />
</jsp:include>


<div id="primaryContent">

	<!-- Main content -->
	<h1>New Channel</h1>

	<div class="breadcrumb">
		<a href="/channel/">Channels</a> &gt; New Channel
	</div>

	<div id="innerContent">
		<p class="block">
		Add channel using wizard <a href="/wizard/?op=new" class="button">Run wizard</a>
		</p>

		<form action="/channel/" method="post">
			<table>

				<tr><td class="fieldLabel"><label for="name">Name</label></td><td><input name="name" id="name" size="20" value="${fn:escapeXml(param.name)}" /></td></tr>
				<tr>
					<td class="fieldLabel"><label for="description">Description</label></td>
					<td><textarea name="description" id="description" rows="6" cols="40"><c:out value="${fn:escapeXml(param.description)}" /></textarea></td>
				</tr>				
				<tr><td class="fieldLabel"><label for="hl7">HL7 Version</label></td>
					<td>
						<select name="hl7" id="hl7">

							<c:forEach var="hl7" items="${hl7Versions}">
								<c:choose>
									<c:when test="${hl7==param.hl7}">
										<option value="${hl7}" selected>${hl7}</option>
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
					<td><input type="checkbox" name="deployed"  id="deployed"  value="1" /></td>
				</tr>		
				<tr><td class="fieldLabel"><label for="direction">Direction</label></td>
					<td>
						<select name="direction" id="direction">
							<option value="outbound" selected>Outbound</option>
							<option value="inbound">Inbound</option>
						</select>
					</td>
				</tr>
				<tr><td class="fieldLabel"><label for="inEndpoint">Inbound Endpoint</label></td>
					<td>
						<select name="inEndpoint" id="inEndpoint">
							<c:forEach var="endpoint" items="${endpoints}">			    					
								<c:choose>
									<c:when test="${endpoint.id==param.inEndpoint}">
										<option value="${endpoint.id}" selected>${endpoint.name}</option>
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
						<td colspan="2"><label for="filters">Selected Filters</label></td>
					</tr>
					<tr>
						<td>
							<select name="filters_dual" id="filters_dual" multiple="multiple" size="4" style="width: 200px">	
								<c:forEach var="filter" items="${filters}">			    					
									<option value="${filter.id}"><c:out value="${filter.name}" /></option>
								</c:forEach>	
							</select>
						</td>
						<td class="verticalButtons">
							<input type="button" name="filters_rightbtn" class="arrowButton" id="rightButton"/><br />	
							<input type="button" name="filters_leftbtn" class="arrowButton" id="leftButton"/>
						</td>
						<td>
							<select name="filters" id="filters" multiple="multiple" size="4" class="dualSelect" style="width: 200px">										    					
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
						<td colspan="2"><label for="transformers">Selected Transformers</label></td>
					</tr>
					<tr>
						<td>
							<select name="transformers_dual" id="transformers_dual" multiple="multiple" size="4" style="width: 200px">	
								<c:forEach var="transformer" items="${transformers}">			    					
									<option value="${transformer.id}"><c:out value="${transformer.name}" /></option>
								</c:forEach>
							</select>
						</td>
						<td class="verticalButtons">
							<input type="button" name="transformers_rightbtn" class="arrowButton" id="rightButton"/>
							<br />	
							<input type="button" name="transformers_leftbtn" class="arrowButton" id="leftButton"/>
						</td>
						
						<td>
							<select name="transformers" id="transformers" multiple="multiple" size="4" class="dualSelect" style="width: 200px">										    					
							</select>
						</td>
						<td class="verticalButtons">
							<input type="button" name="transformers_upbtn"  class="arrowButton" id="upButton" />
							<br />	
							<input type="button" name="transformers_downbtn" class="arrowButton" id="downButton" />
						</td>
					</tr>
				</table>	
			</td>
		</tr>


		<tr>
			<td class="fieldLabel">Outbound Endpoints</td>
			<td>
				<table>
					<tr>
						<td colspan="2"><label for="outEndpoints_dual">Available Endpoints</label></td>
						<td colspan="2"><label for="outEndpoints">Selected Endpoints</label></td>
					</tr>
					<tr>
						<td>
							<select name="outEndpoints_dual" id="outEndpoints_dual" multiple="multiple" size="4" style="width: 200px">	
								<c:forEach var="endpoint" items="${endpoints}">			    					
									<option value="${endpoint.id}"><c:out value="${endpoint.name}" /></option>
								</c:forEach>
							</select>
						</td>
						<td class="verticalButtons">
							<input type="button" name="outEndpoints_rightbtn" class="arrowButton" id="rightButton"/>
							<br />	
							<input type="button" name="outEndpoints_leftbtn" class="arrowButton" id="leftButton"/>
						</td>
						
						<td>
							<select name="outEndpoints" id="outEndpoints" multiple="multiple" size="4" class="dualSelect" style="width: 200px">									    					
							</select>
						</td>
						<td class="verticalButtons">
							<input type="button" name="outEndpoints_upbtn"  class="arrowButton" id="upButton" />
							<br />	
							<input type="button" name="outEndpoints_downbtn" class="arrowButton" id="downButton" />
						</td>
					</tr>
				</table>	
			</td>
		</tr>

			</table>

			<div class="buttonNav">					
				<input type="reset" value="reset" class="button" />
				<input type="submit" value="submit" class="button" />
			</div>		

			<!-- any extra hidden input -->
			<div><input type="hidden" name="op" value="new" /></div>
		</form>


	</div>
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
