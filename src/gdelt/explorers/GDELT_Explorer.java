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
package gdelt.explorers;

import java.util.ArrayList;
import java.util.List;

import gdelt.query.GDELT_Query;
import gdelt.scorers.Scorer;
import gdelt.server.GDELT_Service;
import gdelt.utils.TimeSeries;

public abstract class GDELT_Explorer {
	
	public static class TestCriteria{
		String               name;
		TimeSeries           groundTruth;
		List<List<Integer>> values = new ArrayList<List<Integer>>(); // Note: List will be list of lists where each list corresponds to a date, 
		                                                             // the first entry is the gt value and the rest are the best values for that date
		
		public TestCriteria(String n, TimeSeries gt) {
			name = n;
			groundTruth = gt;
		}
	}
	
	public List<TestCriteria> testCriteria = new ArrayList<TestCriteria>();
	
	protected GDELT_Query         startQuery;
	protected Scorer              scorer;
	
	public    Scorer              gtScorer = null;
	
	public    GDELT_Service       service;
	public    boolean             useDateTimeAdded; // That is, use this instead of the date and time of the event
	
	public GDELT_Explorer(GDELT_Service serviceToUse) {
		service = serviceToUse;
	}
	
	
	public GDELT_Explorer(GDELT_Service serviceToUse, Scorer scorerToUse, GDELT_Query initialQuery) {
		service = serviceToUse;
		scorer = scorerToUse;
		startQuery = initialQuery;
	}

	public void reset(Scorer scorerToUse, GDELT_Query initialQuery) {
		scorer = scorerToUse;
		startQuery = initialQuery;
	}
	
	public String execute() {
		return execute("Unnamed");
	}
	
	public abstract String execute(String name);
	
}
