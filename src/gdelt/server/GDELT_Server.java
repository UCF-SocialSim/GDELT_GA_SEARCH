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

import java.io.StringWriter;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import gdelt.runners.GDELT_Query_Task;
import gdelt.utils.IO;

public class GDELT_Server {

	GDELT_Local_Service service  = null;

	public GDELT_Server() {
		service = new GDELT_Local_Service("./data/cp4.venezuela.gdelt.v2_rev.json", "./data/GDELT_RAW/GDELT_Mentions_Data.csv");
	}
	
	public void start() {
		IO.log("Waiting for contact....");		

		try (ZContext context = new ZContext()){
            ZMQ.Socket socket = context.createSocket(ZMQ.REP);
            socket.bind("tcp://*:5555");
            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                String recd = new String(reply, ZMQ.CHARSET);

                if(recd.compareToIgnoreCase("PING") == 0) {
                	IO.log("Received ping.");
                	StringWriter sw = new StringWriter();
	                sw.write("PONG" + IO.LS);
	                String response = sw.toString();
	                socket.send(response.getBytes(ZMQ.CHARSET), 0);
                }
                else if(recd.compareToIgnoreCase("STOP") == 0) {
                	IO.log("Received command to stop server.");
                	return;
                }
                else {
	                IO.log("SERVER Received: [" + recd + "]");
	                GDELT_Query_Task queryTask = GDELT_Query_Task.get(recd);
	                service.processQueryTask(queryTask);
	                String toSend = queryTask.getJson();
		    		IO.log("SERVER Sending: " + toSend);
			    	socket.send(toSend.getBytes(ZMQ.CHARSET), 0);
                }
                Thread.sleep(1000);
            }
        }
		catch(Exception E) {
			System.out.println("SERVER:" + E.getMessage());
			E.printStackTrace();
		}	
	}

	
	public static void main(String[] args) {
		GDELT_Server server = new GDELT_Server();
		server.start();
		System.out.println("Done.");
	}
}
