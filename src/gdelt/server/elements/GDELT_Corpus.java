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
package gdelt.server.elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import gdelt.utils.DateFormats;
import gdelt.utils.IO;

/**
 * A collection of GDELT information.
 * 
 * 
 * This collection has more than one level:
 * 
 * The main list is a list of all events;
 * Every event has multiple 'mentions', which may
 * be loaded from the second 'mentions' table
 * Mentions occur in specific documents; one
 * document can have multiple mentions
 * 
 */
public class GDELT_Corpus {
	
	public static enum GDELT_Corpus_File_Type{
		JSON,
		CSV
	}

	// Load occurs using JSON directly
	@SerializedName("values")
	public LinkedList<GDELT_Global_Event> allEvents;

	// All the documents are reachable through this map
	Map<String, GDELTMentionDoc>       docs         = new HashMap<String, GDELTMentionDoc>();
	Map<String, List<GDELTMentionDoc>> docsBySource = new HashMap<String, List<GDELTMentionDoc>>();
	Map<String, GDELT_Global_Event>    events       = new HashMap<String, GDELT_Global_Event>();
	
	
    public void initAllEvents() {
    	allEvents = new LinkedList<GDELT_Global_Event>();
    }
	
	
	
	public Map<String, Long> getStringDomainRange(String domain){
		Map<String, Long> ret = new TreeMap<String, Long>();
		switch(domain) {
		   case "Actor1":{
			   for(GDELT_Global_Event article: allEvents) {
				   String actor1 = article.Actor1CountryCode;
				   if(actor1 != null) {
					   if(!ret.containsKey(actor1)) ret.put(actor1, 1l);
					   else                         ret.put(actor1, ret.get(actor1) + 1);
				   }
			   }
			   break;
		   }
		   case "Actor2":{
			   for(GDELT_Global_Event article: allEvents) {
				   String actor2 = article.Actor2CountryCode;
				   if(actor2 != null) {
					   if(!ret.containsKey(actor2)) ret.put(actor2, 1l);
					   else                         ret.put(actor2, ret.get(actor2) + 1);
				   }
			   }
			   break;
		   }		}
		return ret;
	}
	
	
	public void write() {
		try {
			Date post = DateFormats.dateFormat1.parse("2018-12-23");
			FileWriter fileout = new FileWriter("./output/gdelt/GDELT.csv");
			fileout.write("Actor1,Actor2,EventRootCode,EventBasecode,EventCode,NumArticles,NumMentions,dateOfEvent,avgTone" + IO.LS);
			for(GDELT_Global_Event article: allEvents) {
				if(article.dateOfEvent.after(post)) fileout.write(
						article.Actor1Name + "," + 
				        article.Actor2Name + "," +  
					    article.EventRootCode + "," + 
				        article.EventBaseCode + "," + 
					    article.EventCode + "," + 
				        article.NumArticles + "," +
			            article.NumMentions + "," + 
			            DateFormats.dateFormat1.format(article.dateOfEvent) + "," + article.AvgTone + IO.LS);
			}
			fileout.close();
		}
		catch(Exception E) {
			System.out.println(E.getMessage());
		}
	}
	
	
	public boolean containsID(String eventID) {
		return events.keySet().contains(eventID);
	}
	
	public long sumOfAllMentions() {
		long ret = 0;
		for(GDELT_Global_Event article: allEvents) ret += article.NumMentions;
		return ret;
	}
	
	
	
	
	
	public static GDELT_Corpus get(String globalEventsFile) {
		return get(globalEventsFile, null);
	}
	
	public static GDELT_Corpus get(String globalEventsfile, String mentionsFile){
		IO.log(IO.LEVEL.LEVEL_0, "Beginning get of corpus from " + globalEventsfile + " mentions = " + mentionsFile);
		GDELT_Corpus_File_Type fileType = (globalEventsfile.substring(globalEventsfile.lastIndexOf(".") + 1).compareToIgnoreCase("csv") == 0 ? GDELT_Corpus_File_Type.CSV : GDELT_Corpus_File_Type.JSON);
		GDELT_Corpus articleCollection = null;
		switch(fileType) {
			case JSON:{
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				Reader eventsReader;
				try {
					eventsReader = new FileReader(globalEventsfile);
					IO.log(IO.LEVEL.LEVEL_1, "Starting gson read for corpus load");
					articleCollection = gson.fromJson(eventsReader,  GDELT_Corpus.class);
//					articleCollection.init(); // Not needed yet
					for(GDELT_Global_Event article: articleCollection.allEvents) {
						article.init();
						articleCollection.events.put(article.globalEventID, article);
					}
					IO.log(IO.LEVEL.LEVEL_0, "Loaded " + articleCollection.allEvents.size() + " from " + globalEventsfile);					
					if(mentionsFile != null) {
						BufferedReader mentionsReader;
						IO.log(IO.LEVEL.LEVEL_1, "Starting read of mentions file");
						mentionsReader = new BufferedReader(new FileReader(new File(mentionsFile)));
						String line = mentionsReader.readLine(); // Skip header
						int lineCount = 0;
						while((line = mentionsReader.readLine()) != null) {
							// Get the data for the source
							String[] vals = line.split("\t");
							String mentionIdentifier  = vals[5];
							String mentionSourceName = vals[4];
							// Need to create the mention and the article
							if(!articleCollection.docs.containsKey(mentionIdentifier)) {
								GDELTMentionDoc doc = new GDELTMentionDoc();
								doc.MentionTimeDate   =  DateFormats.dateFormat2.parse(vals[2]);
								doc.MentionType       =  Integer.parseInt(vals[3]);
								doc.MentionSourceName = mentionSourceName;
								doc.MentionDocLen     = Integer.parseInt(vals[12]);
								doc.MentionDocTone    = Double.parseDouble(vals[13]);
								String mentionDocTranslationInfo = "";
								if(vals.length > 14) mentionDocTranslationInfo = vals[14]; // Null for all English articles
								doc.MentionDocTranslationInfo = mentionDocTranslationInfo;
								articleCollection.docs.put(mentionIdentifier, doc);
								
								// Also track documents by sources (e.g. all URLS from nytimes.com)
								if(!articleCollection.docsBySource.containsKey(mentionSourceName)) {
									articleCollection.docsBySource.put(mentionSourceName, new ArrayList<GDELTMentionDoc>());
								}
								articleCollection.docsBySource.get(mentionSourceName).add(doc);
							}
								
							GDELTMentionDoc doc = articleCollection.docs.get(mentionIdentifier); // Retrieve; will exist, even if just placed
							GDELTMention mention = new GDELTMention();
							mention.EventTimeDate    = DateFormats.dateFormat2.parse(vals[1]);
							mention.mentionURL       = mentionIdentifier;
							mention.SentenceID       = Integer.parseInt(vals[6]);
							mention.Actor1CharOffset = Integer.parseInt(vals[7]);
							mention.Actor2CharOffset = Integer.parseInt(vals[8]);
							mention.ActionCharOffset = Integer.parseInt(vals[9]);
							mention.InRawText        = Boolean.parseBoolean(vals[10]);
							mention.Confidence       = Integer.parseInt(vals[11]);
							
							// Add the mention to the document's list of mentions
							doc.addMention(mention);

							GDELT_Global_Event event = articleCollection.events.get(vals[0]);
							if(event == null) {
								System.out.println("Orphan mention: refers to event " + vals[0]);
							}
							else {
								event.mentions.add(mention); // This sets the mention's event pointer and adds the mention to the event's list
							}
							
							lineCount++;
							
						}
						IO.log(IO.LEVEL.LEVEL_1, "Read mentions file, " + lineCount + " lines, " + articleCollection.docs.size() + " distinct documents"); 
						mentionsReader.close();
					}
				}
				catch(IOException e) {
					e.printStackTrace();
				}
				catch(ParseException pe) {
					pe.printStackTrace();
				}
				
								
				break;
			}
			case CSV:{
				try {
					IO.log(IO.LEVEL.LEVEL_1, "Starting csv read for corpus load");
			        File myObj = new File(globalEventsfile);
			        Scanner myReader = new Scanner(myObj);
			        articleCollection = new GDELT_Corpus();
			        articleCollection.initAllEvents();
			        while (myReader.hasNextLine()) articleCollection.allEvents.add(new GDELT_Global_Event(myReader.nextLine()));
			        myReader.close();
//					articleCollection.init(); // Not needed yet
					for(GDELT_Global_Event article: articleCollection.allEvents) {
						article.init();
						articleCollection.events.put(article.globalEventID, article);
					}
					IO.log(IO.LEVEL.LEVEL_0, "Loaded " + articleCollection.allEvents.size() + " from " + globalEventsfile);					
					if(mentionsFile != null) {
						BufferedReader mentionsReader;
						IO.log(IO.LEVEL.LEVEL_1, "Starting read of mentions file");
						mentionsReader = new BufferedReader(new FileReader(new File(mentionsFile)));
						String line = mentionsReader.readLine(); // Skip header
						int lineCount = 0;
						while((line = mentionsReader.readLine()) != null) {
							// Get the data for the source
							String[] vals = line.split("\t");
							String mentionIdentifier  = vals[5];
							String mentionSourceName = vals[4];
							// Need to create the mention and the article
							if(!articleCollection.docs.containsKey(mentionIdentifier)) {
								GDELTMentionDoc doc = new GDELTMentionDoc();
								doc.MentionTimeDate   =  DateFormats.dateFormat2.parse(vals[2]);
								doc.MentionType       =  Integer.parseInt(vals[3]);
								doc.MentionSourceName = mentionSourceName;
								doc.MentionDocLen     = Integer.parseInt(vals[12]);
								doc.MentionDocTone    = Double.parseDouble(vals[13]);
								String mentionDocTranslationInfo = "";
								if(vals.length > 14) mentionDocTranslationInfo = vals[14]; // Null for all English articles
								doc.MentionDocTranslationInfo = mentionDocTranslationInfo;
								articleCollection.docs.put(mentionIdentifier, doc);
								
								// Also track documents by sources (e.g. all URLS from nytimes.com)
								if(!articleCollection.docsBySource.containsKey(mentionSourceName)) {
									articleCollection.docsBySource.put(mentionSourceName, new ArrayList<GDELTMentionDoc>());
								}
								articleCollection.docsBySource.get(mentionSourceName).add(doc);
							}
								
							GDELTMentionDoc doc = articleCollection.docs.get(mentionIdentifier); // Retrieve; will exist, even if just placed
							GDELTMention mention = new GDELTMention();
							mention.EventTimeDate    = DateFormats.dateFormat2.parse(vals[1]);
							mention.mentionURL       = mentionIdentifier;
							mention.SentenceID       = Integer.parseInt(vals[6]);
							mention.Actor1CharOffset = Integer.parseInt(vals[7]);
							mention.Actor2CharOffset = Integer.parseInt(vals[8]);
							mention.ActionCharOffset = Integer.parseInt(vals[9]);
							mention.InRawText        = Boolean.parseBoolean(vals[10]);
							mention.Confidence       = Integer.parseInt(vals[11]);
							
							// Add the mention to the document's list of mentions
							doc.addMention(mention);

							GDELT_Global_Event event = articleCollection.events.get(vals[0]);
							if(event == null) {
								System.out.println("Orphan mention: refers to event " + vals[0]);
							}
							else {
								event.mentions.add(mention); // This sets the mention's event pointer and adds the mention to the event's list
							}
							
							lineCount++;
							
						}
						IO.log(IO.LEVEL.LEVEL_1, "Read mentions file, " + lineCount + " lines, " + articleCollection.docs.size() + " distinct documents"); 
						mentionsReader.close();
					}
				}
				catch(IOException e) {
					e.printStackTrace();
				}
				catch(ParseException pe) {
					pe.printStackTrace();
				}
				
				break;
			}
		}
		return articleCollection;
	}
	
	public void resetWeights() {
		for(GDELT_Global_Event event: allEvents) event.weight = 0;
	}
		
	
	public static void main(String[] args) {
		GDELT_Corpus.get("/home/jmurphy/Downloads/20150218230000.export.CSV", null);
	}
}
