<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<TITLE> Jin Applet </TITLE>
</HEAD>

<BODY>

<CENTER>
<APPLET CODE="free.jin.JinApplet"
        ARCHIVE="jin.jar, libs/chess.jar, libs/util.jar, libs/jregex.jar, libs/timestamping.jar, servers/chessclub.jar, libs/console.jar, plugins/icc/console.jar, libs/board.jar, plugins/icc/board.jar, libs/seek.jar, plugins/icc/seek.jar, libs/sound.jar, plugins/icc/sound.jar"
		WIDTH="400" HEIGHT="300">
		
	<PARAM NAME="port" VALUE="5001">
		
	<PARAM NAME="server.classname"
	           VALUE="free.jin.chessclub.ChessclubServer">


			   
	<PARAM NAME="plugins.count" VALUE="4">
			   
	<PARAM NAME="plugins.0.classname"
	           VALUE="free.jin.console.icc.ChessclubConsoleManager">

	<PARAM NAME="plugins.1.classname"
	           VALUE="free.jin.board.icc.ChessclubBoardManager">
			   
	<PARAM NAME="plugins.2.classname"
	           VALUE="free.jin.seek.icc.ChessclubSoughtGraphPlugin">

	<PARAM NAME="plugins.3.classname"
	           VALUE="free.jin.sound.icc.ChessclubSoundManager">


			   
	<PARAM NAME="resources.boards" VALUE="cold-marble gray-tiles green-marble pale-wood plain red-marble slate winter wooden-dark icc/wooden-light">
	
	<PARAM NAME="resources.pieces" VALUE="icc/blitzin icc/bookup icc/dyche1 icc/dyche2 icc/dyche3 xboard">
			   
<!--

	Uncomment this to allow saving preferences.
	
	<PARAM NAME="savePrefsUrl" VALUE="save_prefs.php">			   
	<PARAM NAME="reserveSpaceUrl" VALUE="reserve_space.php">			   
			   
	<?php
		require "load_prefs.php";
	?>
	
-->
			   
			   
	Please enable Java to run Jin
</APPLET>
</CENTER>

</BODY>
