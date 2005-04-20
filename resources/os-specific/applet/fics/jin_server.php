<?php
	// A bunch of FICS specific functions.
	// This file defines functions which the people responsible for the
	// webserver where the applet is running from should provide. The
	// implementations in this file are simply for testing.

	
	// Returns whether prefs.php should be accessed via https, false if via
	// plain http
	function isSSLPrefs(){
		return !($_REQUEST['oldjava']);
	}
	
	
	// Returns the port to which the applet should connect
	function getPort(){
		return 5000;
	}
	
	
	// Returns the background color of the applet, in hexadecimal RRGGBB format
	function getBackgroundColor(){
		return "ffffff";
	}

	
	// Connects to MySQL and selects the Jin database. Returns whether
	// successful.
	function connectToMySQL(){
		return (mysql_connect("localhost", "jin", "") && mysql_select_db("jin_fics"));
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
						"unencrypted to the server<br>because you are using an old version of Java. <br>" . 
						"To avoid this, <A HREF=\"http://www.java.com\">download and install</A> " .
						"the latest version of Java.";
			
		$s = $s . "<P><H3><A HREF=\"http://www.jinchess.com\">The Jin website</A></H3>";
		
		return $s;
	}
		
?>