/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package free.freechess;

import java.io.*;
import jregex.*;
import free.chess.Player;
import java.util.StringTokenizer;


/**
 * <P>This class implements an easy way to communicate with a freechess.org
 * server. It provides parsing of messages sent by the server and allows
 * receiving notifications of various events in an easy manner.
 * <P>Information usually arrives and is parsed/processed line by line.
 * Usage usually involves overriding one of the many
 * <code>processXXX(<arguments>)</code> methods and handling the arrived
 * information. The boolean value returned by the
 * <code>processXXX(<arguments>)</code> methods determines whether the arrived
 * information has been processed completely and shouldn't be processed any
 * further. Currently, returning <code>false</code> will mean that the line sent
 * by the server will be sent to the <code>processLine(String)</code> method as
 * well, but in the future "further processing" might include other procedures.
 * Since by default, all the <code>processXXX(parsed data)</code> methods return
 * <code>false</code>, the information usually handled by any methods you don't
 * override will end up in <code>processLine(String)</code> which you can easily
 * use for printing the output to the screen or a file.
 */

public class FreechessConnection extends free.util.Connection implements Runnable{



  /**
   * A regular expression string matching a FICS username.
   */

  private static final String usernameRegex = "[A-z]{3,17}";



  /**
   * A regular expression string for matching FICS titles.
   */

  private static final String titlesRegex = "\\([A-Z\\*\\(\\)]*\\)";



  /**
   * The lock we wait on when logging.
   */

  private final Object loginLock = new String("Login Lock");




  /**
   * The OutputStream to the server.
   */
  
  private OutputStream out;




  /**
   * The current board sending "style".
   */

  private int style = 1;




  /**
   * <code>true</code> when seek information is turned on, <code>false</code>
   * when off.
   */

  private boolean seekInfoOn = false;




  /**
   * The value we're supposed to assign to the interface variable during login.
   */

  private String interfaceVar = "Java freechess.org library by Alexander Maryanovsky";




  /**
   * Creates a new FreechessConnection to the given hostname, on the given port,
   * with the given username and password.
   */

  public FreechessConnection(String hostname, int port, String username, String password){
    super(hostname, port, username, password);
  }




  /**
   * Creates a new ReaderThread that will do the reading from the server.
   */

  protected Thread createReaderThread() throws IOException{
    return new Thread(this);
  }




  /**
   * Sets the style. If the ChessclubConnection is already logged in, then
   * a "set style <style>" command is send immediately, otherwise, the setting
   * is saved and sent immediately after logging in.
   */

  public synchronized final void setStyle(int style){
    this.style = style;
    if (isLoggedIn())
      sendCommand("set style "+style);
  }




  /**
   * Sets the interface variable to have the given value. This works only if the
   * FreechessConnection is not logged on yet, otherwise, throws an 
   * IllegalArgumentException. The actual interface variable will be set during 
   * the login procedure.
   */

  public synchronized final void setInterface(String interfaceVar){
    if (isLoggedIn())
      throw new IllegalStateException();

    this.interfaceVar = interfaceVar;
  }




  /**
   * Sets the state of seek information sending. If the passed argument is
   * <code>true</code>, the server will be asked to send seek information, if
   * <code>false</code>, it will be asked not to send it. If the
   * FreechessConnection is not logged on yet, the setting will be saved and
   * sent to the server on login.
   */

  public synchronized final void setSeekInfoState(boolean state){
    if (seekInfoOn == state)
      return;

    seekInfoOn = state;

    if (isLoggedIn()){
      sendCommand("iset seekinfo " + (seekInfoOn ? "1" : "0"));
      sendCommand("iset seekremove " + (seekInfoOn ? "1" : "0")); // This is not really needed, but "help iv_seekinfo" says it is.
    }
  }




  /**
   * Returns a Player object corresponding to the specified string. If the
   * string is "W", returns <code>Player.WHITE</code>. If it's "B", returns
   * <code>Player.BLACK</code>. Otherwise, throws an IllegalArgumentException.
   */

  public static Player playerForString(String s){
    if (s.equals("B"))
      return Player.BLACK_PLAYER;
    else if (s.equals("W"))
      return Player.WHITE_PLAYER;
    else
      throw new IllegalArgumentException("Bad player string: "+s);
  }



  /**
   * Logs in.
   */

  protected void login() throws IOException{
    out = sock.getOutputStream();

    sendCommand(getRequestedUsername());
    sendCommand(getPassword());

    synchronized(loginLock){
      try{
        loginLock.wait(); // Wait until we receive the login line.
      } catch (InterruptedException e){
          throw new InterruptedIOException(e.getMessage());
        } 
    }
  }




  /**
   * Sets the various things we need to set on login.
   */

  protected void onLogin(){
    super.onLogin();

    sendCommand("set style "+style);
    sendCommand("set interface "+interfaceVar);
    sendCommand("iset nowrap 1");
    sendCommand("iset ms 1");
    if (seekInfoOn){
      sendCommand("iset seekinfo 1");
      sendCommand("iset seekremove 1"); // This is not really needed, but "help iv_seekinfo" says it is, so we'll do it :-)
    }
//    sendCommand("iset lock 1");
  }





  /**
   * Sends the given command to the server. You should not include the end of
   * line symbol in the command.
   */

  public void sendCommand(String command){
    if (!isConnected())
      throw new IllegalStateException("Not connected");

    System.out.println("SENDING COMMAND: "+command);

    try{
      out.write(command.getBytes());
      out.write('\n');
      out.flush();
    } catch (IOException e){
        e.printStackTrace();
        try{
          sock.close(); // Disconnect
        } catch (IOException ex){
            ex.printStackTrace();
          }
      }
  }




  /**
   * This method is called when a line of text that isn't identified as some
   * known type of information arrives from the server.
   */

  protected void processLine(String line){}




  /**
   * This method is called to process disconnection from the server.
   */

  protected void processDisconnection(){}





  /**
   * This method is called by the reader thread when the connection the server
   * is terminated.
   */

  private synchronized void handleDisconnection(){
    if (isConnected())
      try{
        disconnect();
      } catch (IOException e){
          e.printStackTrace();
        }

    processDisconnection();
  }




  /**
   * This method is called by the reader thread when a line of text arrives from
   * the server. The method is responsible for determining the type of the
   * information, parsing it and sending it for further processing.
   */

  private void handleLine(String line){
    if (handleGameInfo(line))
      return;
    if (handleStyle12(line))
      return;
    if (handleDeltaBoard(line))
      return;
    if (handleSeeksCleared(line))
      return;
    if (handleSeekAdded(line))
      return;
    if (handleSeeksRemoved(line))
      return;
    if (handleGameEnd(line))
      return;
    if (handleStoppedObserving(line))
      return;
    if (handleStoppedExamining(line))
      return;
    if (handleIllegalMove(line))
      return;
    if (handleChannelTell(line))
      return;
    if (handleLogin(line))
      return;
    if (handlePersonalTell(line))
      return;
    if (handleSayTell(line))
      return;
    if (handlePTell(line))
      return;
    if (handleShout(line))
      return;
    if (handleIShout(line))
      return;
    if (handleTShout(line))
      return;
    if (handleCShout(line))
      return;
    if (handleAnnouncement(line))
      return;
    if (handleKibitz(line))
      return;
    if (handleWhisper(line))
      return;

    processLine(line);
  }




  /**
   * The regular expression matching login confirmation lines.
   * Example: "**** Starting FICS session as AlexTheGreat ****"
   */

  private static final Pattern loginPattern =
    new Pattern("^\\*\\*\\*\\* Starting FICS session as ("+usernameRegex+")("+titlesRegex+")? \\*\\*\\*\\*");




  /**
   * Called to determine if the given line of text is a login confirming line
   * and to handle that information if it is. Returns <code>true</code> if the
   * given line is a login confirming line, otherwise, returns
   * <code>false</code>.
   */

  private boolean handleLogin(String line){
    Matcher matcher = loginPattern.matcher(line);
    if (!matcher.find())
      return false;

    synchronized(loginLock){
      setUsername(matcher.group(1));
      loginLock.notify();
    }

    processLine(line);

    return true;
  }




  /**
   * The regular expression matching personal tells.
   */

  private static final Pattern personalTellPattern = 
    new Pattern("^("+usernameRegex+")("+titlesRegex+")? tells you: (.*)");




  /**
   * Called to determine whether the given line of text is a personal tell and
   * to further process it if it is.
   */

  private boolean handlePersonalTell(String line){
    Matcher matcher = personalTellPattern.matcher(line);
    if (!matcher.find())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String message = matcher.group(3);

    if (!processPersonalTell(username, titles, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a personal tell is received. 
   */

  protected boolean processPersonalTell(String username, String titles, String message){return false;}




  /**
   * The regular expression matching "say" tells.
   */

  private static final Pattern sayPattern = 
    new Pattern("^("+usernameRegex+")("+titlesRegex+")?(\\[(\\d+)\\])? says: (.*)");




  /**
   * Called to determine whether the given line of text is a "say" tell and
   * to further process it if it is.
   */

  private boolean handleSayTell(String line){
    Matcher matcher = sayPattern.matcher(line);
    if (!matcher.find())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String gameNumberString = matcher.group(4);
    String message = matcher.group(5);

    int gameNumber = gameNumberString == null ? -1 : Integer.parseInt(gameNumberString);

    if (!processSayTell(username, titles, gameNumber, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a "say" tell is received. The
   * <code>gameNumber</code> argument will contain -1 if the game number was not
   * specified.
   */

  protected boolean processSayTell(String username, String titles, int gameNumber, String message){return false;}




  /**
   * The regular expression matching "ptell" tells.
   */

  private static final Pattern ptellPattern = 
    new Pattern("^("+usernameRegex+")("+titlesRegex+")? \\(your partner\\) tells you: (.*)");




  /**
   * Called to determine whether the given line of text is a "ptell" tell and
   * to further process it if it is.
   */

  private boolean handlePTell(String line){
    Matcher matcher = ptellPattern.matcher(line);
    if (!matcher.find())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String message = matcher.group(3);

    if (!processPTell(username, titles, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a "ptell" tell is received. 
   */

  protected boolean processPTell(String username, String titles, String message){return false;}




  /**
   * The regular expression matching channel tells.
   */

  private static final Pattern channelTellPattern = 
    new Pattern("^("+usernameRegex+")("+titlesRegex+")?\\((\\d+)\\): (.*)");




  /**
   * Called to determine whether the given line of text is a channel tell and
   * to further process it if it is.
   */

  private boolean handleChannelTell(String line){
    Matcher matcher = channelTellPattern.matcher(line);
    if (!matcher.find())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String channelNumberString = matcher.group(3);
    String message = matcher.group(4);

    int channelNumber = Integer.parseInt(channelNumberString);

    if (!processChannelTell(username, titles, channelNumber, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a channel tell is received. 
   */

  protected boolean processChannelTell(String username, String titles, int channelNumber, String message){return false;}




  /**
   * The regular expression matching kibitzes.
   */

  private static final Pattern kibitzPattern = 
    new Pattern("^("+usernameRegex+")("+titlesRegex+")?\\(([-0-9]+)\\)\\[(\\d+)\\] kibitzes: (.*)");




  /**
   * Called to determine whether the given line of text is a kibitz and
   * to further process it if it is.
   */

  private boolean handleKibitz(String line){
    Matcher matcher = kibitzPattern.matcher(line);
    if (!matcher.find())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String ratingString = matcher.group(3);
    String gameNumberString = matcher.group(4);
    String message = matcher.group(5);

    int rating = (ratingString != null) && !ratingString.equals("----") ? Integer.parseInt(ratingString) : -1;
    int gameNumber = Integer.parseInt(gameNumberString);

    if (!processKibitz(username, titles, rating, gameNumber, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a kibitz is received. The rating argument
   * contains -1 if the player is unrated or otherwise doesn't have a rating.
   */

  protected boolean processKibitz(String username, String titles, int rating, int gameNumber, String message){return false;}





  /**
   * The regular expression matching whispers.
   */

  private static final Pattern whisperPattern = 
    new Pattern("^("+usernameRegex+")("+titlesRegex+")?\\(([-0-9]+)\\)\\[(\\d+)\\] whispers: (.*)");




  /**
   * Called to determine whether the given line of text is a whisper and
   * to further process it if it is.
   */

  private boolean handleWhisper(String line){
    Matcher matcher = whisperPattern.matcher(line);
    if (!matcher.find())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String ratingString = matcher.group(3);
    String gameNumberString = matcher.group(4);
    String message = matcher.group(5);

    int rating = (ratingString != null) && !ratingString.equals("----") ? Integer.parseInt(ratingString) : -1;
    int gameNumber = Integer.parseInt(gameNumberString);

    if (!processWhisper(username, titles, rating, gameNumber, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a whisper is received. The rating argument
   * contains -1 if the player is unrated or otherwise doesn't have a rating.
   */

  protected boolean processWhisper(String username, String titles, int rating, int gameNumber, String message){return false;}



  
  /**
   * The regular expression matching shouts.
   */

  private static final Pattern shoutPattern = 
    new Pattern("^("+usernameRegex+")("+titlesRegex+")? shouts: (.*)");




  /**
   * Called to determine whether the given line of text is a shout and
   * to further process it if it is.
   */

  private boolean handleShout(String line){
    Matcher matcher = shoutPattern.matcher(line);
    if (!matcher.find())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String message = matcher.group(3);

    if (!processShout(username, titles, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a shout is received. 
   */

  protected boolean processShout(String username, String titles, String message){return false;}




  /**
   * The regular expression matching "ishouts".
   */

  private static final Pattern ishoutPattern = 
    new Pattern("^--> ("+usernameRegex+")("+titlesRegex+")? ?(.*)");




  /**
   * Called to determine whether the given line of text is an "ishout" and
   * to further process it if it is.
   */

  private boolean handleIShout(String line){
    Matcher matcher = ishoutPattern.matcher(line);
    if (!matcher.find())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String message = matcher.group(3);

    if (!processIShout(username, titles, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when an "ishout" is received. 
   */

  protected boolean processIShout(String username, String titles, String message){return false;}





  /**
   * The regular expression matching "tshouts".
   */

  private static final Pattern tshoutPattern = 
    new Pattern("^:("+usernameRegex+")("+titlesRegex+")? t-shouts: (.*)");




  /**
   * Called to determine whether the given line of text is a "tshout" and
   * to further process it if it is.
   */

  private boolean handleTShout(String line){
    Matcher matcher = tshoutPattern.matcher(line);
    if (!matcher.find())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String message = matcher.group(3);

    if (!processTShout(username, titles, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a "tshout" is received. 
   */

  protected boolean processTShout(String username, String titles, String message){return false;}





  /**
   * The regular expression matching "cshouts".
   */

  private static final Pattern cshoutPattern = 
    new Pattern("^("+usernameRegex+")("+titlesRegex+")? c-shouts: (.*)");




  /**
   * Called to determine whether the given line of text is a "cshout" and
   * to further process it if it is.
   */

  private boolean handleCShout(String line){
    Matcher matcher = cshoutPattern.matcher(line);
    if (!matcher.find())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String message = matcher.group(3);

    if (!processCShout(username, titles, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a "cshout" is received. 
   */

  protected boolean processCShout(String username, String titles, String message){return false;}




  /**
   * The regular expression matching announcements.
   */

  private static final Pattern announcementPattern = 
    new Pattern("^    \\*\\*ANNOUNCEMENT\\*\\* from ("+usernameRegex+"): (.*)");




  /**
   * Called to determine whether the given line of text is an announcement and
   * to further process it if it is.
   */

  private boolean handleAnnouncement(String line){
    Matcher matcher = announcementPattern.matcher(line);
    if (!matcher.find())
      return false;

    String username = matcher.group(1);
    String message = matcher.group(2);

    if (!processAnnouncement(username, message))
      processLine(line);

    return true;
  }




  /**
   * This method is called when an announcement is received. 
   */

  protected boolean processAnnouncement(String username, String message){return false;}




  /**
   * The regular expression matching gameinfo lines.
   */

  private static final Pattern gameinfoPattern = new Pattern("^<g1> .*");




  /**
   * Called to determine whether the given line of text is a gameinfo line and
   * to further process it if it is.
   */

  private boolean handleGameInfo(String line){
    Matcher matcher = gameinfoPattern.matcher(line);
    if (!matcher.find())
      return false;

    GameInfoStruct data = GameInfoStruct.parseGameInfoLine(line);

    if (!processGameInfo(data))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a gameinfo line is received. To turn gameinfo
   * lines on, use <code>sendCommand("iset gameinfo 1")</code>
   */

  protected boolean processGameInfo(GameInfoStruct data){return false;}




  
  /**
   * The regular expression matching style12 lines.
   */

  private static final Pattern style12Pattern = new Pattern("^<12> .*");




  /**
   * Called to determine whether the given line of text is a style12 line and
   * to further process it if it is.
   */

  private boolean handleStyle12(String line){
    Matcher matcher = style12Pattern.matcher(line);
    if (!matcher.find())
      return false;

    Style12Struct data = Style12Struct.parseStyle12Line(line);

    if (!processStyle12(data))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a style12 line is received. To turn on style 12,
   * use <code>setStyle(12)</code>.
   */

  protected boolean processStyle12(Style12Struct data){return false;}




  /**
   * The regular expression matching delta board lines.
   */

  private static final Pattern deltaBoardPattern = new Pattern("^<d1> .*");




  /**
   * Called to determine whether the given line of text is a delta board line
   * and to further process it if it is.
   */

  private boolean handleDeltaBoard(String line){
    Matcher matcher = deltaBoardPattern.matcher(line);
    if (!matcher.find())
      return false;

    DeltaBoardStruct data = DeltaBoardStruct.parseDeltaBoardLine(line);

    if (!processDeltaBoard(data))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a delta board line is received. To turn delta
   * board on, use <code>sendCommand("iset compressmove 1")</code>. Note,
   * however, that it will disable the sending of a full board (like a style12
   * board) in some cases.
   */

  protected boolean processDeltaBoard(DeltaBoardStruct data){return false;}




  /**
   * The regular expression matching game end lines, like the following:<br>
   * {Game 6 (Strakh vs. Svag) Strakh forfeits on time} 0-1.
   */

  private static final Pattern gameEndPattern = 
    new Pattern("^\\{Game (\\d+) \\(("+usernameRegex+") vs\\. ("+usernameRegex+")\\) ([^\\}]+)\\} (.*)");




  /**
   * Called to determine whether the given line of text is a game end line
   * and to further process it if it is.
   */

  private boolean handleGameEnd(String line){
    Matcher matcher = gameEndPattern.matcher(line);
    if (!matcher.find())
      return false;

    int gameNumber = Integer.parseInt(matcher.group(1));
    String whiteName = matcher.group(2);
    String blackName = matcher.group(3);
    String reason = matcher.group(4);
    String result = matcher.group(5);

    if (!processGameEnd(gameNumber, whiteName, blackName, reason, result))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a game end line is received.
   */

  protected boolean processGameEnd(int gameNumber, String whiteName, String blackName, String reason, String result){return false;}




  /**
   * The regular expression matching lines specifying that we've stopped
   * observing a game, like the following:<br>
   * Removing game 7 from observation list.
   */

  private static final Pattern stoppedObservingPattern = 
    new Pattern("^Removing game (\\d+) from observation list\\.$");




  /**
   * Called to determine whether the given line of text is a line specifying
   * that we've stopped observing a game and to further process it if it is.
   */

  private boolean handleStoppedObserving(String line){
    Matcher matcher = stoppedObservingPattern.matcher(line);
    if (!matcher.find())
      return false;

    int gameNumber = Integer.parseInt(matcher.group(1));

    if (!processStoppedObserving(gameNumber))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a line specifying that we've stopped observing a
   * game is received.
   */

  protected boolean processStoppedObserving(int gameNumber){return false;}




  /**
   * The regular expression matching lines specifying that we've stopped
   * examining a game, like the following:<br>
   * You are no longer examining game 114.
   */

  private static final Pattern stoppedExaminingPattern = 
    new Pattern("^You are no longer examining game (\\d+)\\.$");




  /**
   * Called to determine whether the given line of text is a line specifying
   * that we've stopped examining a game and to further process it if it is.
   */

  private boolean handleStoppedExamining(String line){
    Matcher matcher = stoppedExaminingPattern.matcher(line);
    if (!matcher.find())
      return false;

    int gameNumber = Integer.parseInt(matcher.group(1));

    if (!processStoppedExamining(gameNumber))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a line specifying that we've stopped examining a
   * game is received.
   */

  protected boolean processStoppedExamining(int gameNumber){return false;}




  /**
   * The regular expression matching lines specifying that an illegal move has
   * been attempted.
   */

  private static final Pattern illegalMovePattern = 
    new Pattern("^Illegal move \\((.*)\\)\\.$");




  /**
   * Called to determine whether the given line of text is a line specifying
   * that an illegal move has been attempted and to further process it if it is.
   */

  private boolean handleIllegalMove(String line){
    Matcher matcher = illegalMovePattern.matcher(line);
    if (!matcher.find())
      return false;

    String moveString = matcher.group(1);

    if (!processIllegalMove(moveString))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a line specifying that an illegal move has been
   * attempted is received. The specified string is the move string that was
   * sent to the server.
   */

  protected boolean processIllegalMove(String moveString){return false;}





  /**
   * The regular expression matching lines specifying that all seeks have been
   * cleared.
   */

  private static final Pattern seeksClearedPattern = new Pattern("^<sc>$");




  /**
   * Called to determine whether the given line of text is a line specifying
   * that all seeks have been cleared.
   */

  private boolean handleSeeksCleared(String line){
    Matcher matcher = seeksClearedPattern.matcher(line);
    if (!matcher.find())
      return false;

    if (!processSeeksCleared())
      processLine(line);

    return true;
  }




  /**
   * This method is called when a line specifying that all seeks have been
   * cleared is received.
   */

  protected boolean processSeeksCleared(){return false;}





  /**
   * The regular expression matching lines specifying that a new seek has been
   * added.
   */

  private static final Pattern seekAddedPattern = new Pattern("^<s> .*");




  /**
   * Called to determine whether the given line of text is a line specifying
   * that a new seek has been added.
   */

  private boolean handleSeekAdded(String line){
    Matcher matcher = seekAddedPattern.matcher(line);
    if (!matcher.find())
      return false;

    SeekInfoStruct seekInfo = SeekInfoStruct.parseSeekInfoLine(line);

    if (!processSeekAdded(seekInfo))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a line specifying that a new seek has been added
   * is received.
   */

  protected boolean processSeekAdded(SeekInfoStruct seekInfo){return false;}





  /**
   * The regular expression matching lines specifying that seeks have been
   * removed.
   */

  private static final Pattern seeksRemovedPattern = new Pattern("^<sr> .*");




  /**
   * Called to determine whether the given line of text is a line specifying
   * that seeks have been removed.
   */

  private boolean handleSeeksRemoved(String line){
    Matcher matcher = seeksRemovedPattern.matcher(line);
    if (!matcher.find())
      return false;

    StringTokenizer tokenizer = new StringTokenizer(line, " ");
    tokenizer.nextToken(); // Skip the "<sr>"

    int [] removedSeeks = new int[tokenizer.countTokens()];
    for (int i = 0; i < removedSeeks.length; i++)
      removedSeeks[i] = Integer.parseInt(tokenizer.nextToken());

    if (!processSeeksRemoved(removedSeeks))
      processLine(line);

    return true;
  }




  /**
   * This method is called when a line specifying that seeks have been removed
   * is received. The array specifies the numbers of the removed seeks.
   */

  protected boolean processSeeksRemoved(int [] removedSeeks){return false;}




  /**
   * The run() method. This is called by the reader thread. Continuously reads
   * lines of text from the server and sends them for further processing.
   */

  public void run(){
    try{
      final String prompt = "fics% ";
      final int promptLength = prompt.length();

      InputStream in = new BufferedInputStream(sock.getInputStream());

      StringBuffer buf = new StringBuffer();
      int b;
      mainLoop: while ((b = in.read()) != -1){
        if (b == '\r')
          System.out.print("\\r");
        else if (b == '\n')
          System.out.print("\\n");
        System.out.print((char)b);
        if (b == '\r')
          continue;
        else if (b == '\n'){
          String s = buf.toString();
          while (s.startsWith(prompt)){
            s = s.substring(promptLength);
            if (s.length() == 0) // An all prompt line
              continue mainLoop;
          }
          System.out.print("Sending line: \""+s+"\"");
          execRunnable(new HandleLineRunnable(s));
          buf.setLength(0);
        }
        else{
          buf.append((char)b);
        }
      }
    } catch (IOException e){}
    execRunnable(new Runnable(){

      public void run(){
        handleDisconnection();
      }

    });
  }




  /**
   * A runnable which takes a String and invokes handleLine(String) with it when
   * run. Used by the reader thread to communicate with the data handling code.
   */

  private class HandleLineRunnable implements Runnable{


    /**
     * The string.
     */

    private final String line;




    /**
     * Creates a new HandleLineRunnable with the specified string.
     */

    public HandleLineRunnable(String line){
      this.line = line;
    }




    /**
     * Calls the outer class' handleLine method with the string given in the
     * constructor.
     */

    public void run(){
      handleLine(line);
    }


  }



}

