<%@page contentType="text/html;charset=ISO-8859-1" language="java"%>
<%@include file="/WEB-INF/jsp/common/taglibs.jsp"%>
<%
    String ua = request.getHeader("User-Agent").toLowerCase();
	boolean mobile = ua.matches("(?i).*(android.+mobile|avantgo|bada\\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|meego.+mobile|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino).*") || ua.substring(0, 4).matches("(?i)1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\\-(n|u)|c55\\/|capi|ccwa|cdm\\-|cell|chtm|cldc|cmd\\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\\-s|devi|dica|dmob|do(c|p)o|ds(12|\\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\\-|_)|g1 u|g560|gene|gf\\-5|g\\-mo|go(\\.w|od)|gr(ad|un)|haie|hcit|hd\\-(m|p|t)|hei\\-|hi(pt|ta)|hp( i|ip)|hs\\-c|ht(c(\\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\\-(20|go|ma)|i230|iac( |\\-|\\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\\/)|klon|kpt |kwc\\-|kyo(c|k)|le(no|xi)|lg( g|\\/(k|l|u)|50|54|\\-[a-w])|libw|lynx|m1\\-w|m3ga|m50\\/|ma(te|ui|xo)|mc(01|21|ca)|m\\-cr|me(di|rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\\-2|po(ck|rt|se)|prox|psio|pt\\-g|qa\\-a|qc(07|12|21|32|60|\\-[2-7]|i\\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\\-|oo|p\\-)|sdk\\/|se(c(\\-|0|1)|47|mc|nd|ri)|sgh\\-|shar|sie(\\-|m)|sk\\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\\-|v\\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\\-|tdg\\-|tel(i|m)|tim\\-|t\\-mo|to(pl|sh)|ts(70|m\\-|m3|m5)|tx\\-9|up(\\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\\-|your|zeto|zte\\-");
	pageContext.setAttribute("mobile", mobile);
%>

<s:layout-definition>
    <!DOCTYPE html>
    <html>
        <head>
            <title>Mirth Connect Web Administrator</title>
            <link rel="shortcut icon" type="image/x-icon" href="${contextPath}/images/favicon.ico" />
            <link rel="stylesheet" type="text/css" href="${contextPath}/css/bootstrap.css" />
            <link rel="stylesheet" type="text/css" href="${contextPath}/css/statistics.css" />
            <s:layout-component name="head" />
        </head>

        <body>
            <nav id="header" class="navbar navbar-inverse navbar-fixed-top" role="navigation">
                <div class="navbar-inner">
                    <div id="statisticsNavbarContainer" class="container" style="width: 98%;">
                        <a class="navbar-brand"> <img alt="Mirth Connect" src="${contextPath}/css/mirthconnectlogowide.png" style="height: 30px"></a>
                        <ul id="navbarList" class="nav navbar-nav">
                            <li id="li_dashboardstatistics"><s:link beanclass="com.mirth.connect.webadmin.action.DashboardStatisticsActionBean">Dashboard Statistics</s:link></li>
                        </ul>
        
                        <div id="userButtonContainer" class="nav navbar-nav btn-group navbar-right" style="margin-top: 8px;">
                            <a id="userButton" class="btn btn-default dropdown-toggle" data-toggle="dropdown" href="#"> <span class="glyphicon glyphicon-user"></span> ${user.username} <span class="caret"></span></a>
        
                            <ul class="dropdown-menu">
                                <c:if test="${not mobile}">
                                    <li><a href="#" onClick="launchAdministrator()"> <span class="glyphicon glyphicon-upload"></span> Launch Administrator</a></li>
                                    <li class="divider"></li>
                                </c:if>
        
                                <li><a href="Logout.action"> <span class="glyphicon glyphicon-share"></span> Logout</a></li>
                            </ul>
                        </div>
                    </div>
                </div>
            </nav>
            <div id="body">
                <s:layout-component name="body" />
            </div>
        
            <div id="footer"></div>
        
            <!-- Scripts placed at the end of the document so the pages load faster -->
            <script src="${contextPath}/js/jquery-1.8.0.js"></script>
            <script src="${contextPath}/js/bootstrap.min.js"></script>
        
            <!-- Script to highlight navbar links as active upon click -->
            <script type="text/javascript">
                function getCurrentPageName() {
                    var pageURL = document.location.href;
                    var pageName = pageURL.substring(pageURL.lastIndexOf('/') + 1);

                    return pageName.toLowerCase();
                }

                $(document).ready(function() {
                    var currPage = getCurrentPageName();

                    switch (currPage) {
                    case 'dashboardstatistics.action':
                        $('#li_dashboardstatistics').addClass('active');
                        break;
                    }
                });
            </script>
            
            <script type="text/javascript">
	       		function launchAdministrator(){
	       			window.location.href = 'http://' + window.location.hostname + ':${actionBean.context.httpPort}${actionBean.context.contextPath}/webstart.jnlp?time=' + new Date().getTime(); 
	       		}
       		</script>
        
            <s:layout-component name="scripts" />
        </body>
    </html>
</s:layout-definition>