<?php
	$submit = $_POST['submit'];
	if ($submit){
		if (!(mysql_connect("localhost", "root") && mysql_select_db("@database_name@"))){
			echo "Couldn't connect to MySQL or select database<br>";
			echo mysql_error();
			return;	
		}
		
		// Create the Preferences table if it doesn't exist yet
		if (!mysql_query("SELECT * FROM Preferences")){
			if (!mysql_query("CREATE TABLE Preferences ( CookieKey varchar(20) NOT NULL, PrefsBlob blob, PRIMARY KEY (CookieKey), UNIQUE CookieKey (CookieKey) )")){
				echo "Couldn't create Preferences table<br>";
				echo mysql_error();
				return;
			}
		}
		
		// Create the KnownUsers table if it doesn't exist yet
		if (!mysql_query("SELECT * FROM KnownUsers")){
			if (!mysql_query("CREATE TABLE KnownUsers ( ICSUsername varchar(30) NOT NULL, CookieKey varchar(20), PRIMARY KEY (ICSUsername), UNIQUE ICSUsername (ICSUsername) )")){
				echo "Couldn't create KnownUsers table<br>";
				echo mysql_error();
				return;
			}
		}
		
		
		$username = $_POST['username'];
		$password = $_POST['password'];
		// Check username/password here.
		
		$authenticated = true;
		
		$username = strtolower($username);
		
		if ($authenticated){
			
			// Check whether a known user
			$result = mysql_fetch_array(mysql_query("SELECT CookieKey FROM KnownUsers WHERE ICSUsername = '$username'"));
			if ($result){
				$known_user = true;
				$prefs_key = $result[0];
			}
			else{
				$known_user = false;
				
				// Generate a random new CookieKey
				do{
					$prefs_key = sprintf("%s%s%s", mt_rand(0, 10000), mt_rand(0, 10000), mt_rand(0, 10000));
				} while (mysql_fetch_array(mysql_query("SELECT * FROM Preferences WHERE CookieKey = '$prefs_key'")));
				
				$insert_result = mysql_query("INSERT INTO KnownUsers VALUES ( '$username' , '$prefs_key' )");
				$insert_result = $insert_result && mysql_query("INSERT INTO Preferences ( CookieKey ) VALUES ( '$prefs_key' )"); 
			}
			
			setcookie("prefs_key", $prefs_key, time() + 60*60*24*365); // Expires in a year
		}
	}
		
?>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<TITLE> Reserve space for preferences </TITLE>
</HEAD>

<BODY>

	<?php
		if ($submit){
			if ($authenticated){
				if ($known_user){
					echo "<p>You already have space reserved for your preferences. Your key is:<br>";
					echo "<center><h2>$prefs_key</h2></center>";
				}
				else if ($insert_result){
					echo "<p>Space has been reserved for your preferences on the server. Your key is:<br>";
					echo "<center><h2>$prefs_key</h2></center>";
				}
				else{
					echo "An error occurred when reserving space for your preferences";	
				}
			}
			else{
			
			}	
		}
		else{
		?>
		
		<P>Please provide your username and password to reserve space for preferences:
		<P><form method="post" action="<?php echo $_SERVER['PHP_SELF'] ?>">
	
		<table>
			<tr><td>Username:</td><td><input type="Text" name="username"></td></tr>
			<tr><td>Password:</td><td><input type="Password" name="password"></td></tr>
		</table>
		<br>
		<input type="Submit" name="submit" value="Reserve space">
		
		<?php
		}
		?>
</form>

</BODY>
