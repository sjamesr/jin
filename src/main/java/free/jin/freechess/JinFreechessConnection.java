/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2002, 2003 Alexander Maryanovsky. All rights reserved.
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.freechess;

import free.chess.Chess;
import free.chess.ChessMove;
import free.chess.ChessPiece;
import free.chess.ChesslikeGenericVariant;
import free.chess.FischerTimeControl;
import free.chess.Move;
import free.chess.OddsTimeControl;
import free.chess.Piece;
import free.chess.Player;
import free.chess.Position;
import free.chess.Square;
import free.chess.TimeControl;
import free.chess.WildVariant;
import free.chess.variants.BothSidesCastlingVariant;
import free.chess.variants.NoCastlingVariant;
import free.chess.variants.atomic.Atomic;
import free.chess.variants.fischerrandom.FischerRandom;
import free.chess.variants.suicide.Suicide;
import free.freechess.DeltaBoardStruct;
import free.freechess.FreechessConnection;
import free.freechess.GameInfoStruct;
import free.freechess.Ivar;
import free.freechess.SeekInfoStruct;
import free.freechess.Style12Struct;
import free.jin.Connection;
import free.jin.Game;
import free.jin.I18n;
import free.jin.Jin;
import free.jin.PGNConnection;
import free.jin.Seek;
import free.jin.SeekConnection;
import free.jin.ServerUser;
import free.jin.UserSeek;
import free.jin.event.BoardFlipEvent;
import free.jin.event.ChatEvent;
import free.jin.event.ClockAdjustmentEvent;
import free.jin.event.GameEndEvent;
import free.jin.event.GameStartEvent;
import free.jin.event.IllegalMoveEvent;
import free.jin.event.ListenerManager;
import free.jin.event.MoveMadeEvent;
import free.jin.event.OfferEvent;
import free.jin.event.PlainTextEvent;
import free.jin.event.PositionChangedEvent;
import free.jin.event.SeekEvent;
import free.jin.event.SeekListenerManager;
import free.jin.event.TakebackEvent;
import free.jin.freechess.event.IvarStateChangeEvent;
import free.util.Pair;
import free.util.TextUtilities;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;

/** An implementation of the JinConnection interface for the freechess.org server. */
public class JinFreechessConnection extends FreechessConnection
    implements Connection, SeekConnection, PGNConnection {

  /** Our listener manager. */
  private final FreechessListenerManager listenerManager = new FreechessListenerManager(this);

  /**
   * Creates a new JinFreechessConnection with the specified hostname, port, requested username and
   * password.
   */
  public JinFreechessConnection(String requestedUsername, String password) {
    super(requestedUsername, password, System.out);

    setInterface(
        Jin.getAppName()
            + " "
            + Jin.getAppVersion()
            + " ("
            + System.getProperty("java.vendor")
            + " "
            + System.getProperty("java.version")
            + ", "
            + System.getProperty("os.name")
            + " "
            + getSafeOSVersion()
            + ")");

    setStyle(12);

    setIvarState(Ivar.GAMEINFO, true);
    setIvarState(Ivar.SHOWOWNSEEK, true);
    setIvarState(Ivar.PENDINFO, true);
    setIvarState(Ivar.MOVECASE, true);
    // setIvarState(Ivar.COMPRESSMOVE, true); Pending DAV's bugfixing spree
    setIvarState(Ivar.LOCK, true);
  }

  /**
   * Returns the OS version after stripping out the patch level from it. We do this to avoid
   * revealing that information to everyone on the server.
   */
  private static String getSafeOSVersion() {
    String osVersion = System.getProperty("os.version");
    int i = osVersion.indexOf(".", osVersion.indexOf(".") + 1);
    if (i != -1) osVersion = osVersion.substring(0, i) + ".x";

    return osVersion;
  }

  /** Returns <code>null</code>, since FICS doesn't even support 8-bit characters. */
  @Override
  public String getTextEncoding() {
    return null;
  }

  /**
   * Returns a Player object corresponding to the specified string. If the string is "W", returns
   * <code>Player.WHITE</code>. If it's "B", returns <code>Player.BLACK</code>. Otherwise, throws an
   * IllegalArgumentException.
   */
  public static Player playerForString(String s) {
    if (s.equals("B")) return Player.BLACK_PLAYER;
    else if (s.equals("W")) return Player.WHITE_PLAYER;
    else throw new IllegalArgumentException("Bad player string: " + s);
  }

  /** Returns our ListenerManager. */
  @Override
  public ListenerManager getListenerManager() {
    return getFreechessListenerManager();
  }

  /** Returns out ListenerManager as a reference to FreechessListenerManager. */
  public FreechessListenerManager getFreechessListenerManager() {
    return listenerManager;
  }

  /** Sends the specified command to the server. */
  @Override
  public void sendCommand(String command) {
    sendCommand(command, false, false, false);
  }

  /**
   * If we're logged in, sends the specified command to the server, otherwise the command is put
   * into a queue and sent on-login.
   */
  public void sendCommandWhenLoggedIn(String command) {
    sendCommand(command, true, false, false);
  }

  /**
   * Fires an "attempting" connection event and invokes {@link
   * free.util.Connection#initiateConnect(String, int)}.
   */
  @Override
  public void initiateConnectAndLogin(String hostname, int port) {
    listenerManager.fireConnectionAttempted(this, hostname, port);

    initiateConnect(hostname, port);
  }

  /** Fires an "established" connection event. */
  @Override
  protected void handleConnected() {
    listenerManager.fireConnectionEstablished(this);

    super.handleConnected();
  }

  /** Fires a "failed" connection event. */
  @Override
  protected void handleConnectingFailed(IOException e) {
    listenerManager.fireConnectingFailed(this, e.getMessage());

    super.handleConnectingFailed(e);
  }

  /** Fires a "login succeeded" connection event and performs other on-login tasks. */
  @Override
  protected void handleLoginSucceeded() {
    super.handleLoginSucceeded();

    sendCommand("set bell 0", false, true, false);
    filterLine("Bell off.");

    listenerManager.fireLoginSucceeded(this);
  }

  /** Fires a "login failed" connection event. */
  @Override
  protected void handleLoginFailed(String reason) {
    listenerManager.fireLoginFailed(this, reason);

    super.handleLoginFailed(reason);
  }

  /** Fires a "connection lost" connection event. */
  @Override
  protected void handleDisconnection(IOException e) {
    listenerManager.fireConnectionLost(this);

    super.handleDisconnection(e);
  }

  /**
   * Overrides {@link free.util.Connection#connectImpl(String, int)} to return a timesealing socket.
   */
  @Override
  protected Socket connectImpl(String hostname, int port) throws IOException {
    Socket result = null;
    try {
      Class tsSocketClass = Class.forName("free.freechess.timeseal.TimesealingSocket");
      Constructor tsSocketConstructor =
          tsSocketClass.getConstructor(new Class[] {String.class, int.class});
      result = (Socket) tsSocketConstructor.newInstance(new Object[] {hostname, new Integer(port)});
    } catch (ClassNotFoundException e) {
    } catch (SecurityException e) {
    } catch (NoSuchMethodException e) {
    } catch (IllegalArgumentException e) {
    } catch (InstantiationException e) {
    } catch (IllegalAccessException e) {
    } catch (InvocationTargetException e) {
      Throwable targetException = e.getTargetException();
      if (targetException instanceof IOException) throw (IOException) targetException;
      else if (targetException instanceof RuntimeException)
        throw (RuntimeException) targetException;
      else if (targetException instanceof Error) throw (Error) targetException;
      else e.printStackTrace(); // Shouldn't happen, I think
    }

    if (result == null) result = new Socket(hostname, port);

    return result;
  }

  /**
   * Notifies any interested PlainTextListener of the received line of otherwise unidentified text.
   */
  @Override
  protected void processLine(String line) {
    listenerManager.firePlainTextEvent(new PlainTextEvent(this, null, line));
  }

  /** Gets called when the server notifies us of a change in the state of some ivar. */
  @Override
  protected boolean processIvarStateChanged(Ivar ivar, boolean state) {
    if (ivar == Ivar.SEEKINFO) seekInfoChanged(state);

    IvarStateChangeEvent evt = new IvarStateChangeEvent(this, ivar, state);

    listenerManager.fireIvarStateChangeEvent(evt);

    return false;
  }

  /** Simply forwards to <code>sendCommand</code>, since FICS doesn't support tagged commands. */
  @Override
  public void sendTaggedCommand(String command, String tag) {
    sendCommand(command);
  }

  /** Sends a personal tell to the specified user. */
  @Override
  public void sendPersonalTell(ServerUser user, String message, String tag) {
    sendCommand("xtell " + user.getName() + "! " + message, true, true, false);
  }

  /** Does nothing, since nothing needs to be done. */
  @Override
  public void joinPersonalChat(ServerUser user) {}

  /** Joins channel 1. */
  @Override
  public void joinHelpForum() {
    sendCommand("+channel 1", true, true, true);
  }

  /** Joins the specified chat forum. */
  @Override
  public void joinChat(String type, Object forum) {
    if ("shout".equals(type)) sendCommand("set shout 1", true, true, true);
    else if ("cshout".equals(type)) sendCommand("set sshout 1", true, true, true);
    else if ("channel-tell".equals(type)) {
      Integer channel = (Integer) forum;
      sendCommand("+channel " + channel.intValue(), true, true, true);
    } else if ("kibitz".equals(type)) {
      Integer game = (Integer) forum;
      sendCommand("observe " + game, true, true, true);
    }
  }

  /** Fires an appropriate ChatEvent. */
  @Override
  protected boolean processPersonalTell(String username, String titles, String message) {
    listenerManager.fireChatEvent(
        new ChatEvent(
            this,
            null,
            "tell",
            ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY,
            userForName(username),
            (titles == null ? "" : titles),
            -1,
            message,
            null));

    return true;
  }

  /** Fires an appropriate ChatEvent. */
  @Override
  protected boolean processSayTell(String username, String titles, int gameNumber, String message) {
    listenerManager.fireChatEvent(
        new ChatEvent(
            this,
            null,
            "say",
            ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY,
            userForName(username),
            (titles == null ? "" : titles),
            -1,
            message,
            new Integer(gameNumber)));

    return true;
  }

  /** Fires an appropriate ChatEvent. */
  @Override
  protected boolean processPTell(String username, String titles, String message) {
    listenerManager.fireChatEvent(
        new ChatEvent(
            this,
            null,
            "ptell",
            ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY,
            userForName(username),
            (titles == null ? "" : titles),
            -1,
            message,
            null));

    return true;
  }

  /** Fires an appropriate ChatEvent. */
  @Override
  protected boolean processChannelTell(
      String username, String titles, int channelNumber, String message) {

    listenerManager.fireChatEvent(
        new ChatEvent(
            this,
            null,
            "channel-tell",
            ChatEvent.ROOM_CHAT_CATEGORY,
            userForName(username),
            (titles == null ? "" : titles),
            -1,
            message,
            new Integer(channelNumber)));

    return true;
  }

  /** Fires an appropriate ChatEvent. */
  @Override
  protected boolean processKibitz(
      String username, String titles, int rating, int gameNumber, String message) {
    if (titles == null) titles = "";

    Object forum;
    InternalGameData gameData = (InternalGameData) ongoingGamesData.get(new Integer(gameNumber));
    if (gameData == null)
      forum = new Integer(gameNumber); // This shouldn't happen, but just in case
    else forum = gameData.game;

    listenerManager.fireChatEvent(
        new ChatEvent(
            this,
            null,
            "kibitz",
            ChatEvent.GAME_CHAT_CATEGORY,
            userForName(username),
            titles,
            rating,
            message,
            forum));

    return true;
  }

  /** Fires an appropriate ChatEvent. */
  @Override
  protected boolean processWhisper(
      String username, String titles, int rating, int gameNumber, String message) {
    if (titles == null) titles = "";

    Object forum;
    InternalGameData gameData = (InternalGameData) ongoingGamesData.get(new Integer(gameNumber));
    if (gameData == null)
      forum = new Integer(gameNumber); // This shouldn't happen, but just in case
    else forum = gameData.game;

    listenerManager.fireChatEvent(
        new ChatEvent(
            this,
            null,
            "whisper",
            ChatEvent.GAME_CHAT_CATEGORY,
            userForName(username),
            titles,
            rating,
            message,
            forum));

    return true;
  }

  /** Regex for matching tourney tell qtells. */
  private static final Pattern TOURNEY_TELL_REGEX =
      Pattern.compile("^(" + USERNAME_REGEX + ")(" + TITLES_REGEX + ")?\\(T(\\d+)\\): (.*)");

  /** Fires an appropriate ChatEvent. */
  @Override
  protected boolean processQTell(String message) {
    ChatEvent evt;
    Matcher matcher = TOURNEY_TELL_REGEX.matcher(message);
    if (matcher.matches()) {
      String sender = matcher.group(1);
      String title = matcher.group(2);
      if (title == null) title = "";
      Integer tourneyIndex = new Integer(matcher.group(3));
      message = matcher.group(4);
      evt =
          new ChatEvent(
              this,
              null,
              "qtell.tourney",
              ChatEvent.TOURNEY_CHAT_CATEGORY,
              userForName(sender),
              title,
              -1,
              message,
              tourneyIndex);
    } else {
      evt =
          new ChatEvent(
              this,
              null,
              "qtell",
              ChatEvent.PERSON_TO_PERSON_CHAT_CATEGORY,
              null,
              null,
              -1,
              message,
              null);
    }

    listenerManager.fireChatEvent(evt);

    return true;
  }

  /** Fires an appropriate ChatEvent. */
  @Override
  protected boolean processShout(String username, String titles, String message) {
    listenerManager.fireChatEvent(
        new ChatEvent(
            this,
            null,
            "shout",
            ChatEvent.ROOM_CHAT_CATEGORY,
            userForName(username),
            (titles == null ? "" : titles),
            -1,
            message,
            null));

    return true;
  }

  /** Fires an appropriate ChatEvent. */
  @Override
  protected boolean processIShout(String username, String titles, String message) {
    listenerManager.fireChatEvent(
        new ChatEvent(
            this,
            null,
            "ishout",
            ChatEvent.ROOM_CHAT_CATEGORY,
            userForName(username),
            (titles == null ? "" : titles),
            -1,
            message,
            null));

    return true;
  }

  /** Fires an appropriate ChatEvent. */
  @Override
  protected boolean processTShout(String username, String titles, String message) {
    listenerManager.fireChatEvent(
        new ChatEvent(
            this,
            null,
            "tshout",
            ChatEvent.TOURNEY_CHAT_CATEGORY,
            userForName(username),
            (titles == null ? "" : titles),
            -1,
            message,
            null));

    return true;
  }

  /** Fires an appropriate ChatEvent. */
  @Override
  protected boolean processCShout(String username, String titles, String message) {
    listenerManager.fireChatEvent(
        new ChatEvent(
            this,
            null,
            "cshout",
            ChatEvent.ROOM_CHAT_CATEGORY,
            userForName(username),
            (titles == null ? "" : titles),
            -1,
            message,
            null));

    return true;
  }

  /** Fires an appropriate ChatEvent. */
  @Override
  protected boolean processAnnouncement(String username, String message) {
    listenerManager.fireChatEvent(
        new ChatEvent(
            this,
            null,
            "announcement",
            ChatEvent.BROADCAST_CHAT_CATEGORY,
            userForName(username),
            "",
            -1,
            message,
            null));

    return true;
  }

  /**
   * Returns the wild variant corresponding to the given server wild variant name/category name, or
   * <code>null</code> if that category is not supported.
   */
  private static WildVariant getVariant(String categoryName) {
    if (categoryName.equalsIgnoreCase("lightning")
        || categoryName.equalsIgnoreCase("blitz")
        || categoryName.equalsIgnoreCase("standard")
        || categoryName.equalsIgnoreCase("untimed")) return Chess.getInstance();

    if (categoryName.startsWith("wild/")) {
      String wildId = categoryName.substring("wild/".length());
      if (wildId.equals("0") || wildId.equals("1"))
        return new BothSidesCastlingVariant(Chess.INITIAL_POSITION_FEN, categoryName);
      else if (wildId.equals("2") || wildId.equals("3"))
        return new NoCastlingVariant(Chess.INITIAL_POSITION_FEN, categoryName);
      else if (wildId.equals("5") || wildId.equals("8") || wildId.equals("8a"))
        return new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, categoryName);
      else if (wildId.equals("fr")) return FischerRandom.getInstance();
    } else if (categoryName.equals("suicide")) return Suicide.getInstance();
    else if (categoryName.equals("losers"))
      return new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, categoryName);
    else if (categoryName.equals("atomic")) return Atomic.getInstance();

    // This means it's a fake variant we're using because the server hasn't told us the real one.
    else if (categoryName.equals("Unknown variant")) return Chess.getInstance();

    return null;
  }

  /**
   * Returns the wild variant name corresponding to the specified wild variant, that can be used for
   * issuing a seek, e.g. "w1" or "fr". Returns null if the specified wild variant is not supported
   * by FICS.
   */
  private String getWildName(WildVariant variant) {
    if (variant == null) throw new IllegalArgumentException("Null variant");

    String variantName = variant.getName();
    if (variantName.startsWith("wild/")) return "w" + variantName.substring("wild/".length());
    else if (variant.equals(Chess.getInstance())) return "";
    else if (variant.equals(FischerRandom.getInstance())) return "wfr";
    else if (variant.equals(Suicide.getInstance())) return "suicide";
    else if (variant.equals(Atomic.getInstance())) return "atomic";
    else if ("losers".equals(variantName)) return "losers";

    return null;
  }

  /** A list of supported wild variants, initialized lazily. */
  private static WildVariant[] wildVariants;

  /** Returns a list of support wild variants. */
  @Override
  public WildVariant[] getSupportedVariants() {
    if (wildVariants == null) {
      wildVariants =
          new WildVariant[] {
            Chess.getInstance(),
            FischerRandom.getInstance(),
            Suicide.getInstance(),
            Atomic.getInstance(),
            new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "losers"),
            new BothSidesCastlingVariant(Chess.INITIAL_POSITION_FEN, "wild/0"),
            new BothSidesCastlingVariant(Chess.INITIAL_POSITION_FEN, "wild/1"),
            new NoCastlingVariant(Chess.INITIAL_POSITION_FEN, "wild/2"),
            new NoCastlingVariant(Chess.INITIAL_POSITION_FEN, "wild/3"),
            new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "wild/5"),
            new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "wild/8"),
            new ChesslikeGenericVariant(Chess.INITIAL_POSITION_FEN, "wild/8a"),
          };
    }

    return wildVariants.clone();
  }

  /**
   * A hashtable where we keep game numbers mapped to GameInfoStruct objects of games that haven't
   * started yet.
   */
  private final Hashtable unstartedGamesData = new Hashtable(1);

  /** Maps game numbers to InternalGameData objects of ongoing games. */
  private final Hashtable ongoingGamesData = new Hashtable(5);

  /**
   * A Hashtable mapping Game objects to Vectors of moves which were sent for these games but the
   * server didn't tell us yet whether the move is legal or not.
   */
  private final Hashtable unechoedMoves = new Hashtable(1);

  /**
   * A list of game numbers of ongoing games which we can't support for some reason (not a supported
   * variant for example).
   */
  private final Vector unsupportedGames = new Vector();

  /**
   * The user's primary played (by the user) game, -1 if unknown. This is only set when the user is
   * playing more than one game.
   */
  private int primaryPlayedGame = -1;

  /**
   * The user's primary observed game, -1 if unknown. This is only set when the user is observing
   * more than one game.
   */
  private int primaryObservedGame = -1;

  /**
   * Returns the game with the specified number. This method (currently) exists solely for the
   * benefit of the arrow/circle script.
   */
  public Game getGame(int gameNumber) throws NoSuchGameException {
    return getGameData(gameNumber).game;
  }

  /**
   * Returns the InternalGameData for the ongoing game with the specified number. Throws a <code>
   * NoSuchGameException</code> if there's no such game.
   */
  private InternalGameData getGameData(int gameNumber) throws NoSuchGameException {
    InternalGameData gameData = (InternalGameData) ongoingGamesData.get(new Integer(gameNumber));
    if (gameData == null) throw new NoSuchGameException();

    return gameData;
  }

  /**
   * Finds the (primary) game played by the user. Throws a <code>NoSuchGameException</code> if
   * there's no such game.
   */
  private InternalGameData findMyGame() throws NoSuchGameException {
    if (primaryPlayedGame != -1) return getGameData(primaryPlayedGame);

    Enumeration gameNumbers = ongoingGamesData.keys();
    while (gameNumbers.hasMoreElements()) {
      Integer gameNumber = (Integer) gameNumbers.nextElement();
      InternalGameData gameData = (InternalGameData) ongoingGamesData.get(gameNumber);
      Game game = gameData.game;
      if (game.getGameType() == Game.MY_GAME) return gameData;
    }

    throw new NoSuchGameException();
  }

  /**
   * Finds the played user's game against the specified opponent. Returns the game number of null if
   * no such game exists.
   */
  private InternalGameData findMyGameAgainst(String playerName) throws NoSuchGameException {
    Enumeration gameNumbers = ongoingGamesData.keys();
    while (gameNumbers.hasMoreElements()) {
      Integer gameNumber = (Integer) gameNumbers.nextElement();
      InternalGameData gameData = (InternalGameData) ongoingGamesData.get(gameNumber);
      Game game = gameData.game;
      Player userPlayer = game.getUserPlayer();
      if (userPlayer == null) // Not our game or not played
      continue;
      Player oppPlayer = userPlayer.getOpponent();
      if ((oppPlayer.isWhite() && game.getWhiteName().equals(playerName))
          || (oppPlayer.isBlack() && game.getBlackName().equals(playerName))) return gameData;
    }

    throw new NoSuchGameException();
  }

  /** Saves the GameInfoStruct until we receive enough info to fire a GameStartEvent. */
  @Override
  protected boolean processGameInfo(GameInfoStruct data) {
    unstartedGamesData.put(new Integer(data.getGameNumber()), data);

    return true;
  }

  /** Fires an appropriate GameEvent depending on the situation. */
  @Override
  protected boolean processStyle12(Style12Struct boardData) {
    Integer gameNumber = new Integer(boardData.getGameNumber());
    InternalGameData gameData = (InternalGameData) ongoingGamesData.get(gameNumber);
    GameInfoStruct unstartedGameInfo = (GameInfoStruct) unstartedGamesData.remove(gameNumber);

    if (unstartedGameInfo != null) // A new game
    gameData = startGame(unstartedGameInfo, boardData);
    else if (gameData != null) { // A known game
      Style12Struct oldBoardData = gameData.boardData;
      int plyDifference = boardData.getPlayedPlyCount() - oldBoardData.getPlayedPlyCount();

      if (plyDifference < 0) tryIssueTakeback(gameData, boardData);
      else if (plyDifference == 0) {
        if (!oldBoardData.getBoardFEN().equals(boardData.getBoardFEN()))
          changePosition(gameData, boardData);

        // This happens if you:
        // 1. Issue "refresh".
        // 2. Make an illegal move, because the server will re-send us the board
        //    (although we don't need it)
        // 3. Issue board setup commands.
        // 4. Use "wname" or "bname" to change the names of the white or black
        //    players.
      } else if (plyDifference == 1) {
        if (boardData.getMoveVerbose() != null) makeMove(gameData, boardData);
        else changePosition(gameData, boardData);
        // This shouldn't happen, but I'll leave it just in case
      } else if (plyDifference > 1) {
        changePosition(gameData, boardData);
        // This happens if you:
        // 1. Issue "forward" with an argument of 2 or bigger.
      }
    } else if (!unsupportedGames.contains(gameNumber)) {
      // Grr, the server started a game without sending us a GameInfo line.
      // Currently happens if you start examining a game (26.08.2002), or
      // doing "refresh <game>" (04.07.2004).

      // We have no choice but to fake the data, since the server simply doesn't
      // send us this information.
      GameInfoStruct fakeGameInfo =
          new GameInfoStruct(
              boardData.getGameNumber(),
              false,
              "Unknown variant",
              false,
              false,
              false,
              boardData.getInitialTime(),
              boardData.getIncrement(),
              boardData.getInitialTime(),
              boardData.getIncrement(),
              0,
              -1,
              ' ',
              -1,
              ' ',
              false,
              false);

      gameData = startGame(fakeGameInfo, boardData);
    }

    if (gameData != null) updateGame(gameData, boardData);

    return true;
  }

  /**
   * Processes a delta-board. Instead of actually handing the delta-board, this method, instead,
   * creates a Style12Struct object and then asks <code>processStyle12</code> to handle it.
   */
  @Override
  protected boolean processDeltaBoard(DeltaBoardStruct data) {
    Integer gameNumber = new Integer(data.getGameNumber());
    InternalGameData gameData = (InternalGameData) ongoingGamesData.get(gameNumber);

    Game game = gameData.game;
    if (game.getVariant() != Chess.getInstance())
      throw new IllegalStateException("delta-boards should only be sent for regular chess");

    Style12Struct lastBoardData = gameData.boardData;
    Vector moveList = gameData.moveList;

    Position pos = game.getInitialPosition();
    for (int i = 0; i < moveList.size(); i++) pos.makeMove((Move) moveList.elementAt(i));

    ChessMove move =
        (ChessMove) (Move.parseWarrenSmith(data.getMoveSmith(), pos, data.getMoveAlgebraic()));

    Square startSquare = move.getStartingSquare();
    ChessPiece movingPiece =
        (ChessPiece) ((startSquare == null) ? null : pos.getPieceAt(startSquare));

    pos.makeMove(move);

    String boardLexigraphic = pos.getLexigraphic();
    String currentPlayer = pos.getCurrentPlayer().isWhite() ? "W" : "B";
    int doublePawnPushFile = move.getDoublePawnPushFile();
    boolean kingMoved = movingPiece.isKing();
    boolean canWhiteCastleKingside =
        lastBoardData.canWhiteCastleKingside()
            && !kingMoved
            && !Square.getInstance(7, 0).equals(startSquare);
    boolean canWhiteCastleQueenside =
        lastBoardData.canBlackCastleQueenside()
            && !kingMoved
            && !Square.getInstance(0, 0).equals(startSquare);
    boolean canBlackCastleKingside =
        lastBoardData.canBlackCastleKingside()
            && !kingMoved
            && !Square.getInstance(7, 7).equals(startSquare);
    boolean canBlackCastleQueenside =
        lastBoardData.canBlackCastleQueenside()
            && !kingMoved
            && !Square.getInstance(0, 7).equals(startSquare);

    boolean isIrreversibleMove =
        movingPiece.isPawn()
            || move.isCapture()
            || (canWhiteCastleKingside != lastBoardData.canWhiteCastleKingside())
            || (canWhiteCastleQueenside != lastBoardData.canWhiteCastleQueenside())
            || (canBlackCastleKingside != lastBoardData.canBlackCastleKingside())
            || (canBlackCastleQueenside != lastBoardData.canBlackCastleQueenside());
    int pliesSinceIrreversible =
        isIrreversibleMove ? 0 : lastBoardData.getPliesSinceIrreversible() + 1;

    String whiteName = lastBoardData.getWhiteName();
    String blackName = lastBoardData.getBlackName();
    int gameType = lastBoardData.getGameType();
    boolean isPlayedGame = lastBoardData.isPlayedGame();
    boolean isMyTurn = pos.getCurrentPlayer() == game.getUserPlayer();
    int initTime = lastBoardData.getInitialTime();
    int inc = lastBoardData.getIncrement();
    int whiteStrength = calcStrength(pos, Player.WHITE_PLAYER);
    int blackStrength = calcStrength(pos, Player.BLACK_PLAYER);
    int whiteTime =
        pos.getCurrentPlayer().isBlack() ? data.getRemainingTime() : lastBoardData.getWhiteTime();
    int blackTime =
        pos.getCurrentPlayer().isWhite() ? data.getRemainingTime() : lastBoardData.getBlackTime();
    int nextMoveNumber =
        lastBoardData.getNextMoveNumber() + (pos.getCurrentPlayer().isWhite() ? 1 : 0);
    String moveVerbose = createVerboseMove(pos, move);
    String moveSAN = data.getMoveAlgebraic();
    int moveTime = data.getTakenTime();
    boolean isBoardFlipped = lastBoardData.isBoardFlipped();
    boolean isClockRunning = true;
    int lag = 0; // The server doesn't currently send us this information

    Style12Struct boardData =
        new Style12Struct(
            boardLexigraphic,
            currentPlayer,
            doublePawnPushFile,
            canWhiteCastleKingside,
            canWhiteCastleQueenside,
            canBlackCastleKingside,
            canBlackCastleQueenside,
            pliesSinceIrreversible,
            gameNumber.intValue(),
            whiteName,
            blackName,
            gameType,
            isPlayedGame,
            isMyTurn,
            initTime,
            inc,
            whiteStrength,
            blackStrength,
            whiteTime,
            blackTime,
            nextMoveNumber,
            moveVerbose,
            moveSAN,
            moveTime,
            isBoardFlipped,
            isClockRunning,
            lag);

    processStyle12(boardData);

    return true;
  }

  /** Calculates the material strength of the specified player in the specified position. */
  private static int calcStrength(Position pos, Player player) {
    int count = 0;
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        ChessPiece piece = (ChessPiece) (pos.getPieceAt(i, j));
        if ((piece != null) && (piece.getPlayer() == player)) {
          if (piece.isPawn()) count += 1;
          else if (piece.isBishop()) count += 3;
          else if (piece.isKnight()) count += 3;
          else if (piece.isRook()) count += 5;
          else if (piece.isQueen()) count += 9;
          else if (piece.isKing()) count += 0;
        }
      }
    }

    return count;
  }

  /**
   * Creates a verbose representation of the specified move in the specified position. The move has
   * already been made in the position.
   */
  private static String createVerboseMove(Position pos, ChessMove move) {
    if (move.isShortCastling()) return "o-o";
    else if (move.isLongCastling()) return "o-o-o";
    else {
      ChessPiece piece = (ChessPiece) pos.getPieceAt(move.getEndingSquare());
      String moveVerbose =
          piece.toShortString() + "/" + move.getStartingSquare() + "-" + move.getEndingSquare();
      if (move.isPromotion()) return moveVerbose + "=" + move.getPromotionTarget().toShortString();
      else return moveVerbose;
    }
  }

  /** Changes the bsetup state of the game. */
  @Override
  protected boolean processBSetupMode(boolean entered) {
    try {
      findMyGame().isBSetup = entered;
    } catch (NoSuchGameException e) {
    }

    return super.processBSetupMode(entered);
  }

  /** A small class for keeping internal data about a game. */
  private static class InternalGameData {

    /** The Game object representing the game. */
    public final Game game;

    /** A list of Moves done in the game. */
    public Vector moveList = new Vector();

    /** The last Style12Struct we got for this game. */
    public Style12Struct boardData = null;

    /** Is this game in bsetup mode? */
    public boolean isBSetup = false;

    /**
     * Maps offer indices to offers. Offers are Pairs where the first element is the <code>Player
     * </code> who made the offer and the 2nd is the offer id. Takeback offers are kept separately.
     */
    public final Hashtable indicesToOffers = new Hashtable();

    /**
     * Maps takeback offer indices to takeback offers. Takeback offers are Pairs where the first
     * element is the <code>Player</code> who made the offer and the 2nd is an <code>Integer</code>
     * specifying the amount of plies offered to take back.
     */
    public final Hashtable indicesToTakebackOffers = new Hashtable();

    /**
     * Works as a set of the offers currently in this game. The elements are Pairs in which the
     * first item is the <code>Player</code> who made the offer and the second one is the offer id.
     * Takeback offers are kept separately.
     */
    private final Hashtable offers = new Hashtable();

    /** The number of plies the white player offerred to takeback. */
    private int whiteTakeback;

    /** The number of plies the black player offerred to takeback. */
    private int blackTakeback;

    /** Creates a new InternalGameData. */
    public InternalGameData(Game game) {
      this.game = game;
    }

    /** Returns the amount of moves made in the game (as far as we counted). */
    public int getMoveCount() {
      return moveList.size();
    }

    /** Adds the specified move to the moves list. */
    public void addMove(Move move) {
      moveList.addElement(move);
    }

    /**
     * Removes the last <code>count</code> moves from the movelist, if possible. Otherwise, throws
     * an <code>IllegalArgumentException</code>.
     */
    public void removeLastMoves(int count) {
      if (count > moveList.size())
        throw new IllegalArgumentException("Can't remove more elements than there are elements");

      int first = moveList.size() - 1;
      int last = moveList.size() - count;
      for (int i = first; i >= last; i--) moveList.removeElementAt(i);
    }

    /** Removes all the moves made in the game. */
    public void clearMoves() {
      moveList.removeAllElements();
    }

    /**
     * Returns true if the specified offer is currently made by the specified player in this game.
     */
    public boolean isOffered(int offerId, Player player) {
      return offers.containsKey(new Pair(player, new Integer(offerId)));
    }

    /**
     * Sets the state of the specified offer in the game. Takeback offers are handled by the
     * setTakebackCount method.
     */
    public void setOffer(int offerId, Player player, boolean isMade) {
      Pair offer = new Pair(player, new Integer(offerId));
      if (isMade) offers.put(offer, offer);
      else offers.remove(offer);
    }

    /** Sets the takeback offer in the game to the specified amount of plies. */
    public void setTakebackOffer(Player player, int plies) {
      if (player.isWhite()) whiteTakeback = plies;
      else blackTakeback = plies;
    }

    /** Returns the amount of plies offered to take back by the specified player. */
    public int getTakebackOffer(Player player) {
      if (player.isWhite()) return whiteTakeback;
      else return blackTakeback;
    }
  }

  /** Changes the primary played game. */
  @Override
  protected boolean processSimulCurrentBoardChanged(int gameNumber, String oppName) {
    primaryPlayedGame = gameNumber;

    return true;
  }

  /** Changes the primary observed game. */
  @Override
  protected boolean processPrimaryGameChanged(int gameNumber) {
    primaryObservedGame = gameNumber;

    return true;
  }

  /** Invokes <code>closeGame(int)</code>. */
  @Override
  protected boolean processGameEnd(
      int gameNumber, String whiteName, String blackName, String reason, String result) {

    int resultCode;
    if ("1-0".equals(result)) resultCode = Game.WHITE_WINS;
    else if ("0-1".equals(result)) resultCode = Game.BLACK_WINS;
    else if ("1/2-1/2".equals(result)) resultCode = Game.DRAW;
    else resultCode = Game.UNKNOWN_RESULT;

    closeGame(gameNumber, resultCode);

    return false;
  }

  /** Invokes <code>closeGame(int)</code>. */
  @Override
  protected boolean processStoppedObserving(int gameNumber) {
    closeGame(gameNumber, Game.UNKNOWN_RESULT);

    return false;
  }

  /** Invokes <code>closeGame(int)</code>. */
  @Override
  protected boolean processStoppedExamining(int gameNumber) {
    closeGame(gameNumber, Game.UNKNOWN_RESULT);

    return false;
  }

  /** Invokes <code>illegalMoveAttempted</code>. */
  @Override
  protected boolean processIllegalMove(String moveString, int reasonCode, String reason) {
    illegalMoveAttempted(moveString, reasonCode, reason);

    return false;
  }

  /**
   * This method informs the user that he tried to use (observe, play etc.) a wild variant not
   * supported by Jin. Please use this method when appropriate instead of sending your own message.
   */
  protected void warnVariantUnsupported(String variantName) {
    Object[] messageFormatArgs = new Object[] {Jin.getAppName(), variantName};
    String message =
        I18n.get(JinFreechessConnection.class)
            .getFormattedString("unsupportedVariantMessage", messageFormatArgs);
    String[] messageLines = message.split("\n");

    int maxLineLength = 0;
    for (int i = 0; i < messageLines.length; i++)
      if (messageLines[i].length() > maxLineLength) maxLineLength = messageLines[i].length();

    String border = TextUtilities.padStart("", '*', maxLineLength + 4);

    processLine(border);
    for (int i = 0; i < messageLines.length; i++)
      processLine("* " + TextUtilities.padEnd(messageLines[i], ' ', maxLineLength) + " *");
    processLine(border);
  }

  /**
   * Returns the <code>TimeControl</code> object to be used for the specified parameters.
   *
   * @param whiteInitial White's initial amount of time, in seconds.
   * @param whiteIncrement White's increment, in seconds.
   * @param blackInitial Black's initial amount of time, in seconds.
   * @param blackIncrement Black's increment, in seconds.
   */
  private static TimeControl getTimeControl(
      int whiteInitial, int whiteIncrement, int blackInitial, int blackIncrement) {
    TimeControl whiteTimeControl =
        new FischerTimeControl(1000 * whiteInitial, 1000 * whiteIncrement);
    TimeControl blackTimeControl =
        new FischerTimeControl(1000 * blackInitial, 1000 * blackIncrement);
    if (whiteTimeControl.equals(blackTimeControl)) return whiteTimeControl;
    else return new OddsTimeControl(whiteTimeControl, blackTimeControl);
  }

  /**
   * Called when a new game is starting. Responsible for creating the game on the client side and
   * firing appropriate events. Returns an InternalGameData instance for the newly created Game.
   */
  private InternalGameData startGame(GameInfoStruct gameInfo, Style12Struct boardData) {
    String categoryName = gameInfo.getGameCategory();
    WildVariant variant = getVariant(categoryName);
    if (variant == null) {
      warnVariantUnsupported(categoryName);
      unsupportedGames.addElement(new Integer(gameInfo.getGameNumber()));
      return null;
    }

    int gameType;
    switch (boardData.getGameType()) {
      case Style12Struct.MY_GAME:
        gameType = Game.MY_GAME;
        break;
      case Style12Struct.OBSERVED_GAME:
        gameType = Game.OBSERVED_GAME;
        break;
      case Style12Struct.ISOLATED_BOARD:
        gameType = Game.ISOLATED_BOARD;
        break;
      default:
        throw new IllegalArgumentException("Bad game type value: " + boardData.getGameType());
    }

    Position initPos = new Position(variant);
    initPos.setFEN(boardData.getBoardFEN());

    String whiteName = boardData.getWhiteName();
    String blackName = boardData.getBlackName();

    TimeControl timeControl =
        getTimeControl(
            gameInfo.getWhiteTime(),
            gameInfo.getWhiteInc(),
            gameInfo.getBlackTime(),
            gameInfo.getBlackInc());

    int whiteRating = gameInfo.isWhiteRegistered() ? -1 : gameInfo.getWhiteRating();
    int blackRating = gameInfo.isBlackRegistered() ? -1 : gameInfo.getBlackRating();

    String gameID = String.valueOf(gameInfo.getGameNumber());

    boolean isRated = gameInfo.isGameRated();

    boolean isPlayed = boardData.isPlayedGame();

    String whiteTitles = "";
    String blackTitles = "";

    boolean initiallyFlipped = boardData.isBoardFlipped();

    Player currentPlayer = playerForString(boardData.getCurrentPlayer());
    Player userPlayer = null;
    if ((gameType == Game.MY_GAME) && isPlayed)
      userPlayer = boardData.isMyTurn() ? currentPlayer : currentPlayer.getOpponent();

    Game game =
        new Game(
            gameType,
            initPos,
            boardData.getPlayedPlyCount(),
            whiteName,
            blackName,
            timeControl,
            whiteRating,
            blackRating,
            gameID,
            categoryName,
            isRated,
            isPlayed,
            whiteTitles,
            blackTitles,
            initiallyFlipped,
            userPlayer);

    InternalGameData gameData = new InternalGameData(game);

    ongoingGamesData.put(new Integer(gameInfo.getGameNumber()), gameData);

    listenerManager.fireGameEvent(new GameStartEvent(this, null, game));

    // The server doesn't send us seek remove lines during games, so we have
    // no choice but to remove *all* seeks during a game. The seeks are restored
    // when a game ends by setting seekinfo to 1 again.
    if (gameType == Game.MY_GAME) clearSeeks();

    return gameData;
  }

  /** Updates any game parameters that differ in the board data from the current game data. */
  private void updateGame(InternalGameData gameData, Style12Struct boardData) {
    Game game = gameData.game;
    Style12Struct oldBoardData = gameData.boardData;

    updateClocks(gameData, boardData); // Update the clocks

    // Flip board
    if ((oldBoardData != null) && (oldBoardData.isBoardFlipped() != boardData.isBoardFlipped()))
      flipBoard(gameData, boardData);

    game.setWhiteName(boardData.getWhiteName()); // Change white name
    game.setBlackName(boardData.getBlackName()); // Change black name
    game.setTimeControl(
        getTimeControl(
            boardData.getInitialTime(),
            boardData.getIncrement(),
            boardData.getInitialTime(),
            boardData.getIncrement()));

    gameData.boardData = boardData;
  }

  /** Gets called when a move is made. Fires an appropriate MoveMadeEvent. */
  private void makeMove(InternalGameData gameData, Style12Struct boardData) {
    Game game = gameData.game;
    Style12Struct oldBoardData = gameData.boardData;

    String moveVerbose = boardData.getMoveVerbose();
    String moveSAN = boardData.getMoveSAN();

    WildVariant variant = game.getVariant();
    Position position = new Position(variant);
    position.setLexigraphic(oldBoardData.getBoardLexigraphic());
    Player currentPlayer = playerForString(oldBoardData.getCurrentPlayer());
    position.setCurrentPlayer(currentPlayer);

    Move move;
    Square fromSquare, toSquare;
    Piece promotionPiece = null;
    if (moveVerbose.equals("o-o")) move = variant.createShortCastling(position);
    else if (moveVerbose.equals("o-o-o")) move = variant.createLongCastling(position);
    else {
      fromSquare = Square.parseSquare(moveVerbose.substring(2, 4));
      toSquare = Square.parseSquare(moveVerbose.substring(5, 7));
      int promotionCharIndex = moveVerbose.indexOf("=") + 1;
      if (promotionCharIndex != 0) {
        String pieceString = moveVerbose.substring(promotionCharIndex, promotionCharIndex + 1);
        if (currentPlayer
            .isBlack()) // The server always sends upper case characters, even for black pieces.
        pieceString = pieceString.toLowerCase();
        promotionPiece = variant.parsePiece(pieceString);
      }

      move = variant.createMove(position, fromSquare, toSquare, promotionPiece, moveSAN);
    }

    listenerManager.fireGameEvent(new MoveMadeEvent(this, null, game, move, true));
    // (isNew == true) because FICS never sends the entire move history

    Vector unechoedGameMoves = (Vector) unechoedMoves.get(game);
    if ((unechoedGameMoves != null) && (unechoedGameMoves.size() != 0)) { // Might be our move.
      Move madeMove = (Move) unechoedGameMoves.elementAt(0);
      if (isSameMove(game, move, madeMove)) unechoedGameMoves.removeElementAt(0);
    }

    gameData.addMove(move);
  }

  /**
   * Returns whether <code>echoedMove</code> (sent to us by the server) is the same move as <code>
   * sentMove</code> (a move we sent to the server).
   */
  private static boolean isSameMove(Game game, Move echoedMove, Move sentMove) {
    try {
      String echoedMoveString = moveToString(game, echoedMove);
      String sentMoveString = moveToString(game, sentMove);
      return echoedMoveString.equals(sentMoveString);
    } catch (IllegalArgumentException e) {
      // An exception shouldn't be thrown for sentMove (since moveToString was
      // already called on it when it was sent to the server). Thus if it is
      // thrown, it's for echoedMove, in which case it's certainly not the
      // same move.
      return false;
    }
  }

  /** Fires an appropriate ClockAdjustmentEvent. */
  private void updateClocks(InternalGameData gameData, Style12Struct boardData) {
    Game game = gameData.game;

    int whiteTime = boardData.getWhiteTime();
    int blackTime = boardData.getBlackTime();

    Player currentPlayer = playerForString(boardData.getCurrentPlayer());

    // Don't make clocks run for an isolated position.
    boolean isIsolatedBoard = game.getGameType() == Game.ISOLATED_BOARD;
    boolean whiteRunning =
        (!isIsolatedBoard) && boardData.isClockRunning() && currentPlayer.isWhite();
    boolean blackRunning =
        (!isIsolatedBoard) && boardData.isClockRunning() && currentPlayer.isBlack();

    listenerManager.fireGameEvent(
        new ClockAdjustmentEvent(this, null, game, Player.WHITE_PLAYER, whiteTime, whiteRunning));
    listenerManager.fireGameEvent(
        new ClockAdjustmentEvent(this, null, game, Player.BLACK_PLAYER, blackTime, blackRunning));
  }

  /** Fires an appropriate GameEndEvent. */
  private void closeGame(int gameNumber, int result) {
    Integer gameID = new Integer(gameNumber);

    if (gameID.intValue() == primaryPlayedGame) primaryPlayedGame = -1;
    else if (gameID.intValue() == primaryObservedGame) primaryObservedGame = -1;

    InternalGameData gameData = (InternalGameData) ongoingGamesData.remove(gameID);
    if (gameData != null) {
      Game game = gameData.game;

      Player actor;
      switch (result) {
        case Game.WHITE_WINS:
          actor = Player.BLACK_PLAYER;
          break;
        case Game.BLACK_WINS:
          actor = Player.WHITE_PLAYER;
          break;
        default:
          actor = null;
          break;
      }

      game.setResult(result, Game.UNKNOWN_REASON, actor);
      listenerManager.fireGameEvent(new GameEndEvent(this, null, game));

      if ((game.getGameType() == Game.MY_GAME) && getIvarState(Ivar.SEEKINFO))
        setIvarState(Ivar.SEEKINFO, true); // Refresh the seeks
    } else unsupportedGames.removeElement(gameID);
  }

  /** Fires an appropriate BoardFlipEvent. */
  private void flipBoard(InternalGameData gameData, Style12Struct newBoardData) {
    listenerManager.fireGameEvent(
        new BoardFlipEvent(this, null, gameData.game, newBoardData.isBoardFlipped()));
  }

  /** Fires an appropriate IllegalMoveEvent. */
  private void illegalMoveAttempted(String moveString, int ficsReasonCode, String reason) {
    try {
      InternalGameData gameData = findMyGame();
      Game game = gameData.game;

      Vector unechoedGameMoves = (Vector) unechoedMoves.get(game);

      // Not a move we made (probably the user typed it in)
      if ((unechoedGameMoves == null) || (unechoedGameMoves.size() == 0)) return;

      int reasonCode;
      switch (ficsReasonCode) {
        case MOVE_REJECTED_ILLEGAL_MOVE:
          reasonCode = IllegalMoveEvent.ILLEGAL_MOVE;
          break;
        case MOVE_REJECTED_NOT_YOUR_TURN:
          reasonCode = IllegalMoveEvent.NOT_YOUR_TURN;
          break;
        default:
          reasonCode = IllegalMoveEvent.OTHER;
      }

      Move move = (Move) unechoedGameMoves.elementAt(0);

      // We have no choice but to allow (moveString == null) because the server
      // doesn't always send us the move string (for example if it's not our turn).
      if ((moveString == null) || moveToString(game, move).equals(moveString)) {
        // Our move, probably

        unechoedGameMoves.removeAllElements();
        listenerManager.fireGameEvent(new IllegalMoveEvent(this, null, game, move, reasonCode));
      }
    } catch (NoSuchGameException e) {
    }
  }

  /**
   * Determines whether it's possible to issue a takeback for the specified game change and if so
   * calls issueTakeback, otherwise calls changePosition.
   */
  private void tryIssueTakeback(InternalGameData gameData, Style12Struct boardData) {
    Style12Struct oldBoardData = gameData.boardData;
    int plyDifference = oldBoardData.getPlayedPlyCount() - boardData.getPlayedPlyCount();

    if ((gameData.getMoveCount() < plyDifference)) // Can't issue takeback
    changePosition(gameData, boardData);
    else if (gameData.isBSetup) changePosition(gameData, boardData);
    else {
      Game game = gameData.game;
      Vector moveList = gameData.moveList;
      // Check whether the positions match, otherwise it could just be someone
      // issuing "bsetup fen ..." after making a few moves which resets the ply
      // count.

      Position oldPos = game.getInitialPosition();
      for (int i = 0; i < moveList.size() - plyDifference; i++) {
        Move move = (Move) moveList.elementAt(i);
        oldPos.makeMove(move);
      }

      Position newPos = game.getInitialPosition();
      newPos.setFEN(boardData.getBoardFEN());

      if (newPos.equals(oldPos)) issueTakeback(gameData, boardData);
      else changePosition(gameData, boardData);
    }
  }

  /** Fires an appropriate TakebackEvent. */
  private void issueTakeback(InternalGameData gameData, Style12Struct newBoardData) {
    Style12Struct oldBoardData = gameData.boardData;
    int takebackCount = oldBoardData.getPlayedPlyCount() - newBoardData.getPlayedPlyCount();

    listenerManager.fireGameEvent(new TakebackEvent(this, null, gameData.game, takebackCount));

    gameData.removeLastMoves(takebackCount);
  }

  /** Fires an appropriate PositionChangedEvent. */
  private void changePosition(InternalGameData gameData, Style12Struct newBoardData) {
    Game game = gameData.game;

    Position newPos = game.getInitialPosition();
    newPos.setFEN(newBoardData.getBoardFEN());

    game.setInitialPosition(newPos);
    game.setPliesSinceStart(newBoardData.getPlayedPlyCount());

    listenerManager.fireGameEvent(new PositionChangedEvent(this, null, game, newPos));

    gameData.clearMoves();

    // We do this because moves in bsetup mode cause position change events, not move events
    if (gameData.isBSetup) {
      Vector unechoedGameMoves = (Vector) unechoedMoves.get(game);
      if ((unechoedGameMoves != null) && (unechoedGameMoves.size() != 0))
        unechoedGameMoves.removeElementAt(0);
    }
  }

  /** Maps seek IDs to Seek objects currently in the sought list. */
  private final Hashtable seeks = new Hashtable();

  /** Returns the SeekListenerManager via which you can register and unregister SeekListeners. */
  @Override
  public SeekListenerManager getSeekListenerManager() {
    return getFreechessListenerManager();
  }

  /** Invoked when seekinfo ivar's state changes. */
  protected void seekInfoChanged(boolean isOn) {
    if (!isOn) clearSeeks();
  }

  /** Creates an appropriate Seek object and fires a SeekEvent. */
  @Override
  protected boolean processSeekAdded(SeekInfoStruct seekInfo) {
    // We may get seeks after setting seekinfo to false because the server
    // already sent them when we sent it the request to set seekInfo to false.
    if (getRequestedIvarState(Ivar.SEEKINFO)) {
      WildVariant variant = getVariant(seekInfo.getMatchType());
      if (variant != null) {
        String seekID = String.valueOf(seekInfo.getSeekIndex());
        StringBuffer titlesBuf = new StringBuffer();
        int titles = seekInfo.getSeekerTitles();

        if ((titles & SeekInfoStruct.COMPUTER) != 0) titlesBuf.append("(C)");
        if ((titles & SeekInfoStruct.GM) != 0) titlesBuf.append("(GM)");
        if ((titles & SeekInfoStruct.IM) != 0) titlesBuf.append("(IM)");
        if ((titles & SeekInfoStruct.FM) != 0) titlesBuf.append("(FM)");
        if ((titles & SeekInfoStruct.WGM) != 0) titlesBuf.append("(WGM)");
        if ((titles & SeekInfoStruct.WIM) != 0) titlesBuf.append("(WIM)");
        if ((titles & SeekInfoStruct.WFM) != 0) titlesBuf.append("(WFM)");

        boolean isProvisional = (seekInfo.getSeekerProvShow() == 'P');

        boolean isSeekerRated = (seekInfo.getSeekerRating() != 0);

        boolean isRegistered = ((seekInfo.getSeekerTitles() & SeekInfoStruct.UNREGISTERED) == 0);

        boolean isComputer = ((seekInfo.getSeekerTitles() & SeekInfoStruct.COMPUTER) != 0);

        Player color;
        switch (seekInfo.getSeekerColor()) {
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
            throw new IllegalStateException("Bad desired color char: " + seekInfo.getSeekerColor());
        }

        boolean isRatingLimited =
            ((seekInfo.getOpponentMinRating() > 0) || (seekInfo.getOpponentMaxRating() < 9999));

        TimeControl timeControl =
            new FischerTimeControl(
                seekInfo.getMatchTime() * 60 * 1000, seekInfo.getMatchIncrement() * 1000);

        Seek seek =
            new Seek(
                seekID,
                userForName(seekInfo.getSeekerHandle()),
                titlesBuf.toString(),
                seekInfo.getSeekerRating(),
                isProvisional,
                isRegistered,
                isSeekerRated,
                isComputer,
                variant,
                seekInfo.getMatchType(),
                timeControl,
                seekInfo.isMatchRated(),
                color,
                isRatingLimited,
                seekInfo.getOpponentMinRating(),
                seekInfo.getOpponentMaxRating(),
                !seekInfo.isAutomaticAccept(),
                seekInfo.isFormulaUsed());

        Integer seekIndex = new Integer(seekInfo.getSeekIndex());

        Seek oldSeek = (Seek) seeks.get(seekIndex);
        if (oldSeek != null)
          listenerManager.fireSeekEvent(new SeekEvent(this, null, SeekEvent.SEEK_REMOVED, oldSeek));

        seeks.put(seekIndex, seek);
        listenerManager.fireSeekEvent(new SeekEvent(this, null, SeekEvent.SEEK_ADDED, seek));
      }
    }

    return true;
  }

  /** Issues the appropriate SeekEvents and removes the seeks. */
  @Override
  protected boolean processSeeksRemoved(int[] removedSeeks) {
    for (int i = 0; i < removedSeeks.length; i++) {
      Integer seekIndex = new Integer(removedSeeks[i]);
      Seek seek = (Seek) seeks.get(seekIndex);
      if (seek == null) // Happens if the seek is one we didn't fire an event for,
      continue; // for example if we don't support the variant.

      listenerManager.fireSeekEvent(new SeekEvent(this, null, SeekEvent.SEEK_REMOVED, seek));

      seeks.remove(seekIndex);
    }

    return true;
  }

  /** Issues the appropriate SeeksEvents and removes the seeks. */
  @Override
  protected boolean processSeeksCleared() {
    clearSeeks();
    return true;
  }

  /** Removes all currently known seeks, notifying any interested listeners. */
  private void clearSeeks() {
    Set seekIndices = seeks.keySet();
    for (Iterator i = seekIndices.iterator(); i.hasNext(); ) {
      Integer seekIndex = (Integer) i.next();
      Seek seek = (Seek) seeks.get(seekIndex);

      i.remove();
      listenerManager.fireSeekEvent(new SeekEvent(this, null, SeekEvent.SEEK_REMOVED, seek));
    }
  }

  /** Returns the current set of seeks. */
  @Override
  public Collection getSeeks() {
    return Collections.unmodifiableCollection(seeks.values());
  }

  /**
   * Accepts the given seek. Note that the given seek must be an instance generated by this
   * SeekJinConnection and it must be in the current sought list.
   */
  @Override
  public void accept(Seek seek) {
    if (!seeks.contains(seek))
      throw new IllegalArgumentException("The specified seek is not on the seek list");

    sendCommand("play " + seek.getID(), true, true, false);
  }

  /** Withdraws the specified seek, issued by the user. */
  @Override
  public void withdraw(Seek seek) {
    if (!seeks.contains(seek))
      throw new IllegalArgumentException("The specified seek is not on the seek list");

    sendCommand("unseek " + seek.getID(), true, true, false);
  }

  /** Withdraws all seeks. */
  @Override
  public void withdrawAllSeeks() {
    sendCommand("unseek", true, true, false);
  }

  /** Issues the specified seek. */
  @Override
  public void issue(UserSeek seek) {
    WildVariant variant = seek.getVariant();
    String wildName = getWildName(variant);
    if (wildName == null) throw new IllegalArgumentException("Unsupported variant: " + variant);

    Player color = seek.getColor();

    String seekCommand =
        "seek "
            + seek.getTime()
            + " "
            + seek.getInc()
            + " "
            + (seek.isRated() ? "rated" : "unrated")
            + " "
            + (color == null ? "" : color.isWhite() ? "white " : "black ")
            + wildName
            + " "
            + (seek.isManualAccept() ? "manual " : "")
            + (seek.isFormula() ? "formula " : "")
            + (seek.getMinRating() == Integer.MIN_VALUE ? "0" : String.valueOf(seek.getMinRating()))
            + "-"
            + (seek.getMaxRating() == Integer.MAX_VALUE
                ? "9999"
                : String.valueOf(seek.getMaxRating()))
            + " ";

    sendCommand(seekCommand, true, true, false);
  }

  /**
   * Maps offer indices to the <code>InternalGameData</code> objects representing the games in which
   * the offer was made.
   */
  private final Hashtable offerIndicesToGameData = new Hashtable();

  /**
   * Override processOffer to always return true, since we don't want the user to ever see these
   * messages.
   */
  @Override
  protected boolean processOffer(
      boolean toUser, String offerType, int offerIndex, String oppName, String offerParams) {

    super.processOffer(toUser, offerType, offerIndex, oppName, offerParams);
    return true;
  }

  /** Overrides the superclass' method only to return true. */
  @Override
  protected boolean processMatchOffered(
      boolean toUser, int offerIndex, String oppName, String matchDetails) {
    super.processMatchOffered(toUser, offerIndex, oppName, matchDetails);

    return true;
  }

  /** Fires the appropriate OfferEvent(s). */
  @Override
  protected boolean processTakebackOffered(
      boolean toUser, int offerIndex, String oppName, int takebackCount) {
    super.processTakebackOffered(toUser, offerIndex, oppName, takebackCount);

    try {
      InternalGameData gameData = findMyGameAgainst(oppName);
      Player userPlayer = gameData.game.getUserPlayer();
      Player player = toUser ? userPlayer.getOpponent() : userPlayer;

      offerIndicesToGameData.put(new Integer(offerIndex), gameData);
      gameData.indicesToTakebackOffers.put(
          new Integer(offerIndex), new Pair(player, new Integer(takebackCount)));

      updateTakebackOffer(gameData, player, takebackCount);
    } catch (NoSuchGameException e) {
    }

    return true;
  }

  /** Fires the appropriate OfferEvent(s). */
  @Override
  protected boolean processDrawOffered(boolean toUser, int offerIndex, String oppName) {
    super.processDrawOffered(toUser, offerIndex, oppName);

    processOffered(toUser, offerIndex, oppName, OfferEvent.DRAW_OFFER);

    return true;
  }

  /** Fires the appropriate OfferEvent(s). */
  @Override
  protected boolean processAbortOffered(boolean toUser, int offerIndex, String oppName) {
    super.processAbortOffered(toUser, offerIndex, oppName);

    processOffered(toUser, offerIndex, oppName, OfferEvent.ABORT_OFFER);

    return true;
  }

  /** Fires the appropriate OfferEvent(s). */
  @Override
  protected boolean processAdjournOffered(boolean toUser, int offerIndex, String oppName) {
    super.processAdjournOffered(toUser, offerIndex, oppName);

    processOffered(toUser, offerIndex, oppName, OfferEvent.ADJOURN_OFFER);

    return true;
  }

  /**
   * Gets called by the various process[offerType]Offered() methods to handle the offers uniformly.
   */
  private void processOffered(boolean toUser, int offerIndex, String oppName, int offerId) {
    try {
      InternalGameData gameData = findMyGameAgainst(oppName);
      Player userPlayer = gameData.game.getUserPlayer();
      Player player = toUser ? userPlayer.getOpponent() : userPlayer;

      offerIndicesToGameData.put(new Integer(offerIndex), gameData);
      gameData.indicesToOffers.put(new Integer(offerIndex), new Pair(player, new Integer(offerId)));

      updateOffers(gameData, offerId, player, true);
    } catch (NoSuchGameException e) {
    }
  }

  /** Fires the appropriate OfferEvent(s). */
  @Override
  protected boolean processOfferRemoved(int offerIndex) {
    super.processOfferRemoved(offerIndex);

    InternalGameData gameData =
        (InternalGameData) offerIndicesToGameData.remove(new Integer(offerIndex));

    if (gameData != null) {
      // Check regular offers
      Pair offer = (Pair) gameData.indicesToOffers.remove(new Integer(offerIndex));
      if (offer != null) {
        Player player = (Player) offer.getFirst();
        int offerId = ((Integer) offer.getSecond()).intValue();
        updateOffers(gameData, offerId, player, false);
      } else {
        // Check takeback offers
        offer = (Pair) gameData.indicesToTakebackOffers.remove(new Integer(offerIndex));
        if (offer != null) {
          Player player = (Player) offer.getFirst();
          updateTakebackOffer(gameData, player, 0);
        }
      }
    }

    return true;
  }

  /** Fires the appropriate OfferEvent(s). */
  @Override
  protected boolean processPlayerCounteredTakebackOffer(
      int gameNum, String playerName, int takebackCount) {
    super.processPlayerCounteredTakebackOffer(gameNum, playerName, takebackCount);

    try {
      InternalGameData gameData = getGameData(gameNum);
      Player player = gameData.game.getPlayerNamed(playerName);

      updateTakebackOffer(gameData, player.getOpponent(), 0);
      updateTakebackOffer(gameData, player, takebackCount);
    } catch (NoSuchGameException e) {
    }

    return false;
  }

  /** Fires the appropriate OfferEvent(s). */
  @Override
  protected boolean processPlayerOffered(int gameNum, String playerName, String offerName) {
    super.processPlayerOffered(gameNum, playerName, offerName);

    try {
      InternalGameData gameData = getGameData(gameNum);
      Player player = gameData.game.getPlayerNamed(playerName);
      int offerId;
      try {
        offerId = offerIdForOfferName(offerName);
        updateOffers(gameData, offerId, player, true);
      } catch (IllegalArgumentException e) {
      }
    } catch (NoSuchGameException e) {
    }

    return false;
  }

  /** Fires the appropriate OfferEvent(s). */
  @Override
  protected boolean processPlayerDeclined(int gameNum, String playerName, String offerName) {
    super.processPlayerDeclined(gameNum, playerName, offerName);

    try {
      InternalGameData gameData = getGameData(gameNum);
      Player player = gameData.game.getPlayerNamed(playerName);
      int offerId;
      try {
        offerId = offerIdForOfferName(offerName);
        updateOffers(gameData, offerId, player.getOpponent(), false);
      } catch (IllegalArgumentException e) {
      }
    } catch (NoSuchGameException e) {
    }

    return false;
  }

  /** Fires the appropriate OfferEvent(s). */
  @Override
  protected boolean processPlayerWithdrew(int gameNum, String playerName, String offerName) {
    super.processPlayerWithdrew(gameNum, playerName, offerName);

    try {
      InternalGameData gameData = getGameData(gameNum);
      Player player = gameData.game.getPlayerNamed(playerName);
      int offerId;
      try {
        offerId = offerIdForOfferName(offerName);
        updateOffers(gameData, offerId, player, false);
      } catch (IllegalArgumentException e) {
      }
    } catch (NoSuchGameException e) {
    }

    return false;
  }

  /** Fires the appropriate OfferEvent(s). */
  @Override
  protected boolean processPlayerOfferedTakeback(
      int gameNum, String playerName, int takebackCount) {
    super.processPlayerOfferedTakeback(gameNum, playerName, takebackCount);

    try {
      InternalGameData gameData = getGameData(gameNum);
      Player player = gameData.game.getPlayerNamed(playerName);

      updateTakebackOffer(gameData, player, takebackCount);
    } catch (NoSuchGameException e) {
    }

    return false;
  }

  /**
   * Returns the offerId (as defined by OfferEvent) corresponding to the specified offer name.
   * Throws an IllegalArgumentException if the offer name is not recognizes.
   */
  private static int offerIdForOfferName(String offerName) throws IllegalArgumentException {
    if ("draw".equals(offerName)) return OfferEvent.DRAW_OFFER;
    else if ("abort".equals(offerName)) return OfferEvent.ABORT_OFFER;
    else if ("adjourn".equals(offerName)) return OfferEvent.ADJOURN_OFFER;
    else if ("takeback".equals(offerName)) return OfferEvent.TAKEBACK_OFFER;
    else throw new IllegalArgumentException("Unknown offer name: " + offerName);
  }

  /** Updates the specified offer, firing any necessary events. */
  private void updateOffers(InternalGameData gameData, int offerId, Player player, boolean on) {
    Game game = gameData.game;

    if (offerId == OfferEvent.TAKEBACK_OFFER) {
      // We're forced to fake this so that an event is fired even if we start observing a game
      // with an existing takeback offer (of which we're not aware).
      if ((!on) && (gameData.getTakebackOffer(player) == 0)) gameData.setTakebackOffer(player, 1);

      updateTakebackOffer(gameData, player.getOpponent(), 0); // Remove any existing offers
      updateTakebackOffer(gameData, player, on ? 1 : 0);
      // 1 as the server doesn't tell us how many
    } else { // if (gameData.isOffered(offerId, player) != on){ this
      // We check this because we might get such an event if we start observing a game with
      // an existing offer.

      gameData.setOffer(offerId, player, on);
      listenerManager.fireGameEvent(new OfferEvent(this, null, game, offerId, on, player));
    }
  }

  /** Updates the takeback offer in the specified game to the specified amount of plies. */
  private void updateTakebackOffer(InternalGameData gameData, Player player, int takebackCount) {
    Game game = gameData.game;

    int oldTakeback = gameData.getTakebackOffer(player);
    if (oldTakeback != 0)
      listenerManager.fireGameEvent(new OfferEvent(this, null, game, false, player, oldTakeback));

    gameData.setTakebackOffer(player, takebackCount);

    if (takebackCount != 0)
      listenerManager.fireGameEvent(new OfferEvent(this, null, game, true, player, takebackCount));
  }

  /** Sends the "exit" command to the server. */
  @Override
  public void exit() {
    sendCommand("quit", true, true, false);
  }

  /** Returns the user with which we are logged in. */
  @Override
  public ServerUser getUser() {
    return userForName(getUsername());
  }

  /** Returns a <code>FreechessUser</code> with the specified name. */
  @Override
  public ServerUser userForName(String name) {
    return FreechessUser.get(name);
  }

  /** Starts a new, empty, examination game. */
  @Override
  public void examineNewGame() {
    sendCommand("examine", true, true, false);
  }

  /** Starts observing the specified player. */
  @Override
  public void observeBoard(ServerUser user) {
    sendCommand("observe " + user.getName(), true, true, false);
  }

  /** Quits the specified game. */
  @Override
  public void quitGame(Game game) {
    Object id = game.getID();
    switch (game.getGameType()) {
      case Game.MY_GAME:
        if (game.isPlayed()) sendCommand("resign", true, true, false);
        else sendCommand("unexamine", true, true, false);
        break;
      case Game.OBSERVED_GAME:
        sendCommand("unobserve " + id, true, true, false);
        break;
      case Game.ISOLATED_BOARD:
        break;
    }
  }

  /** Makes the given move in the given game. */
  @Override
  public void makeMove(Game game, Move move) {
    Enumeration gamesDataEnum = ongoingGamesData.elements();
    boolean ourGame = false;
    while (gamesDataEnum.hasMoreElements()) {
      InternalGameData gameData = (InternalGameData) gamesDataEnum.nextElement();
      if (gameData.game == game) {
        ourGame = true;
        break;
      }
    }

    if (!ourGame)
      throw new IllegalArgumentException(
          "The specified Game object was not created by this JinConnection or the game has ended.");

    sendCommand(moveToString(game, move), true, true, false);

    Vector unechoedGameMoves = (Vector) unechoedMoves.get(game);
    if (unechoedGameMoves == null) {
      unechoedGameMoves = new Vector(2);
      unechoedMoves.put(game, unechoedGameMoves);
    }
    unechoedGameMoves.addElement(move);
  }

  /**
   * Converts the given move into a string we can send to the server. Throws an <code>
   * IllegalArgumentException</code> if the move is not of a type that we know how to send to the
   * server.
   */
  private static String moveToString(Game game, Move move) throws IllegalArgumentException {
    WildVariant variant = game.getVariant();
    if (move instanceof ChessMove) {
      ChessMove cmove = (ChessMove) move;
      if (cmove.isShortCastling()) return "O-O";
      else if (cmove.isLongCastling()) return "O-O-O";

      String s = cmove.getStartingSquare().toString() + cmove.getEndingSquare().toString();
      if (cmove.isPromotion()) return s + "=" + variant.pieceToString(cmove.getPromotionTarget());
      else return s;
    } else throw new IllegalArgumentException("Unsupported Move type: " + move.getClass());
  }

  /** Resigns the given game. The given game must be a played game and of type Game.MY_GAME. */
  @Override
  public void resign(Game game) {
    checkGameMineAndPlayed(game);

    sendCommand("resign", true, true, false);
  }

  /**
   * Sends a request to draw the given game. The given game must be a played game and of type
   * Game.MY_GAME.
   */
  @Override
  public void requestDraw(Game game) {
    checkGameMineAndPlayed(game);

    sendCommand("draw", true, true, false);
  }

  /** Returns <code>true</code>. */
  @Override
  public boolean isAbortSupported() {
    return true;
  }

  /**
   * Sends a request to abort the given game. The given game must be a played game and of type
   * Game.MY_GAME.
   */
  @Override
  public void requestAbort(Game game) {
    checkGameMineAndPlayed(game);

    sendCommand("abort", true, true, false);
  }

  /** Returns <code>true</code>. */
  @Override
  public boolean isAdjournSupported() {
    return true;
  }

  /**
   * Sends a request to adjourn the given game. The given game must be a played game and of type
   * Game.MY_GAME.
   */
  @Override
  public void requestAdjourn(Game game) {
    checkGameMineAndPlayed(game);

    sendCommand("adjourn", true, true, false);
  }

  /** Returns <code>true</code>. */
  @Override
  public boolean isTakebackSupported() {
    return true;
  }

  /** Sends "takeback 1" to the server. */
  @Override
  public void requestTakeback(Game game) {
    checkGameMineAndPlayed(game);

    sendCommand("takeback 1", true, true, false);
  }

  /** Returns <code>true</code>. */
  @Override
  public boolean isMultipleTakebackSupported() {
    return true;
  }

  /** Sends "takeback plyCount" to the server. */
  @Override
  public void requestTakeback(Game game, int plyCount) {
    checkGameMineAndPlayed(game);

    if (plyCount < 1) throw new IllegalArgumentException("Illegal ply count: " + plyCount);

    sendCommand("takeback " + plyCount, true, true, false);
  }

  /**
   * Goes back the given amount of plies in the given game. If the given amount of plies is bigger
   * than the amount of plies since the beginning of the game, goes to the beginning of the game.
   */
  @Override
  public void goBackward(Game game, int plyCount) {
    checkGameMineAndExamined(game);

    if (plyCount < 1) throw new IllegalArgumentException("Illegal ply count: " + plyCount);

    sendCommand("backward " + plyCount, true, true, false);
  }

  /**
   * Goes forward the given amount of plies in the given game. If the given amount of plies is
   * bigger than the amount of plies remaining until the end of the game, goes to the end of the
   * game.
   */
  @Override
  public void goForward(Game game, int plyCount) {
    checkGameMineAndExamined(game);

    if (plyCount < 1) throw new IllegalArgumentException("Illegal ply count: " + plyCount);

    sendCommand("forward " + plyCount, true, true, false);
  }

  /** Goes to the beginning of the given game. */
  @Override
  public void goToBeginning(Game game) {
    checkGameMineAndExamined(game);

    sendCommand("backward 999", true, true, false);
  }

  /** Goes to the end of the given game. */
  @Override
  public void goToEnd(Game game) {
    checkGameMineAndExamined(game);

    sendCommand("forward 999", true, true, false);
  }

  /**
   * Throws an IllegalArgumentException if the given Game is not of type Game.MY_GAME or is not a
   * played game. Otherwise, simply returns.
   */
  private void checkGameMineAndPlayed(Game game) {
    if ((game.getGameType() != Game.MY_GAME) || (!game.isPlayed()))
      throw new IllegalArgumentException(
          "The given game must be of type Game.MY_GAME and a played one");
  }

  /**
   * Throws an IllegalArgumentException if the given Game is not of type Game.MY_GAME or is a played
   * game. Otherwise, simply returns.
   */
  private void checkGameMineAndExamined(Game game) {
    if ((game.getGameType() != Game.MY_GAME) || game.isPlayed())
      throw new IllegalArgumentException(
          "The given game must be of type Game.MY_GAME and an examined one");
  }

  /** Sends the "help" command to the server. */
  @Override
  public void showServerHelp() {
    sendCommand("help", true, true, false);
  }

  /** Sends the specified question string to channel 1. */
  @Override
  public void sendHelpQuestion(String question, String tag) {
    sendCommand(
        "xtell 1 [" + Jin.getAppName() + " " + Jin.getAppVersion() + "] " + question,
        true,
        true,
        false);
  }

  /**
   * Overrides Connection.execRunnable(Runnable) to execute the runnable on the AWT thread using
   * SwingUtilities.invokeLater(Runnable), since this class is meant to be used by Jin, a graphical
   * interface using Swing.
   *
   * @see Connection#execRunnable(Runnable)
   * @see SwingUtilities.invokeLater(Runnable)
   */
  @Override
  public void execRunnable(Runnable runnable) {
    SwingUtilities.invokeLater(runnable);
  }
}
