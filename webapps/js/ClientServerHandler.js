/*
This is the heavier backend portion of the application.
It contains some important functions like send message and stuff.
*/
var window_focus =true;
var isNotify = false;

function ClientServerHandler(ch, m){

	this.channel = ch;
	this.ajax = getAjaxClient();
	this.onMessage = m;

	// stick a reference to "this" into the ajax client, so that the handleMessage 
	// function can access the push client - its "this" is an XMLHttpRequest object
	// rather than the push client, coz thats how javascript works!
	this.ajax.ClientServerHandler = this;
	
	
	function getAjaxClient(){
		/*
		 * Gets the ajax client
		 * http://en.wikipedia.org/wiki/XMLHttpRequest
		 * http://www.w3.org/TR/XMLHttpRequest/#responsetext
		 */
	    var client = null;
	    try{
			// Firefox, Opera 8.0+, Safari
			client = new XMLHttpRequest();
		}catch (e){
			// Internet Explorer
			try{
				client = new ActiveXObject("Msxml2.XMLHTTP");
			}catch (e){
				client = new ActiveXObject("Microsoft.XMLHTTP");
			}
		}
		return client;
	};
	
	/** 
	 * pass in a callback and a channel.  
	 * the callback should take a string, 
	 * which is the latest version of the model
	 * 
	 *  This method is called on login and it will register user to server
	 */
	ClientServerHandler.prototype.createConnection = function(){
		try{
			var params = escape("channel") + "=" + escape(this.channel) +"&"+ escape("userName")+ "=" + escape($("#userName").val());
			var url = "/cockadoodle/login?" + params;
			this.ajax.onreadystatechange = handleMessage;
			this.ajax.open("GET",url,true); //true means async, which is the safest way to do it
			this.ajax.send(null);

		}catch(e){
			alert(e);
		}
	};
	
	/**
	 * If any message to be sent to the server calls this method to send the message
	 * note :all the messages 
	 */
	ClientServerHandler.prototype.sendMessage = function (message){
		
		
		try{
		
			var requestParams = {
					message:message,
					"channel":'default'
			      
			};
			//below param method take cares of special characters in data
			requestParams = jQuery.param(requestParams);
			
			$.ajax({
				  type : 'GET',//GET Or POST 
				  url  : '/cockadoodle/publish',
				  cache: false, //get fresh copy of details.html instead of cached one
				  data :requestParams,
				  
				  // callback handler that will be called on error
				  error: function(jqXHR, textStatus, errorThrown){
				      // log the error to the console
				      console.log(
				          "The following error occured: "+
				          textStatus, errorThrown
				      );
				  }
				 
				});
		
		}catch(e){
			alert(e);
		}
	};

	var oldResponseMessage ="";
	function handleMessage() {
		//states are:
		//	0 (Uninitialized)	The object has been created, but not initialized (the open method has not been called).
		//	1 (Open)	The object has been created, but the send method has not been called.
		//	2 (Sent)	The send method has been called. responseText is not available. responseBody is not available.
		//	3 (Receiving)	Some data has been received. responseText is not available. responseBody is not available.
		//	4 (Loaded)
		try{
			if(this.status==200){
				if(this.getResponseHeader("LOGIN_FAILED")=='1'){
					$("#userName").val("");
					setCookie("login_failed","true");
					location.reload(true);

				}
			}
			if(this.readyState == 0){
				//this.ClientServerOperationsHandler.onMessage("0/-/-");
			}else if (this.readyState == 1){
				//this.ClientServerOperationsHandler.onMessage("1/-/-");
			}else if (this.readyState == 2){
				//this.ClientServerOperationsHandler.onMessage("2/-/-");
			}else if (this.readyState == 3){
				//for chunked encoding, we get the newest version of the entire response here, 
				//rather than in readyState 4, which is more usual.
				if (this.status == 200){
					//this.ClientServerOperationsHandler.onMessage("3/200/" + this.responseText.substring(this.responseText.lastIndexOf("|")));
					
					var newMessage = this.responseText;
					newMessage = newMessage.replace(oldResponseMessage, "");
					
					this.ClientServerHandler.onMessage( newMessage );
					
					oldResponseMessage = this.responseText;
					//alert(" Response 3 -200: "+ newMessage );
				}else{
					//this.ClientServerOperationsHandler.onMessage("3/200/" + this.responseText.substring(this.responseText.lastIndexOf("|")));
					
					var newMessage = this.responseText;
					newMessage = newMessage.replace(oldResponseMessage, "");
					
					this.ClientServerHandler.onMessage( newMessage );
					oldResponseMessage = this.responseText;
				}
			}else if (this.readyState == 4){
				if (this.status == 200){

					//this.ClientServerOperationsHandler.onMessage("3/200/" + this.responseText.substring(this.responseText.lastIndexOf("|")));
					
					var newMessage = this.responseText;
					newMessage = newMessage.replace(oldResponseMessage, "");
					this.ClientServerHandler.onMessage( newMessage );
					//alert(" Response 4 -200: "+ newMessage );
					//the connection is now closed.
					//start again - we were just disconnected!
					this.ClientServerHandler.createConnection();
					
					oldResponseMessage = this.responseText;
					

				}else{
					this.ClientServerHandler.onMessage("4/" + this.status + "/-");
					
				}
			}
			this.responseText = "";
		}catch(e){
			alert(e);
		}
	};
}



function onMessageReceive(model){
	//format of chat message is so split it as chatmessage#from#to#message=message
	var messages = model.split("#");
	
	switch(messages[0]){
		/*
		case CHATMESSAGE: 
						  var messageFrom =messages[1];
						  renderChatDiv(messageFrom);
						  var message =messages[3].split("=")[1];//get message
						  message = messageFrom +":"+message;
						  var oldMessages = $("#"+messageFrom).find("#chatHistory").val();
						  $("#"+messageFrom).find("#chatHistory").val(oldMessages  +""+message);
						  
						  if(! $( "#"+messageFrom).is(":visible")  ){
								$( "#"+messageFrom).show();
						  }
						  
						  //set scroller down ofchatHistory text area
						  var $charHistory = $("#"+messageFrom).find("#chatHistory");
						  $charHistory .scrollTop($charHistory.prop("scrollHeight"));
						  
						  blinkChatBox($("#"+messageFrom));

						  newTitle =messageFrom+ " says "+message;
						  
						  if(window_focused == false){
							  interval = setInterval(changeTitle, 1000);
						  }
						  
						  
						  
		break;
		*/
		case "NEWUSER" :
					  $(".listOfUsers").append("<li id='"+messages[1]+"'>"+messages[1]+"</li>");
					  $(".groupChatHistory").append(messages[1]+" has joined the roost. \n");
					  //;("Added New User is: "+messages[1]);
					  	
					  //setSelectFriendEvent();
		break;
		case "GROUPCHATMESSAGE":
			  var messageFrom =messages[1];
			  var message =messages[2].split("=")[1];//get message
			  message = messageFrom +": "+message+"\n";
			 $(".groupChatHistory").append(message);
			 var $groupChatHistory = $(".groupChatHistory");
			 $groupChatHistory.scrollTop($groupChatHistory.prop("scrollHeight"));
			 if(window_focus==false){
			 	if(!isNotify){
			 		toggleNotify();
			 	}
			 }
			break;
		case "ERRORMESSAGE":
			 var errorMessage =messages[1];
			alert(errorMessage);
			location.reload();
			break;

		case "LOGEDOUT" :
			if(!messages[1]==""){
			$(".groupChatHistory").append(messages[1]+" has flown away.\n");
			 var $groupChatHistory = $(".groupChatHistory");
			 $groupChatHistory.scrollTop($groupChatHistory.prop("scrollHeight"));
			}
			$("#"+messages[1]).remove();
			break;
	}
		
}
var interval = null;
function toggleNotify(){
	var bol = false;
	function switchTitle(){
		var title1 = "New Chirp!";
		var title2 = "(!) Cockadoodle";
		document.title= bol ? title1 : title2;
		bol = !bol;
	}
	if(isNotify){
		clearInterval(interval);
		document.title = "Cockadoodle";
		isNotify=false;
	}
	else {
		interval = setInterval(switchTitle,1000);
		isNotify=true;
	}
}

function setCookie(name, value){
	document.cookie=name+"="+value;
}

function isCookie(name){
	var cookies = document.cookie.split("; ");
	for (var i = cookies.length - 1; i >= 0; i--) {
		var id = cookies[i].split("=");
		if(id[0]==name){
			return true;
			break;
		}
	};
	return false;
}

function getCookie(name){
	var cookies = document.cookies.split("; ");
	for (var i = cookies.length - 1; i >= 0; i--) {
		var id = cookies[i].split("=");
		if(id[0]==name){
			return id[1];
			break;
		}
	};
	return false;
}

function deleteCookie(name){
	document.cookie=name+"=; expires=Thu, 01 Jan 1970 00:00:00 UTC";
}
