<?php

	require_once 'jin_server.php';

	
	// Connects to the database and creates (if needed) the various tables
	// needed for preferences management.
	// Returns null on success; an error message on failure.
	// Prerequisites: connectToDatabase has been called.
	function initDatabase(){
		// Connect to MySQL and select the database
		if (!connectToMySQL())
			return "Couldn't connect to MySQL or select database - " . mysql_error();	
		
		// Create the Preferences table if it doesn't exist yet
		// The table maps user ids to user preferences (stored as a blob).
		// Another field is PrefsSaveKey - PrefsSaveKeys are one time keys which
		// are given to the applet which it then uses to identify itself to us
		// when saving preferences. 

		if (!mysql_query("SELECT * FROM Preferences")){
			if (!mysql_query("CREATE TABLE Preferences ( ICSUserId varchar(20) NOT NULL, PrefsBlob blob, PRIMARY KEY (ICSUserId), UNIQUE ICSUserId (ICSUserId) )"))
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
			$result = mysql_query("INSERT INTO Preferences VALUES ( '" . addslashes($userid) . "' , NULL )");
			if (!$result)
				return "Couldn't insert new user - " . mysql_error();
		}
		
		return null;
	}
	
	
	
	
	// Saves the user prefs specified by the given blob.
	// Returns null on success; an error message on failure.
	// Prerequisites: initDatabase has been called
	function savePrefs($userid, $userPrefsBlob){
		if (!mysql_query("UPDATE Preferences SET PrefsBlob = '" . addslashes($userPrefsBlob) . "' WHERE ICSUserId = '" . addslashes($userid) . "'"))
			return "Unknown user: $userid";
			
		return null;
	}
	
	
	
	// Fetches the user prefs blob for the specified user. Returns an array
	// whose first element is an error string (null if no error) and whose
	// second element is the blob (null if an error occurred, or if the
	// user doesn't have preferences yet).
	// Prerequisites: initUser has been called for the specified user.
	function loadPrefs($userid){
		$sqlresult = mysql_query("SELECT PrefsBlob FROM Preferences WHERE ICSUserId = '" . addslashes($userid) . "'");
			
		if ($sqlresult){
			if (mysql_num_rows($sqlresult) > 0)
				return array(null, mysql_result($sqlresult, 0));
			else
				return array(null, null);
		}
		else
			return array("Could not query database for preferences of user $userid", null);
	}


?>
<?php

	if (isset($_REQUEST['savePrefs']) || isset($_REQUEST['loadPrefs'])){
		if (!$HTTP_RAW_POST_DATA){
			echo "Missing HTTP POST data";
			return;
		}
		
		$data = $HTTP_RAW_POST_DATA;
		
		$username = strtok($data, "\n");
		$password = strtok("\n");
		
		if ($username === false){
			echo "Missing username in POST data\n";
			return;
		}
		else if ($password === false){
			echo "Missing password in POST data\n";
			return;
		}
		
		$userid = authenticate($username, $password);
		if ($userid === null){
			echo "Wrong username or password\n";
			return;
		}
		
		$data = substr($data, strlen($username) + strlen($password) + 2);
		
		initDatabase();
		initUser($userid);
		
		
		if (isset($_REQUEST['savePrefs'])){
			$endKeyword = "PREFS_UPLOAD_END";
			if (substr($data, strlen($data) - strlen($endKeyword)) == $endKeyword){
				$data = substr($data, 0, strlen($data) - strlen($endKeyword));
				$result = savePrefs($userid, $data);
				if ($result === null)
					echo "OK";
				else
					echo $result;
			}
			else
				echo "Upload of preferences interrupted\n";
		}
		else if (isset($_REQUEST['loadPrefs'])){
			$result = loadPrefs($userid);
			
			if ($result[0] !== null){ // Error
				echo $result[0] . "\n";
				return;
			}
			else if ($result[1] !== null){ // We've got prefs
				echo "OK\n";
				echo $result[1];
			}
			else{ // A new user - no prefs yet
				echo "NOPREFS\n";
			}
		}
	}
?>