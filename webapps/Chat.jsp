<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<link rel="stylesheet" type="text/css" href="stylesheet.css">
		<link rel="shortcut icon" type="img/png" href="/cockadoodle/logo.png">
		<title>Cockadoodle</title>
		
		<script language="JavaScript">//Constants for the message type.
			NEWUSER = "newuser" //the format is newuser#username
			GROUPCHATMESSAGE = "groupchatmessage";
			ERRORMESSAGE="error";
			LOGEDOUT = "logedout";


		</script>
		
		<script language="Javascript" type="text/javascript" src="js/jquery1.7.1.js"></script>
		<script language="Javascript" type="text/javascript" src="js/ClientServerHandler.js"></script>
		<script language="Javascript" type="text/javascript" src="js/client.js"></script>
		<script type="text/javascript">
		
		$(window).focus(function() {
    		window_focus = true;
    		if(isNotify){
    			toggleNotify();
    		}
		}).blur(function() {
    		window_focus = false;
		});
		</script>
	</head>
	
	<body>
		<h1>Cockadoodle</h1>
		
		<span class="loginDetails"></span>
		<br>
		<div id="loginDiv" class="loginDiv">
			<p class="name"><b>User Name</b></p><p class="textbox"><input type="text" id="userName" size="20"></p>
			<p><input type="text" id="channel" class="channel"></p>
			<p id="error">Sorry, that name is in use.</p>
			<p><input type="button" id="login" class="btn" value="Login"></p>
		</div>
		
		<div id="chatArea" class="chatArea">
				<div class="list">
					Roosters:
					<ul class="listOfUsers"></ul>
				</div>
			<div class="boxes">
				<textarea id="groupChatHistory" class="groupChatHistory" name="MessageArea" readonly="true"></textarea>
				<p><input type="text" class="groupChatBox" id="groupChatBox" size='63'></input></p>
			</div>
			<div style="float:right;width: 200px;">
			</div>
		</div>
	</body>
	<script type="text/javascript">
	$(".chatArea").hide();
	$(".channel").hide();
	if(!isCookie("login_failed")){
		$("#error").invisible();
	}
	deleteCookie("login_failed");
	
	</script>
</html>