/*
This servlet handles sending the messages to all the people that need it sent to.

*/
package net.cockadoodle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.cockadoodle.Client;

@WebServlet(name="MessageSender", urlPatterns={"/publish"}, asyncSupported=true)
public class MessageSender extends HttpServlet{
	
	private static final long serialVersionUID=1L;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
		//This servlet is supposed to just spawn a new thread that deals with all the aCtx of the clients, and when there is a message sends it to them.
		// This part works alongside with the ajax requests to do the stuff.
		final String msg = request.getParameter("message");
		final String channel = request.getParameter("channel");
		
		System.out.println("Publisher : Request received on channel ["+channel+"].");
		//Here we copy the map locally so that we don't intefer with the login of the other potential people.
		//Publishing is relatively long, so we don't want to waste that time.
		
		ServletContext appScope = request.getServletContext();
		@SuppressWarnings("unchecked")
		final Map<String, List<Client>> clients = (Map<String, List<Client>>) appScope.getAttribute("CLIENTS");
		final List<Client> subscribers = new ArrayList<Client>(clients.get(channel));
		
		//we are going to hand off the longer running work off to another thread.
		// this means that the whole processing and loop iteration goes through a different thread.
		final AsyncContext publisherAsyncCtx = request.startAsync();
		
		//Here is the logic for publishing: it will be passed onto the container so that it runs whenever it has a free thread.
		Runnable r = new Runnable(){
			public void run(){
				long startTime = System.currentTimeMillis();
				
				String[] uNameMsg = msg.split("#");
				//split the outgoing message so that we can process its delivery properly
				
				if(uNameMsg.length<=2){
					return;
				}
				
				String msgType=uNameMsg[0];
				String msgFromUser = uNameMsg[1];
				
				//chatmessage#from#to#message=message
				
				List<Client> toRemove = new ArrayList<Client>();
				
				//Now we iterate through the loop of all the users.
				for(Client s: subscribers){
					//more logic is required for the user to user type of thing.
					if (msgType.equals("GROUPCHATMESSAGE")){
						if(s.getUserName().equals(msgFromUser)){
							continue;
						}
						synchronized(s){
							AsyncContext aCtx = s.getaCtx();
							try{
								aCtx.getResponse().getOutputStream().print(msg);
								aCtx.getResponse().flushBuffer();
							}catch(Exception e){
								System.err.println("Publisher : failed to send to client - removing from the list of subscribers on this channel");
								e.printStackTrace();
								toRemove.add(s);
							}
						}
					}
				}
				//here we remove the failed subscribers
				synchronized(clients){
					clients.get(channel).removeAll(toRemove);
				}
				
				//log success
				long ms = System.currentTimeMillis() - startTime;
				System.out.println("Publisher : The message was successfully published on channel ["+channel+"]");
				
				//aknowledge to the publishing client that we have finished.
					publisherAsyncCtx.complete(); //we are done, the connection can be closed now
			}
		};
		publisherAsyncCtx.start(r);
	}
}