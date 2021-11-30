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

public abstract class AbstractQueryDomain<T>{
	
	public enum INIT_METHOD{
		FULL,
		RANDOM
	}
	
	protected boolean includeMatches = true;
	private final String name;
	
	public AbstractQueryDomain(String n){
		name = n;
	}
	
	public void init(INIT_METHOD initMethod) {
		switch(initMethod) {
			case FULL:{
				initToFullRange();
				break;
			}
			case RANDOM:{
				initToRandomRange();
				break;
			}
		}
	}
	
	public void invert() {
		includeMatches = !includeMatches;
	}
	
	public abstract void initToFullRange();
	public abstract void initToRandomRange();
	
	
	// These should return true if the values were changed, false if not
	
	public boolean mutate() {
		return mutate(.5);
	} 
	
	public boolean mutate(double expandBias) {
		return
			canReduce() ? 
				canExpand() ?
					Math.random() < expandBias ? expand() : reduce() :
					reduce() :
				canExpand() ? expand() : false;
	}
	
	
	public abstract boolean canReduce();
	public abstract boolean canExpand();
	public abstract boolean reduce();
	public abstract boolean expand();

	public abstract boolean matches(T comparand);
	
	public String getName() {
		return name;
	}
	
}
