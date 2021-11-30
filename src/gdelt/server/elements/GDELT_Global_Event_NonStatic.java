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
package gdelt.server.elements;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class GDELT_Global_Event_NonStatic extends GDELT_Global_Event{

	public Map<Date, GDELT_Global_Event> representations = new TreeMap<Date, GDELT_Global_Event>();
		
	public GDELT_Global_Event getAsOf(Date asof) {
		// If dateAndTimeAdded is after 'asof', return null
		// If asof is already in the map, return the representation in the map
		GDELT_Global_Event ret = representations.get(asof);
		if(ret != null) return ret;
		ret = this.copy(asof);
		representations.put(asof,  ret);
		return ret;
	}
	
	private GDELT_Global_Event copy(Date asof) {
		return this; // TODO
	}
	
}
