/*
	Contains methods and functions and stuff.
	Its like the first layer to all the backend type of stuff that happens, like mapping all the buttons to stuff.
	The ClientServerHandler is used to call functions that do other things, like ajax requests, and actually processing the messages
*/

var clientServerOperations;

$(function(){

	$("#login").click(function(){
		loginUser();

	});
	//Here for example, the key input is being pointed to a much more complex function too keep this code here neat.
	
	$("#userName").keyup(function(event){
		if(event.keyCode =='13'){
			loginUser();
		}
	});
	//keyup is the equivalent of keylistener.
	
	$(".groupChatBox").keyup(function(event){
		if(event.keyCode=='13'){
			$(".groupChatBox").val(cleanString($(".groupChatBox").val()));
			$(".groupChatHistory").append($("#userName").val()+": "+$(this).val()+"\n");
			
			var $groupChatHistory = $(".groupChatHistory");
			$groupChatHistory.scrollTop($groupChatHistory.prop("scrollHeight"));
			
			var from=$('#userName').val();
			var message = "message="+$(this).val();
			var chatMessage = "GROUPCHATMESSAGE"+"#"+from+"#"+message;
			// In these lines, we just constructed the message.
			
			clientServerOperations.sendMessage(chatMessage);
			//Now we call a clientServerHandler to send the message to the server for us for processing.
			
			$(this).val('');
			//reset the textarea to null when done
		}
	});
});

function loginUser(){
	if($("#userName").val()==""){
		return;
	}
	$("#userName").val(cleanString($("#userName").val()));
	clientServerOperations = new ClientServerHandler("default",onMessageReceive);
	clientServerOperations.createConnection();
	
	$(".loginDiv").hide();
	$(".chatArea").show();
	var loginUserDetails = "<div class='buttondiv'> <button class='logoutbtn' id='logout'>Logout</button> <div class='welcomemsg'>Welcome "+$("#userName").val()+"</div></div>";
	$(".loginDetails").html(loginUserDetails);
	var height = 500;
	if(isCookie("height")){
		height = getCookie("height");
	}
	$(".groupChatHistory").height(height);
	
	$("#logout").click(function(){
		logout();
	});
	populateUsers();
}
//Here we would normally set the onclick even for the people on the side. But we don't want that right now.
//Then we would normally generate the chat divs for the individual popups. But none of that today.

//When the window is closed do this
$(window).on('beforeunload',function(){
	logout();
});

//Set the cookie of the desired height value, doesn't work yet


function logout(){
	var param =  "userName="+$("#userName").val()+'&channel=default';
	jQuery.ajax({url:"/cockadoodle/logout?"+param, async:true });
	//location.reload();
	$(".loginDiv").show();
	$(".chatArea").hide();
	$(".loginDetails").html("");
	$(".groupChatHistory").val("");
}

function populateUsers(){
	$(".listOfUsers").empty();
	var requestParams = {
		channel:'default'
	};
	
	//below param method takes catre of special characters in the data
	requestParams=jQuery.param(requestParams);
	//ajax to get comma separated list of online users
	$.ajax({
		type:'GET',
		url:"/cockadoodle/getOnlineUsersList",
		cache:false,
		async:true,
		data:requestParams,
		
		complete: function(jqXHR, textStatus){
			response = jqXHR.responseText;
			if(response==""){
			}
			else{
				var users = response.split(",");
				
				for(var i=0; i<users.length; i++){//loop through the users, which is an array, and add them all the list.
					if(users[i]!=$(".userName").val()){
						$(".listOfUsers").append("<li id='"+users[i]+"'>"+users[i]+"</li>");
						$(".listOfUsers").addClass("borderBottom");
					}
				}
			}
			},
		error: function(jqXHR, textStatus, errorThrown){console.log("ERROR: "+textStatus, errorThrown);}
		
	});
}
//Cleans the input so that there are no illegal characters, and if there are, reports an error.
function cleanString(input){
	return input.replace(/#|&|=/g,"");
}

//These are jQuery Plugins.

jQuery.fn.visible = function() {
    return this.css('visibility', 'visible');
};

jQuery.fn.invisible = function() {
    return this.css('visibility', 'hidden');
};