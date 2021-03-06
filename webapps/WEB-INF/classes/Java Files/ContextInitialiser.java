/*
This is the context initialiser.
It creates the instance of the Map first, and sets it in the context of the container.
I think.
*/
package net.cockadoodle.listeners;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import net.cockadoodle.Client;

@WebListener
public class ContextInitialiser implements ServletContextListener {
	
	public void contextInitialized(ServletContextEvent sce){
		
		ServletContext appScope = sce.getServletContext();
		//list of online clients in the application.
		final Map<String, List<Client>> onlineClients = Collections.synchronizedMap(new HashMap<String, List<Client>>());
		appScope.setAttribute("CLIENTS", onlineClients);
	}
	
	public void contextDestroyed(ServletContextEvent sce){
		
		
	}
}
