<?php
	// This file simply dispatches (includes) the correct file based on where
	// in the login/login-failure/java-detection/usage/save prefs process the
	// user is.
	// This is done so that the user only has one URL to go to.
	
	require "prefs.php";
	require "server.php";
	
	// Save prefs request
	if (isset($_REQUEST['savePrefs']) && $HTTP_RAW_POST_DATA){
		initDatabase();
		echo savePrefs($HTTP_RAW_POST_DATA);
		return;
	}
	
	session_start();
	
	$isGuest = null;
	$username = null;
	$password = null;

	// Pressed the 'continue as guest' button on the login form or already
	// logged in as guest
	if (isset($_POST['guestContinue'])){
		$isGuest = 1;
		$username = null;
		$password = null;
	}
	// Pressed the 'continue' button on the login form
	else if (isset($_POST['continue'])){
		$isGuest = 0;
		$username = $_POST['username'];
		$password = $_POST['password'];
	}
	// Already logged in
	else if (isset($_SESSION['username']) && isset($_SESSION['password'])){
		$isGuest = 0;
		$username = $_SESSION['username'];
		$password = $_SESSION['password'];
	} // Already logged in as guest
	else if (isset($_SESSION['isGuest'])){
		$isGuest = 1;
		$username = null;
		$password = null;
	}
	
	// Put the variables in the session
	$_SESSION['isGuest'] = $isGuest;
	$_SESSION['username'] = $username;
	$_SESSION['password'] = $password;
	
	
	if ($isGuest){
		if (isset($_REQUEST['oldjava']))
			require "applet.php";
		else
			require "detect_java.html";
	}
	else if (isset($username) && isset($password)){
		if (($result = initDatabase()) != null){
			echo $result;
			return;
		}

		$userid = authenticate($username, $password);

		if ($userid){
			initUser($userid);
			
			// Java already detected
			if (isset($_REQUEST['oldjava']))
				require "applet.php";
			else
				require "detect_java.html";
		}
		else{
			$bad_password = 1;
			require "auth.php";
		}
	}
	else
		require "auth.php";
?>
