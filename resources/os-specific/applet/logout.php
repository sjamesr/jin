<?php
	ini_set("session.use_only_cookies", "1");
	session_start();
	$_SESSION['isGuest'] = NULL;
	$_SESSION['username'] = NULL;
	$_SESSION['password'] = NULL;
?>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<TITLE> Jin Applet </TITLE>
</HEAD>

<BODY>

<CENTER>
You have been successfully logged out.<br>
<A HREF=".">Return to the Jin Applet page</A>
</CENTER>
</BODY>
</HTML>