<?php
	// A bunch of FICS specific functions.

	

	// Connects to MySQL and selects the Jin database. Returns whether
	// successful.
	function connectToMySQL(){
		return (mysql_connect("localhost", "jin", "") && mysql_select_db("jin_fics"));
	}

	

	// Returns the userid of the specified user or null if the specified
	// username and password don't match.
	function authenticate($username, $password){
		$authenticated = (file_get_contents("http://www.freechess.org/cgi-bin/pro/Login/jin/AuthenticateUser.cgi?username=$username&password=$password") == "1");
		if ($authenticated)
			return strtolower($username);
		else
			return null;
	}
?>