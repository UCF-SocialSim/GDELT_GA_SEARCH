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

/**
 * This class allows for values that must 'wrap'. The canonical
 * example is latitude, which ranges from -180 to 180,
 * but should be able to include ranges from 170 to -170
 * and correctly interpret this to mean that it starts at
 * 170 East and ends at 170 West, spanning 20 degrees.
 * 
 * This will be achieved by allowing the 'min' value to
 * move between the domain min and domain max; the 'max'
 * value will be able to range from domain min to
 * domain max + (domain max - domain min), but the difference
 * between the two should never exceed the longest
 * interval. If it
 * exceeds domain max, then it is assumed to 'wrap around'
 * to include intervals above domain min. 
 *
 */
public class IntegerWrappedQueryDomain extends AbstractQueryDomain<Integer>{

	public int domainMin = Integer.MIN_VALUE;
	public int domainMax = Integer.MAX_VALUE;
	
	public int rangeSize;
	
	public int min = domainMin;
	public int max = domainMax;
	
	public IntegerWrappedQueryDomain(String name, int minimumPossibleVal, int maximumPossibleVal, AbstractQueryDomain.INIT_METHOD initMethod) {
		super(name);
		domainMin = minimumPossibleVal;
		domainMax = maximumPossibleVal;
		rangeSize = domainMax - domainMin;
		init(initMethod);
	}

	@Override
	public void initToFullRange() {
		min = domainMin;
		max = domainMax;
	}

	@Override
	public void initToRandomRange() {
		long index1 = (long)(Math.floor(Math.random() * (double)rangeSize));
		long index2 = (long)(Math.floor(Math.random() * (double)rangeSize));
		min = (int)(domainMin + index1);
		max = (int)(min + index2);
	}

	@Override
	public boolean canReduce() {
		return max > min;
	}


	@Override
	public boolean reduce() {
		if(!canReduce()) return false; // Already as small as it can be
		boolean raiseFloor = Math.random() < .5; // Flip coin to see if we raise the floor or lower the ceiling
		if(raiseFloor) raiseFloor(1);
		else           max--;
		if(Math.random() < .2) reduce(); // A chance you'll do more; note: still return true
		return true;
	}
	

	private void raiseFloor(int amt) {
		min += amt;
		// Does this exceed the max value?
		if(min == domainMax) {
			min = domainMin;
			max -= rangeSize;
		}
	}
	

	@Override
	public boolean canExpand() {
		return (max - min) < rangeSize;
	}
	
	@Override
	public boolean expand() {
		if(!canExpand()) return false;
		if(Math.random() < .5) lowerFloor(1);
		else max++;
		if(Math.random() < .2) expand(); // A chance you'll do more; note: still return true
		return true;
	}

	private void lowerFloor(int amt) {
		min -= amt;
		// Does this go below the min value?
		if(min < domainMin) {
			min += rangeSize;
			max += rangeSize;
		}
	}
	
	@Override
	public boolean matches(Integer comparand) {
		int val = comparand.intValue();
		if(val < min) val += rangeSize;
		System.out.println("MIN: " + min + " COMP: " + val + " MAX " + max);
		boolean match = 
			(min == max ) ? // If the min and max values are the same, then a matching value matches
					(val == min) :			
			        (max < domainMax) ?	   // Typically this will be true
					      ((val >= min) && (val < max)) : // Standard, exclusive of max value
					      ((val >= min) && (val <= max)); // If max is at ceiling, including it
		//System.out.println("MATCH IS: " + match);
		return includeMatches ? match : !match;
	}
	
	public String showRange() {
		return min + " - " + (max > domainMax ? domainMax + "|" + domainMin + " - " + (max - rangeSize): max);
	}
	
	public static void main(String args[]) {
		IntegerWrappedQueryDomain d;

		int domainMin = -4;
		int domainMax = 8;
		
		// 1 Basic test will full initialization, everything should be 'true'
		System.out.println("Test with full init");
		d= new IntegerWrappedQueryDomain("test", domainMin, domainMax, AbstractQueryDomain.INIT_METHOD.FULL);
		for(int i = domainMin; i <= domainMax; i++) {
			System.out.println("MIN: " + d.min + " MAX: " + d.max + " [" + d.showRange() + "] TEST: " + i + " MATCHES " + d.matches(i));		
		}

		
		// Different test with random initialization
		System.out.println("Test with random init");
		for(int t = 0; t < 100; t++) {
			d= new IntegerWrappedQueryDomain("test", domainMin, domainMax, AbstractQueryDomain.INIT_METHOD.RANDOM);
			System.out.println("TEST " + t + ": RANGE = [" + d.showRange() + "]");
			for(int i = domainMin; i <= domainMax; i++) {
				System.out.println("MIN: " + d.min + " MAX: " + d.max + " [" + d.showRange() + "] TEST: " + i + " MATCHES " + d.matches(i));		
			}
		}
		
//		for(int i = 0; i < 1000; i++) {
//			int t = (int)(Math.random() * (min - max + 1)) + min;
//			System.out.println("MIN: " + d.min + " MAX: " + d.max + " [" + d.showRange() + "] TEST: " + t + " MATCHES " + d.matches(t));
//		}
	}

}
