<?php
	include "begin.php";
	require_once 'jin_server.php';
?>

<APPLET CODE="free.jin.JinApplet"
        ARCHIVE="jin.jar, chess.jar, util.jar, libs/fics/timesealing.jar, servers/freechess.jar, libs/console.jar, plugins/fics/console.jar, libs/board.jar, plugins/fics/board.jar, libs/seek.jar, plugins/fics/seek.jar, libs/sound.jar, plugins/fics/sound.jar, plugins/actions.jar, actions/getserverhelp.jar, actions/askquestion.jar, actions/seek.jar"
		WIDTH="450" HEIGHT="250">
		
	<PARAM NAME="prefsProtocol" VALUE="http<?php if (isSSLPrefs()) echo "s"?>">
	<PARAM NAME="loadPrefsURL" VALUE="prefs.php?loadPrefs">
	<PARAM NAME="savePrefsURL" VALUE="prefs.php?savePrefs">
	
	<PARAM NAME="bgcolor" VALUE="<?php echo getBackgroundColor(); ?>">
	<PARAM NAME="port" VALUE="<?php echo getPort(); ?>">

	
	<PARAM NAME="server.classname" VALUE="free.jin.freechess.FreechessServer">
			   
	<PARAM NAME="plugins.count" VALUE="5">
	<PARAM NAME="plugins.0.classname" VALUE="free.jin.console.fics.FreechessConsoleManager">
	<PARAM NAME="plugins.1.classname" VALUE="free.jin.board.fics.FreechessBoardManager">
	<PARAM NAME="plugins.2.classname" VALUE="free.jin.seek.fics.FreechessSoughtGraphPlugin">
	<PARAM NAME="plugins.3.classname" VALUE="free.jin.sound.fics.FreechessSoundManager">
	<PARAM NAME="plugins.4.classname" VALUE="free.jin.actions.ActionsPlugin">
	
	<PARAM NAME="actions.count" VALUE="3">
	<PARAM NAME="actions.0.classname" VALUE="free.jin.action.getserverhelp.GetServerHelpAction">
	<PARAM NAME="actions.1.classname" VALUE="free.jin.action.askquestion.AskQuestionAction">
	<PARAM NAME="actions.2.classname" VALUE="free.jin.action.seek.SeekAction">
	
</APPLET>

<?php
  include "end.php";
?>
