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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import gdelt.utils.DateFormats;

public class GDELT_Global_Event{
		
		double weight = 0;
		
		// When
		@SerializedName("day")
		public String day;
		
		public Date dateOfEvent;
		
		// Who acted
		@SerializedName("Actor1Name")
		public String Actor1Name;
		
		// What did they do
		@SerializedName("QuadClass")
	    public long QuadClass;
		
		@SerializedName("EventBaseCode")
		public long EventBaseCode;
		
		@SerializedName("EventCode")
		public long EventCode;
		
		@SerializedName("EventRootCode")
		public long EventRootCode;
		
		// Who did they do it to
		@SerializedName("Actor2Name")
		public String Actor2Name;

		// Data collection
		@SerializedName("NumSources")
		public String NumSources;
		
		@SerializedName("GoldsteinScale")
		public double GoldsteinScale;
		
		@SerializedName("AvgTone")
		public double AvgTone;
		
		@SerializedName("sourceurl")
		public String sourceurl;
		public String domain;
		
		@SerializedName("NumArticles")
		public Long NumArticles;

		@SerializedName("NumMentions")
		public Long NumMentions;

		@SerializedName("IsRootEvent")
		public int IsRootEvent;

		@SerializedName("globaleventid")
		public String globalEventID;
		
		
//		  public String ActionGeo_ADM1Code;
//		  public String ActionGeo_FullName;
//		  public String Actor1Geo_CountryCode;
		
		@SerializedName("dateadded")
		public String dateadded;
		public Date dateAndTimeAdded;
		
		  
//		  public String EventRootCode;
//		  @SerializedName("Actor2_GeoFullName")
//		  public String Actor2Geo_FullName;
//		  public String Actor1Geo_ADM2Code;
//		  public String Actor1Code;
		  
//		  @SerializedName("Actor1Geo_FullName")
//		  public String Actor1Geo_FullName;
//		  public String ActionGeo_CountryCode;
		  @SerializedName("Actor1CountryCode")
		  public String Actor1CountryCode;
//		  public String Actor1Geo_FeatureID;
//		  public String Actor2Geo_Long;
		  
//		  public String Actor2Geo_CountryCode;
//		  public String Actor1Geo_Type;
//		  public String Actor2Geo_FeatureID;
//		  public String globaleventid;
//		  public String ActionGeo_Type;
//		  public String Actor2Geo_ADM1Code;
//		  public String Actor1Geo_Long;
//		  public String Actor2Geo_ADM2Code;
//		  public String Actor1Geo_ADM1Code;
//		  public String Actor2Code;
		  @SerializedName("Actor2CountryCode")
		  public String Actor2CountryCode;
//		  public String Actor2Geo_Type;
//		  public String ActionGeo_FeatureID;
		  
//		  public String filename;
//		  public String FractionDate;
//		  public String Year;
//		  public String Actor2Geo_Lat;
//		  public String Actor1Geo_Lat;
//		  public String ActionGeo_ADM2Codepublic; 

		  @SerializedName("ActionGeo_Lat")
		  public double ActionGeo_Lat;
		  @SerializedName("ActionGeo_Long")
		  public double ActionGeo_Long;
//		  public String sourceurl_h;
		
		  
		  // This will be empty at first
		  public List<GDELTMention> mentions = new ArrayList<GDELTMention>();
		  // This will be initialized when needed
		  private Set<GDELTMentionDoc> mentionDocuments = null;
		  
		  private Double unboundAverageToneFromMentions = null;
		  
		  private void setMentionDocuments() {
			  if(mentionDocuments != null) return;
			  mentionDocuments = new HashSet<GDELTMentionDoc>();
			  for(GDELTMention mention: mentions) mentionDocuments.add(mention.document);
		  }

		  public GDELT_Global_Event() {}
		  
		  private long parseLong(String val) {
			  return(val == null || val.length() == 0) ? 0 : Long.parseLong(val);
		  }
		  
		  private double parseDouble(String val) {
			  return(val == null || val.length() == 0) ? 0 : Double.parseDouble(val);
		  }
		  
		  public GDELT_Global_Event(String csvLine) {
			  String[] elements = csvLine.split("\t");
			  day                     = elements[1].substring(0, 4) + "-" + elements[1].substring(4,6) + "-" + elements[1].substring(6);
			  Actor1Name              = elements[6];
			  QuadClass               = parseLong(elements[29]);
			  EventBaseCode           = parseLong(elements[27]);
			  EventCode               = parseLong(elements[26]);
			  EventRootCode           = parseLong(elements[28]);
			  Actor2Name              = elements[16];
              NumSources              = elements[32];
			  GoldsteinScale          = parseDouble(elements[30]);
			  AvgTone                 = parseDouble(elements[34]);
              sourceurl               = elements[60];
			  NumArticles             = parseLong(elements[33]);
              NumMentions             = parseLong(elements[31]);
              IsRootEvent             = Integer.parseInt(elements[25]);
              globalEventID           = elements[0];
			  dateadded               = elements[59];
			  Actor1CountryCode       = elements[7];
			  Actor2CountryCode       = elements[17];
              ActionGeo_Lat           = parseDouble(elements[56]);
			  ActionGeo_Long          = parseDouble(elements[57]);
		  }
		  
		  public void init() {
			  try {
				  dateOfEvent      = DateFormats.dateFormat1.parse(day.substring(0, 10));
				  dateAndTimeAdded = DateFormats.dateFormat2.parse(dateadded);				  
			  }
			  catch(Exception E) {
				  System.out.println("PROBLEM 1: " + day);
			  }
			  try {
				  domain      = sourceurl.split("/")[2];
			  }
			  catch(Exception E) {
				  System.out.println("PROBLEM 2: " + domain);
			  }
		  }
		  
		  public double getAverageToneFromArticles() {
			  if(unboundAverageToneFromMentions != null) return unboundAverageToneFromMentions;
			  setMentionDocuments();
			  double sum = 0;
			  for(GDELTMentionDoc doc: mentionDocuments) sum += doc.MentionDocTone;
			  
			  return (unboundAverageToneFromMentions = sum/((double)mentionDocuments.size()));
		  }

			  
	}