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
			<jsp:param name="selected" value="Filters" />
			<jsp:param name="lastState" value="${lastState}" />
		</jsp:include>

		<form action="/wizard/?state=filters" method="post">
			<fieldset>
				<legend>Available Filters</legend>
				<table>
					<tr>
						<td colspan="2">All Filters</td>
						<td colspan="2"><label for="filters">Selected Filters</label></td></tr>						
					<tr>
						<td>
							<select name="filters_dual" id="filters_dual" multiple="multiple" size="4" style="width: 200px">	
								<c:forEach var="filter" items="${unselectedFilters}">			    					
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
								<c:forEach var="filter" items="${selectedFilters}">			    					
									<option value="${filter.id}"><c:out value="${filter.name}" /></option>
								</c:forEach>
							</select>
						</td>
						<td class="verticalButtons">
							<input type="button" name="filters_upbtn"  class="arrowButton" id="upButton" /><br />	
							<input type="button" name="filters_downbtn" class="arrowButton" id="downButton" />
						</td>
					</tr>
				</table>
			</fieldset>

			<!-- I dont know if you want forms or simple links provided previous data has already been saved -->

			<fieldset>
				<legend>Add new filter</legend>

				<a href="/filter/?state=new&amp;orig=%2Fwizard%2F%3Fstate%3Dfilters%26newFilter%3D" id="filterNew" class="actionButton"><span>New Filter</span></a>

			</fieldset>

			<div class="wizardButtonNav">
				<input type="submit" name="next" class="button next" value="Next &gt;" />
				<c:if test="${lastState == 5}">
					<input type="submit" name="preview" class="button next" value="Back to preview" />
				</c:if>
				<input type="submit" name="previous" class="button previous" value="&lt; Previous" />
			</div>
			<div>
				<input type="hidden" name="op" value="filters" />
			</div>
		</form>
	</div><!-- innerContent -->
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
