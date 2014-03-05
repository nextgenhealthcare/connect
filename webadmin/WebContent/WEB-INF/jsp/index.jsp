<%@include file="/WEB-INF/jsp/common/taglibs.jsp"%>
<!doctype html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="x-ua-compatible" content="IE=edge">
        
        <title>Mirth Connect Administrator</title>
        
        <link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico" />
        <link rel="stylesheet" type="text/css" href="css/bootstrap.css" />
        <link rel="stylesheet" type="text/css" href="css/main.css" />
        
        <script type="text/javascript">
            /* Break out of frame if inside a frame. */
            if (window != window.top) {
                window.top.location = window.location;
            }
        </script>
        
        <script type="text/javascript" src="js/jquery-1.8.0.js"></script>
    </head>
    
    <body id="body" style="display: none;" class="subpage" <c:if test="${actionBean.secureHttps == true}">onload="document.loginform.username.focus();"</c:if>>
        <div id="centerWrapper" class="container">
            <div class="row">
                <div id="mirthLogoWrapper">
                    <img id="mirthLogo" src="images/mirthconnectlogowide.png" />
                </div>
    
                <div id="mcadministrator" class="col-md-6 col-md-6-custom">
                    <h1 style="text-align: center;">Mirth Connect Administrator</h1>
    				
    				<div id="overviewwebstart">
	    				<div class="help-block">
	                        <strong>Overview of Web Start:</strong><br /> Java Web Start is a framework developed by Sun Microsystems that enables launching Java applications directly from a browser. Unlike Java applets, Web Start applications do not run inside the browser.
	                    </div>
	                    <div class="help-block">
	                        <br />Click the big green button below to launch the Mirth Connect Administrator using Java Web Start.
	                    </div>
    				</div>

                    <div style="text-align: center;">
                        <a class="btn btn-md btn-themebutton" href="javascript:launchAdministrator()">Launch Mirth Connect Administrator</a>
                    </div>
                </div>
    
                <div id="webdashboardsignin" class="col-md-6 col-md-6-custom">
                    <h1 id="webDashboardHeader" style="text-align: center;">Web Dashboard Sign in</h1>
    				    				
    				<c:choose>
    				 	<c:when test="${actionBean.secureHttps == true}">
		                     <form id="webLoginForm" name="loginform" action="Login.action" method="post">
		                        <div id="loginErrorAlert" class="alert alert-danger hide fade in" data-alert="alert">
			                    	<p>Invalid login credentials</p>
			                    </div>
			                    <div id="webLoginWrapper">
			    					<div id="webLogin">
			                            <input type="hidden" name="op" value="login" /> <input type="hidden" name="version" value="0.0.0" /> <label for="username">Username</label>
			                            <input id="username" type="text" name="username" autocomplete="off" maxlength="32" /> <label for="password">Password</label>
			                            <input id="password" type="password" name="password" autocomplete="off" />
			                            <div id="webLoginSecurityReminder" class="help-block">
			                                <strong>Security Reminder:</strong><br /> Sign out of your account when you finish your session.
			                            </div>
				                    </div>
			                    </div>
			                    <div id="webLoginButton">
				                	<input class="btn btn-md btn-themebutton" type="submit" value="Sign in"/>
				                </div>
				        	</form>
		            	</c:when>
		              	<c:otherwise>
		              		<div id="securesiteaccess">
		              			<p>The Mirth Connect Web Dashboard must be accessed over HTTPS. Click below button to switch to the secure site.</p>
		                   		<div class="help-block">
		                	        <br/><strong>Note:</strong><br/> You may see a certificate error if your server is using a <a href="http://en.wikipedia.org/wiki/Self-signed_certificate" target="_blank">self-signed certificate</a>. To prevent further warnings, you can add this certificate to your browser or operating system.
		                        </div>
			                </div>
		                    <div id="accessSecureSiteButton" style="text-align: center;">
		                    	<a class="btn btn-md btn-themebutton" href="SecureAccess.action">Access Secure Site</a>
		                    </div> 
  						</c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
        <div id="smallSubPage">
        	<p>&copy; 2014 Mirth Corporation | Mirth Connect</p>
        </div>
    
        <script type="text/javascript">
            $(document).ready(
                    function detectMobile() {
                        /**
                         * jQuery.browser.mobile (http://detectmobilebrowser.com/)
                         * jQuery.browser.mobile will be true if the browser is a mobile device
                         **/
                        (function(a) {
                            jQuery.browser.mobile = /android.+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(a) || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|e\-|e\/|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(di|rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|xda(\-|2|g)|yas\-|your|zeto|zte\-/i.test(a.substr(0, 4))
                        })(navigator.userAgent || navigator.vendor || window.opera);

                        if (jQuery.browser.mobile) {
                            $("#mcadministrator").hide();

                            $("#centerWrapper").css("margin", "0");
                            $("#centerWrapper").css("padding", "0");
                            $("#centerWrapper").css("border", "none");
                            $("#centerWrapper").css("width", "100%");

                            $("#webdashboardsignin").css("border-left", "0");
                            $("#webdashboardsignin").css("margin-left", "0");
                            $("#webdashboardsignin").css("padding-left", "0");
                            $("#webdashboardsignin").css("width", "100%");

                            $("#username").css("width", "100%");
                            $("#password").css("width", "100%");

                            $("#securesiteaccess").css("margin-left", "30px");
    
                            // Set viewport meta tag
                            var mt = $('meta[name=viewport]');
            				mt = mt.length ? mt : $('<meta name="viewport" />').appendTo('head');
            				mt.attr('content', 'initial-scale=.8,maximum-scale=.8,user-scalable=no,width=device-width');
                        } else {
                            $("#mcadministrator").show();
                        }
                        $("#body").css("display", "inline");
                    });
        </script>
        <script type="text/javascript">
            var showAlert = false;
            $(document).ready(function() {
                $.urlParam = function(name) {
                    var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
                    if (results != null) {
                        return results[1] || 0;
                    }
                }
                showAlert = $.urlParam('showAlert');

                if (showAlert) {
                    $("#loginErrorAlert").removeClass('hide');
                    return false;
                } else {
                    $("#loginErrorAlert").addClass('hide');
                    return true;
                }
            });
        </script>
        <script type="text/javascript">
       		function launchAdministrator(){
       			window.location.href = 'http://' + window.location.hostname + ':${actionBean.context.httpPort}${actionBean.context.contextPath}/webstart.jnlp?time=' + new Date().getTime(); 
       		}
        </script>
    </body>
</html>