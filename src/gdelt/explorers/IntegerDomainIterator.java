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

import gdelt.query.GDELT_Query;

public abstract class IntegerDomainIterator extends QueryIterator {
	
	private GDELT_Query query;

	protected int min = 1;
	protected int max = 0;
	protected int overallMax;
	protected int totalIterations = 0;
	
	public IntegerDomainIterator(int minVal, int maxVal, GDELT_Query queryToIterate) {
		query = queryToIterate;
		min = minVal;
		max = minVal - 1; // Has to be at init
		overallMax = maxVal;
		for(int i = minVal; i<=overallMax; i++) totalIterations+= (overallMax - i + 1);
		query.rootCode.setValues(0, 0);
	}
	
	protected boolean increment() {
		max++;
		if(max > overallMax) {
			min++;
			if (min > overallMax) return false;
			max = min;
		}
		return true;
	}
	
	@Override
	public int countOfTotalIterations() {
		return totalIterations;
	}
	
	
	@Override
	protected GDELT_Query nextQuery() {
		return increment() && setVals(query) ? query : null;
	}
	
	// This should set the values in the query and return true if successful, false if not
	public abstract boolean setVals(GDELT_Query query);

}
