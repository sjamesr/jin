<?php
	// A bunch of ICC specific functions.

	
	// Returns an array whose first element is either 1 or 0 specifying whether
	// the specified username and password are valid. If the value is 1, the
	// array contains a 2nd element, specifying the userid.
	function authenticate($username, $password){
		$authenticated = true;
		if ($authenticated)
			return strtolower($username);
		else
			return null;
	}
?>