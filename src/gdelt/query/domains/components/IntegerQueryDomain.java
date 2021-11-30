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
package gdelt.query.domains.components;

public class IntegerQueryDomain extends AbstractQueryDomain<Integer>{

	public int domainMin = Integer.MIN_VALUE;
	public int domainMax = Integer.MAX_VALUE;
	
	public int min = domainMin;
	public int max = domainMax;
	
	public IntegerQueryDomain(String name, int minimumPossibleVal, int maximumPossibleVal, AbstractQueryDomain.INIT_METHOD initMethod) {
		super(name);
		domainMin = minimumPossibleVal;
		domainMax = maximumPossibleVal;
		init(initMethod);
	}
	
	public void initToFullRange() {
		min = domainMin;
		max = domainMax;
	}
	
	public void initToRandomRange() {
		long countOfPossible = domainMax - domainMin + 1;
		long index1 = (long)(Math.floor(Math.random() * (double)countOfPossible));
		long index2 = (long)(Math.floor(Math.random() * (double)countOfPossible));
		if(index2 < index1) {
			long t = index2;
			index2 = index1;
			index1 = t;
		}
		min = (int)(domainMin + index1);
		max = (int)(domainMin + index2);
	}
	
	// Returns  true if the values were set exactly as specified;
	// Returns false if the values passed were invalid or
	// if either value was outside the boundaries. In the latter
	// case, the value would be set to the boundary, but the return
	// flag is still false.
	public boolean setValues(int newMin, int newMax) {
		if(newMin > newMax) return false; // Do nothing if the input is nonsense
		boolean ret = true;
		min = newMin;
		if(min < domainMin) {
			min = domainMin;
			ret = false;
		}
		max = newMax;
		if(max > domainMax) {
			max = domainMax;
			ret = false;
		}
		return ret;
	}
	
	public boolean canReduce() {
		return min != max;
	}
	
	// Here we need to make a decision:
	// What magnitude reduction should be applied?
	public boolean reduce() {
		if(!canReduce()) return false; // Already as small as it can be
		boolean raiseFloor = Math.random() < .5; // Flip coin to see if we raise the floor or lower the ceiling
		if(raiseFloor) min++;
		else           max--;
		if(Math.random() < .2) reduce(); // A chance you'll do more; note: still return true
		return true;
	}
	
	public boolean canExpand() {
		return !(min == domainMin && max == domainMax);
	}
	
	public boolean expand() {
		if(!canExpand()) return false;
		if     (min == domainMin) max++;
		else if(max == domainMax) min--;
		else if(Math.random() < .5) min--;
		else max++;
		if(Math.random() < .2) expand(); // A chance you'll do more; note: still return true
		return true;
	}
	
	public AbstractQueryDomain<Integer> split() {
		return this;
	}
	
	public boolean matches(Integer comparand) {
		int val = comparand.intValue();
		//System.out.println("MIN: " + min + " COMP: " + val + " MAX" + max);
		boolean match = 
			(min == max ) ? // If the min and max values are the same, then a matching value matches
					(val == min) :			
			        (max < domainMax) ?	   // Typically this will be true
					      ((val >= min) && (val < max)) : // Standard, exclusive of max value
					      ((val >= min) && (val <= max)); // If max is at ceiling, including it
		//System.out.println("MATCH IS: " + match);
		return includeMatches ? match : !match;
	}
	
	public String toString() {
		return getName() + "[" + min + "," + max + (max == domainMax ? "]" : ")");
	}

	
}
