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
		background-color:#B4D671;
		filter:progid:DXImageTransform.Microsoft.Gradient(
		GradientType=0,StartColorStr='#ffffffff',EndColorStr='#B4D671');
	   }
//-->
body
{
font-family: verdana, tahoma, arial, sans-serif
}
p
{
font-family: verdana, tahoma, arial, sans-serif
}
h2
{
font-family: verdana, tahoma, arial, sans-serif
}
h3
{
font-family: verdana, tahoma, arial, sans-serif
}
</style>
<html>
<head>
<title>Launch Mirth Administration</title>
</head>

<body background="#FFFFFF">


<table cellspacing=5 cellpadding=5>
	<tr>
		<td><a href="http://www.mirthproject.org/"><img src="mirth_logo.png" border=0></a></td>
		<td><center><h1><font face="tahoma, verdana, arial, courier new" color="#000000"> Launch Mirth Administrator </font></h1></center></td>
	</tr>
</table>

<hr>

<table cellpadding=15>
	<h2>Using Mirth - Java Web Start</h2>
	
	<tr><td>
		<h3>Overview of Web Start</h3><p>
			Java Web Start is a framework developed by Sun Microsystems that enables starting 
			Java applications directly from the Web using a browser. Unlike Java applets, Web 
			Start applications do not run inside the browser.</p>
			
	<tr><td>
		<h3>Starting Mirth Administrator</h3>
		
			<p>
			Click the green link to launch the Mirth Administrator using Java Web Start. <br><br>
			</p>
			<center><input type="button" value="Launch Mirth Administrator" STYLE="font-size:14pt" class="launch" onClick="parent.location='mirth-client.jnlp'"</input></center>
	</td></tr>
	
	
	
	
<table>

</font>
</body>
		
</html>
