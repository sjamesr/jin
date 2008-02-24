<?php
	include "begin.php";
	require_once "server_support.php";
?>

<div id="@APP.ID@Applet" style="display:none;width:450px;height:250px">
	<applet code="free.jin.JinApplet"
        	archive="@CLASSPATH@"
			width="450" height="250">
		
		<param name="prefsProtocol" value="http<?php if (isSSLPrefs()) echo "s"?>">
		<param name="loadPrefsURL" value="prefs.php?loadPrefs">
		<param name="savePrefsURL" value="prefs.php?savePrefs">
		
		<param name="bgcolor" value="<?php echo getBackgroundColor(); ?>">
		<param name="port" value="<?php echo getPort(); ?>">
		
		<param name="server.classname" value="@SERVER.CLASSNAME@">
		
		<param name="plugin.classnames" value="@PLUGIN.CLASSNAMES@">
		
		<param name="action.classnames" value="@ACTION.CLASSNAMES@">
		
	</applet>
</div>

<p id="popupRequest" style="display:none;">
	Please disable the popup blocker for this page and reload it if necessary; @APP.NAME@ Applet needs this to run properly.
	<a href="#" id="runWithPopupBlocker" onClick="showApplet(); document.getElementById('runWithPopupBlocker').style.display='none'; return false;">Run @APP.NAME@ Applet anyway.</a>
</p>

<p id="noJS">
	Please enable Java and JavaScript for this page to work.
</p>

<script type="text/javascript">

	function showApplet(){
		var applet = document.getElementById("@APP.ID@Applet");
		applet.style.display="block";
	}

	document.getElementById("noJS").style.display="none";
	var win = window.open("/", "dummy", "width=100,height=100");
	
	var blockerDisabled = false;
	
	blockerDisabled = blockerDisabled || (win != null);
	blockerDisabled = blockerDisabled || (win != undefined);

	// Special case for Opera (9.25)
	if ((win != null) && (win != undefined) && (win["close"] == undefined))
		blockerDisabled = false;

	if (blockerDisabled){
		showApplet();
		win.close();
	}
	else{
		document.getElementById("popupRequest").style.display="block";
	}
	
</script>


<?php
  include "end.php";
?>
