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

package free.jin.freechess;

import free.jin.*;
import free.jin.event.*;
import free.chess.*;
import java.io.IOException;
import javax.swing.SwingUtilities;
import free.jin.event.JinListenerManager;
import free.freechess.FreechessConnection;
import free.util.EventListenerList;


/**
 * An implementation of the JinConnection interface for the freechess.org
 * server.
 */

public class JinFreechessConnection extends FreechessConnection implements JinConnection{



  /**
   * Our listener manager
   */

  private final FreechessJinListenerManager listenerManager = new FreechessJinListenerManager(this);




  /**
   * Creates a new JinFreechessConnection with the specified hostname, port,
   * requested username and password.
   */

  public JinFreechessConnection(String hostname, int port, String username, String password){
    super(hostname, port, username, password);

    setInterface(Jin.getInterfaceName());
  }




  /**
   * Returns our JinListenerManager.
   */

  public JinListenerManager getJinListenerManager(){
    return getFreechessJinListenerManager();
  }




  /**
   * Returns out JinListenerManager as a reference to FreechessJinListenerManager.
   */

  public FreechessJinListenerManager getFreechessJinListenerManager(){
    return listenerManager;
  }




  /**
   * Overrides createSocket() to fire a ConnectionEvent specifying that the connection
   * was established when super.createSocket() returns (in the Event dispatching
   * thread of course).
   */

  protected java.net.Socket createSocket(String hostname, int port) throws IOException{
    java.net.Socket sock = new free.freechess.timeseal.TimesealingSocket(hostname, port);
//    java.net.Socket sock = new java.net.Socket(hostname, port);

    execRunnable(new Runnable(){

      public void run(){
        listenerManager.fireConnectionEvent(new ConnectionEvent(JinFreechessConnection.this, ConnectionEvent.ESTABLISHED));
      }

    });

    return sock;
  }




  /**
   * Performs various on-login tasks. Also notifies all interested
   * ConnectionListeners that we've successfully logged in.
   */

  public void onLogin(){
    super.onLogin();

    listenerManager.fireConnectionEvent(new ConnectionEvent(this, ConnectionEvent.LOGGED_IN));
  }




  /**
   * Overrides processDisconnection() to fire a ConnectionEvent specifying that
   * the connection was lost.
   */

  protected void processDisconnection(){
    listenerManager.fireConnectionEvent(new ConnectionEvent(this, ConnectionEvent.LOST));
  }




  /**
   * Notifies any interested PlainTextListener of the received line of otherwise
   * unidentified text.
   */

  protected void processLine(String line){
    listenerManager.firePlainTextEvent(new PlainTextEvent(this, line));
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected void processPersonalTell(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "tell", username, (titles == null ? "" : titles), message, null));
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected void processSayTell(String username, String titles, int gameNumber, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "say", username, (titles == null ? "" : titles), message, new Integer(gameNumber)));
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected void processPTell(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "ptell", username, (titles == null ? "" : titles), message, null));
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected void processChannelTell(String username, String titles, int channelNumber, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "channel-tell", username, (titles == null ? "" : titles), message, new Integer(channelNumber)));
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected void processKibitz(String username, String titles, int rating, int gameNumber, String message){
    if (titles == null)
      titles = "";
    if (rating != -1)
      titles = titles+"("+rating+")";

    listenerManager.fireChatEvent(new ChatEvent(this, "kibitz", username, titles, message, new Integer(gameNumber)));
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected void processWhisper(String username, String titles, int rating, int gameNumber, String message){
    if (titles == null)
      titles = "";
    if (rating != -1)
      titles = titles+"("+rating+")";

    listenerManager.fireChatEvent(new ChatEvent(this, "whisper", username, titles, message, new Integer(gameNumber)));
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected void processShout(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "shout", username, (titles == null ? "" : titles), message, null));
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected void processIShout(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "ishout", username, (titles == null ? "" : titles), message, null));
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected void processTShout(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "tshout", username, (titles == null ? "" : titles), message, null));
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected void processCShout(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "cshout", username, (titles == null ? "" : titles), message, null));
  }




  /**
   * Sends the "exit" command to the server.
   */

  public void exit(){
    sendCommand("exit");
  }




  /**
   * Quits the specified game.
   */

  public void quitGame(Game game){
    throw new free.util.UnsupportedOperationException();
  }




  /**
   * Makes the given move in the given game.
   */

  public void makeMove(Game game, Move move) throws IllegalArgumentException{
    throw new free.util.UnsupportedOperationException();
  }




  /**
   * Resigns the given game. The given game must be a played game and of type
   * Game.MY_GAME.
   */

  public void resign(Game game){
    throw new free.util.UnsupportedOperationException();
  }



  /**
   * Sends a request to draw the given game. The given game must be a played 
   * game and of type Game.MY_GAME.
   */

  public void requestDraw(Game game){
    throw new free.util.UnsupportedOperationException();
  }




  /**
   * Returns <code>true</code>.
   */

  public boolean isAbortSupported(){
    return true;
  }




  /**
   * Sends a request to abort the given game. The given game must be a played 
   * game and of type Game.MY_GAME.
   */

  public void requestAbort(Game game){
    throw new free.util.UnsupportedOperationException();
  }



  /**
   * Returns <code>true</code>.
   */

  public boolean isAdjournSupported(){
    return true;
  }




  /**
   * Sends a request to adjourn the given game. The given game must be a played
   * game and of type Game.MY_GAME.
   */

  public void requestAdjourn(Game game){
    throw new free.util.UnsupportedOperationException();    
  }



  /**
   * Goes back the given amount of plies in the given game. If the given amount
   * of plies is bigger than the amount of plies since the beginning of the game,
   * goes to the beginning of the game.
   */

  public void goBackward(Game game, int plyCount){
    throw new free.util.UnsupportedOperationException();
  }




  /**
   * Goes forward the given amount of plies in the given game. If the given amount
   * of plies is bigger than the amount of plies remaining until the end of the
   * game, goes to the end of the game.
   */

  public void goForward(Game game, int plyCount){
    throw new free.util.UnsupportedOperationException();
  }




  /**
   * Goes to the beginning of the given game.
   */

  public void goToBeginning(Game game){
    throw new free.util.UnsupportedOperationException();
  }



  /**
   * Goes to the end of the given game.
   */

  public void goToEnd(Game game){
    throw new free.util.UnsupportedOperationException();
  }




  /**
   * Overrides ChessclubConnection.execRunnable(Runnable) to execute the
   * runnable on the AWT thread using SwingUtilities.invokeLater(Runnable), 
   * since this class is meant to be used by Jin, a graphical interface using 
   * Swing.
   *
   * @see ChessclubConnection#execRunnable(Runnable)
   * @see SwingUtilities.invokeLater(Runnable)
   */

  public void execRunnable(Runnable runnable){
    SwingUtilities.invokeLater(runnable);
  }


}