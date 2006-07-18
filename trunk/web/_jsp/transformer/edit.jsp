<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<%-- param pageTitle: default title if none is chosen --%>
<jsp:include page="/_jsp/header.jsp">
	<jsp:param name="pageTitle" value="Edit Transformer" />
	<jsp:param name="selected" value="transformer" />
</jsp:include>

<div id="primaryContent">
	<!-- Main content -->

	<h1>Edit Transformer</h1>

	<div class="breadcrumb">
		<a href="/transformer/">Transformers</a> &gt; Edit Transformer
	</div>

	<div id="innerContent">

		<form action="/transformer/" method="post" onSubmit="return setScript()">
			<table>

				<c:set var="name" value="${transformer.name}" />
				<c:if test="${param.name != null}">
					<c:set var="name" value="${param.name}" />
				</c:if>
				<tr><td class="fieldLabel"><label for="name">Name</label></td><td><input name="name" id="name" size="20" value="${fn:escapeXml(name)}" /></td></tr>
				<tr>
					<c:set var="description" value="${transformer.description}" />
					<c:if test="${param.description != null}">
						<c:set var="description" value="${param.description}" />
					</c:if>
					<td class="fieldLabel"><label for="description">Description</label></td>
					<td><textarea name="description" id="description" rows="6" cols="40"><c:out value="${description}" /></textarea></td>
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
					
						<c:choose>
							<c:when test="${fn:escapeXml(!empty param.scriptString && param.script == 'JavaScript' )}">
								<param name="scriptValue" value="${param.scriptString}" />								
							</c:when>
							<c:otherwise>
								scriptValue = ${javascriptValue}
								<param name="scriptValue" value="${javascriptValue}" />
							</c:otherwise>
						</c:choose>
					</applet>
			</fieldset>

			<div class="buttonNav">					
				<input type="reset" value="reset" class="button" />
				<input type="button" value="delete" class="button deleteButton" />
				<input type="submit" value="submit" class="button"  />
			</div>		

			<!-- any extra hidden input -->
			<div>
				<input type="hidden" name="script" value="JavaScript" />
				<input type="hidden" name="op" value="edit" />
				<input type="hidden" name="scriptString" value="" />					
				<input type="hidden" name="id" value="${transformer.id}" />
			</div>
		</form>


	</div>
</div><!-- primaryContent -->
<jsp:include page="/_jsp/footer.jsp" />
