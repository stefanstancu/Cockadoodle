/*  
 * 
 */
package net.cockadoodle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.cockadoodle.listeners.AsyncListener;
import net.cockadoodle.Client;

@WebServlet(name = "Logout", urlPatterns = { "/logout" }, asyncSupported = true)
public class Logout extends HttpServlet {

	public static final String CLIENTS = "ClientMap";

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{ 
		
		String channel = request.getParameter("channel");
		String userName = request.getParameter("userName");
		
		System.out.println("Logout : Request received from ["+userName+"] on channel ["+channel+"].");
		
		// get the application scope so that we can add our data to the model
		ServletContext appScope = request.getServletContext();

		// fetch the model from the app scope
		@SuppressWarnings("unchecked")
		Map<String, List<Client>> channelsClientsMap = (Map<String, List<Client>>) appScope.getAttribute("CLIENTS");
		
		List<Client> subscribers = channelsClientsMap.get(channel);
		synchronized (subscribers) {
			if(subscribers !=null ){
				for(Client s : subscribers){
					if(s.getUserName().equals(userName)){
						
						subscribers.remove(s);
						System.out.println("Logout : Removed user ["+userName+"] from the list of users.");
						break;
					}
		
				}
			}
		}
		
		tellOtherAboutLogoutUser(subscribers,userName);
		System.out.println("Logout : Request from ["+userName+"] on channel ["+channel+"] completed.");		
	}
	
	public void tellOtherAboutLogoutUser(List<Client> subscribers,String userName ){
		
		
		//List<Client> clients = channelsClientsMap.get(channel);
		if(subscribers ==null ){
			System.out.println("Logout : List was null");
			return;
		}
				List<Client> toRemove = new ArrayList<Client>();
				
				for(Client s : subscribers){
					
					if(s.getUserName().equals(userName)){
						continue;
					}
					synchronized (s) {
						AsyncContext aCtx = s.getaCtx();
						try {
							aCtx.getResponse().getOutputStream().print("LOGEDOUT#"+userName);
							aCtx.getResponse().flushBuffer(); //so the client gets it NOW
							System.out.println("Logout : Printed [LOGEDOUT#"+userName+"] to "+s.getUserName());
						} catch (Exception e) {
							
							System.err.println("Logout : failed to send to client - removing from list of subscribers on this channel");
							e.printStackTrace();
							toRemove.add(s);
						}
					}
				}
				
				// remove the failed subscribers from the model in app scope, not our copy of them
				synchronized (subscribers) {
					subscribers.removeAll(toRemove);
					
				}

				
			}
	
}
