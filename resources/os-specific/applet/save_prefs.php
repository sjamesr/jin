<?php
	
	if ($HTTP_RAW_POST_DATA){
		$uploaded = explode("\n", $HTTP_RAW_POST_DATA); 
		$prefs_key = $uploaded[0];
		$is_done = $uploaded[count($uploaded) - 1];
		
		if ($prefs_key && ($is_done == "Done") && mysql_connect("localhost", "root") && mysql_select_db("jin")){
			$prefs_blob = addslashes(implode("\n", array_slice($uploaded, 1, count($uploaded) - 2)));
			
			if (!mysql_query("UPDATE Preferences SET PrefsBlob = '$prefs_blob' WHERE CookieKey = '$prefs_key'"))
				echo "Unknown key: $prefs_key";
		}
		else if (!$prefs_key){
			echo "No CookieKey specified";	
		}
		else if ($is_done != "Done"){
			echo "Upload did not complete";	
		}
		else{
			echo "Unable to connect to or select database";	
		}
	}
	
?>
