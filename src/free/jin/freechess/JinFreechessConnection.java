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
import free.freechess.*;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.SwingUtilities;
import free.jin.event.JinListenerManager;
import free.util.EventListenerList;
import free.chess.variants.BothSidesCastlingVariant;
import free.chess.variants.NoCastlingVariant;
import free.chess.variants.fischerrandom.FischerRandom;
import free.chess.variants.suicide.Suicide;


/**
 * An implementation of the JinConnection interface for the freechess.org
 * server.
 */

public class JinFreechessConnection extends FreechessConnection implements JinConnection,
    SeekJinConnection, PGNJinConnection{



  /**
   * Our listener manager.
   */

  private final FreechessJinListenerManager listenerManager = new FreechessJinListenerManager(this);





  /**
   * Creates a new JinFreechessConnection with the specified hostname, port,
   * requested username and password.
   */

  public JinFreechessConnection(String hostname, int port, String username, String password){
    super(hostname, port, username, password);

    setInterface(Jin.getInterfaceName());
    setStyle(12);
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
    java.net.Socket sock = new free.freechess.timeseal.TimesealingSocket(hostname, port); // Comment this to disable timesealing
//    java.net.Socket sock = new java.net.Socket(hostname, port); // Comment this to enable timesealing

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
    sendCommand("set bell 0");
    sendCommand("iset gameinfo 1");
    sendCommand("iset showownseek 1");

    filterLine("Bell off.");
    filterLine("gameinfo set.");
    filterLine("showownseek set.");

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

  protected boolean processPersonalTell(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "tell", username, (titles == null ? "" : titles), message, null));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processSayTell(String username, String titles, int gameNumber, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "say", username, (titles == null ? "" : titles), message, new Integer(gameNumber)));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processPTell(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "ptell", username, (titles == null ? "" : titles), message, null));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processChannelTell(String username, String titles, int channelNumber, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "channel-tell", username, (titles == null ? "" : titles), message, new Integer(channelNumber)));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processKibitz(String username, String titles, int rating, int gameNumber, String message){
    if (titles == null)
      titles = "";
    if (rating != -1)
      titles = titles+"("+rating+")";

    listenerManager.fireChatEvent(new ChatEvent(this, "kibitz", username, titles, message, new Integer(gameNumber)));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processWhisper(String username, String titles, int rating, int gameNumber, String message){
    if (titles == null)
      titles = "";
    if (rating != -1)
      titles = titles+"("+rating+")";

    listenerManager.fireChatEvent(new ChatEvent(this, "whisper", username, titles, message, new Integer(gameNumber)));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processShout(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "shout", username, (titles == null ? "" : titles), message, null));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processIShout(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "ishout", username, (titles == null ? "" : titles), message, null));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processTShout(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "tshout", username, (titles == null ? "" : titles), message, null));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processCShout(String username, String titles, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "cshout", username, (titles == null ? "" : titles), message, null));

    return true;
  }




  /**
   * Fires an appropriate ChatEvent.
   */

  protected boolean processAnnouncement(String username, String message){
    listenerManager.fireChatEvent(new ChatEvent(this, "announcement", username, "", message, null));

    return true;
  }




  /**
   * Returns the wild variant corresponding to the given server wild variant 
   * name/category name, or <code>null</code> if that category is not supported. 
   */

  private static WildVariant getVariant(String categoryName){
    if (categoryName.equalsIgnoreCase("lightning") ||
        categoryName.equalsIgnoreCase("blitz") ||
        categoryName.equalsIgnoreCase("standard"))
      return Chess.getInstance();

    
    if (categoryName.startsWith("wild/")){
      String wildId = categoryName.substring("wild/.".length());
      if (wildId.equals("0") || wildId.equals("1"))
        return new BothSidesCastlingVariant(Chess.INITIAL_POSITION_FEN, categoryName);
      else if (wildId.equals("2") || wildId.equals("3"))
        return new NoCastlingVariant(Chess.INITIAL_POSITION_FEN, categoryName);
      else if (wildId.equals("5") || wildId.equals("8") || wildId.equals("8a"))
        return new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, categoryName);
      else if (wildId.equals("fr"))
        return FischerRandom.getInstance();
    }
    else if (categoryName.equals("suicide"))
      return Suicide.getInstance();
    else if (categoryName.equals("losers"))
      return new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, categoryName);

    // This means it's a fake variant we're using because the server hasn't told us the real one.
    else if (categoryName.equals("fake-variant"))
      return Chess.getInstance();

    return null;
  }




  /**
   * A hashtable where we keep game numbers mapped to GameInfoStruct objects
   * of games that haven't started yet.
   */

  private final Hashtable unstartedGameInfo = new Hashtable(1);



  /**
   * Maps game numbers to Game objects of ongoing games.
   */

  private final Hashtable ongoingGames = new Hashtable(5);



  /**
   * Maps game numbers to last Style12Structs we're received for that game
   * (only for ongoing games).
   */

  private final Hashtable lastStyle12Structs = new Hashtable(5);



  /**
   * A Hashtable mapping Game objects to Vectors of moves which were sent for
   * these games but the server didn't tell us yet whether the move is legal
   * or not.
   */

  private final Hashtable unechoedMoves = new Hashtable(1);




  /**
   * Saves the GameInfoStruct until we receive enough info to fire a
   * GameStartEvent.
   */

  protected boolean processGameInfo(GameInfoStruct data){
    unstartedGameInfo.put(new Integer(data.getGameNumber()), data);
    
    return true;
  }



  /**
   * Fires an appropriate GameEvent depending on the situation.
   */

  protected boolean processStyle12(Style12Struct boardData){
    Integer gameNumber = new Integer(boardData.getGameNumber());
    Game game = (Game)ongoingGames.get(gameNumber);
    GameInfoStruct gameInfo = (GameInfoStruct)unstartedGameInfo.remove(gameNumber);
    Style12Struct oldBoardData = (Style12Struct)lastStyle12Structs.put(gameNumber, boardData);

    if (gameInfo != null) // A new game
      game = startGame(gameInfo, boardData);
    else if (game != null){ // A known game

      // The server does that sometimes, to update clocks perhaps?
      int plyDifference = boardData.getPlayedPlyCount() - oldBoardData.getPlayedPlyCount();

      if (game.isPlayed()){
        if (plyDifference < 0)
          issueTakeback(game, oldBoardData, boardData);
        else if (plyDifference == 0){
//          changePosition(game, oldBoardData, boardData);
          System.out.println("*********** Position changed incompatibly ("+plyDifference+") **********");
          // This happens if you:
          // 1. Issue "refresh".
          // 2. Make an illegal move, because the server will re-send us the board (although we don't need it)
        }
        else if (plyDifference == 1){
          if (boardData.getMoveVerbose() != null)
            makeMove(game, oldBoardData, boardData);
          else
            changePosition(game, oldBoardData, boardData); // This shouldn't happen, but I'll leave it just in case
        }
        else if (plyDifference > 1){
          changePosition(game, oldBoardData, boardData);
          // This happens if you:
          // 1. Issue "forward" with an argument of 2 or bigger.
        }
      }
      else{
        changePosition(game, oldBoardData, boardData); // Since we don't have the move list, this is the best we can do
      }
    }
    else{ // Grr, the server started a game without sending us a GameInfo line.
          // Currently happens if you start examining a game (26.08.2002)

      // We have no choice but to fake the data, since the server simply doesn't send us this information.
      GameInfoStruct fakeGameInfo = new GameInfoStruct(boardData.getGameNumber(), false, "fake-variant", false, false, false,
        boardData.getInitialTime(), boardData.getIncrement(), boardData.getInitialTime(), boardData.getIncrement(), 0,
        -1, ' ', -1, ' ', false, false);

      game = startGame(fakeGameInfo, boardData);
    }

    if (game != null){
      updateClocks(game, boardData);
      if ((oldBoardData != null) && (oldBoardData.isBoardFlipped() != boardData.isBoardFlipped()))
        flipBoard(game, oldBoardData, boardData);
    }

    return true;
  }




  /**
   * Invokes <code>closeGame(int)</code>.
   */

  protected boolean processGameEnd(int gameNumber, String whiteName, String blackName,
      String reason, String result){

    int resultCode;
    if ("1-0".equals(result))
      resultCode = Game.WHITE_WINS;
    else if ("0-1".equals(result))
      resultCode = Game.BLACK_WINS;
    else if ("1/2-1/2".equals(result))
      resultCode = Game.DRAW;
    else
      resultCode = Game.UNKNOWN_RESULT;

    closeGame(gameNumber, resultCode);

    return false;
  }




  /**
   * Invokes <code>closeGame(int)</code>.
   */

  protected boolean processStoppedObserving(int gameNumber){
    closeGame(gameNumber, Game.UNKNOWN_RESULT);

    return false;
  }




  /**
   * Invokes <code>closeGame(int)</code>.
   */

  protected boolean processStoppedExamining(int gameNumber){
    closeGame(gameNumber, Game.UNKNOWN_RESULT);

    return false;
  }




  /**
   * Invokes <code>illegalMoveAttempted</code>.
   */

  protected boolean processIllegalMove(String moveString){
    illegalMoveAttempted(moveString);

    return false;
  }






  /**
   * Called when a new game is starting. Responsible for creating the game on
   * the client side and firing appropriate events. Returns the newly created
   * game.
   */

  private Game startGame(GameInfoStruct gameInfo, Style12Struct boardData){
    String categoryName = gameInfo.getGameCategory();
    WildVariant variant = getVariant(categoryName);
    if (variant == null){
      processLine("This version of Jin does not support the wild variant ("+categoryName+") and is thus unable to display the game.");
      processLine("Please activate the appropriate command to abort this game");
      return null;
    }

    int gameType;
    switch (boardData.getGameType()){
      case Style12Struct.MY_GAME: gameType = Game.MY_GAME; break;
      case Style12Struct.OBSERVED_GAME: gameType = Game.OBSERVED_GAME; break;
      case Style12Struct.ISOLATED_BOARD: gameType = Game.ISOLATED_BOARD; break;
      default:
        throw new IllegalArgumentException("Bad game type value: "+boardData.getGameType());
    }

    Position initPos = new Position(variant);
    initPos.setFEN(boardData.getBoardFEN());
    Player currentPlayer = playerForString(boardData.getCurrentPlayer());
    initPos.setCurrentPlayer(currentPlayer);

    String whiteName = boardData.getWhiteName();
    String blackName = boardData.getBlackName();

    int whiteTime = 1000 * gameInfo.getWhiteTime();
    int blackTime = 1000 * gameInfo.getBlackTime();
    int whiteInc = 1000 * gameInfo.getWhiteInc();
    int blackInc = 1000 * gameInfo.getBlackInc();

    int whiteRating = gameInfo.isWhiteRegistered() ? -1 : gameInfo.getWhiteRating();
    int blackRating = gameInfo.isBlackRegistered() ? -1 : gameInfo.getBlackRating();

    String gameID = String.valueOf(gameInfo.getGameNumber());

    boolean isRated = gameInfo.isGameRated();

    boolean isPlayed = boardData.isPlayedGame();

    String whiteTitles = "";
    String blackTitles = "";

    boolean initiallyFlipped = boardData.isBoardFlipped();

    Player userPlayer = null;
    if ((gameType == Game.MY_GAME) && isPlayed)
      userPlayer = boardData.isMyTurn() ? currentPlayer : currentPlayer.getOpponent();

    Game game = new Game(gameType, initPos, whiteName, blackName, whiteTime, whiteInc,
      blackTime, blackInc, whiteRating, blackRating, gameID, categoryName, isRated, isPlayed,
      whiteTitles, blackTitles, initiallyFlipped, userPlayer);

    ongoingGames.put(new Integer(gameInfo.getGameNumber()), game);

    listenerManager.fireGameEvent(new GameStartEvent(this, game));

    return game;
  }




  /**
   * Gets called when a move is made. Fires an appropriate MoveMadeEvent.
   */

  private void makeMove(Game game, Style12Struct oldBoardData, Style12Struct boardData){
    String moveVerbose = boardData.getMoveVerbose();
    String moveSAN = boardData.getMoveSAN();

    WildVariant variant = game.getVariant();
    Position position = new Position(variant);
    position.setLexigraphic(oldBoardData.getBoardLexigraphic());
    Player currentPlayer = playerForString(oldBoardData.getCurrentPlayer());
    position.setCurrentPlayer(currentPlayer);

    Square fromSquare, toSquare;
    Piece promotionPiece = null;
    if (moveVerbose.equals("o-o")){
      if (variant instanceof BothSidesCastlingVariant){
        if (currentPlayer.isWhite()){
          if (position.getPieceAt("e1") == ChessPiece.WHITE_KING){
            fromSquare = Square.parseSquare("e1");
            toSquare = Square.parseSquare("g1");
          }
          else{
            fromSquare = Square.parseSquare("d1");
            toSquare = Square.parseSquare("b1");
          }
        }
        else{
          if (position.getPieceAt("e8") == ChessPiece.BLACK_KING){
            fromSquare = Square.parseSquare("e8");
            toSquare = Square.parseSquare("g8");
          }
          else{
            fromSquare = Square.parseSquare("d8");
            toSquare = Square.parseSquare("b8");
          }
        }
      }
      else{      
        if (currentPlayer.isWhite()){
          fromSquare = Square.parseSquare("e1");
          toSquare = Square.parseSquare("g1");
        }
        else{
          fromSquare = Square.parseSquare("e8");
          toSquare = Square.parseSquare("g8");
        }
      }
    }
    else if (moveVerbose.equals("o-o-o")){
      if (variant instanceof BothSidesCastlingVariant){
        if (currentPlayer.isWhite()){
          if (position.getPieceAt("e1") == ChessPiece.WHITE_KING){
            fromSquare = Square.parseSquare("e1");
            toSquare = Square.parseSquare("c1");
          }
          else{
            fromSquare = Square.parseSquare("d1");
            toSquare = Square.parseSquare("f1");
          }
        }
        else{
          if (position.getPieceAt("e8") == ChessPiece.BLACK_KING){
            fromSquare = Square.parseSquare("e8");
            toSquare = Square.parseSquare("c8");
          }
          else{
            fromSquare = Square.parseSquare("d8");
            toSquare = Square.parseSquare("f8");
          }
        }
      }
      else{
        if (currentPlayer.isWhite()){
          fromSquare = Square.parseSquare("e1");
          toSquare = Square.parseSquare("c1");
        }
        else{
          fromSquare = Square.parseSquare("e8");
          toSquare = Square.parseSquare("c8");
        }
      }
    }
    else{
      fromSquare = Square.parseSquare(moveVerbose.substring(2, 4));
      toSquare = Square.parseSquare(moveVerbose.substring(5, 7));
      int promotionCharIndex = moveVerbose.indexOf("=")+1;
      if (promotionCharIndex != 0){
        String pieceString = moveVerbose.substring(promotionCharIndex, promotionCharIndex + 1);
        if (currentPlayer.isBlack()) // The server always sends upper case characters, even for black pieces.
          pieceString = pieceString.toLowerCase(); 
        promotionPiece = variant.parsePiece(pieceString);
      }
    }

    Move move = variant.createMove(position, fromSquare, toSquare, promotionPiece, moveSAN);

    listenerManager.fireGameEvent(new MoveMadeEvent(this, game, move, false)); 
      // (isNew == false) because FICS never sends the entire move history

    Vector unechoedGameMoves = (Vector)unechoedMoves.get(game);
    if ((unechoedGameMoves != null) && (unechoedGameMoves.size() != 0)){ // Looks like it's our move.
      Move madeMove = (Move)unechoedGameMoves.elementAt(0);
      if (moveToString(game, move).equals(moveToString(game, madeMove))) // Same move.
        unechoedGameMoves.removeElementAt(0); 
    }
  }




  /**
   * Fires an appropriate ClockAdjustmentEvent.
   */

  private void updateClocks(Game game, Style12Struct boardData){
    int whiteTime = boardData.getWhiteTime();
    int blackTime = boardData.getBlackTime();

    Player currentPlayer = playerForString(boardData.getCurrentPlayer());

    boolean whiteRunning = game.isPlayed() && currentPlayer.isWhite();
    boolean blackRunning = game.isPlayed() && currentPlayer.isBlack();

    listenerManager.fireGameEvent(new ClockAdjustmentEvent(this, game, Player.WHITE_PLAYER, whiteTime, whiteRunning));
    listenerManager.fireGameEvent(new ClockAdjustmentEvent(this, game, Player.BLACK_PLAYER, blackTime, blackRunning));
  }




  /**
   * Fires an appropriate GameEndEvent.
   */

  private void closeGame(int gameNumber, int result){
    Integer gameID = new Integer(gameNumber);
    Game game = (Game)ongoingGames.remove(gameID);
    game.setResult(result);
    if (game != null){
      lastStyle12Structs.remove(gameID);
      listenerManager.fireGameEvent(new GameEndEvent(this, game, result));
    }
  }



  /**
   * Fires an appropriate BoardFlipEvent.
   */

  private void flipBoard(Game game, Style12Struct oldBoardData, Style12Struct newBoardData){
    listenerManager.fireGameEvent(new BoardFlipEvent(this, game, newBoardData.isBoardFlipped()));
  }




  /**
   * Fires an appropriate IllegalMoveEvent.
   */

  private void illegalMoveAttempted(String moveString){
    Integer gameNumber = null; // We must find the played game, since the server doesn't provide its game number.
    Game game = null; 
    Style12Struct boardData = null;
    
    Enumeration gameNumbers = ongoingGames.keys();
    while (gameNumbers.hasMoreElements()){
      gameNumber = (Integer)gameNumbers.nextElement();
      Game nextGame = (Game)ongoingGames.get(gameNumber);
      if (nextGame.getGameType() == Game.MY_GAME){
        game = nextGame;
        boardData = (Style12Struct)lastStyle12Structs.get(gameNumber);
        break;
      }
    }

    if (game == null) // We're not playing a game. Weird, but what can we do?
      return;

    Vector unechoedGameMoves = (Vector)unechoedMoves.get(game);
    if ((unechoedGameMoves == null) || (unechoedGameMoves.size() == 0)) // Not a move we made (probably the user typed it in)
      return;

    Move move = (Move)unechoedGameMoves.elementAt(0);
    if (moveToString(game, move).equals(moveString)){ // Our move
      unechoedGameMoves.removeAllElements();
      listenerManager.fireGameEvent(new IllegalMoveEvent(this, game, move));
    }
  }




  /**
   * Fires an appropriate TakebackEvent.
   */

  private void issueTakeback(Game game, Style12Struct oldBoardData, Style12Struct newBoardData){
    int takebackCount = oldBoardData.getPlayedPlyCount() - newBoardData.getPlayedPlyCount();
    listenerManager.fireGameEvent(new TakebackEvent(this, game, takebackCount));
  }




  /**
   * Fires an appropriate PositionChangedEvent.
   */

  private void changePosition(Game game, Style12Struct oldBoardData, Style12Struct newBoardData){
    Position newPos = game.getInitialPosition();
    newPos.setFEN(newBoardData.getBoardFEN());
    Player currentPlayer = playerForString(newBoardData.getCurrentPlayer());
    newPos.setCurrentPlayer(currentPlayer);

    game.setInitialPosition(newPos);

    listenerManager.fireGameEvent(new PositionChangedEvent(this, game, newPos));
  }





  /**
   * Maps seek IDs to Seek objects currently in the sought list.
   */

  private final Hashtable seeks = new Hashtable();




  /**
   * Returns the SeekJinListenerManager via which you can register and
   * unregister SeekListeners.
   */

  public SeekJinListenerManager getSeekJinListenerManager(){
    return getFreechessJinListenerManager();
  }




  /**
   * Creates an appropriate Seek object and fires a SeekEvent.
   */

  protected boolean processSeekAdded(SeekInfoStruct seekInfo){
    WildVariant variant = getVariant(seekInfo.getMatchType());
    if (variant != null){
      String seekID = String.valueOf(seekInfo.getSeekIndex());
      StringBuffer titlesBuf = new StringBuffer();
      int titles = seekInfo.getSeekerTitles();

      if ((titles & SeekInfoStruct.COMPUTER) != 0)
        titlesBuf.append("(C)");
      if ((titles & SeekInfoStruct.GM) != 0)
        titlesBuf.append("(GM)");
      if ((titles & SeekInfoStruct.IM) != 0)
        titlesBuf.append("(IM)");
      if ((titles & SeekInfoStruct.FM) != 0)
        titlesBuf.append("(FM)");
      if ((titles & SeekInfoStruct.WGM) != 0)
        titlesBuf.append("(WGM)");
      if ((titles & SeekInfoStruct.WIM) != 0)
        titlesBuf.append("(WIM)");
      if ((titles & SeekInfoStruct.WFM) != 0)
        titlesBuf.append("(WFM)");

      boolean isProvisional = (seekInfo.getSeekerProvShow() == 'P');

      boolean isSeekerRated = (seekInfo.getSeekerRating() != 0);

      boolean isRegistered = ((seekInfo.getSeekerTitles() & SeekInfoStruct.UNREGISTERED) == 0);

      boolean isComputer = ((seekInfo.getSeekerTitles() & SeekInfoStruct.COMPUTER) != 0);

      Player color;
      switch (seekInfo.getSeekerColor()){
        case 'W':
          color = Player.WHITE_PLAYER;
          break;
        case 'B':
          color = Player.BLACK_PLAYER;
          break;
        case '?':
          color = null;
          break;
        default:
          throw new IllegalStateException("Bad desired color char: "+seekInfo.getSeekerColor());
      }

      boolean isRatingLimited = ((seekInfo.getOpponentMinRating() > 0) || (seekInfo.getOpponentMaxRating() < 9999));

      Seek seek = new Seek(seekID, seekInfo.getSeekerHandle(), titlesBuf.toString(), seekInfo.getSeekerRating(),
        isProvisional, isRegistered, isSeekerRated, isComputer, variant, seekInfo.getMatchType(),
        seekInfo.getMatchTime()*60*1000, seekInfo.getMatchIncrement()*1000, seekInfo.isMatchRated(), color,
        isRatingLimited, seekInfo.getOpponentMinRating(), seekInfo.getOpponentMaxRating(),
        !seekInfo.isAutomaticAccept(), seekInfo.isFormulaUsed());

      Integer seekIndex = new Integer(seekInfo.getSeekIndex());

      Seek oldSeek = (Seek)seeks.get(seekIndex);
      if (oldSeek != null)
        listenerManager.fireSeekEvent(new SeekEvent(this, SeekEvent.SEEK_REMOVED, oldSeek));

      seeks.put(seekIndex, seek);
      listenerManager.fireSeekEvent(new SeekEvent(this, SeekEvent.SEEK_ADDED, seek));
    }
    
    return true;
  }




  /**
   * Issues the appropriate SeekEvents and removes the seeks.
   */

  protected boolean processSeeksRemoved(int [] removedSeeks){
    for (int i = 0; i < removedSeeks.length; i++){
      Integer seekIndex = new Integer(removedSeeks[i]);
      Seek seek = (Seek)seeks.get(seekIndex);
      if (seek == null) // Shouldn't happen
        continue;

      listenerManager.fireSeekEvent(new SeekEvent(this, SeekEvent.SEEK_REMOVED, seek));

      seeks.remove(seekIndex);
    }
    
    return true;
  }




  /**
   * Issues the appropriate SeeksEvents and removes the seeks.
   */

  protected boolean processSeeksCleared(){
    int seeksCount = seeks.size();
    if (seeksCount != 0){
      Object [] seeksIndices = new Object[seeksCount];

      // Copy all the keys into a temporary array
      Enumeration seekIDsEnum = seeks.keys();
      for (int i = 0; i < seeksCount; i++)
        seeksIndices[i] = seekIDsEnum.nextElement();

      // Remove all the seeks one by one, notifying any interested listeners.
      for (int i = 0; i < seeksCount; i++){
        Object seekIndex = seeksIndices[i];
        Seek seek = (Seek)seeks.get(seekIndex);
        listenerManager.fireSeekEvent(new SeekEvent(this, SeekEvent.SEEK_REMOVED, seek));
        seeks.remove(seekIndex);
      }
    }
    
    return true;
  }




  /**
   * This method is called by our FreechessJinListenerManager when a new
   * SeekListener is added and we already had registered listeners (meaning that
   * iv_seekinfo was already on, so we need to notify the new listeners of all
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
   * Accepts the given seek. Note that the given seek must be an instance generated
   * by this SeekJinConnection and it must be in the current sought list.
   */

  public void acceptSeek(Seek seek){
    if (!seeks.contains(seek))
      throw new IllegalArgumentException("The specified seek is not on the seek list");

    sendCommand("play "+seek.getID());
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
    Object id = game.getID();
    switch (game.getGameType()){
      case Game.MY_GAME:
        if (game.isPlayed())
          sendCommand("resign");
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

  public void makeMove(Game game, Move move) throws IllegalArgumentException{
    Enumeration gamesEnum = ongoingGames.elements();
    boolean ourGame = false;
    while (gamesEnum.hasMoreElements()){
      if ((Game)gamesEnum.nextElement() == game){
        ourGame = true;
        break;
      }
    }

    if (!ourGame)
      throw new IllegalArgumentException("The specified Game object was not created by this JinConnection or the game has ended.");

    sendCommand(moveToString(game, move));

    Vector unechoedGameMoves = (Vector)unechoedMoves.get(game);
    if (unechoedGameMoves == null){
      unechoedGameMoves = new Vector(2);
      unechoedMoves.put(game, unechoedGameMoves);
    }
    unechoedGameMoves.addElement(move);
  }




  /**
   * Converts the given move into a string we can send to the server.
   */

  private static String moveToString(Game game, Move move){
    WildVariant variant = game.getVariant();
    if (move instanceof ChessMove){
      ChessMove cmove = (ChessMove)move;
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
   * Resigns the given game. The given game must be a played game and of type
   * Game.MY_GAME.
   */

  public void resign(Game game){
    checkGameMineAndPlayed(game);

    sendCommand("resign");
  }



  /**
   * Sends a request to draw the given game. The given game must be a played 
   * game and of type Game.MY_GAME.
   */

  public void requestDraw(Game game){
    checkGameMineAndPlayed(game);

    sendCommand("draw");
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
    checkGameMineAndPlayed(game);

    sendCommand("abort");
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
    checkGameMineAndPlayed(game);

    sendCommand("adjourn");
  }



  /**
   * Goes back the given amount of plies in the given game. If the given amount
   * of plies is bigger than the amount of plies since the beginning of the game,
   * goes to the beginning of the game.
   */

  public void goBackward(Game game, int plyCount){
    checkGameMineAndExamined(game);

    sendCommand("backward "+plyCount);
  }




  /**
   * Goes forward the given amount of plies in the given game. If the given amount
   * of plies is bigger than the amount of plies remaining until the end of the
   * game, goes to the end of the game.
   */

  public void goForward(Game game, int plyCount){
    checkGameMineAndExamined(game);

    sendCommand("forward "+plyCount);
  }




  /**
   * Goes to the beginning of the given game.
   */

  public void goToBeginning(Game game){
    checkGameMineAndExamined(game);

    sendCommand("backward 999");
  }



  /**
   * Goes to the end of the given game.
   */

  public void goToEnd(Game game){
    checkGameMineAndExamined(game);

    sendCommand("forward 999");
  }



  /**
   * Throws an IllegalArgumentException if the given Game is not of type 
   * Game.MY_GAME or is not a played game. Otherwise, simply returns.
   */

  private void checkGameMineAndPlayed(Game game){
    if ((game.getGameType() != Game.MY_GAME) || (!game.isPlayed()))
      throw new IllegalArgumentException("The given game must be of type Game.MY_GAME and a played one");
  }




  /**
   * Throws an IllegalArgumentException if the given Game is not of type 
   * Game.MY_GAME or is a played game. Otherwise, simply returns.
   */

  private void checkGameMineAndExamined(Game game){
    if ((game.getGameType() != Game.MY_GAME)||game.isPlayed())
      throw new IllegalArgumentException("The given game must be of type Game.MY_GAME and an examined one");
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