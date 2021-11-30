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

import com.google.gson.Gson;

import gdelt.runners.GDELT_Query_Task;
import gdelt.server.elements.GDELT_Corpus;
import gdelt.utils.IO;

public class GDELT_Local_Service extends GDELT_Service{
	
	GDELT_Corpus all                  = null;
	Gson         queryTaskGsonBuilder = GDELT_Query_Task.getGson();

	// The optional 'adjustDateAndTimeAdded' flag can be used to force the
	// Corpus load routine to modify the date and time added to be
	// on the same day as the event time
	
	public GDELT_Local_Service(String corpusFileName) {
	  this(corpusFileName, null);
	}
	
	public GDELT_Local_Service(String corpusFileName, String mentionsFileName) {
		all = GDELT_Corpus.get(corpusFileName, mentionsFileName);
		IO.log(IO.LEVEL.LEVEL_1, "Corpus size: " + all.allEvents.size());
	}
	
	public String processQueryTask(GDELT_Query_Task task) {
		task.evaluateCorpus(all);
		return queryTaskGsonBuilder.toJson(task);
	}
	
}
