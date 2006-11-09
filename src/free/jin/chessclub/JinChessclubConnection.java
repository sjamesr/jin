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

package free.jin.chessclub;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.*;

import javax.swing.SwingUtilities;

import free.chess.*;
import free.chess.variants.NoCastlingVariant;
import free.chess.variants.atomic.Atomic;
import free.chess.variants.fischerrandom.FischerRandom;
import free.chess.variants.giveaway.Giveaway;
import free.chess.variants.kriegspiel.Kriegspiel;
import free.chess.variants.shuffleboth.ShuffleBoth;
import free.chessclub.ChessclubConnection;
import free.chessclub.ChessclubConstants;
import free.chessclub.level2.Datagram;
import free.chessclub.level2.DatagramEvent;
import free.chessclub.level2.DatagramListener;
import free.jin.*;
import free.jin.chessclub.event.ArrowEvent;
import free.jin.chessclub.event.ChessEventEvent;
import free.jin.chessclub.event.CircleEvent;
import free.jin.event.*;
import free.util.Pair;
import free.util.TextUtilities;
import free.util.Utilities;



/**
 * An implementation of the JinConnection interface (and several subinterfaces) 
 * for the chessclub.com server.
 */

public class JinChessclubConnection extends ChessclubConnection implements DatagramListener,
    Connection, SeekConnection, GameListConnection, PGNConnection{



  /**
   * Our listener manager.
   */

  private final ChessclubListenerManager listenerManager = new ChessclubListenerManager(this);


 
  /**
   * Creates a new JinChessclubConnection with the specified details.
   */

  public JinChessclubConnection(String username, String password){
    super(username, password, System.out);

    setInterface(Jin.getInstance().getAppName() + " " + Jin.getInstance().getAppVersion() +
      " (" + System.getProperty("java.vendor") + " " + System.getProperty("java.version") +
      ", " + System.getProperty("os.name") + " " + getSafeOSVersion() + ")");
  }
  
  
  
  /**
   * Returns the OS version after stripping out the patch level from it.
   * We do this to avoid revealing that information to everyone on the server.
   */
  
  private static String getSafeOSVersion(){
    String osVersion = System.getProperty("os.version");
    int i  = osVersion.indexOf(".", osVersion.indexOf(".") + 1);
    if (i != -1)
      osVersion = osVersion.substring(0, i) + ".x";
    
    return osVersion;
  }




  /**
   * Returns the listener manager as its actual type for use by ICC specific
   * code.
   */

  public ChessclubListenerManager getChessclubListenerManager(){
    return listenerManager;
  }



  /**
   * Returns the listener manager.
   */

  public ListenerManager getListenerManager(){
    return getChessclubListenerManager();
  }




  /**
   * Fires an "attempting" connection event and invokes {@link free.util.Connection#initiateConnect(String, int)}.
   */
  
  public void initiateConnectAndLogin(String hostname, int port){
    listenerManager.fireConnectionAttempted(this, hostname, port);

    initiateConnect(hostname, port);
  }
  
  
  
  /**
   * Fires an "established" connection event.
   */
  
  protected void handleConnected(){
    listenerManager.fireConnectionEstablished(this);
    
    super.handleConnected();
  }
  
  
  
  /**
   * Fires a "failed" connection event.
   */
  
  protected void handleConnectingFailed(IOException e){
    listenerManager.fireConnectingFailed(this, e.getMessage());
    
    super.handleConnectingFailed(e);
  }
  
  
  
  /**
   * Fires a "login succeeded" connection event and performs other on-login tasks.
   */
  
  protected void handleLoginSucceeded(){
    sendCommand("set-quietly wrap 0");

    // Hack, currently, the server has a bug which causes it not to send us
    // the current event list even if we have turned DG_TOURNEY on at the login
    // line. Remove when Bert fixes it.
    if (isDGOn(Datagram.DG_TOURNEY))
      setDGOnAgain(Datagram.DG_TOURNEY);

    listenerManager.fireLoginSucceeded(this);
    
    super.handleLoginSucceeded();
  }
  
  
  
  /**
   * Fires a "login failed" connection event.
   */
  
  protected void handleLoginFailed(String reason){
    listenerManager.fireLoginFailed(this, reason);
    
    super.handleLoginFailed(reason);
  }
  
  
  
  /**
   * Fires a "connection lost" connection event.
   */
  
  protected void handleDisconnection(IOException e){
    listenerManager.fireConnectionLost(this);
    
    super.handleDisconnection(e);
  }



  /**
   * Converts the given title to a displayable string. If the given title is empty,
   * returns an empty string, otherwise, returns the given string surrounded by
   * parentheses.
   */

  public static String displayableTitle(String title){
    if (title.length() == 0)
      return "";
    else
      return "("+title+")";
  }



  /**
   * Given the title of a player determines whether he's a computer player.
   * The title must consist of single title strings separated by spaces.
   * Example: "WGM C *".
   */

  public static boolean isComputer(String title){
    StringTokenizer tokenizer = new StringTokenizer(title," ");
    while (tokenizer.hasMoreTokens()){
      String token = tokenizer.nextToken();
      if (token.equals("C"))
        return true;
    }

    return false;
  }




  /**
   * Given the title of a player determines whether he's an unrated (guest)
   * player. The title must consist of single title strings separated by spaces.
   * Example: "WGM C *".
   */

  public static boolean isUnrated(String title){
    StringTokenizer tokenizer = new StringTokenizer(title," ");
    while (tokenizer.hasMoreTokens()){
      String token = tokenizer.nextToken();
      if (token.equals("U"))
        return true;
    }

    return false;
  }




  /**
   * Overrides {@link free.util.Connection#connectImpl(String, int)} to return a timestamping socket.
   */

  protected Socket connectImpl(String hostname, int port) throws IOException{
    Socket result = null;
    try{
      Class tsSocketClass = Class.forName("free.chessclub.timestamp.TimestampingSocket");
      Constructor tsSocketConstructor = tsSocketClass.getConstructor(new Class[]{String.class, int.class});
      result = (Socket)tsSocketConstructor.newInstance(new Object[]{hostname, new Integer(port)});
    } catch (ClassNotFoundException e){}
      catch (SecurityException e){}
      catch (NoSuchMethodException e){}
      catch (IllegalArgumentException e){}
      catch (InstantiationException e){}
      catch (IllegalAccessException e){}
      catch (InvocationTargetException e){
        Throwable targetException = e.getTargetException(); 
        if (targetException instanceof IOException)
          throw (IOException)targetException;
        else if (targetException instanceof RuntimeException)
          throw (RuntimeException)targetException;
        else if (targetException instanceof Error)
          throw (Error)targetException;
        else
          e.printStackTrace(); // Shouldn't happen, I think
      }
    
    if (result == null)
      result = new Socket(hostname, port);
    
    return result;
  }



  /**
   * Returns the WildVariant corresponding to the given wild number, or null
   * if that wild variant is not supported.
   */

  private WildVariant getVariant(int variantNumber){
    String variantName = getVariantName(variantNumber);
    if (variantName == null)           // Defend against the server not doing its
      variantName = "w"+variantNumber; // job properly (not sending the variant name).

    switch(variantNumber){
      case 0:  // Chess
        return Chess.getInstance();
      case 1:  // Shuffle both
        return ShuffleBoth.getInstance();
      case 16: // Kriegspiel
        return Kriegspiel.getInstance();
      case 22: // Fischer random
        return FischerRandom.getInstance();
      case 23: // Crazyhouse
        return null;
      case 24: // Bughouse
        return null;
      case 26: // Giveaway (Not ChesslikeGenericVariant because promotion to king is allowed)
        return Giveaway.getInstance();
      case 27: // Atomic
        return Atomic.getInstance();
      case 28: // Shatranj
        return null;


     /*
       We can use the classic position to initialize all the variants which only
       differ from classic chess by their initial position because we know the
       server will have to send us the real initial position in a
       DG_POSITION_BEGIN datagram and then we'll use it to *really* initialize
       the position. The server *has* to send it because for most of these
       variants the initial position is somehow randomized so if the server
       won't tell us, we won't know the position. The server sends the initial
       position on non randomized variants too, according to the protocol
       (except for w0 of course). On a side note, the server may, according to
       the protocol documentation, send an empty FEN string in
       DG_POSITION_BEGIN, in which case we have to initialize the position to
       the classic initial position even if wild!=0.
     */

      case 2: // Shuffle mirror
      case 3: // Random mirror
      case 4: // Random shuffle (castling not allowed according to Kiebitz)
      case 5: // Reversed 
        return new NoCastlingVariant(Chess.INITIAL_POSITION_FEN, variantName);

      case 6: // Empty board
      case 7: // KPPP vs KPPP
      case 8: // Advanced pawns
      case 9:  // Two kings each (It seems that the castling rules are the same as in normal chess).
      case 10: // Pawn+move odds
      case 11: // Knight odds
      case 12: // Rook odds
      case 13: // Queen odds
      case 14: // Rook odds a3
      case 15: // KBN vs K
      case 17: // Loser's chess
      case 18: // Power chess
      case 19: // KNN vs KP
      case 20: // Loadgame
      case 21: // Thematic
      case 25: // Three checks
        return new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, variantName);
    }

    return null;
  }
  
  
  
  /**
   * Returns the wild number corresponding to the specified wild variant.
   * Returns -1 if the specified wild variant is not supported by ICC.
   */
   
  private int getWildNumber(WildVariant variant){
    if (variant == null)
      throw new IllegalArgumentException("Null variant");
    
    String [] variantNames = new String[]{
      "Chess", "Shuffle both", "Shuffle mirror", "Random mirror", "Random shuffle",
      "Reversed", "Empty board", "KPPP vs KPPP", "Advanced pawns", "Two kings each",
      "Pawn+move odds", "Knight odds", "Rook odds", "Queen odds","Rook odds a3", "KBN vs K",
      "Kriegspiel", "Loser's chess", "Power chess", "KNN vs KP", "Loadgame", "Thematic",
      "Fischer random", "Crazyhouse", "Bughouse", "Three checks", "Giveaway", "Atomic", "Shatranj",
      "Random Wild"      
    };
    
    return Utilities.indexOf(variantNames, variant.getName());
  }
  
  
  
  /**
   * A list of supported wild variants, initialized lazily.
   */
   
  private static WildVariant [] wildVariants;
  
  
  
  /**
   * Returns a list of support wild variants.
   */
   
  public WildVariant [] getSupportedVariants(){
    if (wildVariants == null){
      wildVariants = new WildVariant[]{
        Chess.getInstance(),
        ShuffleBoth.getInstance(),
        new NoCastlingVariant(Chess.INITIAL_POSITION_FEN, "Shuffle mirror"),
        new NoCastlingVariant(Chess.INITIAL_POSITION_FEN, "Random shuffle"),
        new NoCastlingVariant(Chess.INITIAL_POSITION_FEN, "Reversed"),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "Empty board"),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "KPPP vs KPPP"),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "Advanced pawns"),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "Two kings each"),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "Pawn+move odds"),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "Knight odds"),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "Rook odds"),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "Queen odds"),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "Rood odds a3"),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "KBN vs K"),
        Kriegspiel.getInstance(),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "Loser's chess"),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "Power chess"),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "KNN vs KP"),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "Loadgame"),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "Thematic"),
        FischerRandom.getInstance(),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "Three checks"),
        Giveaway.getInstance(),
        Atomic.getInstance(),
        new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "Random Wild") // This is bad, but there's nothing else to do
      };
    }
    
    return (WildVariant [])wildVariants.clone();
  }
  
  


  /**
   * This method informs the user that he tried to use (observe, play etc.)
   * a wild variant not supported by Jin. Please use this method when
   * appropriate instead of sending your own message.
   */

  protected void warnVariantUnsupported(int variantNumber){
    String variantName = getVariantName(variantNumber);
    if (variantName == null)
      variantName = "w" + variantNumber;
    
    String message = 
      I18n.get(JinChessclubConnection.class).getFormattedString("unsupportedVariantMessage", new Object[]{variantName});
    String [] messageLines = message.split("\n");
    
    int maxLineLength = 0;
    for (int i = 0; i < messageLines.length; i++)
      if (messageLines[i].length() > maxLineLength)
        maxLineLength = messageLines[i].length();
    
    String border = TextUtilities.padStart("", '*', maxLineLength + 4);
    
    processLine(border);
    for (int i = 0; i < messageLines.length; i++)
      processLine("* " + TextUtilities.padEnd(messageLines[i], ' ', maxLineLength) + " *");
    processLine(border);
  }



  /**
   * Sends the "exit" command to the server.
   */

  public void exit(){
    quit();
  }



  /**
   * Processes a single line of plain text.
   */

  protected void processLine(String line){
    listenerManager.firePlainTextEvent(new PlainTextEvent(this, line));
  }
  
  
  
  /**
   * Called when a datagram we're interested in arrives from the server
   * (implementation of <code>DatagramListener</code>). This method simply
   * does a huge switch on the id of the datagram and dispatches the actual
   * handling to the appropriate method.
   */
  
  public void datagramReceived(DatagramEvent evt){
    Datagram dg = evt.getDatagram();
    switch (dg.getId()){
      // Chat related
      case Datagram.DG_PERSONAL_TELL: processPersonalTellDG(dg); break; 
      case Datagram.DG_PERSONAL_QTELL: processPersonalQTellDG(dg); break;
      case Datagram.DG_SHOUT: processShoutDG(dg); break;
      case Datagram.DG_CHANNEL_TELL: processChannelTellDG(dg); break;
      case Datagram.DG_CHANNEL_QTELL: processChannelQTellDG(dg); break;
      case Datagram.DG_KIBITZ: processKibitzDG(dg); break;
      
      // Game related
      case Datagram.DG_MY_GAME_STARTED: processMyGameStartedDG(dg); break;
      case Datagram.DG_STARTED_OBSERVING: processStartedObservingDG(dg); break;
      case Datagram.DG_ISOLATED_BOARD: processIsolatedBoardDG(dg); break;
      case Datagram.DG_MY_GAME_CHANGE: processMyGameChangeDG(dg); break;
      case Datagram.DG_MY_GAME_RESULT: processMyGameResultDG(dg); break;
      case Datagram.DG_POSITION_BEGIN: processPositionBeginDG(dg); break;
      case Datagram.DG_MY_RELATION_TO_GAME: processMyRelationToGameDG(dg); break;
      case Datagram.DG_SEND_MOVES: processSendMovesDG(dg); break;
      case Datagram.DG_BACKWARD: processBackwardDG(dg); break;
      case Datagram.DG_TAKEBACK: processTakebackDG(dg); break;
      case Datagram.DG_ILLEGAL_MOVE: processIllegalMoveDG(dg); break;
      case Datagram.DG_MSEC: processMsecDG(dg); break;
      case Datagram.DG_OFFERS_IN_MY_GAME: processOffersInMyGameDG(dg); break;
      case Datagram.DG_MORETIME: processMoretimeDG(dg); break;
      case Datagram.DG_FLIP: processFlipDG(dg); break;
      case Datagram.DG_ARROW: processArrowDG(dg); break;
      case Datagram.DG_UNARROW: processUnarrowDG(dg); break;
      case Datagram.DG_CIRCLE: processCircleDG(dg); break;
      case Datagram.DG_UNCIRCLE: processUncircleDG(dg); break;
      
      // Seek related
      case Datagram.DG_SEEK: processSeekDG(dg); break;
      case Datagram.DG_SEEK_REMOVED: processSeekRemovedDG(dg); break;
      
      // Game list related
      case Datagram.DG_GAMELIST_BEGIN: processGamelistBeginDG(dg); break;
      case Datagram.DG_GAMELIST_ITEM: processGamelistItemDG(dg); break;
      
      // Tourney related
      case Datagram.DG_TOURNEY: processTourneyDG(dg); break;
      case Datagram.DG_REMOVE_TOURNEY: processRemoveTourneyDG(dg); break;
      
      default:
        throw new IllegalStateException("Unhandled datagram received: " + dg);
    }
  }



  
  /**
   * Processes a DG_PERSONAL_TELL.
   */
   
  private void processPersonalTellDG(Datagram dg){
    processPersonalTell(dg.getString(0), dg.getString(1), dg.getString(2), dg.getInteger(3));
  }
  
  
  
  /**
   * Creates and dispatches an appropriate ChatEvent to all registered
   * ChatListeners.
   */

  protected void processPersonalTell(String playername, String titles, String message, int tellType){
    String tellTypeString;
    switch (tellType){
      case ChessclubConstants.REGULAR_TELL: tellTypeString = "tell"; break;
      case ChessclubConstants.SAY_TELL: tellTypeString = "say"; break;
      case ChessclubConstants.P_TELL: tellTypeString = "ptell"; break;
      case ChessclubConstants.Q_TELL: tellTypeString = "qtell"; break;
      case ChessclubConstants.A_TELL: tellTypeString = "atell"; break;
      default:
        return; // Ignore unknown types.
    }

    String title = displayableTitle(titles);

    ChatEvent evt = new ChatEvent(this, tellTypeString, ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY,
      playername, title, -1, message, null);
      
    listenerManager.fireChatEvent(evt);
  }



  /**
   * Processes a DG_PERSONAL_QTELL. 
   */
   
  private void processPersonalQTellDG(Datagram dg){
    processPersonalQTell(dg.getString(0), dg.getString(1), dg.getString(2));
  }
   
   

  /**
   * Creates and dispatches an appropriate ChatEvent to all registered
   * ChatListeners.
   */

  protected void processPersonalQTell(String name, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "qtell", ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY,
      name, displayableTitle(titles), -1, message, null));
  }



  /**
   * Processes a DG_SHOUT.
   */
   
  private void processShoutDG(Datagram dg){
    processShout(dg.getString(0), dg.getString(1), dg.getInteger(2), dg.getString(3));
  }



  /**
   * Creates and dispatches an appropriate ChatEvent to all registered
   * ChatListeners.
   */

  protected void processShout(String playerName, String titles, int shoutType, String message){
    String tellTypeString;
    switch (shoutType){
      case ChessclubConstants.REGULAR_SHOUT: tellTypeString = "shout"; break;
      case ChessclubConstants.I_SHOUT: tellTypeString = "ishout"; break;
      case ChessclubConstants.S_SHOUT: tellTypeString = "sshout"; break;
      case ChessclubConstants.ANNOUNCEMENT_SHOUT: tellTypeString = "announcement"; break;
      default:
        return; // Ignore unknown types.
    }

    String title = displayableTitle(titles);

    ChatEvent evt = (shoutType == ChessclubConstants.ANNOUNCEMENT_SHOUT) ? 
      new ChatEvent(this, tellTypeString, ChatEvent.BROADCAST_CHAT_CATEGORY, playerName, title, -1, message, null):
      new ChatEvent(this, tellTypeString, ChatEvent.ROOM_CHAT_CATEGORY, playerName, title, -1, message, null);
      
    listenerManager.fireChatEvent(evt);
  }



  /**
   * Processes a DG_CHANNEL_TELL.
   */
  
  private void processChannelTellDG(Datagram dg){
    processChannelTell(dg.getInteger(0), dg.getString(1), dg.getString(2),
      dg.getString(3), dg.getInteger(4));
  }

  
  /**
   * Creates and dispatches an appropriate ChatEvent to all registered
   * ChatListeners.
   */

  protected void processChannelTell(int channel, String playerName, String titles, String message, int tellType){
    String tellTypeString;
    switch (tellType){
      case ChessclubConstants.REGULAR_CHANNEL_TELL: tellTypeString = "channel-tell"; break;
      case ChessclubConstants.A_CHANNEL_TELL: tellTypeString = "channel-atell"; break;
      default:
        return; // Ignore unknown types.
    }

    String title = displayableTitle(titles);

    ChatEvent evt = new ChatEvent(this, tellTypeString, ChatEvent.ROOM_CHAT_CATEGORY, 
      playerName, title, -1, message, new Integer(channel));
      
    listenerManager.fireChatEvent(evt);
  }




  /**
   * Processes a DG_CHANNEL_QTELL. 
   */
   
  private void processChannelQTellDG(Datagram dg){
    processChannelQTell(dg.getInteger(0), dg.getString(1), dg.getString(2), dg.getString(3));
  }
   
   
   
  /**
   * Creates and dispatches an appropriate ChatEvent to all registered
   * ChatListeners.
   */

  protected void processChannelQTell(int channel, String name, String titles, String message){
    ChatEvent evt = new ChatEvent(this, "channel-qtell", ChatEvent.ROOM_CHAT_CATEGORY,
      name, displayableTitle(titles), -1, message, new Integer(channel));

    listenerManager.fireChatEvent(evt);
  }



  /**
   * Processes a DG_KIBITZ. 
   */
   
  private void processKibitzDG(Datagram dg){
    processKibitz(dg.getInteger(0), dg.getString(1), dg.getString(2),
      dg.getBoolean(3), dg.getString(4));
  }
  
  

  /**
   * Creates and dispatches an appropriate ChatEvent to all registered
   * ChatListeners.
   */

  protected void processKibitz(int gameNumber, String playerName, String titles, boolean isKibitz, String message){
    String tellTypeString = isKibitz ? "kibitz" : "whisper";

    String title = displayableTitle(titles);

    ChatEvent evt = new ChatEvent(this, tellTypeString, ChatEvent.GAME_CHAT_CATEGORY,
      playerName, title, -1, message, new Integer(gameNumber));
    listenerManager.fireChatEvent(evt);
  }






  /**
   * This method is called by ChessclubJinListenerManager when the last
   * GameListener is removed.
   */

  void lastGameListenerRemoved(){
    // Why do we clear this? What if another listener registers?
//    gameNumbersToGameInfo.clear();
//    nonStartedGames.clear();
  }





  /**
   * A Hashtable mapping gameNumbers to GameInfo objects about ongoing games.
   */

  private final Hashtable gameNumbersToGameInfo = new Hashtable(1);
  




  /**
   * A Hashtable mapping game numbers to Hashtables containing game properties.
   * This is used for passing information between the various datagrams that
   * tell us a game started and the DG_POSITION_BEGIN datagram when we actually
   * fire the game started event.
   */

  private final Hashtable nonStartedGames = new Hashtable(1); 




  /**
   * A Hashtable mapping Game objects to Vectors of moves which were sent for
   * these games but the server didn't tell us yet whether the move is legal
   * or not.
   */

  private final Hashtable unechoedMoves = new Hashtable(1);




  /**
   * The amount of user played games.
   */

  private int userGamesCount = 0;




  /**
   * <P>Returns the GameInfo object for the given game number. This method 
   * throws a <code>NoSuchGameException</code> if there is no started game with
   * the given number. It is important to throw it here and to force the user to
   * catch the exception and handle it. This can always happen if the server 
   * thinks the game started, but the client doesn't (if the wild variant is 
   * not supported for example). What will happen in this case is that the 
   * datagrams will continue to arrive, but all the datagram processing code 
   * must ignore them. Forcing the datagram processing code to catch the 
   * exception ensures that such a case will be properly handled. The side
   * effect is that this method never returns null.
   * <P>Of course you can always ignore this method and access
   * <code>gameNumbersToGameInfo</code> directly.
   */

  private GameInfo getGameInfo(int gameNumber) throws NoSuchGameException{
    GameInfo gameInfo = (GameInfo)gameNumbersToGameInfo.get(new Integer(gameNumber));
    if (gameInfo == null)
      throw new NoSuchGameException();

    return gameInfo;
  }




  /**
   * Adds the specified <code>GameInfo</code> to the
   * <code>gameNumbersToGameInfo</code> hashtable.
   */

  private void addGameInfo(int gameNumber, GameInfo gameInfo){
    gameNumbersToGameInfo.put(new Integer(gameNumber), gameInfo);
    if (gameInfo.game.getGameType() == Game.MY_GAME)
      userGamesCount++;
  }




  /**
   * Removes the <code>GameInfo</code> of the specified game from the
   * <code>gameNumbersToGameInfo</code> hashtable.
   */

  private GameInfo removeGameInfo(int gameNumber){
    GameInfo gameInfo = (GameInfo)gameNumbersToGameInfo.remove(new Integer(gameNumber));
    if ((gameInfo != null) && (gameInfo.game.getGameType() == Game.MY_GAME))
      userGamesCount--;

    return gameInfo;
  }




  /**
   * Returns <code>true</code> if a game with the given number exists and is
   * a started game. Returns <code>false</code> otherwise.
   */

  private boolean gameExists(int gameNumber){
    return gameNumbersToGameInfo.containsKey(new Integer(gameNumber));
  }





  /**
   * Adds a Hashtable containing the given game information to the list of games
   * we haven't fired a game started event yet. This and the createGameFromNonStarted
   * methods are here essentially for hiding some of the ugliness of passing information
   * from the various DGs notifying of game start and the DG_POSITION_BEGIN arrival
   * when we actually fire the game started event.
   */

  private void addNonStartedGame(int gameType, int gameNumber, String whiteName, String blackName,
          WildVariant variant, String ratingCategoryString, boolean isRated, int whiteInitial, int whiteIncrement,
          int blackInitial, int blackIncrement, boolean isPlayedGame, String exString,
          int whiteRating, int blackRating, long gameID, String whiteTitles, String blackTitles,
          boolean isIrregularLegality, boolean isIrregularSemantics, boolean usesPlunkers,
          String fancyTimeControls){

    Hashtable gameProps = new Hashtable();
    gameProps.put("WhiteName", whiteName);
    gameProps.put("BlackName", blackName);
    gameProps.put("Variant", variant);
    gameProps.put("RatingCategoryString", ratingCategoryString);
    gameProps.put("IsRated", isRated ? Boolean.TRUE : Boolean.FALSE);
    gameProps.put("WhiteInitial", new Integer(whiteInitial));
    gameProps.put("WhiteIncrement", new Integer(whiteIncrement));
    gameProps.put("BlackInitial", new Integer(blackInitial));
    gameProps.put("BlackIncrement", new Integer(blackIncrement));
    gameProps.put("IsPlayedGame", isPlayedGame ? Boolean.TRUE : Boolean.FALSE);
    gameProps.put("WhiteRating", new Integer(whiteRating));
    gameProps.put("BlackRating", new Integer(blackRating));
    gameProps.put("WhiteTitles", displayableTitle(whiteTitles));
    gameProps.put("BlackTitles", displayableTitle(blackTitles));

    gameProps.put("GameType", new Integer(gameType));

    nonStartedGames.put(new Integer(gameNumber), gameProps);
  }




  /**
   * Returns true if there is a non-started game with the given gameNumber.
   */

  private boolean existsNonStarted(int gameNumber){
    return nonStartedGames.containsKey(new Integer(gameNumber));
  }




  /**
   * Returns the given property of a non started game with the given gameNumber.
   */

  private Object getPropertyForNonStarted(int gameNumber, String propertyName){
    Hashtable gameProps = (Hashtable)nonStartedGames.get(new Integer(gameNumber));    
    return gameProps.get(propertyName);
  }



  /**
   * Sets the value of the given property in the given non-started game to the
   * given value.
   */

  private void putPropertyForNonStarted(int gameNumber, String propertyName, Object propertyValue){
    Hashtable gameProps = (Hashtable)nonStartedGames.get(new Integer(gameNumber));    
    gameProps.put(propertyName, propertyValue);
  }





  /**
   * Creates a <code>Game</code> object from the properties saved by the
   * addNonStartedGame method and the given initial position. The Hashtable with
   * game information saved by the addNonStartedGame method is removed from the
   * list of games for whom a game started event hasn't been fired yet.
   */

  private Game createGameFromNonStarted(int gameNumber, Position initialPosition){
    
    Hashtable gameProps = (Hashtable)nonStartedGames.remove(new Integer(gameNumber));

    String whiteName = (String)gameProps.get("WhiteName");
    String blackName = (String)gameProps.get("BlackName");
    String ratingCategoryString = (String)gameProps.get("RatingCategoryString");
    boolean isRated = ((Boolean)gameProps.get("IsRated")).booleanValue();
    int whiteInitial = 1000*60*((Integer)gameProps.get("WhiteInitial")).intValue();
    int whiteIncrement = 1000*((Integer)gameProps.get("WhiteIncrement")).intValue();
    int blackInitial = 1000*60*((Integer)gameProps.get("BlackInitial")).intValue();
    int blackIncrement = 1000*((Integer)gameProps.get("BlackIncrement")).intValue();
    boolean isPlayedGame = ((Boolean)gameProps.get("IsPlayedGame")).booleanValue();
    int whiteRating = ((Integer)gameProps.get("WhiteRating")).intValue();
    int blackRating = ((Integer)gameProps.get("BlackRating")).intValue();
    String whiteTitles = (String)gameProps.get("WhiteTitles");
    String blackTitles = (String)gameProps.get("BlackTitles");
    int gameType = ((Integer)gameProps.get("GameType")).intValue();
    boolean isInitiallyFlipped = ((Boolean)gameProps.get("InitiallyFlipped")).booleanValue();

    Player userPlayer;
    if (gameType == Game.ISOLATED_BOARD)
      userPlayer = null;
    else if (gameType == Game.OBSERVED_GAME)
      userPlayer = null;
    else{ // MY_GAME
      String username = getUsername();
      if (isPlayedGame){
        if (whiteName.equals(username))
          userPlayer = Player.WHITE_PLAYER;
        else
          userPlayer = Player.BLACK_PLAYER;
      }
      else
        userPlayer = null;
    }

    return new Game(gameType, initialPosition, 0, whiteName, blackName, whiteInitial,
      whiteIncrement, blackInitial, blackIncrement, whiteRating, blackRating,
      new Integer(gameNumber), ratingCategoryString, isRated, isPlayedGame, whiteTitles,
      blackTitles, isInitiallyFlipped, userPlayer);
  }



  /**
   * Processes a DG_MY_GAME_STARTED. 
   */
   
  private void processMyGameStartedDG(Datagram dg){
    int gameNumber = dg.getInteger(0);
    String whiteName = dg.getString(1);
    String blackName = dg.getString(2);
    int wildNumber = dg.getInteger(3);
    String ratingCategoryString = dg.getString(4);
    boolean isRated = dg.getBoolean(5);
    int whiteInitial = dg.getInteger(6);
    int whiteIncrement = dg.getInteger(7);
    int blackInitial = dg.getInteger(8);
    int blackIncrement = dg.getInteger(9);
    boolean isPlayedGame = dg.getBoolean(10);
    String exString = dg.getString(11);
    int whiteRating = dg.getInteger(12);
    int blackRating = dg.getInteger(13);
    long gameID = dg.getLong(14);
    String whiteTitles = dg.getString(15);
    String blackTitles = dg.getString(16);
    boolean isIrregularLegality = dg.getBoolean(17);
    boolean isIrregularSemantics = dg.getBoolean(18);
    boolean usesPlunkers = dg.getBoolean(19);
    String fancyTimeControls = dg.getString(20);
    
    processMyGameStarted(gameNumber, whiteName, blackName, wildNumber, ratingCategoryString, isRated,
      whiteInitial, whiteIncrement, blackInitial, blackIncrement, isPlayedGame, exString, whiteRating,
      blackRating, gameID, whiteTitles, blackTitles, isIrregularLegality, isIrregularSemantics,
      usesPlunkers, fancyTimeControls);
  }
  
  

  /**
   * Creates the game record for a game played by the user.
   */

  protected void processMyGameStarted(int gameNumber, String whiteName, String blackName,
      int variantNumber, String ratingCategoryString, boolean isRated, int whiteInitial, int whiteIncrement,
      int blackInitial, int blackIncrement, boolean isPlayedGame, String exString,
      int whiteRating, int blackRating, long gameID, String whiteTitles, String blackTitles,
      boolean isIrregularLegality, boolean isIrregularSemantics, boolean usesPlunkers,
      String fancyTimeControls){


    WildVariant variant = getVariant(variantNumber);
    if (variant == null){ // Not a supported variant.
      warnVariantUnsupported(variantNumber);
      return;
    }

    addNonStartedGame(Game.MY_GAME, gameNumber, whiteName, blackName, variant, ratingCategoryString, isRated,
      whiteInitial, whiteIncrement, blackInitial, blackIncrement, isPlayedGame, exString,
      whiteRating, blackRating, gameID, whiteTitles, blackTitles, isIrregularLegality, isIrregularSemantics,
      usesPlunkers, fancyTimeControls);
  }



  /**
   * Processes a DG_STARTED_OBSERVING.
   */
   
  private void processStartedObservingDG(Datagram dg){
    int gameNumber = dg.getInteger(0);
    String whiteName = dg.getString(1);
    String blackName = dg.getString(2);
    int wildNumber = dg.getInteger(3);
    String ratingCategoryString = dg.getString(4);
    boolean isRated = dg.getBoolean(5);
    int whiteInitial = dg.getInteger(6);
    int whiteIncrement = dg.getInteger(7);
    int blackInitial = dg.getInteger(8);
    int blackIncrement = dg.getInteger(9);
    boolean isPlayedGame = dg.getBoolean(10);
    String exString = dg.getString(11);
    int whiteRating = dg.getInteger(12);
    int blackRating = dg.getInteger(13);
    long gameID = dg.getLong(14);
    String whiteTitles = dg.getString(15);
    String blackTitles = dg.getString(16);
    boolean isIrregularLegality = dg.getBoolean(17);
    boolean isIrregularSemantics = dg.getBoolean(18);
    boolean usesPlunkers = dg.getBoolean(19);
    String fancyTimeControls = dg.getString(20);
    
    processStartedObserving(gameNumber, whiteName, blackName, wildNumber, ratingCategoryString,
      isRated, whiteInitial, whiteIncrement, blackInitial, blackIncrement, isPlayedGame,
      exString, whiteRating, blackRating, gameID, whiteTitles, blackTitles, isIrregularLegality,
      isIrregularSemantics, usesPlunkers, fancyTimeControls);
  }


  /**
   * Creates the game record for an observed game.
   */

  protected void processStartedObserving(int gameNumber, String whiteName, String blackName,
    int variantNumber, String ratingCategoryString, boolean isRated, int whiteInitial,
    int whiteIncrement, int blackInitial, int blackIncrement, boolean isPlayedGame,
    String exString, int whiteRating, int blackRating, long gameID, String whiteTitles,
    String blackTitles, boolean isIrregularLegality, boolean isIrregularSemantics,
    boolean usesPlunkers, String fancyTimeControls){

    WildVariant variant = getVariant(variantNumber);
    if (variant == null){ // Not a supported variant.
      warnVariantUnsupported(variantNumber);
      return;
    }

    addNonStartedGame(Game.OBSERVED_GAME, gameNumber, whiteName, blackName, variant, ratingCategoryString, isRated,
      whiteInitial, whiteIncrement, blackInitial, blackIncrement, isPlayedGame, exString,
      whiteRating, blackRating, gameID, whiteTitles, blackTitles, isIrregularLegality, isIrregularSemantics,
      usesPlunkers, fancyTimeControls);
  }
  
  
  
  /**
   * Processes a DG_ISOLATED_BOARD.
   */
   
  private void processIsolatedBoardDG(Datagram dg){
    int gameNumber = dg.getInteger(0);
    String whiteName = dg.getString(1);
    String blackName = dg.getString(2);
    int wildNumber = dg.getInteger(3);
    String ratingCategoryString = dg.getString(4);
    boolean isRated = dg.getBoolean(5);
    int whiteInitial = dg.getInteger(6);
    int whiteIncrement = dg.getInteger(7);
    int blackInitial = dg.getInteger(8);
    int blackIncrement = dg.getInteger(9);
    boolean isPlayedGame = dg.getBoolean(10);
    String exString = dg.getString(11);
    int whiteRating = dg.getInteger(12);
    int blackRating = dg.getInteger(13);
    long gameID = dg.getLong(14);
    String whiteTitles = dg.getString(15);
    String blackTitles = dg.getString(16);
    boolean isIrregularLegality = dg.getBoolean(17);
    boolean isIrregularSemantics = dg.getBoolean(18);
    boolean usesPlunkers = dg.getBoolean(19);
    String fancyTimeControls = dg.getString(20);
    
    processIsolatedBoard(gameNumber, whiteName, blackName, wildNumber, ratingCategoryString,
      isRated, whiteInitial, whiteIncrement, blackInitial, blackIncrement, isPlayedGame, exString,
      whiteRating, blackRating, gameID, whiteTitles, blackTitles, isIrregularLegality,
      isIrregularSemantics, usesPlunkers, fancyTimeControls);
  }



  /**
   * Creates the game record for an isolated board.
   */

  protected void processIsolatedBoard(int gameNumber, String whiteName, String blackName,
      int variantNumber, String ratingCategoryString, boolean isRated, int whiteInitial,
      int whiteIncrement, int blackInitial, int blackIncrement, boolean isPlayedGame,
      String exString, int whiteRating, int blackRating, long gameID, String whiteTitles,
      String blackTitles, boolean isIrregularLegality, boolean isIrregularSemantics, 
      boolean usesPlunkers, String fancyTimeControls){


    WildVariant variant = getVariant(variantNumber);
    if (variant == null){ // Not a supported variant.
      warnVariantUnsupported(variantNumber);
      return;
    }

    addNonStartedGame(Game.ISOLATED_BOARD, gameNumber, whiteName, blackName, variant, ratingCategoryString, isRated,
      whiteInitial, whiteIncrement, blackInitial, blackIncrement, isPlayedGame, exString,
      whiteRating, blackRating, gameID, whiteTitles, blackTitles, isIrregularLegality, isIrregularSemantics,
      usesPlunkers, fancyTimeControls);
  }
  
  
  
  /**
   * Processes a DG_MY_GAME_CHANGE.
   */
   
  private void processMyGameChangeDG(Datagram dg){
    int gameNumber = dg.getInteger(0);
    String whiteName = dg.getString(1);
    String blackName = dg.getString(2);
    int wildNumber = dg.getInteger(3);
    String ratingCategoryString = dg.getString(4);
    boolean isRated = dg.getBoolean(5);
    int whiteInitial = dg.getInteger(6);
    int whiteIncrement = dg.getInteger(7);
    int blackInitial = dg.getInteger(8);
    int blackIncrement = dg.getInteger(9);
    boolean isPlayedGame = dg.getBoolean(10);
    String exString = dg.getString(11);
    int whiteRating = dg.getInteger(12);
    int blackRating = dg.getInteger(13);
    long gameID = dg.getLong(14);
    String whiteTitles = dg.getString(15);
    String blackTitles = dg.getString(16);
    boolean isIrregularLegality = dg.getBoolean(17);
    boolean isIrregularSemantics = dg.getBoolean(18);
    boolean usesPlunkers = dg.getBoolean(19);
    String fancyTimeControls = dg.getString(20);
    
    processMyGameChange(gameNumber, whiteName, blackName, wildNumber, ratingCategoryString,
      isRated, whiteInitial, whiteIncrement, blackInitial, blackIncrement, isPlayedGame, exString,
      whiteRating, blackRating, gameID, whiteTitles, blackTitles, isIrregularLegality,
      isIrregularSemantics, usesPlunkers, fancyTimeControls);
  }



  /**
   * Gets called when a DG_MY_GAME_CHANGE datagram arrives. This method merely
   * calls updateGame() with the new game properties.
   */
  
  protected void processMyGameChange(int gameNumber, String whiteName, String blackName,
      int variantNumber, String ratingCategoryString, boolean isRated, int whiteInitial,
      int whiteIncrement, int blackInitial, int blackIncrement, boolean isPlayedGame,
      String exString, int whiteRating, int blackRating, long gameID, String whiteTitles,
      String blackTitles, boolean isIrregularLegality, boolean isIrregularSemantics,
      boolean usesPlunkers, String fancyTimeControls){


    try{
      GameInfo gameInfo = getGameInfo(gameNumber);
      Game game = gameInfo.game;

      updateGame(game.getGameType(), gameNumber, whiteName, blackName, ratingCategoryString,
        isRated, 60*1000*whiteInitial, 1000*whiteIncrement, 60*1000*blackInitial, 1000*blackIncrement,
        isPlayedGame, whiteRating, blackRating, String.valueOf(gameID), whiteTitles, blackTitles);
    } catch (NoSuchGameException e){}
  }
  
  
  
  /**
   * Processes a DG_MY_GAME_RESULT. 
   */
   
  private void processMyGameResultDG(Datagram dg){
    processMyGameResult(dg.getInteger(0), dg.getBoolean(1), dg.getString(2), dg.getString(3),
      dg.getString(4));
  }



  /**
   * Gets called when a DG_MY_GAME_RESULT datagram arrives. This method checks
   * if becomesExamined is true and if so, calls updateGame with the game 
   * properties of the ended game, except for the "isPlayedGame" property, 
   * which now becomes false. Also sets the result of the game on the
   * <code>Game</code> object.
   */

  protected void processMyGameResult(int gameNumber, boolean becomesExamined,
      String gameResultCode, String scoreString, String descriptionString){

    try{
      GameInfo gameInfo = getGameInfo(gameNumber);
      Game game = gameInfo.game;

      try{
        int result;
        if ("1-0".equals(scoreString))
          result = Game.WHITE_WINS;
        else if ("0-1".equals(scoreString))
          result = Game.BLACK_WINS;
        else if ("1/2-1/2".equals(scoreString))
          result = Game.DRAW;
        else
          result = Game.UNKNOWN_RESULT;

        game.setResult(result);

        if (becomesExamined){
          // For our own games, we will get a DG_MY_RELATION_TO_GAME
          // where we will change the state of the game to nonplayed.
          if (game.getGameType() == Game.OBSERVED_GAME){
            updateGame(game.getGameType(), gameNumber, game.getWhiteName(), game.getBlackName(),
              game.getRatingCategoryString(), game.isRated(), game.getWhiteTime(),
              game.getWhiteInc(), game.getBlackTime(), game.getBlackInc(), false, // <-- the change
              game.getWhiteRating(), game.getBlackRating(), game.getID(), game.getWhiteTitles(),
              game.getBlackTitles());
          }

        }
        else if (game.getGameType() == Game.ISOLATED_BOARD){
          fireGameEvent(new GameEndEvent(this, game, result));
        }


      } catch (IllegalStateException e){
          e.printStackTrace();
        }
    } catch (NoSuchGameException e){}
  }




  /**
   * This method changes the settings of the given game to the given settings.
   */
  
  protected void updateGame(int gameType, int gameNumber, String whiteName, String blackName,
      String ratingCategoryString, boolean isRated, int whiteInitial, int whiteIncrement,
      int blackInitial, int blackIncrement, boolean isPlayedGame, int whiteRating, 
      int blackRating, Object gameID, String whiteTitles, String blackTitles){
        
    try{
      GameInfo gameInfo = getGameInfo(gameNumber);
      Game game = gameInfo.game;
  
      game.setGameType(gameType);
      game.setId(new Integer(gameNumber));
      game.setWhiteName(whiteName);
      game.setBlackName(blackName);
      game.setRatingCategoryString(ratingCategoryString);
      game.setRated(isRated);
      game.setWhiteTime(whiteInitial);
      game.setWhiteInc(whiteIncrement);
      game.setBlackTime(blackInitial);
      game.setBlackInc(blackIncrement);
      game.setPlayed(isPlayedGame);
      game.setWhiteRating(whiteRating);
      game.setBlackRating(blackRating);
      game.setWhiteTitles(whiteTitles);
      game.setBlackTitles(blackTitles);
    } catch (NoSuchGameException e){}
  }
  
  
  
  /**
   * Processes a DG_POSITION_BEGIN. 
   */
   
  private void processPositionBeginDG(Datagram dg){
    processPositionBegin(dg.getInteger(0), dg.getString(1), dg.getInteger(2));
  }


  
  /**
   * Fires a game start event for the game specified by <code>gameNumber</code>.
   */

  protected void processPositionBegin(int gameNumber, String initFEN, int numMovesToFollow){
    if (existsNonStarted(gameNumber)){
      WildVariant variant = (WildVariant)getPropertyForNonStarted(gameNumber, "Variant");
      Position pos = new Position(variant);
      if (!initFEN.equals(""))
        pos.setFEN(initFEN);

      Game newGame = createGameFromNonStarted(gameNumber, pos);
      GameInfo gameInfo = new GameInfo(newGame, new Position(pos), numMovesToFollow);
      addGameInfo(gameNumber, gameInfo);

      fireGameEvent(new GameStartEvent(this, newGame));
    }
    else{ // This can happen during an examined game on a "p@a2", "clearboard" and "loadgame" for example.
      try{
        GameInfo gameInfo = getGameInfo(gameNumber);
        Game game = gameInfo.game;

        Position newInitPos = new Position(game.getVariant());
        newInitPos.setFEN(initFEN);

        game.setInitialPosition(newInitPos);
        game.setPliesSinceStart(0);
        gameInfo.moves.removeAllElements();
        gameInfo.position.copyFrom(game.getInitialPosition());
        gameInfo.numMovesToFollow = numMovesToFollow;

        fireGameEvent(new PositionChangedEvent(this, game, gameInfo.position));
      } catch (NoSuchGameException e){}
    }

  }
  
  
  
  /**
   * Processes a DG_MY_RELATION_TO_GAME.
   */
   
  private void processMyRelationToGameDG(Datagram dg){
    processMyRelationToGame(dg.getInteger(0), dg.getString(1));
  }



  /**
   * If playerState is {@link ChessclubConstants#DOES_NOTHING_PLAYER_STATE},
   * fires the appropriate GameEndEvent to all interested GameListeners.
   */

  protected void processMyRelationToGame(int gameNumber, String playerState){
    if (ChessclubConstants.DOING_NOTHING_PLAYER_STATE.equals(playerState)){

      GameInfo gameInfo = removeGameInfo(gameNumber);

      // Game wasn't set up properly, probably because the wild variant is not supported.
      if (gameInfo == null) 
        return;

      Game game = gameInfo.game;
      unechoedMoves.remove(game);

      int result = 
        (game.getResult() == Game.GAME_IN_PROGRESS) ? Game.UNKNOWN_RESULT : game.getResult();

      // Make sure the game doesn't stay in progress...
      if (game.getResult() == Game.GAME_IN_PROGRESS) 
        game.setResult(result);

      clearOffers(gameInfo, Player.WHITE_PLAYER);
      clearOffers(gameInfo, Player.BLACK_PLAYER);
      fireGameEvent(new GameEndEvent(this, game, result));
    }
    else{
      int newGameType = ChessclubConstants.OBSERVING_PLAYER_STATE.equals(playerState) ?
        Game.OBSERVED_GAME : Game.MY_GAME;

      if (gameExists(gameNumber)){
        try{
          GameInfo gameInfo = getGameInfo(gameNumber);
          Game game = gameInfo.game;

          boolean isPlayedGame = game.isPlayed();
          if (ChessclubConstants.EXAMINING_PLAYER_STATE.equals(playerState))
            isPlayedGame = false;

          if ((game.isPlayed() != isPlayedGame) || (newGameType != game.getGameType())){
            updateGame(newGameType, gameNumber, game.getWhiteName(), game.getBlackName(),
              game.getRatingCategoryString(), game.isRated(), game.getWhiteTime(),
              game.getWhiteInc(), game.getBlackTime(), game.getBlackInc(), isPlayedGame,
              game.getWhiteRating(), game.getBlackRating(), game.getID(), game.getWhiteTitles(),
              game.getBlackTitles());
          }
        } catch (NoSuchGameException e){
            throw new IllegalStateException("This isn't supposed to happen - we checked that the game exists");
          }
      }
      else{ // The game doesn't exist, either because it's a non started game or a game 
            // that wasn't set up properly (usually happens for non-supported variants).

        if (existsNonStarted(gameNumber)) // This way we know it's a non-started game.
          putPropertyForNonStarted(gameNumber, "GameType", new Integer(newGameType));
        else 
          return;
          // A datagram for a game that wasn't processed, probably 
          // because the variant is not supported.
      }
    }
  }
  
  
  
  /**
   * Processes a DG_SEND_MOVES.
   */
   
  private void processSendMovesDG(Datagram dg){
    int i = 0;
    
    int gameNumber = dg.getInteger(i++);
    String algebraicMove = dg.getString(i++);

    String smithMove = dg.getString(i++);

    if (isDGOn(Datagram.DG_MOVE_TIME)) // skip
      i++;

    if (isDGOn(Datagram.DG_MOVE_CLOCK)) // skip
      i++;

    int variationCode = dg.getInteger(i++);

    processSendMoves(gameNumber, algebraicMove, smithMove, variationCode);
  }



  /**
   * Fires the appropriate MoveEvent to all interested GameListeners.
   */

  protected void processSendMoves(int gameNumber, String algebraicMove,
      String smithMove, int variationCode){
    try{
      GameInfo gameInfo = getGameInfo(gameNumber);
      Game game = gameInfo.game;
      Position position = gameInfo.position;
      Move move = parseWarrenSmith(smithMove, position, algebraicMove); 

      position.makeMove(move);
      gameInfo.moves.addElement(move);

      boolean isNewMove = (variationCode != ChessclubConstants.INITIAL_MOVE) &&
                          (variationCode != ChessclubConstants.FORWARD_MOVE);

      clearOffers(gameInfo, move.getPlayer().getOpponent());
      fireGameEvent(new MoveMadeEvent(this, game, move, isNewMove));

      if (gameInfo.numMovesToFollow > 0){
        gameInfo.numMovesToFollow--;
      }
      else{
        Vector unechoedGameMoves = (Vector)unechoedMoves.get(game);
        if ((unechoedGameMoves != null) && (unechoedGameMoves.size() != 0)){ // Might be our move.
          Move madeMove = (Move)unechoedGameMoves.elementAt(0);
          if (isSameMove(game, move, madeMove))
            unechoedGameMoves.removeElementAt(0); 
        }
      }
    } catch (NoSuchGameException e){}
  }




  /**
   * Returns whether <code>echoedMove</code> (sent to us by the server)
   * is the same move as <code>sentMove</code> (a move we sent to the server).
   */
  
  private static boolean isSameMove(Game game, Move echoedMove, Move sentMove){
    try{
      String echoedMoveString = moveToString(game, echoedMove);
      String sentMoveString = moveToString(game, sentMove);
      return echoedMoveString.equals(sentMoveString);
    } catch (IllegalArgumentException e){
      // An exception shouldn't be thrown for sentMove (since moveToString was
      // already called on it when it was sent to the server). Thus if it is
      // thrown, it's for echoedMove, in which case it's certainly not the
      // same move.
      return false;
    }
  }
  
  
  
  /**
   * Converts the specified <code>moveSmith</code> string into a Move object.
   */

  private Move parseWarrenSmith(String moveSmith, Position position, String moveString){
    WildVariant variant = position.getVariant();
    
    // Handle Kriegspiel
    if (variant.equals(Kriegspiel.getInstance()) && (moveSmith.indexOf("?") != -1)){
      if (moveSmith.equals("?")){ // Completely hidden
        return variant.createMove(position, null, null, null, moveString);
      }
      else{
        Square endSquare = Square.parseSquare(moveSmith.substring(2, 4));
        return variant.createMove(position, null, endSquare, null, moveString);
      }
    }
    else
      return Move.parseWarrenSmith(moveSmith, position, moveString);
  }
  
  
  
  /**
   * Processes a DG_BACKWARD. 
   */
   
  private void processBackwardDG(Datagram dg){
    processBackward(dg.getInteger(0), dg.getInteger(1));
  }



  /**
   * Fires a Takeback event.
   */

  protected void processBackward(int gameNumber, int backwardCount){
    try{
      GameInfo gameInfo = getGameInfo(gameNumber);
      Game game = gameInfo.game;
      Position pos = gameInfo.position;
      Vector moves = gameInfo.moves;

      int numMadeMoves = moves.size() - backwardCount;
      for (int i = moves.size() - 1; i >= numMadeMoves; i--)
        moves.removeElementAt(i);

      pos.copyFrom(game.getInitialPosition());
      for (int i = 0; i < numMadeMoves; i++)
        pos.makeMove((Move)moves.elementAt(i));

      fireGameEvent(new TakebackEvent(this, game, backwardCount));
    } catch (NoSuchGameException e){}
  }
  
  
  
  /**
   * Processes a DG_TAKEBACK.
   */
   
  private void processTakebackDG(Datagram dg){
    processTakeback(dg.getInteger(0), dg.getInteger(1));
  }

  

  /**
   * Fires a Takeback event.
   */

  protected void processTakeback(int gameNumber, int takebackCount){
    try{
      GameInfo gameInfo = getGameInfo(gameNumber);
      Game game = gameInfo.game;
      Position pos = gameInfo.position;
      Vector moves = gameInfo.moves;

      int numMadeMoves = moves.size() - takebackCount;
      for (int i = moves.size() - 1; i >= numMadeMoves; i--)
        moves.removeElementAt(i);

      pos.copyFrom(game.getInitialPosition());
      for (int i = 0; i < numMadeMoves; i++)
        pos.makeMove((Move)moves.elementAt(i));

      fireGameEvent(new TakebackEvent(this, game, takebackCount));
      updateTakebackOffer(gameInfo, Player.WHITE_PLAYER, 0); // The server seems to only clear
      updateTakebackOffer(gameInfo, Player.BLACK_PLAYER, 0); // takeback offers on a takeback
    } catch (NoSuchGameException e){}
  }
  
  
  
  /**
   * Processes a DG_OFFERS_IN_MY_GAME.
   */
   
  private void processOffersInMyGameDG(Datagram dg){
    int gameNumber = dg.getInteger(0);
    boolean whiteDraw = dg.getBoolean(1);
    boolean blackDraw = dg.getBoolean(2);
    boolean whiteAdjourn = dg.getBoolean(3);
    boolean blackAdjourn = dg.getBoolean(4);
    boolean whiteAbort = dg.getBoolean(5);
    boolean blackAbort = dg.getBoolean(6);
    int whiteTakeback = dg.getInteger(7);
    int blackTakeback = dg.getInteger(8);
    
    processOffersInMyGame(gameNumber, whiteDraw, blackDraw, whiteAdjourn, blackAdjourn,
      whiteAbort, blackAbort, whiteTakeback, blackTakeback);
  }



  /**
   * Fires the appropriate OfferEvent.
   */

  protected void processOffersInMyGame(int gameNumber, boolean whiteDraw, boolean blackDraw,
      boolean whiteAdjourn, boolean blackAdjourn, boolean whiteAbort, boolean blackAbort,
      int whiteTakeback, int blackTakeback){

    try{
      GameInfo gameInfo = getGameInfo(gameNumber);

      updateOffer(gameInfo, OfferEvent.DRAW_OFFER, Player.WHITE_PLAYER, whiteDraw);
      updateOffer(gameInfo, OfferEvent.DRAW_OFFER, Player.BLACK_PLAYER, blackDraw);
      updateOffer(gameInfo, OfferEvent.ADJOURN_OFFER, Player.WHITE_PLAYER, whiteAdjourn);
      updateOffer(gameInfo, OfferEvent.ADJOURN_OFFER, Player.BLACK_PLAYER, blackAdjourn);
      updateOffer(gameInfo, OfferEvent.ABORT_OFFER, Player.WHITE_PLAYER, whiteAbort);
      updateOffer(gameInfo, OfferEvent.ABORT_OFFER, Player.BLACK_PLAYER, blackAbort);

      updateTakebackOffer(gameInfo, Player.WHITE_PLAYER, whiteTakeback);
      updateTakebackOffer(gameInfo, Player.BLACK_PLAYER, blackTakeback);
    } catch (NoSuchGameException e){}

  }



  /**
   * Updates the specified offer if necessary.
   */

  private void updateOffer(GameInfo gameInfo, int offerId, Player player, boolean newState){
    if (newState ^ gameInfo.isOfferred(offerId, player)){
      gameInfo.setOffer(offerId, player, newState);
      fireGameEvent(new OfferEvent(this, gameInfo.game, offerId, newState, player));
    }
  }



  /**
   * Updates a takeback offer, if necessary.
   */

  private void updateTakebackOffer(GameInfo gameInfo, Player player, int takebackCount){
    int oldTakeback;
    if ((oldTakeback = gameInfo.getTakebackOffer(player)) != takebackCount){
      if (oldTakeback != 0)
        fireGameEvent(new OfferEvent(this, gameInfo.game, false, player, oldTakeback));

      gameInfo.setTakebackOffer(player, takebackCount);

      if (takebackCount != 0)
        fireGameEvent(new OfferEvent(this, gameInfo.game, true, player, takebackCount));
    }
  }



  /**
   * Clears all existing offers for the specified player in the specified game
   * and fires the appropriate events.
   */

  private void clearOffers(GameInfo gameInfo, Player player){
    updateOffer(gameInfo, OfferEvent.DRAW_OFFER, player, false);
    updateOffer(gameInfo, OfferEvent.ADJOURN_OFFER, player, false);
    updateOffer(gameInfo, OfferEvent.ABORT_OFFER, player, false);

    updateTakebackOffer(gameInfo, player, 0);
  }
  
  
  
  /**
   * Processes a DG_ILLEGAL_MOVE.
   */
   
  private void processIllegalMoveDG(Datagram dg){
    processIllegalMove(dg.getInteger(0), dg.getString(1), dg.getInteger(2));
  }



  /**
   * Fires an IllegalMoveEvent.
   */

  protected void processIllegalMove(int gameNumber, String moveString, int reasonCode){
    try{
      GameInfo gameInfo = getGameInfo(gameNumber);
      Game game = gameInfo.game;

      Vector unechoedGameMoves = (Vector)unechoedMoves.get(game);
      if ((unechoedGameMoves == null) || (unechoedGameMoves.size() == 0)) // Not a move we made (probably the user typed it in)
        return;

      Move move = (Move)unechoedGameMoves.elementAt(0);
      if (moveToString(game, move).equals(moveString)){ // Our move
        unechoedGameMoves.removeAllElements();
        fireGameEvent(new IllegalMoveEvent(this, game, move));
      }
    } catch (NoSuchGameException e){}
  }
  
  
  
  /**
   * Processes a DG_MSEC.
   */
   
  private void processMsecDG(Datagram dg){
    processMsec(dg.getInteger(0), dg.getString(1).equals("W"), dg.getInteger(2),
      dg.getBoolean(3));
  }


  
  /**
   * Fires a ClockAdjustmentEvent.
   */

  protected void processMsec(int gameNumber, boolean isWhite, int msec, boolean isRunning){
    try{
      GameInfo gameInfo = getGameInfo(gameNumber);
      Game game = gameInfo.game;
      
      if (isWhite)
        gameInfo.setWhiteClock(msec, isRunning);
      else
        gameInfo.setBlackClock(msec, isRunning);

      Player player = (isWhite ? Player.WHITE_PLAYER : Player.BLACK_PLAYER);
      fireGameEvent(new ClockAdjustmentEvent(this, game, player, msec, isRunning && game.isPlayed()));
      // The server always sends isRunning==true, even for examined games where
      // it doesn't make sense.
      
    } catch (NoSuchGameException e){}
  }
  
  
  
  /**
   * Processes a DG_MORETIME.
   */
   
  private void processMoretimeDG(Datagram dg){
    processMoretime(dg.getInteger(0), dg.getString(1).equals("W"), dg.getInteger(2));
  }



  /**
   * Fires a ClockAdjustmentEvent.
   */

  protected void processMoretime(int gameNumber, boolean isWhite, int seconds){
    try{
      GameInfo gameInfo = getGameInfo(gameNumber);
      Game game = gameInfo.game;

      if (isWhite){
        int newTime = gameInfo.getWhiteTime() + seconds * 1000;
        boolean isRunning = gameInfo.isWhiteClockRunning();
        if (isRunning)
          newTime -= (int)(System.currentTimeMillis() - gameInfo.getWhiteTimestamp());

        gameInfo.setWhiteClock(newTime, isRunning);
        fireGameEvent(new ClockAdjustmentEvent(this, game, Player.WHITE_PLAYER, newTime, isRunning));
      }
      else{
        int newTime = gameInfo.getBlackTime() + seconds * 1000;
        boolean isRunning = gameInfo.isBlackClockRunning();
        if (isRunning)
          newTime -= (int)(System.currentTimeMillis() - gameInfo.getBlackTimestamp());

        gameInfo.setBlackClock(newTime, isRunning);
        fireGameEvent(new ClockAdjustmentEvent(this, game, Player.BLACK_PLAYER, newTime, isRunning));
      }
    } catch (NoSuchGameException e){}
  }


  
  /**
   * Processes a DG_FLIP. 
   */
   
  private void processFlipDG(Datagram dg){
    processFlip(dg.getInteger(0), dg.getBoolean(1));
  }



  /**
   * Fires a BoardFlipEvent if the game has started, or sets the "InitiallyFlipped"
   * property of the new Game if not.
   */

  protected void processFlip(int gameNumber, boolean isFlipped){
    if (existsNonStarted(gameNumber)){
      putPropertyForNonStarted(gameNumber, "InitiallyFlipped", isFlipped ? Boolean.TRUE : Boolean.FALSE);
    }
    else{
      try{
        GameInfo gameInfo = getGameInfo(gameNumber);
        Game game = gameInfo.game;
        gameInfo.isFlipped = isFlipped;

        fireGameEvent(new BoardFlipEvent(this, game, isFlipped));
      } catch (NoSuchGameException e){}
    }
  }
  
  
  
  /**
   * Processes a DG_CIRCLE. 
   */
   
  private void processCircleDG(Datagram dg){
    processCircle(dg.getInteger(0), dg.getString(1), dg.getString(2));    
  }



  /**
   * Gets called when a DG_CIRCLE datagram arrives.
   */

  protected void processCircle(int gameNumber, String examiner, String coordinate){
    try{
      GameInfo gameInfo = getGameInfo(gameNumber);
      Game game = gameInfo.game;

      Square circleSquare = Square.parseSquare(coordinate);

      fireGameEvent(new CircleEvent(this, game, CircleEvent.CIRCLE_ADDED, circleSquare));
    } catch (NoSuchGameException e){}
  }
  
  
  
  /**
   * Processes a DG_UNCIRCLE.
   */
   
  private void processUncircleDG(Datagram dg){
    processUncircle(dg.getInteger(0), dg.getString(1), dg.getString(2));
  }
  
  
  
  /**
   * Gets called when a DG_UNCIRCLE datagram arrives.
   */
   
  protected void processUncircle(int gameNumber, String examiner, String coordinate){
    try{
      GameInfo gameInfo = getGameInfo(gameNumber);
      Game game = gameInfo.game;

      Square circleSquare = Square.parseSquare(coordinate);

      fireGameEvent(new CircleEvent(this, game, CircleEvent.CIRCLE_REMOVED, circleSquare));
    } catch (NoSuchGameException e){}
  }
  
  
  
  /**
   * Processes a DG_ARROW.
   */
   
  private void processArrowDG(Datagram dg){
    processArrow(dg.getInteger(0), dg.getString(1), dg.getString(2), dg.getString(3));
  }



  /**
   * Gets called when a DG_ARROW datagram arrives.
   */

  protected void processArrow(int gameNumber, String examiner, String origin, String destination){
    try{
      GameInfo gameInfo = getGameInfo(gameNumber);
      Game game = gameInfo.game;

      Square fromSquare = Square.parseSquare(origin);
      Square toSquare = Square.parseSquare(destination);

      fireGameEvent(new ArrowEvent(this, game, ArrowEvent.ARROW_ADDED, fromSquare, toSquare));
    } catch (NoSuchGameException e){}
  }
  
  
  
  /**
   * Processes a DG_UNARROW.
   */
   
  private void processUnarrowDG(Datagram dg){
    processUnarrow(dg.getInteger(0), dg.getString(1), dg.getString(2), dg.getString(3));
  }


  
  /**
   * Gets called when a DG_UNARROW datagram arrives.
   */

  protected void processUnarrow(int gameNumber, String examiner, String origin, String destination){
    try{
      GameInfo gameInfo = getGameInfo(gameNumber);
      Game game = gameInfo.game;

      Square fromSquare = Square.parseSquare(origin);
      Square toSquare = Square.parseSquare(destination);

      fireGameEvent(new ArrowEvent(this, game, ArrowEvent.ARROW_REMOVED, fromSquare, toSquare));
    } catch (NoSuchGameException e){}
  }
  


  /**
   * Dispatches the given GameEvent to all interested GameListeners.
   */
  
  private void fireGameEvent(GameEvent evt){
    listenerManager.fireGameEvent(evt);
  }




  /**
   * Depending on the type of the game, issues either "unobserve <gameNumber>",
   * "unexamine" or "resign" command.
   */

  public void quitGame(Game game){
    Object id = game.getID();
    switch (game.getGameType()){
      case Game.MY_GAME:
        if (game.isPlayed())
          resign(game);
        else
          sendCommand("unexamine");
        break;
      case Game.OBSERVED_GAME:
        sendCommand("unobserve "+id);
        break;
      case Game.ISOLATED_BOARD:
        break;
    }
  }




  /**
   * Makes the given move in the given game.
   */

  public void makeMove(Game game, Move move){
    String moveString = moveToString(game, move);
    if (userGamesCount > 1)
      // It seems that "; goto <gamenum> ; <movestring>" will abort making the
      // move (or whatever other command follows it) if you aren't playing
      // a game with the specified number.
      sendCommand("; goto " + game.getID() + " ; chessmove " + moveString);
    else
      sendCommand("chessmove " + moveString);

    Vector unechoedGameMoves = (Vector)unechoedMoves.get(game);
    if (unechoedGameMoves == null){
      unechoedGameMoves = new Vector(2);
      unechoedMoves.put(game, unechoedGameMoves);
    }
    unechoedGameMoves.addElement(move);
  }




  /**
   * Converts the given move into a string we can send to the server.
   * Throws an <code>IllegalArgumentException</code> if the move is not of a
   * type that we know how to send to the server.
   */

  private static String moveToString(Game game, Move move){
    WildVariant variant = game.getVariant();
    if (move instanceof ChessMove){
      ChessMove cmove = (ChessMove)move;
      if (cmove.isShortCastling())
        return "O-O";
      else if (cmove.isLongCastling())
        return "O-O-O";

      String s = cmove.getStartingSquare().toString() + cmove.getEndingSquare().toString();
      if (cmove.isPromotion())
        return s + "=" + variant.pieceToString(cmove.getPromotionTarget());
      else
        return s;
    }
    else
      throw new IllegalArgumentException("Unsupported Move type: "+move.getClass());
  }






  /**
   * Throws an IllegalArgumentException if the given Game is not of type 
   * Game.MY_GAME or is not a played game. Otherwise, simply returns.
   */

  private void checkGameMineAndPlayed(Game game){
    if ((game.getGameType() != Game.MY_GAME) || !game.isPlayed())
      throw new IllegalArgumentException("The given game must be of type Game.MY_GAME and a played one");
  }




  /**
   * Throws an IllegalArgumentException if the given Game is not of type 
   * Game.MY_GAME or is a played game. Otherwise, simply returns.
   */

  private void checkGameMineAndExamined(Game game){
    if ((game.getGameType() != Game.MY_GAME) || game.isPlayed())
      throw new IllegalArgumentException("The given game must be of type Game.MY_GAME and an examined one");
  }




  /**
   * Resigns the given game. The given game must be a played game and of type
   * Game.MY_GAME.
   */

  public void resign(Game game){
    checkGameMineAndPlayed(game);

    if (userGamesCount > 1)
      sendCommand("; goto " + game.getID() + " ; resign");
    else
      sendCommand("resign");
  }




  /**
   * Sends a request to draw the given game. The given game must be a played 
   * game and of type Game.MY_GAME.
   */

  public void requestDraw(Game game){
    checkGameMineAndPlayed(game);

    if (userGamesCount > 1)
      sendCommand("; goto " + game.getID() + " ; draw");
    else
      sendCommand("draw");
  }




  /**
   * Returns true.
   */

  public boolean isAbortSupported(){
    return true;
  }




  /**
   * Sends a request to abort the given game. The given game must be a played 
   * game and of type Game.MY_GAME.
   */

  public void requestAbort(Game game){
    checkGameMineAndPlayed(game);

    if (userGamesCount > 1)
      sendCommand("; goto " + game.getID() + " ; abort");
    else
      sendCommand("abort");
  }



  /**
   * Returns true.
   */

  public boolean isAdjournSupported(){
    return true;
  }



  /**
   * Sends a request to adjourn the given game. The given game must be a played
   * game and of type Game.MY_GAME.
   */

  public void requestAdjourn(Game game){
    checkGameMineAndPlayed(game);

    if (userGamesCount > 1)
      sendCommand("; goto " + game.getID() + " ; adjourn");
    else
      sendCommand("adjourn");
  }
  
  
  
  /**
   * Returns <code>true</code>.
   */
   
  public boolean isTakebackSupported(){
    return true;
  }
  
  
  
  /**
   * Sends "takeback 1" to the server.
   */
   
  public void requestTakeback(Game game){
    checkGameMineAndPlayed(game);
    
    sendCommand("takeback 1");
  }
  
  
  
  /**
   * Returns <code>true</code>.
   */
   
  public boolean isMultipleTakebackSupported(){
    return true;
  }
  
  
  
  /**
   * Sends "takeback plyCount" to the server.
   */
   
  public void requestTakeback(Game game, int plyCount){
    checkGameMineAndPlayed(game);
    
    if (plyCount < 1)
      throw new IllegalArgumentException("Illegal ply count: " + plyCount);
    
    sendCommand("takeback " + plyCount);
  }




   /**
   * Goes back the given amount of plies in the given game. If the given amount
   * of plies is bigger than the amount of plies since the beginning of the game,
   * goes to the beginning of the game.
   */

  public void goBackward(Game game, int plyCount){
    checkGameMineAndExamined(game);

    if (plyCount < 1)
      throw new IllegalArgumentException("Illegal ply count: " + plyCount);
    
    sendCommand("backward " + plyCount);
  }




  /**
   * Goes forward the given amount of plies in the given game. If the given amount
   * of plies is bigger than the amount of plies remaining until the end of the
   * game, goes to the end of the game.
   */

  public void goForward(Game game, int plyCount){
    checkGameMineAndExamined(game);

    if (plyCount < 1)
      throw new IllegalArgumentException("Illegal ply count: " + plyCount);
    
    sendCommand("forward " + plyCount);
  }




  /**
   * Goes to the beginning of the given game.
   */

  public void goToBeginning(Game game){
    checkGameMineAndExamined(game);

    sendCommand("backward 9999");
  }



  /**
   * Goes to the end of the given game.
   */

  public void goToEnd(Game game){
    checkGameMineAndExamined(game);

    sendCommand("forward 9999");
  }
  
  
  
  /**
   * Sends the "help" command to the server. 
   */
   
  public void showServerHelp(){
    sendCommand("help");
  }
  
  
  
  /**
   * Sends the specified question string to channel 1.
   */
   
  public void sendHelpQuestion(String question){
    sendCommand("tell 1 * " + question);    
  }
  


  /**
   * A container for various game information, such as the Game object, the current
   * Position and a list of made moves.
   */

  private static class GameInfo{


    /**
     * The Game.
     */
    
    public final Game game;



    /**
     * The current position.
     */

    public final Position position;




    /**
     * The list of moves.
     */

    public final Vector moves;




    /**
     * The amount of moves to follow before the game actually starts.
     */

    public int numMovesToFollow;





    /**
     * True if the board should be flipped.
     */

    public boolean isFlipped;




    /**
     * White's time, in milliseconds.
     */

    private int whiteTime;



    /**
     * Whether white's clock is running.
     */

    private boolean isWhiteRunning;



    /**
     * The value of System.currentTimeMillis() when whiteTime and isWhiteRunning were
     * last updated.
     */

    private long whiteTimestamp;



    /**
     * Black's time, in milliseconds.
     */

    private int blackTime;



    /**
     * Whether black's clock is running.
     */

    private boolean isBlackRunning;



    /**
     * The value of System.currentTimeMillis() when whiteTime and isBlackRunning
     * were last updated.
     */

    private long blackTimestamp;



    /**
     * Works as a set of the offers currently in this game. The elements are
     * Pairs in which the first item is the player who made the offer and the
     * second one is the offer id. Takeback offers are kept separately.
     */

    private final Hashtable offers = new Hashtable();



    /**
     * The number of plies the white player offerred to takeback.
     */

    private int whiteTakeback;



    /**
     * The number of plies the black player offerred to takeback.
     */

    private int blackTakeback;



    /**
     * Creates a new GameInfo object with the given Game, initial position and
     * the amount of moves to follow before the game actually starts (although
     * the start event is fired right away).
     */

    public GameInfo(Game game, Position initialPos, int numMovesToFollow){
      this.game = game;
      this.position = initialPos;
      this.moves = new Vector();
      this.numMovesToFollow = numMovesToFollow;
      this.isFlipped = game.isBoardInitiallyFlipped();

      setWhiteClock(game.getWhiteTime(), false);
      setBlackClock(game.getBlackTime(), false);
    }



    /**
     * Sets the amount of time remaining on white's clock, in milliseconds.
     */

    public void setWhiteClock(int time, boolean running){
      whiteTime = time;
      isWhiteRunning = running;
      whiteTimestamp = System.currentTimeMillis();
    }



    /**
     * Sets the amount of time remaining on black's clock, in milliseconds.
     */

    public void setBlackClock(int time, boolean running){
      blackTime = time;
      isBlackRunning = running;
      blackTimestamp = System.currentTimeMillis();
    }



    /**
     * Returns the amount of time on white's clock when it was last updated,
     * in milliseconds.
     */

    public int getWhiteTime(){
      return whiteTime;
    }



    /**
     * Returns true if white's clock is currently running.
     */

    public boolean isWhiteClockRunning(){
      return isWhiteRunning;
    }



    /**
     * Returns the value of System.currentTimeMillis() the last time white's clock
     * was updated.
     */

    public long getWhiteTimestamp(){
      return whiteTimestamp;
    }



    /**
     * Returns the amount of time on black's clock when it was last updated,
     * in milliseconds.
     */

    public int getBlackTime(){
      return blackTime;
    }



    /**
     * Returns true if black's clock is currently running.
     */

    public boolean isBlackClockRunning(){
      return isBlackRunning;
    }



    /**
     * Returns the value of System.currentTimeMillis() the last time white's
     * clock was updated.
     */

    public long getBlackTimestamp(){
      return blackTimestamp;
    }



    /**
     * Sets the state of the specified offer in the game. Takeback offers are
     * handled by the setTakebackCount method.
     */

    public void setOffer(int offerId, Player player, boolean isMade){
      Pair offer = new Pair(player, new Integer(offerId));
      if (isMade) 
        offers.put(offer, offer);
      else
        offers.remove(offer);
    }



    /**
     * Adds the specified takeback offer to the offers in the game.
     */

    public void setTakebackOffer(Player player, int plies){
      if (player.isWhite())
        whiteTakeback = plies;
      else
        blackTakeback = plies;
    }



    /**
     * Returns true if the specified offer has been made (and hasn't been
     * withdrawn) in the game. Takeback offers are handled by the
     * getTakebackCount method.
     */

    public boolean isOfferred(int offerId, Player player){
      return offers.containsKey(new Pair(player, new Integer(offerId)));
    }



    /**
     * Returns the amount of plies offered to take back by the specified player.
     */

    public int getTakebackOffer(Player player){
      if (player.isWhite())
        return whiteTakeback;
      else
        return blackTakeback;
    }

    

  }



  /**
   * Maps seek IDs to Seek objects currently in the sought list.
   */

  private final Hashtable seeks = new Hashtable();



  /**
   * Returns the <code>SeekListenerManager</code> via which you can register and
   * unregister SeekListeners.
   */

  public SeekListenerManager getSeekListenerManager(){
    return getChessclubListenerManager();
  }



  /**
   * This method is called by our ChessclubJinListenerManager when a new
   * SeekListener is added and we already had registered listeners (meaning that
   * DG_SEEK was already on, so we need to notify the new listeners of all
   * existing seeks as well).
   */

  void notFirstListenerAdded(SeekListener listener){
    Enumeration seeksEnum = seeks.elements();
    while (seeksEnum.hasMoreElements()){
      Seek seek = (Seek)seeksEnum.nextElement();
      SeekEvent evt = new SeekEvent(this, SeekEvent.SEEK_ADDED, seek);
      listener.seekAdded(evt);
    }
  }





  /**
   * This method is called by our ChessclubJinListenerManager when the last
   * SeekListener is removed.
   */

  void lastSeekListenerRemoved(){
    seeks.clear();
  }
  
  
  
  /**
   * Processes a DG_SEEK. 
   */
   
  private void processSeekDG(Datagram dg){
    int index = dg.getInteger(0);
    String name = dg.getString(1);
    String titles = dg.getString(2);
    int rating = dg.getInteger(3);
    int ratingType = dg.getInteger(4);
    int wild = dg.getInteger(5);
    String ratingCategoryString = dg.getString(6);
    int time = dg.getInteger(7);
    int inc = dg.getInteger(8);
    boolean isRated = dg.getBoolean(9);
    int colorPreference = dg.getInteger(10);
    int minRating = dg.getInteger(11);
    int maxRating = dg.getInteger(12);
    boolean autoAccept = dg.getBoolean(13);
    boolean formula = dg.getBoolean(14);
    String fancyTimeControl = dg.getString(15);
    
    processSeek(index, name, titles, rating, ratingType, wild, ratingCategoryString, time, inc,
      isRated, colorPreference, minRating, maxRating, autoAccept, formula, fancyTimeControl);
  }



  /**
   * Fires the appropriate SeekEvent indicating a seek was added.
   */

  protected void processSeek(int index, String name, String titles, int rating, int ratingType, int wild,
      String ratingCategoryString, int time, int inc, boolean isRated, int color, int minRating, int maxRating, 
      boolean autoaccept, boolean formula, String fancyTimeControl){
    
    WildVariant variant = getVariant(wild);
    if (variant == null)
      return;

    boolean isProvisional = (ratingType != ChessclubConstants.ESTABLISHED_RATING_TYPE);
    Player player;
    if (color == ChessclubConstants.WHITE_COLOR_PREFERENCE)
      player = Player.WHITE_PLAYER;
    else if (color == ChessclubConstants.BLACK_COLOR_PREFERENCE)
      player = Player.BLACK_PLAYER;
    else
      player = null;

    boolean isRegistered = (rating != 0);
    boolean isSeekerRated = !isUnrated(titles);
    boolean isComputer = isComputer(titles);
    boolean isRatingLimited = ((minRating != 0) || (maxRating != 9999));

    String title = displayableTitle(titles);

    Seek seek = new Seek(String.valueOf(index), name, title, rating, isProvisional, isRegistered, isSeekerRated, isComputer, variant,
      ratingCategoryString, time*60*1000, inc*1000, isRated, player, isRatingLimited, minRating, maxRating, !autoaccept, formula);

    seeks.put(new Integer(index), seek);

    listenerManager.fireSeekEvent(new SeekEvent(this, SeekEvent.SEEK_ADDED, seek));
  }


  
  /**
   * Processes a DG_SEEK_REMOVED.
   */
   
  private void processSeekRemovedDG(Datagram dg){
    processSeekRemoved(dg.getInteger(0), dg.getInteger(1));
  }


  /**
   * Fires the appropriate SeekEvent indicating a seek was removed.
   */

  protected void processSeekRemoved(int index, int reasonCode){
    Seek seek = (Seek)seeks.remove(new Integer(index));

    if (seek == null)
      return;

    listenerManager.fireSeekEvent(new SeekEvent(this, SeekEvent.SEEK_REMOVED, seek));    
  }




  /**
   * Accepts the given seek. Note that the given seek must be an instance generated
   * by this SeekJinConnection and it must be in the current sought list.
   */

  public void acceptSeek(Seek seek){
    if (!seeks.contains(seek))
      throw new IllegalArgumentException("The specified seek is not on the seek list");

    sendCommand("play " + seek.getID());
  }
  
  
  
  /**
   * Issues the specified seek.
   */
   
  public void issueSeek(UserSeek seek){
    int wildNumber = getWildNumber(seek.getVariant());
    if (wildNumber == -1)
      throw new IllegalArgumentException("Unsupported sought wild variant: " + seek.getVariant());
    
    Player color = seek.getColor();
    
    
    String seekCommand = "seek " + seek.getTime() + " " + seek.getInc() + " " +
      (seek.isRated() ? "r" : "u") + " " + 
      "w" + wildNumber + " " +
      (color == null ? "" : color.isWhite() ? "white " : "black ") +
      (seek.isManualAccept() ? "manual " : "auto ") +
      (seek.getMinRating() == Integer.MIN_VALUE ? "0" : String.valueOf(seek.getMinRating())) + "-" +
      (seek.getMaxRating() == Integer.MAX_VALUE ? "9999" : String.valueOf(seek.getMaxRating())) + " " +
      (seek.isFormula() ? "f" : "");
      
    sendCommand(seekCommand);
  }




  /**
   * The GameListInfo for the list we're currently reading. I'm not holding
   * a hashtable in hope that different lists will not be mixed.
   */

  private GameListInfo curGameListInfo = null;




  /**
   * Returns the <code>GameListListenerManager</code> via which you can register
   * and unregister GameListListeners.
   */

  public GameListListenerManager getGameListListenerManager(){
    return getChessclubListenerManager();
  }
  
  
  
  /**
   * Processes a DG_GAMELIST_BEGIN.
   */
   
  private void processGamelistBeginDG(Datagram dg){
    processGamelistBegin(dg.getString(0), dg.getString(1), dg.getInteger(2),
          dg.getInteger(3), dg.getInteger(4), dg.getString(5));
  }



  /**
   * Gets called when a DG_GAMELIST_BEGIN datagram arrives. Prepares for
   * receiving the list.
   */

  protected void processGamelistBegin(String command, String argsString,  int hitsCount, 
      int firstIndex, int lastIndex, String summary){

    int id;

    if (command.equalsIgnoreCase("history"))
      id = GameListEvent.HISTORY_LIST_EVENT_ID;
    else if(command.equalsIgnoreCase("search"))
      id = GameListEvent.SEARCH_LIST_EVENT_ID;
    else if (command.equalsIgnoreCase("liblist"))
      id = GameListEvent.LIBLIST_EVENT_ID;
    else if (command.equalsIgnoreCase("stored"))
      id = GameListEvent.STORED_LIST_EVENT_ID;
    else
      return; // Ignore unknown lists

    curGameListInfo = new GameListInfo(id, command, argsString, hitsCount, firstIndex, lastIndex, summary);
  }
  
  
  
  /**
   * Processes a DG_GAMELIST_ITEM.
   */
   
  private void processGamelistItemDG(Datagram dg){
    int index = dg.getInteger(0);
    String id = dg.getString(1);
    String event = dg.getString(2);
    String date = dg.getString(3);
    String time = dg.getString(4);
    String whiteName = dg.getString(5);
    int whiteRating = dg.getString(6).equals("?") ? -1 : dg.getInteger(6);
    String blackName = dg.getString(7);
    int blackRating = dg.getString(8).equals("?") ? -1 : dg.getInteger(8);
    boolean isRated = dg.getBoolean(9);
    int ratingCategory = dg.getInteger(10);
    int wildType = dg.getInteger(11);
    int whiteInit = dg.getInteger(12);
    int whiteInc = dg.getInteger(13);
    int blackInit = dg.getInteger(14);
    int blackInc = dg.getInteger(15);
    String eco = dg.getString(16);
    int status = dg.getInteger(17);
    boolean isWhite = dg.getInteger(18) == 1;
    int mode = dg.getInteger(19);
    String note = dg.getString(20);
    boolean isOppHere = dg.getBoolean(21);
    
    processGamelistItem(index, id, event, date, time, whiteName, whiteRating, blackName,
      blackRating, isRated, ratingCategory, wildType, whiteInit, whiteInc,
      blackInit, blackInc, eco, status, isWhite, mode, note, isOppHere);
  }



  /**
   * Gets called when a DG_GAMELIST_ITEM datagram arrives. Fires a GameListEvent
   * to registered listeners.
   */

  protected void processGamelistItem(int index, String id, String event, String dateString,
      String timeString, String whiteName, int whiteRating, String blackName, int blackRating,
      boolean isRated, int ratingCategory, int wildType, int whiteTime, int whiteInc,
      int blackTime, int blackInc, String eco, int status, boolean isWhite, int mode, String note,
      boolean isHere){

    if (curGameListInfo == null)
      return; // This shouldn't happen, but let's be a little defensive.


    String variantName = "w" + wildType;
    String ratingCategoryName = getRatingCategoryName(ratingCategory);

    String endExplanationString = getEndExplanationString(status, mode, isWhite);
    int resultStatus;
    switch (status){
      case 0:
        if (isWhite)
          resultStatus = WHITE_LOST;
        else
          resultStatus = WHITE_WON;
        break;
      case 1:
        resultStatus = DRAWN;
        break;
      case 2:
        resultStatus = ADJOURNED;
        break;
      case 3:
        resultStatus = ABORTED;
        break;
      default:
        throw new IllegalArgumentException("Unknown result status encountered");
    }

    GameListItem item;
    switch (curGameListInfo.gameListEventID){
      case GameListEvent.HISTORY_LIST_EVENT_ID:{
        Player player;
        if (curGameListInfo.args.indexOf(whiteName)!=-1)
          player = Player.WHITE_PLAYER;
        else if (curGameListInfo.args.indexOf(blackName)!=-1)
          player = Player.BLACK_PLAYER;
        else
          throw new IllegalArgumentException("Unable to determine the player whose history is being retrieved"); 

        item = new HistoryListItem(index, id, dateString, timeString, whiteName, blackName, whiteTime*60*1000,
            whiteInc*1000, blackTime*60*1000, blackInc*1000, whiteRating, blackRating,
            isRated, variantName, ratingCategoryName, eco, endExplanationString, resultStatus, player);
        break;
      }

      case GameListEvent.SEARCH_LIST_EVENT_ID:{
        item = new SearchListItem(index, id, dateString, timeString, whiteName, blackName, whiteTime*60*1000,
            whiteInc*1000, blackTime*60*1000, blackInc*1000, whiteRating, blackRating,
            isRated, variantName, ratingCategoryName, eco, endExplanationString, resultStatus);
        break;
      }

      case GameListEvent.LIBLIST_EVENT_ID:{
        item = new LibListItem(index, id, dateString, timeString, whiteName, blackName, whiteTime*60*1000,
            whiteInc*1000, blackTime*60*1000, blackInc*1000, whiteRating, blackRating,
            isRated, variantName, ratingCategoryName, eco, endExplanationString, resultStatus, note);
        break;
      }

      case GameListEvent.STORED_LIST_EVENT_ID:{
        Player player;
        if (curGameListInfo.args.indexOf(whiteName)!=-1)
          player = Player.WHITE_PLAYER;
        else if (curGameListInfo.args.indexOf(blackName)!=-1)
          player = Player.BLACK_PLAYER;
        else
          throw new IllegalArgumentException("Unable to determine the player whose stored game is being retrieved"); 

        if (resultStatus!=ADJOURNED)
          throw new IllegalArgumentException("ResultStatus is not \"ADJOURNED\" for a stored game");

        String adjournmentReason = getAdjournmentReason(mode, isWhite ? whiteName : blackName);

        item = new StoredListItem(index, id, dateString, timeString, whiteName, blackName, whiteTime*60*1000,
            whiteInc*1000, blackTime*60*1000, blackInc*1000, whiteRating, blackRating,
            isRated, variantName, ratingCategoryName, eco, isHere, adjournmentReason, player);
        break;
      }
      default:
        throw new IllegalStateException("Attempted to create a GameListInfo with an unknown item type");
    }

    curGameListInfo.gameList[curGameListInfo.gameList.length-curGameListInfo.numItemsLeft] = item;
    curGameListInfo.numItemsLeft--;

    if (curGameListInfo.numItemsLeft==0){
      String title = curGameListInfo.summary+" ("+curGameListInfo.command+" "+curGameListInfo.args+")";
      GameListEvent evt = new GameListEvent(this, curGameListInfo.gameListEventID, curGameListInfo.gameList, 
        title, curGameListInfo.totalNumItems, curGameListInfo.firstIndex, curGameListInfo.lastIndex);
      listenerManager.fireGameListEvent(evt);

      curGameListInfo = null;
    }

  }





  /**
   * Returns the end game explanation string corresponding to the given status,
   * mode and the color of the player who lost.
   */

  protected String getEndExplanationString(int status, int mode, boolean isWhite){
    I18n i18n = I18n.get(getClass());
    
    String white = i18n.getString("whitePlayer");
    String black = i18n.getString("blackPlayer");
    
    String winner = (isWhite ? black : white);
    String loser = (isWhite ? white : black);
    String result = (isWhite ? "(0-1)" : "(1-0)");

    try{
      String explanationKey = "gameEndExplanation" + "S" + status + "M" + mode;
      return i18n.getFormattedString(explanationKey, new Object[]{winner, loser, result});
    } catch (MissingResourceException e){
        return i18n.getString("defaultGameEndExplanation");
      }
  }



  /**
   * Returns the adjournment reason corresponding to the given mode. Status
   * is assumed to be ADJOURNED.
   */

  protected String getAdjournmentReason(int mode, String actor){
    I18n i18n = I18n.get(getClass());
    
    
    try{
      String adjournmentReasonKey = "adjournmentReason" + mode;
      return i18n.getFormattedString(adjournmentReasonKey, new Object[]{actor});
    } catch (MissingResourceException e){
        return "";
      }
  }





  private static class GameListInfo{



    /**
     * The GameListEvent id identifying the type of the game list items.
     */

    public final int gameListEventID;


    /**
     * The name of the command that triggered the game list being sent.
     */

    public final String command;



    /**
     * The arguments to the command that triggered the game list being sent.
     */

    public final String args;



    /**
     * The amount of items in the complete list.
     */

    public final int totalNumItems;



    /**
     * The index of the first sent item in the complete list.
     */

    public final int firstIndex;




    /**
     * The index of the last sent item in the complete list.
     */

    public final int lastIndex;




    /**
     * A summary.
     */

    public final String summary;




    /**
     * The amount of items left to be sent in the list.
     */

    public int numItemsLeft;




    /**
     * An array of the game list items.
     */

    public final GameListItem [] gameList;




    /**
     * Creates a new GameListInfo.
     */

    public GameListInfo(int id, String command, String args, int totalNumItems, int firstIndex,
        int lastIndex, String summary){

      this.gameListEventID = id;
      this.command = command;
      this.args = args;
      this.totalNumItems = totalNumItems;
      this.firstIndex = firstIndex;
      this.lastIndex = lastIndex;
      this.summary = summary;
      this.numItemsLeft = lastIndex-firstIndex+1;

      Class gameListItemType;
      switch (gameListEventID){
        case GameListEvent.HISTORY_LIST_EVENT_ID:
          gameListItemType = HistoryListItem.class;
          break;
        case GameListEvent.SEARCH_LIST_EVENT_ID:
          gameListItemType = SearchListItem.class;
          break;
        case GameListEvent.LIBLIST_EVENT_ID:
          gameListItemType = LibListItem.class;
          break;
        case GameListEvent.STORED_LIST_EVENT_ID:
          gameListItemType = StoredListItem.class;
          break;
        default:
          throw new IllegalStateException("Attempted to create a GameListInfo with an unknown item type");
      }

      gameList = (GameListItem [])Array.newInstance(gameListItemType, numItemsLeft);
    }

  }




  /**
   * Maps ChessEvent IDs (as Integer objects) to ChessEvent objects.
   * Contains the currently existing events.
   */

  private final Hashtable chessEvents = new Hashtable();
  


  /**
   * Processes a DG_TOURNEY.
   */
   
  private void processTourneyDG(Datagram dg){
    int id = dg.getInteger(0);
    int bitfield = dg.getInteger(1);
    boolean canGuestsJoinWatch = (bitfield & 1) != 0;
    boolean makeNewWindowOnJoin = (bitfield & 2) != 0;
    boolean makeNewWindowOnWatch = (bitfield & 4) != 0;
    boolean makeNewWindowOnInfo = (bitfield & 8) != 0;
    String description = dg.getString(2);
    String [] joinCommands = parseDGTourneyCommandList(dg.getString(3));
    String [] watchCommands = parseDGTourneyCommandList(dg.getString(4));
    String [] infoCommands = parseDGTourneyCommandList(dg.getString(5));
    String confirmText = dg.getString(6);
    
    processTourney(id, canGuestsJoinWatch, makeNewWindowOnJoin, makeNewWindowOnWatch,
      makeNewWindowOnInfo, description, joinCommands, watchCommands, infoCommands, confirmText);
  }



  /**
   * Parses the given command list as is sent in the DG_TOURNEY datagram.
   * Returns the list of commands.
   */

  private static String [] parseDGTourneyCommandList(String commands){
    StringTokenizer tokenizer = new StringTokenizer(commands, "&");
    String [] commandList = new String[tokenizer.countTokens()];
    int commandCount = 0;

    while (tokenizer.hasMoreTokens()){
      String escaped = tokenizer.nextToken();
      StringBuffer unescaped = new StringBuffer(escaped.length());
      int length = escaped.length();
      int i = 0;
      while (i < length){
        if (escaped.charAt(i) == '\\'){
          unescaped.append(escaped.charAt(i+1));
          i += 2;
        }
        else{
          unescaped.append(escaped.charAt(i));
          i++;
        }
      }
      commandList[commandCount++] = unescaped.toString();
    }

    return commandList;
  }


  

  /**
   * Gets called when a DG_TOURNEY event arrives. Fires the appropriate
   * ChessEvent to any registered listeners.
   */

  protected void processTourney(int id, boolean canGuestsWatchJoin, boolean makeNewWindowOnJoin, boolean makeNewWindowOnWatch,
    boolean makeNewWindowOnInfo, String description, String [] joinCommands, String [] watchCommands, String [] infoCommands,
    String confirmText){

    ChessEvent newEvent = new ChessEvent(id, description, joinCommands.length == 0 ? null : joinCommands, 
      watchCommands.length == 0 ? null : watchCommands, infoCommands.length == 0 ? null : infoCommands, confirmText);
    ChessEvent existingEvent = (ChessEvent)chessEvents.put(new Integer(id), newEvent);

    if (existingEvent != null)
      listenerManager.fireChessEventEvent(new ChessEventEvent(this, ChessEventEvent.EVENT_REMOVED, existingEvent));

    listenerManager.fireChessEventEvent(new ChessEventEvent(this, ChessEventEvent.EVENT_ADDED, newEvent));
  }
  
  
  
  /**
   * Processes a DG_REMOVE_TOURNEY.
   */
   
  private void processRemoveTourneyDG(Datagram dg){
    processRemoveTourney(dg.getInteger(0));
  }



  /**
   * Gets called when a DG_REMOVE_TOURNEY event arrives. Fires the appropriate
   * ChessEvent to any registered listeners.
   */

  protected void processRemoveTourney(int id){
    ChessEvent evt = (ChessEvent)chessEvents.get(new Integer(id));
    if (evt == null) // Ignore DG_REMOVE_TOURNEY for events we didn't get a DG_TOURNEY for.
      return;

    listenerManager.fireChessEventEvent(new ChessEventEvent(this, ChessEventEvent.EVENT_REMOVED, evt));
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
