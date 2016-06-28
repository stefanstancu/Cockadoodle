/*
This is the login page.
This is my first attempt at the application, it will be heavily based on the application that I saw on the internet.
Hopefully I get to learn as much as I can from this experience and progress.
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

@WebServlet(name="Login", urlPatterns={"/login"}, asyncSupported=true)
public class Login extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		
		String channel = request.getParameter("channel");
		String userName = request.getParameter("userName");
		ServletContext appScope = request.getServletContext();
		System.out.println("Login : Request received from ["+userName+"] on channel ["+channel+"]");
		
		@SuppressWarnings("unchecked")
		Map<String, List<Client>> channelsClientsMap = (Map<String, List<Client>>) appScope.getAttribute("CLIENTS");
		
		if(UserAlreadyExists(channelsClientsMap,userName,channel)){
			//reroute them to the login page
			//response.getWriter().write("Nope, the name is used");
			response.setHeader("LOGIN_FAILED","1");
			response.flushBuffer();
			System.out.println("Login : Failed attempt -username["+userName+"] on channel ["+channel+"]already in use");
			return;
		}
		
		//Time to start a new AsyncContext for the client so that we can pass it on to another thread, etc.
		//The comet programing part is here essentially, it revolves around this thing. Its pretty cool actually.
		final AsyncContext aCtx = request.startAsync(request, response);
		
		//Then we set the timeout of the thing.
		//This is learning, so for now just leave it was never ending so that we don't have to deal with resending it evertime to create longpolling.
		//However, we can add that in later to make it safer? Idk, but I think I should include it;
		aCtx.setTimeout(0);
		
		//Create the client object that we can add.
		Client client = new Client(aCtx,channel);
		if(userName==null||userName.equals("")){
			userName="Anon";
		}
		
		client.setUserName(userName);
		
		//Set the headers of the response.
		response.setHeader("Content-Type","application/x-javascript");
		response.setHeader("Connection","keep-alive");
		
		aCtx.addListener(new AsyncListener("login", channelsClientsMap, channel, client));
		//^ notice how we pass these parameters, and that's to be able to remove the user in case of some sort of failure.
		
		//now that we created the user, we add them to the map.
		synchronized (channelsClientsMap){
			List<Client> clients = channelsClientsMap.get(channel);
			if(clients==null){
				//if this dude is the first one to the channel.
				clients = Collections.synchronizedList(new ArrayList<Client>());
				channelsClientsMap.put(channel, clients);
				System.out.println("Login : Map created.");
			}
			//if they are not the first
			clients.add(client);
			System.out.println("Login : new user created ["+userName+"] on channel ["+channel+"]");
		}
		
		AnnounceNewUser(channelsClientsMap,userName,channel);
		System.out.println("Login : Request from ["+userName+"] on channel ["+channel+"] completed.");
		
	}
	
	private boolean UserAlreadyExists(Map<String, List<Client>> map, String name, String channel){
		final List<Client> clients = map.get(channel);
		//assign a local list of the userlist for this particular channel.
		
		if(clients == null){
			return false;
		}
		//If there is no one else there, then obviously the username is ok.
		for(Client s : clients){
			if(s.getUserName().equals(name)){
				return true;
			}
		}
		//check against all the other clients.
		return false;
		//if nothing was found, then its unique, return false.
	}
	
	public void AnnounceNewUser(Map<String, List<Client>> map,String name,String channel){
		final List<Client> clients = map.get(channel);
		//we also make a list of users to remove from the list if they leave or whatever
		List <Client> toRemove = new ArrayList<Client>();
		
		for(Client s : clients){
			if(s.getUserName().equals(name)){
				continue;
			}
			synchronized(s){
				AsyncContext aCtx = s.getaCtx();
				try{
					aCtx.getResponse().getOutputStream().print("NEWUSER#"+name);
					aCtx.getResponse().flushBuffer();//so the client gets it now
					System.out.println("Login : Printed [NEWUSER#"+name+"] to ["+s.getUserName()+"]");
				} catch(Exception e){
					System.err.println("Login : Failed to send to ["+s.getUserName()+"] - removing from the list of clients");
					e.printStackTrace();
					toRemove.add(s);
				}
			}
		}
		
		//remove the failed clients now
		synchronized (clients){
			clients.removeAll(toRemove);
		}
	}
	
}