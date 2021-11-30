/**
 * GDELT_GA_SEARCH: GDELT Genetic Algorithm Search Tool
 *  
 * Copyright (C) 2021  John T. Murphy
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * This code was authored by John T. Murphy with contributions from Awrad Ali,
 * Harleen Lappano, and Lindsey Andrade
 * 
 * If you use this code or the tool in your work, please cite using the following bibtex:
 * @book{murphyAndWadsworth2021,
 *   author =       {Murphy, John T., and Wadsworth, Marin},
 *   title =        {GDELT GA Search Users Manual},
 *   year =         {2021},
 *   url =          {http://USER_MANUAL_URL}
 * }
 */
package gdelt.server;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import com.google.gson.Gson;

import gdelt.runners.GDELT_Query_Task;
import gdelt.utils.IO;

public class GDELT_Remote_Service extends GDELT_Service{
	
	ZContext   context = null;
	ZMQ.Socket socket  = null;
	Gson       queryTaskGsonBuilder = GDELT_Query_Task.getGson();
	boolean    useDateTimeAdded = false;
    
	public GDELT_Remote_Service(String URL) {
		try (ZContext context = new ZContext()){
        	// Socket to talk to client
            ZMQ.Socket socket = context.createSocket(ZMQ.REQ);
            socket.connect("tcp://localhost:5555");    
		}
        catch(Exception E) {
        	System.out.println("CLIENT: " + E.getMessage());
        	E.printStackTrace();
        }
	}
	

	public String processQueryTask(GDELT_Query_Task task) {
		try{
        	String toSend1 = queryTaskGsonBuilder.toJson(task);
	    	System.out.println("CLIENT Sent: " + toSend1);
		    socket.send(toSend1.getBytes(ZMQ.CHARSET), 0);
		    byte[] reply1 = socket.recv(0);
		    System.out.println("CLIENT Received: " + IO.LS + new String(reply1, ZMQ.CHARSET));
	
		    // Received the task back; rebuild it from the response, including the query results
		    new String(reply1, ZMQ.CHARSET);
        }
        catch(Exception E) {
        	System.out.println("CLIENT: " + E.getMessage());
        	E.printStackTrace();
        }
		return null; // If there was an exception, return nothing.
	}
	
}
