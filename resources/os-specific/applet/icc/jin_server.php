<?php
	// A bunch of ICC specific functions.
	// This file defines functions which the people responsible for the
	// webserver where the applet is running from should provide. The
	// implementations in this file are simply for testing.
	
	
	// Returns whether prefs.php should be accessed via https, false if via
	// plain http
	function isSSLPrefs(){
		return false;
	}
	
	
	// Returns the port to which the applet should connect
	function getPort(){
		return 5001;
	}
	
	
	// Returns the background color of the applet, in hexadecimal RRGGBB format
	function getBackgroundColor(){
		return "ffffff";
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
	
	
	// Returns text which will be placed before the applet.
	function beforeApplet(){
		return "";
	}
	
	
	// Returns text which will be placed after the applet.
	function afterApplet(){
		$s = "";
		if (!isSSLPrefs())
			$s = $s . "<strong>Warning:</strong> Your username and password will be sent " . 
						"unencrypted to the server.";
			
		$s = $s . "<P><H3><A HREF=\"http://www.jinchess.com\">The Jin website</A></H3>";
		
		return $s;
	}
	
?>