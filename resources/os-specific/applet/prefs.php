<?php

	// A library of various functions related to managing users' preferences.
	
	
	// Connects to the database and creates (if needed) the various tables
	// needed for preferences management.
	// Returns null on success; an error message on failure.
	// Prerequisites: connectToDatabase has been called.
	function initDatabase(){
		// Connect to MySQL and select the database
		if (!(mysql_connect("localhost", "jin", "@db_pass@") && mysql_select_db("@db_name@")))
			return "Couldn't connect to MySQL or select database - " . mysql_error();	
		
		// Create the Preferences table if it doesn't exist yet
		// The table maps user ids to user preferences (stored as a blob).
		// Another field is PrefsSaveKey - PrefsSaveKeys are one time keys which
		// are given to the applet which it then uses to identify itself to us
		// when saving preferences. 

		if (!mysql_query("SELECT * FROM Preferences")){
			if (!mysql_query("CREATE TABLE Preferences ( ICSUserId varchar(20) NOT NULL, PrefsBlob blob, PrefsSaveKey varchar(20), PRIMARY KEY (ICSUserId), UNIQUE ICSUserId (ICSUserId) )"))
				return "Couldn't create Preferences table - " . mysql_error();
		}
		
		return null;
	}
	
	
	
	// Initializes the various items needed for preferences management of the
	// specified user.
	// Returns null on success; an error message on failure.
	// Prerequisites: initDatabase has been called.
	function initUser($userid){
		$result = mysql_fetch_array(mysql_query("SELECT * FROM Preferences WHERE ICSUserId = '" . addslashes($userid)  . "'"));
		if (!$result){ // A new user
			$result = mysql_query("INSERT INTO Preferences VALUES ( '" . addslashes($userid) . "' , NULL, NULL )");
			if (!$result)
				return "Couldn't insert new user - " . mysql_error();
		}
		
		return null;
	}
	
	
	
	// Generates, saves and returns a PrefsSaveKey for the specified user.
	// Prerequisites: initUser with the id passed to this function has been
	// called.
	function genPrefsSaveKey($userid){
		do{
			$prefsSaveKey = sprintf("%s%s%s%s", mt_rand(0, 10000), mt_rand(0, 10000), mt_rand(0, 10000), mt_rand(0, 10000));
		} while (mysql_fetch_array(mysql_query("SELECT * FROM Preferences WHERE PrefsSaveKey = '" . addslashes($prefsSaveKey) . "'")));
		
		mysql_query("UPDATE Preferences SET PrefsSaveKey = '" . addslashes($prefsSaveKey) . "' WHERE ICSUserId = '" . addslashes($userid) . "'");
		
		return $prefsSaveKey;
	}
	
	
	
	// Saves the preferences specified by the given raw http post data and
	// clears the PrefsSaveKey for the user.
	// Returns an empty string on success; an error message on failure.
	// Prerequisites: initDatabase has been called and a PrefsSaveKey has been
	// generated for the user after the last time this function has been called.
	function savePrefs($rawData){
		$lines = explode("\n", $rawData); 
		$prefsSaveKey = $lines[0];
		$doneString = $lines[count($lines) - 1];
		
		if ($doneString != "Done")
			return "Upload did not complete";
		
		$prefsBlob = implode("\n", array_slice($lines, 1, count($lines) - 2));
		
		if (!mysql_query("UPDATE Preferences SET PrefsBlob = '" . addslashes($prefsBlob) . "' WHERE PrefsSaveKey = '" . addslashes($prefsSaveKey) . "'"))
			return "Unknown PrefsSaveKey: $prefsSaveKey";
			
		mysql_query("UPDATE Preferences SET PrefsSaveKey = NULL WHERE PrefsSaveKey = '" . addslashes($prefsSaveKey) . "'");
		
		return "";
	}
	
	
	
	// Returns a string which lists the preferences of the specified user
	// in the format of applet parameters. The string can be simply inserted
	// into HTML between the applet tags.
	// Prerequisites: initUser has been called for the specified user.
	function loadPrefs($userid){
		$sqlresult = mysql_query("SELECT PrefsBlob FROM Preferences WHERE ICSUserId = '" . addslashes($userid) . "'");
			
		if ($sqlresult)
			$sqlresult = mysql_result($sqlresult, 0);
		
		/*
			The blob is in the following format:
			<PreferencesType>
			<PrefLine>
			<PrefLine>
			..
			<PrefLine>
			
			<PreferencesType>
			<PrefLine>
			<PrefLine>
			..
			<PrefLine>
			
			etc.
			
			Currently, each preference line is in the following format:
			<PrefName>=<PrefType>;<PrefValue>
			The allowed types and values are documented in the Java code
			(currently in free.util.Preferences).
		*/
		
		if ($sqlresult){
			$prefs = explode("\n\n", $sqlresult);
			
			$result = "";
			
			foreach ($prefs as $typeprefsstring){
				$typeprefs = explode("\n", $typeprefsstring);
				
				$type = $typeprefs[0];
				$prefscount = count($typeprefs) - 1;
				$result = $result . "<PARAM NAME=\"$type.prefsCount\" VALUE=\"$prefscount\">\n\t";
				
				for ($i = 0; $i < $prefscount; $i++)
					$result = $result . "<PARAM NAME=\"$type.$i\" VALUE=\"" . $typeprefs[$i+1] . "\">\n\t";
			}
			
			return $result;
		}
		
		return "";
	}
	


?>