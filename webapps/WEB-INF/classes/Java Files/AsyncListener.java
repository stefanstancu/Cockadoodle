/*
This is a listener that  we attach to the aCtx.
That means that it will do stuff when the context does various things.
*/
package net.cockadoodle.listeners;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.AsyncEvent;
import net.cockadoodle.Client;

public class AsyncListener implements javax.servlet.AsyncListener {
	
	private final String name;
	private final Map<String, List<Client>> map;
	private final String channel;
	private final Client client;
	
	public AsyncListener(String name, Map<String, List<Client>> map, String channel, Client client){
		this.name = name;
		this.map = map;
		this.channel=channel;
		this.client=client;		
	}
	
	public void onComplete(AsyncEvent event) throws IOException{
		removeFromModel();
		System.out.println("onComplete for "+ client.getUserName());
		if(event.getThrowable()!=null){
			event.getThrowable().printStackTrace();
		}
	}
	
	public void onTimeout(AsyncEvent event) throws IOException{
		removeFromModel();
		System.out.println("onTimeout for "+ client.getUserName());
		if(event.getThrowable()!=null){
			event.getThrowable().printStackTrace();
		}
	}
	
	public void onError(AsyncEvent event) throws IOException{
		removeFromModel();
		System.out.println("onError for "+ client.getUserName());
		if(event.getThrowable()!=null){
			event.getThrowable().printStackTrace();
		}
	}
	
	public void onStartAsync(AsyncEvent event) throws IOException{
		System.out.println("onTimeout for "+ client.getUserName());
		if(event.getThrowable()!=null){
			event.getThrowable().printStackTrace();
		}
	}
	
	public void removeFromModel(){
		System.out.println("Removed Client"+client.getUserName());
		map.get(channel).remove(client);
	}
}

