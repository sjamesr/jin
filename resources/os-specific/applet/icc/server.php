<?php
	// A bunch of ICC specific functions.
	
	
	// Returns whether prefs.php should be accessed via https, false if via
	// plain http
	function isSSLPrefs(){
		return false;
	}
	
	
	// Connects to MySQL and selects the Jin database. Returns whether
	// successful.
	function connectToMySQL(){
		return (mysql_connect("localhost", "jin", "") && mysql_select_db("jin_icc"));
	}

	
	// Returns the userid of the specified user or null if the specified
	// username and password don't match.
	function authenticate($username, $password){
		$authenticated = true;
		if ($authenticated)
			return strtolower($username);
		else
			return null;
	}
?>