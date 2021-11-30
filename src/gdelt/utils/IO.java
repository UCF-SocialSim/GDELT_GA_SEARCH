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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Contains a collection of useful elements for input/output,
 * including logging
 *
 */
public class IO {
	
	public static enum LEVEL{
		LEVEL_0,
		LEVEL_1,
		LEVEL_2,
		LEVEL_3,
		LEVEL_4,
		ALL
	}
	
	
	// Some helper static values
	private static String   spacer  = "                                                                                                                                                                                ";
	public  static String   LS      = System.getProperty("line.separator");
	public  static boolean  echo    = false;
	
	/**
	 * Pads a string up to at least a given length
	 * @param l
	 * @return
	 */
	private static String padded(int l) {
		String ret = spacer;
		while(ret.length() < l) ret += spacer;
		return ret;
	}
	
	

	// *******
	// String formatting
	
	public static String fitToNCharacters(String A, String B, int length) {
		String spaces = padded(length);
		return A + spaces.substring(0, length - (A.length() + B.length())) + B;
	}
	
	public static String fitToNCharacters(String A, long Bl, int length) {
		String spaces = padded(length);
		String B = "" + Bl;
		return A + spaces.substring(0, length - (A.length() + B.length())) + B;
	}
		
	
	
	// *******
	// Standard data output formatting
	// *******
	
	public static String formatAsSortedCounts(Map<String, Long> toSort, boolean csv) {
		StringWriter sw = new StringWriter();
		Map<String, Long> temp = new HashMap<String, Long>();
		temp.putAll(toSort);
		int maxLength = 0;
		for(String entry: temp.keySet()) if(entry.length() > maxLength) maxLength = entry.length();
		String buffer = ",";
		if(!csv) {
			buffer = " ";
			for(int i = 0; i < maxLength; i++) buffer += "  ";
			buffer += "    ";
		}
		//System.out.println(temp.size());
		while(temp.size() > 0) {
			long max = 0;
			for(Long val: temp.values()) if(val > max) max = val;
			Set<String> toRemove = new HashSet<String>();
			for(Entry<String, Long> entry: temp.entrySet()) {
				if(entry.getValue().longValue() == max) {
					if(!csv) sw.append((entry.getKey() + buffer).substring(0, maxLength + 1) + entry.getValue() + LS);
					else     sw.append(entry.getKey() + "," + entry.getValue() + LS);
					toRemove.add(entry.getKey());
					
				}
			}
			for(String rem: toRemove) temp.remove(rem);
		}
		return sw.toString();
	}

	
	// *******
	// Memory utils
	// *******
	private static long getGCCount() {
		long total = 0;
		for(GarbageCollectorMXBean bean: ManagementFactory.getGarbageCollectorMXBeans()) {
			long count = bean.getCollectionCount();
			total += (count > -1 ? count : 0);
		}
		return total;
	}
	


	// *******	
	// Instances for console logging
	// *******	
	// Note: Static logging is possible using defaults; instances can also be created
	
	public  static LEVEL      log_level  = LEVEL.ALL;
	private static FileWriter outWriter = null;

	
	public static void init(String outputFolder){
		String outPath = "./output/" + outputFolder;
		new File(outPath).mkdirs();
		String path = outPath + "/" + "console_out.txt";
		try {
			if(outWriter == null) {
				outWriter = new FileWriter(path);
			}
			else {
				System.err.println("Init object already exists, close it before creating another one!");
			}
		} catch (IOException e) {
			System.err.println("SOME ERROR OCCURRED HERE!");
			e.printStackTrace();
		}
	}

	

	public static boolean log(FileWriter outWriter, LEVEL level, String msg, boolean omitTimestamp) {
		String fullMSG = "" + (omitTimestamp ? "" : (new GregorianCalendar()).getTime() + ": ") + msg;
		if(level.ordinal() <= log_level.ordinal()){
			if(outWriter != null)
				try {
					outWriter.write(fullMSG + LS);
					outWriter.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if(echo) System.out.println(fullMSG);
			return true;
		}
		return false;
	}

	// Default to include timestamp
	public static boolean log(FileWriter outWriter, LEVEL level, String msg) {
		return log(outWriter, level, msg, false);
	}
	
	// Default that will always write (regardless of level); includes timestamp
	public static boolean log(FileWriter outWriter, String msg) {
		return log(outWriter, LEVEL.ALL, msg);
	}

	// Write a blank message if at a specific level
	public static boolean log(FileWriter outWriter, LEVEL level){
		return log(outWriter, level, "");
	}
	
	// Write a blank message at any level
	public static boolean log(FileWriter outWriter) {
		return log(outWriter, LEVEL.ALL);
	}
	

	// These versions will always call the static instance
	public static boolean log(LEVEL level, String msg, boolean omitTimestamp) {
		return log(outWriter, level, msg, omitTimestamp);
	}	

	public static boolean log(LEVEL level, String msg) {
		return log(outWriter, level, msg, false);
	}

	public static boolean log(String msg) {
		return log(LEVEL.ALL, msg);
	}
	
	public static boolean log() {
		return log(LEVEL.ALL);
	}
	
	public static boolean log(LEVEL level) {
	  return log(level, "");	
	}
	
	
	// Close the file
	public static void close(FileWriter outWriter) {
		if(outWriter != null) {
			try {
				outWriter.flush();
				outWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	// Default for static instance
	public static void close() {
		close(outWriter);
		outWriter = null;
	}
	
	

	// Print memory status
	public static void memCheck(FileWriter outWriter, LEVEL level) {
		long totalBefore = getGCCount();
		System.gc();
		while(getGCCount() == totalBefore);
		
		log(outWriter, level,
			  fitToNCharacters("Total Memory:", Runtime.getRuntime().totalMemory(), 30) + " "
            + fitToNCharacters("Heap Usage:",ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed(), 30) + " "
            + fitToNCharacters("Free Memory:", Runtime.getRuntime().freeMemory(), 30));
	}
	
	
	public static void memCheck(FileWriter outWriter) {
		memCheck(outWriter, LEVEL.LEVEL_0);
	}
	
	// Versions that use the static instance
	public static void memCheck(LEVEL level) {
		memCheck(outWriter, level);
	}
	
	public static void memCheck() {
		memCheck(outWriter, LEVEL.LEVEL_0);
	}
	
}
