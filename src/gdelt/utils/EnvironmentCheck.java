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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * Standardized class for checking that all
 * needed environment variables are in place.
 * 
 * Notes: 
 *   Class is entirely static; no instances permitted
 *
 */
public class EnvironmentCheck {
	
	private static boolean gitStatusIsChecked   = false;
	private static boolean gitStatusIsModified  = false;
	private static String  gitStatus            = null;
	
	public static boolean environmentIsValid() {
		return environmentIsValid(false);
	}
	
	public static boolean environmentIsValid(String gitDir) {
		gitStatus = getGitStatus(gitDir);
		return environmentIsValid(true);
	}
	
	public static boolean environmentIsValid(boolean gitIsUnmodified) {
		try {
			if(gitIsUnmodified) { // If 'true' requires check of git modification status
				if(gitStatusIsChecked == false || gitStatusIsModified == true) return false;
			}
			Optional.ofNullable(System.getProperty("GDELT_GAS_PSCRIPT_PATH")).orElseThrow(() -> new PythonPathNotSpecifiedException());
		}
		catch(PythonPathNotSpecifiedException ppnse) {
			System.err.println(ppnse.getMessage());
			return false;
		}
		return true;
	}
	
	public static boolean getGitStatusIsChecked()  { return gitStatusIsChecked; }
	public static boolean getGitStatusIsModified() { return gitStatusIsModified; }
	
	public static String getGitStatus(String gitDir) {
		if(gitStatus != null) return gitStatus;
		String ret = "";
		try {
			Runtime run  = Runtime.getRuntime();
			File f = new File(gitDir);
			Process proc1 = run.exec("git log -n 1", null, f);
			BufferedReader reader1 =
                    new BufferedReader(new InputStreamReader(proc1.getInputStream() /*proc.getErrorStream()*/));

            proc1.waitFor();
            
            String line;
            while ((line =  reader1.readLine()) != null) {
            	ret += "GIT: " + line + IO.LS;
            }
            
            reader1.close();
			
			Process proc2 = run.exec("git status", null, f);
			BufferedReader reader2 =
                    new BufferedReader(new InputStreamReader(proc2.getInputStream() /*proc.getErrorStream()*/));

            proc2.waitFor();
            
            while ((line =  reader2.readLine()) != null) {
            	ret += "GIT: " + line + IO.LS;
            }            
            
            
			gitStatusIsChecked  = true;
			gitStatusIsModified = ret.contains("modified:") || ret.contains("deleted:") || ret.contains("new file:");
			
		}
		catch(Exception E) {
			System.out.print("EXCEPTION GETTING GIT STATUS");
			E.printStackTrace();
		}
		gitStatus = ret;
		return ret;
	}

	// No instances permitted; all static
	private EnvironmentCheck() {}
	
}
