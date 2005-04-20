<?php
   $page_title = "Jin Applet";
   include("begin.php");
?>
 
<H1>
	<img src="/images/bullet.gif" align="absmiddle" width="12" height="12" border="1">
	<?php echo $page_title; ?>
</H1>


<?php
	require_once 'jin_server.php';
?>


<CENTER>
<?php echo beforeApplet(); ?>
<br>

<P>
<APPLET CODE="free.jin.JinApplet"
        ARCHIVE="jin.jar, <?php if ($_REQUEST['oldjava']) echo "swingall.jar," ?>chess.jar, util.jar, libs/jregex.jar, libs/icc/timestamping.jar, servers/chessclub.jar, libs/console.jar, plugins/icc/console.jar, libs/board.jar, plugins/icc/board.jar, libs/seek.jar, plugins/icc/seek.jar, libs/sound.jar, plugins/icc/sound.jar, plugins/actions.jar, actions/getserverhelp.jar, actions/askquestion.jar, actions/seek.jar"
		WIDTH="450" HEIGHT="250">
	
	<PARAM NAME="prefsProtocol" VALUE="http<?php if (isSSLPrefs()) echo "s"?>">
	<PARAM NAME="loadPrefsURL" VALUE="prefs.php?loadPrefs">
	<PARAM NAME="savePrefsURL" VALUE="prefs.php?savePrefs">
	
	<PARAM NAME="bgcolor" VALUE="<?php echo getBackgroundColor(); ?>">
	<PARAM NAME="port" VALUE="<?php echo getPort(); ?>">
		
	
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
			   
</APPLET>

<?php
	// This only works in IE, which waits for applets to load before firing
	// an onLoad event. This is ok, though, because the applet only takes
	// a long time to load under MS VM.

	if ($_REQUEST['oldjava']){
?>
	<H2 id="waitMessage">Jin is loading - this may take a few minutes</H2>
	<SCRIPT type="text/javascript">
		function clearWaitMessage(){
			document.getElementById('waitMessage').innerHTML = "";
		}
	</SCRIPT>
<?php
	}
?>
<br>
<?php echo afterApplet(); ?>

</CENTER>

<?php
  include("end.php");
?>
