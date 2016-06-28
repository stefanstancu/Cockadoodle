package net.cockadoodle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.cockadoodle.Client;

@WebServlet(name = "onlineUsersServlet", urlPatterns = { "/getOnlineUsersList" })
public class ListOfOnlineUsers extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		String commaSepeparatedStr ="";
		ServletContext appScope = request.getServletContext();
		String channel = request.getParameter("channel");
		System.out.println("UserList : Request received for channel ["+channel+"]");
		@SuppressWarnings("unchecked")
		Map<String, List<Client>> clients = (Map<String, List<Client>>) appScope.getAttribute("CLIENTS");
		System.out.println("UserList : The current array is : "+clients);
		if(clients.size()> 0){
			 List<Client> onlineClients = (List<Client>)clients.get(channel);
			if(onlineClients !=null){
				for (Client client : onlineClients) {
					if(commaSepeparatedStr.equals("") ){
						commaSepeparatedStr = client.getUserName();
					}else{
						commaSepeparatedStr =commaSepeparatedStr+","+ client.getUserName();
					
					}
				}
			}
		}
		response.setContentType("text");
		response.getWriter().write(commaSepeparatedStr);
		System.out.println("UserList : Printed "+ commaSepeparatedStr);
		response.flushBuffer(); 

	}

}