<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%--
The taglib directive below imports the JSTL library. If you uncomment it,
you must also add the JSTL library to the project. The Add Library... action
on Libraries node in Projects view can be used to add the JSTL 1.1 library.
--%>
<%--
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%> 
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<style type="text/css"><!--
input.launch{
		background-color:#cec;
		filter:progid:DXImageTransform.Microsoft.Gradient(
		GradientType=0,StartColorStr='#ffffffff',EndColorStr='#ffaaddaa');
	   }
//--></style>
<html>
<head>
<title>Launch Mirth Administration</title>
</head>

<body background="#FFFFFF">
<font face="sans serif, arial, courier new" color="#000000">

<table cellspacing=5 cellpadding=5>
	<tr>
		<td><a href="http://www.mirthproject.org/"><img src="mirth_logo.png" border=0></a></td>
		<td><center><h1> Launch Mirth Administrator </h1></center></td>
	</tr>
</table>

<hr>

<table cellpadding=15>
	<h2>Using Mirth - Java Web Start</h2>
	
	<tr><td>
		<h3>Overview of Web Start</h3>
			Java Web Start is a framework developed by Sun Microsystems that enables starting 
			Java applications directly from the Web using a browser. Unlike Java applets, Web 
			Start applications do not run inside the browser.
			
	<tr><td>
		<h3>Starting Mirth Administrator</h3>
		
			
			Click the green link to launch the Mirth Administrator using Java Web Start. <br><br><br><br>
			
			<center><input type="button" value="Launch Mirth Administrator" STYLE="font-size:20pt" class="launch" onClick="parent.location='MirthGUI.jnlp'"</input></center>
	</td></tr>
	
	
	
	
<table>

</font>
</body>
		
</html>
