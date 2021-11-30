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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a set of potential match values that have
 * no intrinsic ordering, so elements can be added or
 * removed randomly
 *
 * @param <T>
 */
public class UnorderedSetQueryDomain<T> extends AbstractQueryDomain<T>{
	
	List<T> domainValues     = new ArrayList<T>(); // Note: Keeping these in an ordered list is just to help with random selection
	List<T> valuesToCompare  = new ArrayList<T>();
	List<T> otherValues      = new ArrayList<T>();
	
	
	public UnorderedSetQueryDomain(String name, INIT_METHOD init_method, T ... values) {
		super(name);
		for(T t: values) domainValues.add(t);
		init(init_method);
	}
	
	@Override
	public void initToFullRange() {
		valuesToCompare.addAll(domainValues);
		otherValues.clear();
	}

	@Override
	public void initToRandomRange() {
		valuesToCompare.clear();
		otherValues.clear();
		while(valuesToCompare.size() == 0) { // Can't be left empty
			for(T t: domainValues) {
				if(Math.random() > 0.5) valuesToCompare.add(t);
				else                    otherValues.add(t);
			}
		}
	}

	@Override
	public boolean canReduce() {
		return valuesToCompare.size() > 1; // Could remove one with at least one left
	}

	@Override
	public boolean canExpand() {
		return otherValues.size() > 0;
	}

	@Override
	public boolean reduce() {
		if(!canReduce()) return false;
		otherValues.add(valuesToCompare.remove((int)(Math.floor(Math.random() * valuesToCompare.size()))));
		return true;
	}

	@Override
	public boolean expand() {
		if(!canExpand()) return false;
		valuesToCompare.add(otherValues.remove((int)(Math.floor(Math.random() * otherValues.size()))));
		return false;
	}

	@Override
	public boolean matches(T comparand) {
		return valuesToCompare.contains(comparand);
	}
	
	public boolean set(Set<T> values) {
		Set<T> temp = new HashSet<T>();
		for(T t: values) if(domainValues.contains(t)) temp.add(t);
		if(temp.size() == 0) return false; // No change
		otherValues.clear();
		valuesToCompare.clear();
		for(T t : domainValues) {
			if(values.contains(t)) valuesToCompare.add(t);
			else                   otherValues.add(t);
		}
		return true;
	}
	
	public String toString() {
		StringWriter sw = new StringWriter();
		sw.write(getName());
		sw.write("[");
		String sep ="";
		for(T t: valuesToCompare) {
			sw.write(sep + t.toString());
			sep = ",";
		}
		sw.write("]");
		return sw.toString();
	}
	
	/**
	 * Returns the set of all values that are being used
	 * for matches
	 * @return
	 */
	public Set<T> getMatchingValues(){
		Set<T> ret = new HashSet<T>();
		ret.addAll(valuesToCompare);
		return ret;
	}

}
