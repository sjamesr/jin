/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
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

package free.jin;

import java.io.IOException;
import free.jin.event.*;
import free.chess.Move;
import free.chess.WildVariant;


/**
 * Defines the interface between the chess server and the client.
 * Some restrictions/rules on the implementation that can't be enforced by the compiler:
 * <UL>
 *   <LI> The implementation is responsible for firing events to registered listeners
 *        when needed. However, if a certain feature which is implied by
 *        calling the listener is not supported by the server, the implementation
 *        may ignore addListener and removeListener calls related to that feature
 *        or not fire events of that type.
 *   <LI> The calls to all the listeners must be done in the AWT event dispatching
 *        thread.
 *   <LI> The implementation is supposed to be completely self sufficient, meaning
 *        that only the methods specified in the interface are required to normally
 *        interact with it. For example, you can't require to call a special method
 *        to enable a certain feature - the implementation must decide by itself
 *        when to enable and when to disable the feature.
 *   <LI> The implementation must supress the textual message accompanying some event
 *        (sent by the server) when at least a single listener of that event is
 *        registered. On the other hand, the same textual message must NOT be
 *        supressed when there are no listeners registered.
 *   <LI> The implementation must be consistent in calling listeners. For example
 *        if a certain order of events is implied, the listeners must be called
 *        in that order - a MoveEvent must not be fired before a GameStartedEvent
 *        has been fired for that game or after a GameEndedEvent has been fired.
 *   <LI> If the server supports a certain feature only partially, the implementation
 *        is responsible for implementing the rest itself where possible
 *        and for providing default or blank values where impossible.
 * </UL>
 * Some assumptions the implementation is allowed to make:
 * <UL>
 *   <LI> Although it is not guaranteed that all calls to the Connection's methods
 *        will be done in any particular thread (including the AWT Event dispatching
 *        thread), the implementation may assume that all calls from user code
 *        will be sequential, i.e. no calls to any methods will be done by the 
 *        user code while one of the methods is executing from a thread other than
 *        the one executing that method.
 * </UL>
 *
 * The restrictions above are to be treated as assumptions for user code and the
 * assumptions above are to be treated as restrictions for user code.<P>
 *
 * If the server supports additional features beyond the specified in the
 * interface, it may add methods supporting these features. The server specific
 * user code, may then cast the implementation to its actual type and use
 * these methods. If certain features are supported in an extended form (more
 * information is sent by the server than is needed to support the interface),
 * the implementation may (but doesn't have to) fire subclasses of the appropriate
 * events (or return subclasses of objects in case of queries), adding the 
 * information sent by the server. The server specific user code handling the
 * event (or issuing the query), in turn, may (but again, does not have to)
 * cast the event (or returned object) to its actual type and retrieve the
 * extra information.
 */

public interface Connection{



  /**
   * The code for a game white won.
   */

  int WHITE_WON = 0;



  /**
   * The code for a game white lost.
   */

  int WHITE_LOST = 1;



  /**
   * The code for a drawn game.
   */

  int DRAWN = 2;

                                  

  /**
   * The code for an adjourned game.
   */

  int ADJOURNED = 3;



  /**
   * The code for an aborted game.
   */

  int ABORTED = 4;



  /**
   * Returns the <code>ListenerManager</code> for this <code>Connection</code>.
   * Via this object, you can register and unregister listeners for various
   * events.
   */

  ListenerManager getListenerManager();



  /**
   * Initiates a connect-and-login procedure. The actual procedure should be
   * performed asynchronously, and this method should return without blocking.
   */

  void initiateConnectAndLogin(String hostname, int port);



  /**
   * Returns true if this Connection is currently connected to the server,
   * returns false otherwise.
   */

  boolean isConnected();



  /**
   * Closes the connection. The actual disconnection should be performed
   * asynchronously, and this method should return without blocking.
   * This method may be invoked at any time, even if the connection is
   * not currently connected or logged in.
   */

  void close() throws IOException;



  /**
   * Returns the username of the account. This method should only be called after
   * connect() returns.
   */

  String getUsername();



  /**
   * Sends an arbitrary string to the server. Use this only for server specific
   * commands or commands issued manually by the user. Using this method for
   * sending other commands makes your client incompatible with other servers
   * besides the one you are using.
   */

  void sendCommand(String command);



  /**
   * Sends the command closing the session to the server - this is usually "quit",
   * or "exit". This method will not be called unless the connection is connected.
   */

  void exit();
  
  
  
  /**
   * Returns a list of support wild variants.
   */
   
  WildVariant [] getSupportedVariants();



  /**
   * Quits the given game. Whatever relation I had to this game is closed, this
   * may mean, unobserving, resigning, unexamining or any other operation
   * that closes the game. The passed Game object must be the one created by
   * the implementation of this class, and not just one you created yourself.
   */

  void quitGame(Game game);



  /**
   * Makes the given move in the given Game. The specified <code>Game</code>
   * must be an instance created by this <code>Connection</code>.
   */

  void makeMove(Game game, Move move);



  /**
   * Resigns the given game. The given game must be a played game and of type
   * Game.MY_GAME.
   */

  void resign(Game game);



  /**
   * Sends a request to draw the given game, or if the opponent already offered
   * a draw, accepts it. The given game must be a played game and of type
   * <code>Game.MY_GAME</code>.
   */

  void requestDraw(Game game);



  /**
   * Returns whether the server supports aborting a game.
   */

  boolean isAbortSupported();



  /**
   * Sends a request to abort the given game, or if the opponent already offered
   * to abort, accepts it. The given game must be a played game and of type
   * <code>Game.MY_GAME</code>. This method should throw an
   * <code>UnsupportedOperationException</code> if
   * <code>isAbortSupported</code> returns <code>false</code>.
   */

  void requestAbort(Game game);



  /**
   * Returns whether the server supports adjourning a game.
   */

  boolean isAdjournSupported();



  /**
   * Sends a request to adjourn the given game, or if the opponent already
   * offered adjournment, accepts it. The given game must be a played game and
   * of type <code>Game.MY_GAME</code>. This method should throw an
   * <code>UnsupportedOperationException</code> if
   * <code>isAdjournSupported</code> returns <code>false</code>. 
   */

  void requestAdjourn(Game game);
  
  
  
  /**
   * Returns whether the server supports one ply takebacks.
   */
   
  boolean isTakebackSupported();
  
  
  
  /**
   * Sends a one ply takeback request in the given game, or if the opponent
   * already offered a one ply takeback, accepts it. The given game must be a
   * played game and of type <code>Game.MY_GAME</code>. This method should throw
   * an <code>UnsupportedOperationException</code> if
   * <code>isTakebackSupported</code> returns <code>false</code>.
   */
  
  void requestTakeback(Game game);
   

  
  /**
   * Returns whether the server supports multiple ply takebacks.
   */
   
  boolean isMultipleTakebackSupported();
  
  
  
  /**
   * Sends a takeback request for the specified amount of plies in the given
   * game, or if the opponent already offered a one ply takeback, accepts it.
   * The given game must be a played game and of type <code>Game.MY_GAME</code>.
   * This method should throw an <code>UnsupportedOperationException</code> if
   * <code>isTakebackSupported</code> returns <code>false</code>.
   */
  
  void requestTakeback(Game game, int plyCount);

  

  /**
   * Goes back the given amount of plies in the given game. If the given amount
   * of plies is bigger than the amount of plies since the beginning of the game,
   * goes to the beginning of the game.
   */

  void goBackward(Game game, int plyCount);



  /**
   * Goes forward the given amount of plies in the given game. If the given amount
   * of plies is bigger than the amount of plies remaining until the end of the
   * game, goes to the end of the game.
   */

  void goForward(Game game, int plyCount);



  /**
   * Goes to the beginning of the given game.
   */

  void goToBeginning(Game game);



  /**
   * Goes to the end of the given game.
   */

  void goToEnd(Game game);
  
  
  
  /**
   * Displays help about the server. 
   */
   
  void showServerHelp();
  
  
  
  /**
   * Sends the specified question string to the server's help channel.
   */
   
  void sendHelpQuestion(String question);
  

    
}
