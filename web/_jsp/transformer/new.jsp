<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="New Transformer" />
	<jsp:param name="selected" value="transformer" />
</jsp:include>

<div id="primaryContent">
	<!-- Main content -->
	<h1>New Transformer</h1>

	<div class="breadcrumb">
		<c:choose>
			<c:when test="${!empty param.orig}">
				<a href="/channel/">Channels</a> &gt; <a href="/channel/?state=new">New Channel</a> &gt; <a href="/wizard/?state=transformers">Channel Wizard</a> &gt; New Transformer
			</c:when>
			<c:otherwise>
				<a href="/transformer/">Transformers</a> &gt; New Transformer
			</c:otherwise>
		</c:choose>
	</div>

	<div id="innerContent">

		<form action="/transformer/" method="post" onSubmit="return setScript()">
			<table id="transformerForm">		
				<tr>
					<td class="fieldLabel"><label for="name">Name</label></td>
					<td><input name="name" id="name" size="20" value="${fn:escapeXml(param.name)}" /></td>
				</tr>
				<tr>
					<td class="fieldLabel"><label for="description">Description</label></td>
					<td><textarea name="description" id="description" rows="6" cols="40"><c:out value="${param.description}" /></textarea></td>
				</tr>
			</table>


			<fieldset id="JavaScript">
				<legend>JavaScript</legend>
  				<applet
					code="com/webreach/mirth/applets/editor/Editor.class"
					archive="/assets/applets/applets.jar"
					name="javascript_editor"
					id="javascript_editor"
					width="100%"
					height="400">
			
					<param name="scriptValidatorServlet" value="/validator/">								
					<c:if test="${param.script == 'JavaScript'}">
						<param name="scriptValue" value="${param.scriptString}">
					</c:if>
				</applet>
			</fieldset>

			<div class="buttonNav">					
				<input type="reset" value="reset" class="button" />
				<input type="submit" name="submit" value="submit" class="button" />
			</div>	
				
								
			<!-- any extra hidden input -->
			<div>
				<input type="hidden" name="script" value="JavaScript" />
				<input type="hidden" name="op" value="new" />
				<input type="hidden" name="scriptString" value="" />					
				<c:if test="${param.orig != null}">
					<input type="hidden" name="orig" value="${fn:escapeXml(param.orig)}" />
				</c:if>
			</div>
		</form>


	</div>
</div><!-- primaryContent -->

<jsp:include page="/_jsp/footer.jsp" />
