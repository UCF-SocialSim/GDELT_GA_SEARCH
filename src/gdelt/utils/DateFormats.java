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
package gdelt.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

public class DateFormats {
	
	protected static Month[] months = 
			new Month[]{ null, Month.JANUARY, Month.FEBRUARY, Month.MARCH,
                               Month.APRIL,   Month.MAY,      Month.JUNE, 
                               Month.JULY,    Month.AUGUST,   Month.SEPTEMBER,
                               Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER};

	
	public static SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMddHHmmss");
	
	
	/**
	 * Basic Date to local DateTime
	 * Assumes YYYYMMdd
	 * @param d
	 * @return
	 */
	public static LocalDateTime convertBasicDate(String d) {
		int year  = Integer.parseInt(d.substring(0, 4));
		int month = Integer.parseInt(d.substring(5, 7));
		int day   = Integer.parseInt(d.substring(8));
		return LocalDateTime.of(year, months[month], day, 0, 0, 0);
	}
	
	/**
	 * ISO Date
	 * Assumes ISO date format 2011-12-03+01:00 (offset optional)
	 * @param d
	 * @return
	 */
	public static LocalDateTime convertISODate(String d) {
		try{
			return LocalDate.parse(d, DateTimeFormatter.ISO_DATE).atStartOfDay();
		}
		catch(Exception E) {
			// do nothing- should return null
		}
		return null;
	}

}
