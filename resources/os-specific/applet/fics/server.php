<?php
	// A bunch of FICS specific functions.

	
	// Returns the userid of the specified user or null if the specified username
	// and password don't match.
	function authenticate($username, $password){
		$authenticated = (file_get_contents("http://www.freechess.org/cgi-bin/pro/Login/jin/AuthenticateUser.cgi?username=$username&password=$password") == "1");
		if ($authenticated)
			return strtolower($username);
		else
			return null;
	}
?>