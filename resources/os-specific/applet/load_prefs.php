<?php

	$prefs_key = $_COOKIE["prefs_key"];
	if ($prefs_key && mysql_connect("localhost", "jin", "@db_pass@") && mysql_select_db("@db_name@")){
		
		$result = mysql_query("SELECT PrefsBlob FROM Preferences WHERE CookieKey = '$prefs_key'");
		$result = mysql_result($result, 0);
		
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
		
		if ($result){
			echo "<PARAM NAME=\"cookieKey\" VALUE=\"$prefs_key\">\n\t";
			
			$prefs = explode("\n\n", $result);
			
			foreach ($prefs as $typeprefsstring){
				$typeprefs = explode("\n", $typeprefsstring);
				
				$type = $typeprefs[0];
				$prefscount = count($typeprefs) - 1;
				echo "<PARAM NAME=\"$type.prefsCount\" VALUE=\"$prefscount\">\n\t";
				
				for ($i = 0; $i < $prefscount; $i++)
					printf("<PARAM NAME=\"$type.$i\" VALUE=\"%s\">\n\t", $typeprefs[$i+1]);
			}
		}
	}

?>