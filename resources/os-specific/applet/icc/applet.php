<?php
	header("CacheControl: no-cache");
?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<TITLE> Jin Applet </TITLE>
</HEAD>

<BODY>

<CENTER>

<?php
	if (!$isGuest)
		echo "<P>Welcome, $username. If you are sharing this computer, please close your browser
or <A HREF=\"logout.php\">log out</A> after using JinApplet. You also need to do that if you want to use
a different username.";
?>

<P>
<APPLET CODE="free.jin.JinApplet"
        ARCHIVE="jin.jar, <?php if ($_REQUEST['oldjava']) echo "libs/swingall.jar," ?>libs/chess.jar, libs/util.jar, libs/jregex.jar, libs/icc/timestamping.jar, servers/chessclub.jar, libs/console.jar, plugins/icc/console.jar, libs/board.jar, plugins/icc/board.jar, libs/seek.jar, plugins/icc/seek.jar, libs/sound.jar, plugins/icc/sound.jar, plugins/actions.jar, actions/getserverhelp.jar, actions/askquestion.jar, actions/seek.jar"
		WIDTH="400" HEIGHT="300">
		
	<PARAM NAME="autologin" VALUE="true">
	<PARAM NAME="isGuest" VALUE="<?php echo ($isGuest ? "true" : "false"); ?>">
	<PARAM NAME="username" VALUE="<?php echo $username; ?>">
	<PARAM NAME="password" VALUE="<?php echo $password; ?>">
	
	<PARAM NAME="port" VALUE="5001">
		
	<PARAM NAME="server.classname" VALUE="free.jin.chessclub.ChessclubServer">

			   
	<PARAM NAME="plugins.count" VALUE="5">
	<PARAM NAME="plugins.0.classname" VALUE="free.jin.console.icc.ChessclubConsoleManager">
	<PARAM NAME="plugins.1.classname" VALUE="free.jin.board.icc.ChessclubBoardManager">
	<PARAM NAME="plugins.2.classname" VALUE="free.jin.seek.icc.ChessclubSoughtGraphPlugin">
	<PARAM NAME="plugins.3.classname" VALUE="free.jin.sound.icc.ChessclubSoundManager">
	<PARAM NAME="plugins.4.classname" VALUE="free.jin.actions.ActionsPlugin">
			   
	<PARAM NAME="actions.count" VALUE="3">
	<PARAM NAME="actions.0.classname" VALUE="free.jin.action.getserverhelp.GetServerHelpAction">
	<PARAM NAME="actions.1.classname" VALUE="free.jin.action.askquestion.AskQuestionAction">
	<PARAM NAME="actions.2.classname" VALUE="free.jin.action.seek.SeekAction">

			   
	<PARAM NAME="resources.boards" VALUE="cold-marble gray-tiles green-marble pale-wood plain red-marble slate winter wooden-dark icc/wooden-light">
	<PARAM NAME="resources.pieces" VALUE="icc/blitzin icc/bookup icc/dyche1 icc/dyche2 icc/dyche3 xboard">
			   
	<?php
		if (!$isGuest){
			echo "<PARAM NAME=\"savePrefsUrl\" VALUE=\"?savePrefs\">\n";
			echo "<PARAM NAME=\"prefsSaveKey\" VALUE=\"" . genPrefsSaveKey($userid) . ">";
			echo loadPrefs($userid);
		}
	?>
</APPLET>
</CENTER>

</BODY>
