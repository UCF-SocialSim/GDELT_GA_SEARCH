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
import gdelt.scorers.results.SmartScore;

public abstract class QueryIterator {

	public String label = "";
	
	protected int numIterationsToAllow = -1;
	protected int currentIteration = 0;
	protected int lastNotableIteration = 0;

	public final void setLimit(int numIterations) {
		numIterationsToAllow = numIterations;
	}
	
	public final void resetCurrentIteration() {
		setCurrentIteration(0);
	}
	
	public final void setCurrentIteration(int newVal) {
		currentIteration = newVal;
	}
	
	// The main requirement of this interface is that
	// it should return true if it changes the query,
	// and false if not
	public final GDELT_Query next() {
		currentIteration++;
		if(numIterationsToAllow >= 0 && currentIteration >= numIterationsToAllow) return null;
		return nextQuery();
	}

	
	protected abstract GDELT_Query nextQuery();
	public void recordScore(SmartScore score) {}
	
	// One other requirement: must return the total number of iterations
	public abstract int countOfTotalIterations();

	
	// Skip n forward; returns true so long as the last one skipped
	// was not null
	public boolean advance(int num) {
		boolean ret = true;
		for(int i = 0; i < num; i++) ret = (next() != null);
		return ret;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setNotable() {
		lastNotableIteration = currentIteration;
	}
	
	public int sinceLastNotable() {
		return currentIteration - lastNotableIteration;
	}
}
