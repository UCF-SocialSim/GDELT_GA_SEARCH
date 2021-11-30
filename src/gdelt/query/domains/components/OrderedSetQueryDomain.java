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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This domain represents a set that has an order; adding and removing values
 * is done by expanding or reducing the range of the set left or right (as
 * opposed to randomly removing or adding elements).
 *
 * @param <T>
 */
public class OrderedSetQueryDomain<T> extends AbstractQueryDomain<T> {
	
    int     minPosition;
    int     maxPosition;
    List<T> values       = new ArrayList<T>();
    Set<T>  matchValues  = new HashSet<T>();
    
    private void initMatches() {
    	matchValues.clear();
    	if(minPosition == maxPosition) matchValues.add(values.get(minPosition));
    	else{
    		for(int i = minPosition; i < maxPosition; i++) matchValues.add(values.get(i));
    	}
    }
	
	public OrderedSetQueryDomain(String name, AbstractQueryDomain.INIT_METHOD initMethod, T ... valuesToUse){
		super(name);
		for(T value: valuesToUse) values.add(value);
		minPosition = 0;
		maxPosition = valuesToUse.length;
		init(initMethod);
	}
	
	@Override
	public void initToFullRange() {
		minPosition = 0;
		maxPosition = values.size();
		initMatches();
	}

	@Override
	public void initToRandomRange() {
		if(values.size() == 0) {
			minPosition = 0;
			maxPosition = 0;
		}
		else {
			// Note: must be able to include size, which is ceiling for max position
			int p1 = (int)(Math.floor(Math.random() * (values.size() + 1)));
			int p2 = (int)(Math.floor(Math.random() * (values.size() + 1)));
			if(p1 < p2) {
				minPosition = p1;
				maxPosition = p2;
			}
			else if(p2 < p1) {
				minPosition = p2;
				maxPosition = p1;
			}
			else { // The two values are equal
				if(p1 == values.size()) { // both are at ceiling, which is > 0 due to check above)
					minPosition = p1 - 1;
					maxPosition = p2;
				}
				else {
					minPosition = p1;
					maxPosition = p2 + 1;
				}
			}
		}
		initMatches();
	}

	/**
	 * Returns  true if the values were set exactly as specified;
	 * Returns false if the values passed were invalid or
	 * if either value was outside the boundaries. In the latter
	 * case, the value would be set to the boundary, but the return
	 * flag is still false.
	 * 
	 * @param newMin
	 * @param newMax
	 * @return
	 */
	public boolean setValues(int newMin, int newMax) {
		if(newMin > newMax) return false; // Do nothing if the input is nonsense
		boolean ret = true;
		minPosition = newMin;
		if(minPosition < 0) {
			minPosition = 0;
			ret = false;
		}
		maxPosition = newMax;
		if(maxPosition > values.size()) {
			maxPosition = values.size();
			ret = false;
		}
		initMatches();
		return ret;
	}
	
	
	@Override
	public boolean canReduce() {
		return (minPosition < maxPosition) && (minPosition != (values.size() - 1));
	}

	@Override
	public boolean canExpand() {
		return(! ((minPosition == 0) && (maxPosition == values.size()) ));
	}

	@Override
	public boolean reduce() {
		if(!canReduce()) return false;
		if(minPosition == 0) {
			maxPosition--;
		}
		else if(maxPosition == values.size()) {
			minPosition++; // Note: canReduce() prevents case where this is size - 1
		}
		else {
			if(Math.random() < .5) minPosition++;
			else                   maxPosition--;
		}
		initMatches();
		return true;
	}

	@Override
	public boolean expand() {
		if(!canExpand()) return false;
		if     (minPosition == 0)             maxPosition++;
		else if(maxPosition == values.size()) minPosition--;
		else {
			if(Math.random() < .5) minPosition--;
			else                   maxPosition++;
		}
		initMatches();
		return true;
	}

	@Override
	public boolean matches(T comparand) {
		return matchValues.contains(comparand);
	}
	
	@Override
	public String toString() {
		return getName() + "[" +values.get(minPosition) + "," + (maxPosition == values.size() ? values.get(maxPosition  - 1) + "]" : values.get(maxPosition) + ")");
	}

	
	/**
	 * Returns the values that are currently being used as matches
	 * @return
	 */
	public Set<T> getMatchingValues(){
		Set<T> ret = new TreeSet<T>();
		ret.addAll(matchValues);
		return ret;
	}
	
}
