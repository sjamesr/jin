<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<TITLE> Jin Applet </TITLE>
</HEAD>

<BODY>
	<?php
		if (isset($bad_password) && $bad_password)
			echo "<H3>Wrong username/password, try again.</H3><br>";
	?>
	<P>Type in your username and password or continue as guest:
	<P>
	<form method="post" action="<?php echo $_SERVER['PHP_SELF'] ?>">
		<table>
			<tr><td>Username:</td><td><input type="Text" name="username"></td></tr>
			<tr><td>Password:</td><td><input type="Password" name="password"></td></tr>
		</table>
		<br>
		<input type="Submit" name="continue" value="Continue">
		<input type="Submit" name="guestContinue" value="Continue as Guest">
	</form>
</BODY>
