<?php
	include "begin.php";
	require_once 'server_support.php';
?>

<APPLET CODE="free.jin.JinApplet"
        ARCHIVE="@CLASSPATH@"
		WIDTH="450" HEIGHT="250">
	
	<PARAM NAME="prefsProtocol" VALUE="http<?php if (isSSLPrefs()) echo "s"?>">
	<PARAM NAME="loadPrefsURL" VALUE="prefs.php?loadPrefs">
	<PARAM NAME="savePrefsURL" VALUE="prefs.php?savePrefs">
	
	<PARAM NAME="bgcolor" VALUE="<?php echo getBackgroundColor(); ?>">
	<PARAM NAME="port" VALUE="<?php echo getPort(); ?>">
		
	
	<PARAM NAME="server.classname" VALUE="@SERVER.CLASSNAME@">
			   
	<PARAM NAME="plugin.classnames" VALUE="@PLUGIN.CLASSNAMES@">
			   
	<PARAM NAME="action.classnames" VALUE="@ACTION.CLASSNAMES@">
			   
</APPLET>

<?php
  include "end.php";
?>
