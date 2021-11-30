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
package gdelt.scrapers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import gdelt.utils.IO;

public class GDELT_Events_Scraper {

	public static LocalDateTime     startTime       = LocalDateTime.of(2020, Month.OCTOBER, 31, 0, 0, 0);
	public static LocalDateTime     endTime         = LocalDateTime.of(2021, Month.APRIL, 01, 0, 0, 0);
    public static String            dataPath        = "/home/jmurphy/devMBM/gdelt_ga_search/data/GDELT_RAW/";
    public static String            ZIP_PATH_ROOT   = "/home/jmurphy/devMBM/gdelt_ga_search/data/GDELT_RAW/";
    public static long              LoopLimit       = 21000; 
    public static boolean           keepZip         = false;
    public static String            fileName        = "GDELT_Events_Data.csv";
    public static boolean           appendToFile    = false;
    public static double            PROB            = 1.0;
    public static int               sleepInterval   = 500;
        
    public static DateTimeFormatter df              = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static String            GDELT_URL_BASE  = "http://data.gdeltproject.org/gdeltv2/";
	
	public static void main(String[] args) {
		// Initialize the logging system
		IO.init(".");
		IO.echo = true;
		IO.log_level = IO.LEVEL.LEVEL_2;
		
		try {
			// Create the output writer
			FileWriter outWriter = new FileWriter(new File(dataPath + fileName), appendToFile);		
			int loops = 1;
			LocalDateTime currentDate = startTime.plusDays(0); // To make sure it's a copy
			while(currentDate.isBefore(endTime) && loops <= LoopLimit) {
				IO.log(IO.LEVEL.LEVEL_2, "Beginning loop " + loops);
				
				String dateRepresentation = df.format(currentDate);
				
				// There are two files; the main file and a translated file; this loop gets both
				String filename =  GDELT_URL_BASE + dateRepresentation + ".export.CSV.zip";
				
				int sleepTime = 0;
				try  {
					String zipPathName = ZIP_PATH_ROOT + dateRepresentation + ".events.zip";
					if(!(new File(zipPathName)).exists()) {
						IO.log(IO.LEVEL.LEVEL_2, "Retrieving: " + filename + " for date = " + currentDate);
					    byte data[] = new byte[1024];
					    BufferedInputStream inputStream = new BufferedInputStream(new URL(filename).openStream());
					    int byteContent;
					    FileOutputStream fileOS = new FileOutputStream(zipPathName);
					    while ((byteContent = inputStream.read(data, 0, 1024)) != -1) fileOS.write(data, 0, byteContent);
					    fileOS.close();
					    sleepTime = sleepInterval;
					}
					else {
						System.out.println("File already present on file system: " + zipPathName);
					}
					// In rare cases, the file just doesn't exist on GDELT's server, so it won't exist here
					if(!(new File(zipPathName)).exists()) {
						IO.log("File " + zipPathName + " still does not exist; skipping.");
					}
					else if((new File(zipPathName)).length() < 1){
						IO.log("File " + zipPathName + " was not properly loaded; deleting and passing");
						(new File(zipPathName)).delete();
					}
					else {
						// Now read using ZIP
				        //buffer for read and write data to file
				        byte[] buffer = new byte[1024];
				        File newFile; // Fortunately there will only be one
				        String newFileName = ""; // Again, only one
				        try {
				        	FileInputStream zipFileInputStream = new FileInputStream(zipPathName);
				            ZipInputStream  zipInputStream     = new ZipInputStream(zipFileInputStream);
				            ZipEntry        zipEntry           = zipInputStream.getNextEntry();
				            newFileName = dataPath + File.separator + zipEntry.getName();
			                newFile = new File(newFileName);
			                if(!newFile.exists()) {
				                IO.log(IO.LEVEL.LEVEL_2, "Unzipping to " + newFile.getAbsolutePath());
				                new File(newFile.getParent()).mkdirs();
				                FileOutputStream fos = new FileOutputStream(newFile);
				                int len;
				                while ((len = zipInputStream.read(buffer)) > 0) fos.write(buffer, 0, len);
				                fos.close();
			                }
			                else {
			                	System.out.println("File is already unzipped: " + newFile.getAbsolutePath());
			                }
			                zipInputStream.closeEntry();
				            zipInputStream.close();
				            zipFileInputStream.close();
				        } catch (IOException e) {
				            e.printStackTrace();
				        }
					    
				        // Read in the new file
				        BufferedReader br = new BufferedReader(new FileReader(new File(newFileName)));
				        String line;
				        int l = 0;
				        int written = 0;
				        while(((line = br.readLine()) != null)) {
				        	// In theory you can filter the GDELT data here; specify criteria
				        	// that are 'in' or 'out' and only write the ones that are in
				        	// e.g.: 
				        	// String[] lineSplit = line.split("\t");
				        	// if(lineSplit[5].contains("USA")) ... 
				        	if(Math.random() < PROB) {
				        		outWriter.write(line + IO.LS);
				        		written++;
				        	}
				        	l++;
				        }
				        IO.log(IO.LEVEL.LEVEL_1, "Wrote " + written + " of " + l + " lines");
				        br.close();
				        if(!keepZip) (new File(zipPathName)).delete();
				        (new File(newFileName)).delete();
					}    
				} catch (IOException e) {
				    System.out.println("INNER: " + e.getMessage());
					e.printStackTrace();
					
				}// Wait (this is not an attack on the server)
				if(sleepTime > 0) {
					IO.log(IO.LEVEL.LEVEL_2, "Sleeping : " + sleepTime + " milliseconds...");
					TimeUnit.MILLISECONDS.sleep(sleepTime);
				}
				currentDate = currentDate.plusMinutes(15);				
				loops++;
			}
			outWriter.close();
		}
		catch(Exception E) {
			System.out.println("OUTER: " + E.getMessage());
			E.printStackTrace();
		}
	}

	
}
