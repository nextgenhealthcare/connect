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
			<jsp:param name="selected" value="Transformers" />
			<jsp:param name="lastState" value="${lastState}" />
		</jsp:include>

		<form action="/wizard/?state=transformers" method="post">
			<fieldset>
				<legend>Available Transformers</legend>
				<table>
					<tr>
						<td colspan="2"><label for="transformers_dual">All Transformers</label></td>
						<td colspan="2"><label for="transformers">Selected Transformers</label></td></tr>
					<tr>
						<td>
							<select name="transformers_dual" id="transformers_dual" multiple="multiple" size="4" style="width: 200px">	
								<c:forEach var="transformer" items="${unselectedTransformers}">			    					
									<option value="${transformer.id}"><c:out value="${transformer.name}" /></option>
								</c:forEach>	
							</select>
						</td>
						<td class="verticalButtons">
							<input type="button" name="transformers_rightbtn" class="arrowButton" id="rightButton"/><br />	
							<input type="button" name="transformers_leftbtn" class="arrowButton" id="leftButton"/>
						</td>
						<td>
							<select name="transformers" id="transformers" multiple="multiple" size="4" class="dualSelect" style="width: 200px">										    					
								<c:forEach var="transformer" items="${selectedTransformers}">			    					
									<option value="${transformer.id}"><c:out value="${transformer.name}" /></option>
								</c:forEach>	
							</select>
						</td>
						<td class="verticalButtons">
							<input type="button" name="transformers_upbtn"  class="arrowButton" id="upButton" /><br />	
							<input type="button" name="transformers_downbtn" class="arrowButton" id="downButton" />
						</td>
					</tr>
				</table>	
			</fieldset>

			<fieldset>
				<legend>Add new transformer</legend>

				<a href="/transformer/?state=new&amp;orig=%2Fwizard%2F%3Fstate%3Dtransformers%26newTransformer%3D" id="transformerNew" class="actionButton"><span>New Transformer</span></a>

			</fieldset>

			<div class="wizardButtonNav">
				<input type="submit" name="next" class="button next" value="Next &gt;" />
				<c:if test="${lastState == 5}">
					<input type="submit" name="preview" class="button next" value="Back to preview" />
				</c:if>
				<input type="submit" name="previous" class="button previous" value="&lt; Previous" />
			</div>

			<div>
				<input type="hidden" name="op" value="transformers" />
			</div>

		</form>
	</div><!-- innerContent -->
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
