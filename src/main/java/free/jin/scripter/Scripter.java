/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2003 Alexander Maryanovsky. All rights reserved.
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
package free.jin.scripter;

import bsh.EvalError;
import free.chess.Chess;
import free.chess.ChessMove;
import free.chess.FischerTimeControl;
import free.chess.Move;
import free.chess.OddsTimeControl;
import free.chess.Player;
import free.chess.Position;
import free.chess.Square;
import free.chess.TimeControl;
import free.jin.Connection;
import free.jin.FriendsConnection;
import free.jin.Game;
import free.jin.Preferences;
import free.jin.Seek;
import free.jin.SeekConnection;
import free.jin.event.BoardFlipEvent;
import free.jin.event.ClockAdjustmentEvent;
import free.jin.event.ConnectionListener;
import free.jin.event.FriendsEvent;
import free.jin.event.FriendsListener;
import free.jin.event.FriendsListenerManager;
import free.jin.event.GameEndEvent;
import free.jin.event.GameEvent;
import free.jin.event.GameListener;
import free.jin.event.GameStartEvent;
import free.jin.event.IllegalMoveEvent;
import free.jin.event.JinEvent;
import free.jin.event.ListenerManager;
import free.jin.event.MoveMadeEvent;
import free.jin.event.OfferEvent;
import free.jin.event.PlainTextEvent;
import free.jin.event.PlainTextListener;
import free.jin.event.PositionChangedEvent;
import free.jin.event.SeekEvent;
import free.jin.event.SeekListener;
import free.jin.event.SeekListenerManager;
import free.jin.event.TakebackEvent;
import free.jin.plugin.Plugin;
import free.jin.ui.PreferencesPanel;
import free.util.MemoryFile;
import free.util.Utilities;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/**
 * A plugin allowing to run user specified commands or code in response to various server events.
 */
public class Scripter extends Plugin {

  /**
   * A hashtable mapping event type names to <code>ScriptDispatcher</code> instances supporting
   * those types of events.
   */
  private final Hashtable dispatchers = new Hashtable();

  /** The constructor. duh. */
  public Scripter() {
    registerScriptDispatcher("connection", new ConnectionScriptDispatcher());
    registerScriptDispatcher("plainText", new PlainTextScriptDispatcher());
    registerScriptDispatcher("game", new GameScriptDispatcher());
    registerScriptDispatcher("seek", new SeekScriptDispatcher());
    registerScriptDispatcher("friends", new FriendsScriptDispatcher());
  }

  /**
   * Registers the specified <code>ScriptDispatcher</code> to handle the specified event type. If
   * there is already a <code>ScriptDispatcher</code> handling this event, it is removed and any
   * Scripts registered with it will no longer be called. This method, therefore, should only be
   * called before any scripts are actually registered.
   */
  protected void registerScriptDispatcher(String eventType, ScriptDispatcher dispatcher) {
    dispatchers.put(eventType, dispatcher);
  }

  /**
   * Returns a <code>ScriptDispatcher</code> for the specified event type or <code>null</code> if
   * the specified event type is not supported.
   */
  private ScriptDispatcher getScriptDispatcher(String eventType) {
    ScriptDispatcher dispatcher = (ScriptDispatcher) dispatchers.get(eventType);
    if ((dispatcher == null) || !dispatcher.isSupportedBy(getConn())) return null;

    return dispatcher;
  }

  /** Returns an array of supported event types. */
  public String[] getSupportedEventTypes() {
    Connection conn = getConn();

    Enumeration eventTypesEnum = dispatchers.keys();
    Vector eventTypesVector = new Vector(dispatchers.size());

    while (eventTypesEnum.hasMoreElements()) {
      String eventType = (String) eventTypesEnum.nextElement();
      ScriptDispatcher dispatcher = (ScriptDispatcher) dispatchers.get(eventType);
      if (dispatcher.isSupportedBy(conn)) eventTypesVector.addElement(eventType);
    }

    String[] eventTypesArr = new String[eventTypesVector.size()];
    eventTypesVector.copyInto(eventTypesArr);

    return eventTypesArr;
  }

  /**
   * Returns a list of the subtypes of the specified event type or <code>null</code> if it has no
   * subtypes.
   */
  public String[] getEventSubtypes(String eventType) {
    ScriptDispatcher dispatcher = getScriptDispatcher(eventType);
    if (dispatcher == null)
      throw new IllegalArgumentException(
          "The specified event type (" + eventType + ") is not supported");

    return dispatcher.getEventSubtypes();
  }

  /**
   * Returns a list of the available variables and sample values for the specified event type and a
   * list of event subtypes. Note that some variables may be missing from the list.
   */
  public Object[][] getAvailableVariables(String eventType, String[] eventSubtypes) {
    ScriptDispatcher dispatcher = getScriptDispatcher(eventType);
    if (dispatcher == null)
      throw new IllegalArgumentException(
          "The specified event type (" + eventType + ") is not supported");

    return dispatcher.getAvailableVars(eventSubtypes);
  }

  /** Gets things going :-) */
  @Override
  public void start() {
    loadScripts();
  }

  /** Calls <code>saveScripts</code>. */
  @Override
  public void saveState() {
    saveScripts();
  }

  /**
   * Adds the specified <code>Script</code> to the list of registered scripts.
   *
   * @throws IllegalArgumentException if the specified script's event type is not supported.
   */
  public void addScript(Script script) {
    if (script == null) throw new IllegalArgumentException("Null script specified");

    String eventType = script.getEventType();
    ScriptDispatcher dispatcher = getScriptDispatcher(eventType);
    if (dispatcher == null)
      throw new IllegalArgumentException(
          "" + script + " is of an unsupported/unknown event type (" + eventType + ")");

    dispatcher.addScript(script);
  }

  /** Removes the specified <code>Script</code> from the list of registered scripts. */
  public void removeScript(Script script) {
    String eventType = script.getEventType();
    ScriptDispatcher dispatcher = getScriptDispatcher(eventType);
    if (dispatcher == null)
      throw new IllegalArgumentException(
          "" + script + " is of an unsupported/unknown event type (" + eventType + ")");

    dispatcher.removeScript(script);
  }

  /** Returns an array containing all the currently registered <code>Scripts</code>. */
  public Script[] getScripts() {
    Vector scriptsVector = new Vector();
    String[] eventTypes = getSupportedEventTypes();

    for (int i = 0; i < eventTypes.length; i++) {
      String eventType = eventTypes[i];
      ScriptDispatcher dispatcher = getScriptDispatcher(eventType);
      Script[] scripts = dispatcher.getScripts();
      for (int j = 0; j < scripts.length; j++) scriptsVector.addElement(scripts[j]);
    }

    Script[] scriptsArr = new Script[scriptsVector.size()];
    scriptsVector.copyInto(scriptsArr);

    return scriptsArr;
  }

  /** Returns <code>true</code> to indicate that we have a preferences UI. */
  @Override
  public boolean hasPreferencesUI() {
    return true;
  }

  /** Returns the Scripter's preferences UI panel. */
  @Override
  public PreferencesPanel getPreferencesUI() {
    return new ScripterPreferencesPanel(this);
  }

  /** Loads all the scripts from user files. */
  private void loadScripts() {
    int scriptCount = getPrefs().getInt("scripts.count", 0);

    for (int i = 0; i < scriptCount; i++) {
      MemoryFile scriptFile = getFile("script-" + i);
      if (scriptFile == null) continue;
      Script script = parseScript(scriptFile.getInputStream());

      try {
        if (script != null) addScript(script);
      } catch (IllegalArgumentException e) {
        System.err.println(
            "WARNING: " + e.getMessage() + ". Script " + script + " will not be run");
        continue;
      }
    }
  }

  /** Saves all the scripts into user files. */
  private void saveScripts() {
    Preferences prefs = getPrefs();

    // Delete old scripts first.
    int scriptCount = prefs.getInt("scripts.count", 0);
    for (int i = 0; i < scriptCount; i++) setFile("script-" + i, null);

    Script[] scripts = getScripts();
    prefs.setInt("scripts.count", scripts.length);

    for (int i = 0; i < scripts.length; i++) {
      Script script = scripts[i];
      MemoryFile scriptFile = new MemoryFile();
      try {
        OutputStream out = scriptFile.getOutputStream();
        writeScript(script, out);
        out.close();
        setFile("script-" + i, scriptFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /** Parses and returns a <code>Script</code>. */
  private Script parseScript(InputStream in) {
    try {
      DataInputStream dataIn = new DataInputStream(in);
      Properties props = new Properties();

      // Not using the Properties.load() way because it's not meant
      // for saving code and can probably break it.
      int propsCount = dataIn.readInt();
      for (int i = 0; i < propsCount; i++) {
        String propName = dataIn.readUTF();
        String propValue = dataIn.readUTF();

        props.put(propName, propValue);
      }

      String scriptName = props.getProperty("name");
      String scriptType = props.getProperty("type");
      String eventType = eventTypeBackwardsCompatibility(props.getProperty("event-type"));
      boolean enabled = "true".equals(props.getProperty("enabled"));
      String eventSubtypesCount = props.getProperty("event-subtype.count");
      String[] eventSubtypes = null;
      if (eventSubtypesCount != null) {
        eventSubtypes = new String[Integer.parseInt(eventSubtypesCount)];
        for (int i = 0; i < eventSubtypes.length; i++)
          eventSubtypes[i] =
              eventSubtypeBackwardsCompatibility(
                  eventType, props.getProperty("event-subtype." + i));
      }

      Script script;
      if ("beanshell".equals(scriptType)) {
        String code = props.getProperty("code");
        script = new BeanShellScript(this, scriptName, eventType, eventSubtypes, code);
      } else if ("commands".equals(scriptType)) {
        String condition = props.getProperty("condition");
        int commandCount = Integer.parseInt(props.getProperty("command-count"));
        String[] commands = new String[commandCount];
        for (int i = 0; i < commandCount; i++) commands[i] = props.getProperty("command-" + i);
        script = new CommandScript(this, scriptName, eventType, eventSubtypes, condition, commands);
      } else return null;
      script.setEnabled(enabled);

      return script;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } catch (EvalError e) {
      e.printStackTrace();
      return null;
    }
  }

  /** Maps old event type names to new ones. */
  private Hashtable oldToNewEventTypes = null;

  /**
   * Converts old event type names (which were shown to the user) into the new ones (which are only
   * used as IDs - the actual names are internationalized).
   */
  private String eventTypeBackwardsCompatibility(String eventType) {
    if (oldToNewEventTypes == null) {
      oldToNewEventTypes = new Hashtable();
      oldToNewEventTypes.put("Chat (All types of tells)", "chat");
      oldToNewEventTypes.put("Connection", "connection");
      oldToNewEventTypes.put("Text (Unparsed text)", "plainText");
      oldToNewEventTypes.put("Game", "game");
      oldToNewEventTypes.put("Seek", "seek");
      oldToNewEventTypes.put("Friends", "friends");
      oldToNewEventTypes.put("User Invoked", "userInvoked");
    }

    if (eventType == null) return null;

    String newEventType = (String) oldToNewEventTypes.get(eventType);

    return newEventType == null ? eventType : newEventType;
  }

  /** Maps event types (new ones) to a mapping of old event subtypes to new subtypes. */
  private Hashtable eventTypesToOldToNewEventSubtypes;

  /**
   * Converts old event subtype names (which were shown to the user) into new ones (which are only
   * used as IDs - the actual names are internationalized).
   */
  private String eventSubtypeBackwardsCompatibility(String eventType, String eventSubtype) {
    if (eventTypesToOldToNewEventSubtypes == null) {
      eventTypesToOldToNewEventSubtypes = new Hashtable();

      Hashtable chatOldToNewSubtypes = new Hashtable();
      chatOldToNewSubtypes.put("Personal Tell", "personalTell");
      chatOldToNewSubtypes.put("(BugHouse) Partner Tell", "partnerTell");
      chatOldToNewSubtypes.put("Shout", "shout");
      chatOldToNewSubtypes.put("T-Shout", "tshout");
      chatOldToNewSubtypes.put("C-Shout", "cshout");
      chatOldToNewSubtypes.put("Announcement", "announcement");
      chatOldToNewSubtypes.put("Channel Tell", "channelTell");
      chatOldToNewSubtypes.put("Kibitz", "kibitz");
      chatOldToNewSubtypes.put("Whisper", "whisper");
      chatOldToNewSubtypes.put("QTell", "qtell");
      chatOldToNewSubtypes.put("Serious Shout", "sshout");
      chatOldToNewSubtypes.put("Channel QTell", "channelQTell");
      eventTypesToOldToNewEventSubtypes.put("chat", chatOldToNewSubtypes);

      Hashtable connectionOldToNewSubtypes = new Hashtable();
      connectionOldToNewSubtypes.put("Attempt", "attempt");
      connectionOldToNewSubtypes.put("Connect", "connect");
      connectionOldToNewSubtypes.put("Login", "login");
      connectionOldToNewSubtypes.put("Disconnect", "disconnect");
      eventTypesToOldToNewEventSubtypes.put("connection", connectionOldToNewSubtypes);

      Hashtable gameOldToNewSubtypes = new Hashtable();
      gameOldToNewSubtypes.put("Game Start", "gameStart");
      gameOldToNewSubtypes.put("Move", "move");
      gameOldToNewSubtypes.put("Takeback/Backward", "takebackOrBackward");
      gameOldToNewSubtypes.put("Board Flip", "boardFlip");
      gameOldToNewSubtypes.put("Illegal Move Attempt", "illegalMoveAttempt");
      gameOldToNewSubtypes.put("Clock Update", "clockUpdate");
      gameOldToNewSubtypes.put("Other Position Change", "otherPositionChange");
      gameOldToNewSubtypes.put("Offers", "offers");
      gameOldToNewSubtypes.put("Game End", "gameEnd");
      eventTypesToOldToNewEventSubtypes.put("game", gameOldToNewSubtypes);

      Hashtable seekOldToNewSubtypes = new Hashtable();
      seekOldToNewSubtypes.put("Post", "post");
      seekOldToNewSubtypes.put("Withdraw", "withdraw");
      eventTypesToOldToNewEventSubtypes.put("seek", seekOldToNewSubtypes);

      Hashtable friendsOldToNewSubtypes = new Hashtable();
      friendsOldToNewSubtypes.put("Online", "online");
      friendsOldToNewSubtypes.put("Connected", "connected");
      friendsOldToNewSubtypes.put("Disconnected", "disconnected");
      friendsOldToNewSubtypes.put("Added", "added");
      friendsOldToNewSubtypes.put("Removed", "removed");
      eventTypesToOldToNewEventSubtypes.put("friends", friendsOldToNewSubtypes);
    }

    if ((eventType == null) || (eventSubtype == null)) return null;

    Hashtable oldToNewSubtype = (Hashtable) eventTypesToOldToNewEventSubtypes.get(eventType);
    if (oldToNewSubtype == null) return eventSubtype;

    String newSubtype = (String) oldToNewSubtype.get(eventSubtype);

    return newSubtype == null ? eventSubtype : newSubtype;
  }

  /** Writes the specified <code>Script</code> into the specified <code>OutputStream</code>. */
  private void writeScript(Script script, OutputStream out) throws IOException {
    String scriptType = script.getType();

    Properties props = new Properties();
    props.put("name", script.getName());
    props.put("type", scriptType);
    props.put("event-type", script.getEventType());
    props.put("enabled", script.isEnabled() ? "true" : "false");

    String[] eventSubtypes = script.getEventSubtypes();
    if (eventSubtypes != null) {
      props.put("event-subtype.count", String.valueOf(eventSubtypes.length));
      for (int i = 0; i < eventSubtypes.length; i++)
        props.put("event-subtype." + i, eventSubtypes[i]);
    }

    if ("beanshell".equals(scriptType)) {
      BeanShellScript bshScript = (BeanShellScript) script;
      String code = bshScript.getCode();
      props.put("code", code);
    } else if ("commands".equals(scriptType)) {
      CommandScript cmdScript = (CommandScript) script;
      String condition = cmdScript.getCondition();
      String[] commands = cmdScript.getCommands();

      props.put("condition", condition);
      props.put("command-count", String.valueOf(commands.length));

      for (int i = 0; i < commands.length; i++) props.put("command-" + i, commands[i]);
    }

    DataOutputStream dataOut = new DataOutputStream(out);
    dataOut.writeInt(props.size());

    Enumeration propNames = props.propertyNames();
    while (propNames.hasMoreElements()) {
      String propName = (String) propNames.nextElement();
      String propValue = props.getProperty(propName);
      dataOut.writeUTF(propName);
      dataOut.writeUTF(propValue);
    }
  }

  /** Returns the string "scripter". */
  @Override
  public String getId() {
    return "scripter";
  }

  /**
   * An abstract base class for classes responsible for supporting scripting for a certain event
   * type. It allows registering and unregistering scripts and testing whether the event type is
   * supported by a specified <code>Connection</code> implementation.
   */
  protected abstract class ScriptDispatcher {

    /** The list of user defined scripts we're running when the supported event occurs. */
    private final Vector scripts = new Vector();

    /**
     * The constructor. If we don't do this, jikes declares the constructor with default access for
     * some reason.
     */
    public ScriptDispatcher() {}

    /**
     * Returns a list of event subtypes supported by this ScriptDispatcher. The returned array may
     * be empty if the supported event has no subtypes.
     */
    public final String[] getEventSubtypes() {
      String[] subtypes = getEventSubtypesImpl();
      return subtypes == null ? null : (String[]) (subtypes.clone());
    }

    /**
     * Returns an array holding the names of supported event subtypes or <code>null</code> if there
     * are no subtypes. Implementations may return the actual array (without copying it) as the
     * externally visible method, <code>getEventSubtypes</code> copies the returned array by itself.
     */
    protected abstract String[] getEventSubtypesImpl();

    /**
     * Adds the specified script to the list of scripts that are run when the supported event
     * occurs.
     */
    public void addScript(Script script) {
      if (scripts.size() == 0) registerForEvent(getConn().getListenerManager());

      scripts.addElement(script);
    }

    /**
     * Removes the specified script from the list of scripts that are run when the supported event
     * occurs.
     */
    public void removeScript(Script script) {
      if (!scripts.removeElement(script))
        throw new IllegalArgumentException(
            "The specified script ("
                + script
                + ") has not been previously registered with this ScriptDispatcher ("
                + this
                + ").");

      if (scripts.size() == 0) unregisterForEvent(getConn().getListenerManager());
    }

    /** Returns the scripts registered with this <code>ScriptDispatcher</code>. */
    public Script[] getScripts() {
      Script[] scriptsArr = new Script[scripts.size()];
      scripts.copyInto(scriptsArr);

      return scriptsArr;
    }

    /** Runs all the scripts. */
    protected void runScripts(JinEvent evt, String eventSubtype, Object[][] vars) {
      String[] supportedSubtypes = getEventSubtypesImpl();
      if ((supportedSubtypes != null)
          && ((eventSubtype == null) || !Utilities.contains(supportedSubtypes, eventSubtype))) {
        System.err.println("Unknown event subtype occurred: " + eventSubtype);
        return;
      }

      if (vars == null) vars = new Object[0][];

      int scriptCount = scripts.size();
      for (int i = 0; i < scriptCount; i++) {
        Script script = (Script) scripts.elementAt(i);
        String[] eventSubtypes = script.getEventSubtypes();
        if (script.isEnabled()
            && ((eventSubtype == null) || Utilities.contains(eventSubtypes, eventSubtype))) {
          try {
            script.run(evt, eventSubtype, vars);
          } catch (RuntimeException e) {
            e.printStackTrace();
          }
        }
      }
    }

    /**
     * Returns <code>true</code> if the specified <code>Connection</code> supports the event type
     * this <code>EventTypeSupport</code> is for. Returns <code>false</code> otherwise.
     */
    public abstract boolean isSupportedBy(Connection conn);

    /** Registers for the event with the specified <code>ListenerManager</code>. */
    protected abstract void registerForEvent(ListenerManager listenerManager);

    /** Unregisters for the event with the specified <code>ListenerManager</code>. */
    protected abstract void unregisterForEvent(ListenerManager listenerManager);

    /**
     * Returns a list of the variables provided for scripts by this ScriptDispatcher for the
     * specified event subtypes with sample values of the variables. There is no necessity to
     * provide a complete list, but the more frequently used variables should be included. Variables
     * for which it is impossible or hard to provide sample values should not be included.
     */
    protected abstract Object[][] getAvailableVars(String[] eventSubtypes);
  }

  /** A <code>ScriptDispatcher</code> for <code>ConnectionEvents</code>. */
  private class ConnectionScriptDispatcher extends ScriptDispatcher implements ConnectionListener {

    private final String[] subtypes = new String[] {"attempt", "connect", "login", "disconnect"};

    @Override
    protected String[] getEventSubtypesImpl() {
      return subtypes;
    }

    @Override
    public boolean isSupportedBy(Connection conn) {
      return true;
    }

    @Override
    public void registerForEvent(ListenerManager listenerManager) {
      listenerManager.addConnectionListener(this);
    }

    @Override
    public void unregisterForEvent(ListenerManager listenerManager) {
      listenerManager.removeConnectionListener(this);
    }

    @Override
    public void connectionAttempted(Connection conn, String hostname, int port) {
      runScripts(null, subtypes[0], null);
    }

    @Override
    public void connectionEstablished(Connection conn) {
      runScripts(null, subtypes[1], null);
    }

    @Override
    public void loginSucceeded(Connection conn) {
      runScripts(null, subtypes[2], null);
    }

    @Override
    public void connectionLost(Connection conn) {
      runScripts(null, subtypes[3], null);
    }

    // The rest of ConnectionListener's methods
    @Override
    public void connectingFailed(Connection conn, String reason) {}

    @Override
    public void loginFailed(Connection conn, String reason) {}

    @Override
    protected Object[][] getAvailableVars(String[] eventSubtypes) {
      return null;
    }
  }

  /** A <code>ScriptDispatcher</code> for <code>PlainTextEvents</code>. */
  private class PlainTextScriptDispatcher extends ScriptDispatcher implements PlainTextListener {

    @Override
    protected String[] getEventSubtypesImpl() {
      return null;
    }

    @Override
    public boolean isSupportedBy(Connection conn) {
      return true;
    }

    @Override
    public void registerForEvent(ListenerManager listenerManager) {
      listenerManager.addPlainTextListener(this);
    }

    @Override
    public void unregisterForEvent(ListenerManager listenerManager) {
      listenerManager.removePlainTextListener(this);
    }

    @Override
    public void plainTextReceived(PlainTextEvent evt) {
      runScripts(evt, null, new Object[][] {{"text", evt.getText()}});
    }

    @Override
    protected Object[][] getAvailableVars(String[] eventSubtypes) {
      return new Object[][] {{"text", "hello!"}};
    }
  }

  /** A <code>ScriptDispatcher</code> for <code>GameEvents</code>. */
  private class GameScriptDispatcher extends ScriptDispatcher implements GameListener {

    private final String[] subtypes =
        new String[] {
          "gameStart",
          "move",
          "takebackOrBackward",
          "boardFlip",
          "illegalMoveAttempt",
          "clockUpdate",
          "otherPositionChange",
          "offers",
          "gameEnd"
        };

    @Override
    protected String[] getEventSubtypesImpl() {
      return subtypes;
    }

    @Override
    public boolean isSupportedBy(Connection conn) {
      return true;
    }

    @Override
    public void registerForEvent(ListenerManager listenerManager) {
      listenerManager.addGameListener(this);
    }

    @Override
    public void unregisterForEvent(ListenerManager listenerManager) {
      listenerManager.removeGameListener(this);
    }

    /** Creates the basic set of variables for the specified GameEvent. */
    private Vector createVarsVector(GameEvent evt) {
      Vector vars = new Vector(29);

      Game game = evt.getGame();

      vars.addElement(new Object[] {"game", game});

      int gameType = game.getGameType();
      String gameTypeString;
      switch (gameType) {
        case Game.MY_GAME:
          gameTypeString = "my";
          break;
        case Game.OBSERVED_GAME:
          gameTypeString = "observed";
          break;
        case Game.ISOLATED_BOARD:
          gameTypeString = "isolated";
          break;
        default:
          throw new IllegalStateException("Unknown game type: " + gameType);
      }

      TimeControl timeControl = game.getTimeControl();
      boolean isTimeOdds =
          (timeControl instanceof OddsTimeControl) && ((OddsTimeControl) timeControl).isOdds();

      Integer whiteTime, whiteInc, blackTime, blackInc;
      whiteTime = whiteInc = blackTime = blackInc = null;
      if (game.getTimeControl() instanceof FischerTimeControl) {
        FischerTimeControl tc = (FischerTimeControl) game.getTimeControl();
        whiteTime = new Integer(tc.getInitial() / (1000 * 60));
        whiteInc = new Integer(tc.getIncrement() / 1000);
        blackTime = whiteTime;
        blackInc = whiteInc;
      } else if (timeControl instanceof OddsTimeControl) {
        OddsTimeControl tc = (OddsTimeControl) game.getTimeControl();
        if ((tc.getWhiteTimeControl() instanceof FischerTimeControl)
            && (tc.getBlackTimeControl() instanceof FischerTimeControl)) {
          FischerTimeControl wtc = (FischerTimeControl) tc.getWhiteTimeControl();
          FischerTimeControl btc = (FischerTimeControl) tc.getBlackTimeControl();

          whiteTime = new Integer(wtc.getInitial());
          whiteInc = new Integer(wtc.getIncrement());
          blackTime = new Integer(btc.getInitial());
          blackInc = new Integer(btc.getIncrement());
        }
      }

      vars.addElement(new Object[] {"gameType", gameTypeString});
      vars.addElement(new Object[] {"initialPosition", game.getInitialPosition()});
      vars.addElement(new Object[] {"variant", game.getVariant().getName()});

      Player userPlayer = game.getUserPlayer();
      vars.addElement(new Object[] {"whiteName", game.getWhiteName()});
      vars.addElement(new Object[] {"blackName", game.getBlackName()});
      vars.addElement(new Object[] {"whiteTime", whiteTime});
      vars.addElement(new Object[] {"whiteInc", whiteInc});
      vars.addElement(new Object[] {"blackTime", blackTime});
      vars.addElement(new Object[] {"blackInc", blackInc});
      vars.addElement(new Object[] {"whiteRating", new Integer(game.getWhiteRating())});
      vars.addElement(new Object[] {"blackRating", new Integer(game.getBlackRating())});
      vars.addElement(new Object[] {"whiteTitle", game.getWhiteTitles()});
      vars.addElement(new Object[] {"blackTitle", game.getBlackTitles()});
      vars.addElement(new Object[] {"isGameRated", game.isRated() ? Boolean.TRUE : Boolean.FALSE});
      vars.addElement(new Object[] {"ratingCategory", game.getRatingCategoryString()});
      vars.addElement(new Object[] {"isPlayed", game.isPlayed() ? Boolean.TRUE : Boolean.FALSE});
      vars.addElement(new Object[] {"isTimeOdds", isTimeOdds ? Boolean.TRUE : Boolean.FALSE});

      if (userPlayer != null) {
        if (userPlayer.isWhite()) {
          vars.addElement(new Object[] {"myName", game.getWhiteName()});
          vars.addElement(new Object[] {"oppName", game.getBlackName()});
          vars.addElement(new Object[] {"myTime", whiteTime});
          vars.addElement(new Object[] {"myInc", whiteInc});
          vars.addElement(new Object[] {"oppTime", blackTime});
          vars.addElement(new Object[] {"oppInc", blackInc});
          vars.addElement(new Object[] {"myRating", new Integer(game.getWhiteRating())});
          vars.addElement(new Object[] {"oppRating", new Integer(game.getBlackRating())});
          vars.addElement(new Object[] {"myTitle", game.getWhiteTitles()});
          vars.addElement(new Object[] {"oppTitle", game.getBlackTitles()});
        } else {
          vars.addElement(new Object[] {"oppName", game.getWhiteName()});
          vars.addElement(new Object[] {"myName", game.getBlackName()});
          vars.addElement(new Object[] {"oppTime", whiteTime});
          vars.addElement(new Object[] {"oppInc", whiteInc});
          vars.addElement(new Object[] {"myTime", blackTime});
          vars.addElement(new Object[] {"myInc", blackInc});
          vars.addElement(new Object[] {"oppRating", new Integer(game.getWhiteRating())});
          vars.addElement(new Object[] {"myRating", new Integer(game.getBlackRating())});
          vars.addElement(new Object[] {"oppTitle", game.getWhiteTitles()});
          vars.addElement(new Object[] {"myTitle", game.getBlackTitles()});
        }

        vars.addElement(new Object[] {"userPlayer", game.getUserPlayer().toString().toLowerCase()});
      }

      return vars;
    }

    @Override
    public void gameStarted(GameStartEvent evt) {
      Vector varsVector = createVarsVector(evt);
      Object[][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);

      runScripts(evt, subtypes[0], vars);
    }

    @Override
    public void moveMade(MoveMadeEvent evt) {
      Vector varsVector = createVarsVector(evt);
      varsVector.addElement(new Object[] {"move", evt.getMove()});
      //      varsVector.addElement(new Object[]{"isNewMove", evt.isNew() ? Boolean.TRUE :
      // Boolean.FALSE});
      // Since I'm not sure myself whether isNewMove does what it was intended to,
      // let's not confuse the user...

      Object[][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);

      runScripts(evt, subtypes[1], vars);
    }

    @Override
    public void positionChanged(PositionChangedEvent evt) {
      Vector varsVector = createVarsVector(evt);
      varsVector.addElement(new Object[] {"newPosition", evt.getPosition()});

      Object[][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);

      runScripts(evt, subtypes[6], vars);
    }

    @Override
    public void takebackOccurred(TakebackEvent evt) {
      Vector varsVector = createVarsVector(evt);
      varsVector.addElement(new Object[] {"takebackCount", new Integer(evt.getTakebackCount())});

      Object[][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);

      runScripts(evt, subtypes[2], vars);
    }

    @Override
    public void illegalMoveAttempted(IllegalMoveEvent evt) {
      Vector varsVector = createVarsVector(evt);
      varsVector.addElement(new Object[] {"illegalMove", evt.getMove()});

      Object[][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);

      runScripts(evt, subtypes[4], vars);
    }

    @Override
    public void clockAdjusted(ClockAdjustmentEvent evt) {
      Vector varsVector = createVarsVector(evt);
      varsVector.addElement(new Object[] {"player", evt.getPlayer().toString().toLowerCase()});
      varsVector.addElement(new Object[] {"time", new Integer(evt.getTime())});
      varsVector.addElement(
          new Object[] {"isClockRunning", evt.isClockRunning() ? Boolean.TRUE : Boolean.FALSE});

      Object[][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);

      runScripts(evt, subtypes[5], vars);
    }

    @Override
    public void boardFlipped(BoardFlipEvent evt) {
      Vector varsVector = createVarsVector(evt);
      varsVector.addElement(
          new Object[] {"isFlipped", evt.isFlipped() ? Boolean.TRUE : Boolean.FALSE});

      Object[][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);

      runScripts(evt, subtypes[3], vars);
    }

    @Override
    public void offerUpdated(OfferEvent evt) {
      String offerType;
      switch (evt.getOfferId()) {
        case OfferEvent.DRAW_OFFER:
          offerType = "draw";
          break;
        case OfferEvent.ADJOURN_OFFER:
          offerType = "adjourn";
          break;
        case OfferEvent.ABORT_OFFER:
          offerType = "abort";
          break;
        case OfferEvent.TAKEBACK_OFFER:
          offerType = "takeback";
          break;
        default:
          return;
      }

      Vector varsVector = createVarsVector(evt);
      varsVector.addElement(new Object[] {"offerType", offerType});
      varsVector.addElement(
          new Object[] {"isOffered", evt.isOffered() ? Boolean.TRUE : Boolean.FALSE});
      varsVector.addElement(new Object[] {"player", evt.getPlayer().toString().toLowerCase()});
      if (evt.getOfferId() == OfferEvent.TAKEBACK_OFFER)
        varsVector.addElement(new Object[] {"takebackCount", new Integer(evt.getTakebackCount())});

      Object[][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);

      runScripts(evt, subtypes[7], vars);
    }

    @Override
    public void gameEnded(GameEndEvent evt) {
      Vector varsVector = createVarsVector(evt);

      int gameResult = evt.getGame().getResultCode();
      Player userPlayer = evt.getGame().getUserPlayer();
      String gameResultString;
      if (userPlayer == null) {
        switch (gameResult) {
          case Game.WHITE_WINS:
            gameResultString = "1-0";
            break;
          case Game.BLACK_WINS:
            gameResultString = "0-1";
            break;
          case Game.DRAW:
            gameResultString = "1/2-1/2";
            break;
          case Game.UNKNOWN_RESULT:
            gameResultString = "unknown";
            break;
          case Game.GAME_IN_PROGRESS:
          default:
            throw new IllegalStateException("Unknown/bad game result value: " + gameResult);
        }
      } else if (userPlayer.isWhite()) {
        switch (gameResult) {
          case Game.WHITE_WINS:
            gameResultString = "win";
            break;
          case Game.BLACK_WINS:
            gameResultString = "loss";
            break;
          case Game.DRAW:
            gameResultString = "draw";
            break;
          case Game.UNKNOWN_RESULT:
            gameResultString = "unknown";
            break;
          case Game.GAME_IN_PROGRESS:
          default:
            throw new IllegalStateException("Unknown game result value: " + gameResult);
        }
      } else { // isBlack()
        switch (gameResult) {
          case Game.WHITE_WINS:
            gameResultString = "loss";
            break;
          case Game.BLACK_WINS:
            gameResultString = "win";
            break;
          case Game.DRAW:
            gameResultString = "draw";
            break;
          case Game.UNKNOWN_RESULT:
            gameResultString = "unknown";
            break;
          case Game.GAME_IN_PROGRESS:
          default:
            throw new IllegalStateException("Unknown game result value: " + gameResult);
        }
      }

      varsVector.addElement(new Object[] {"gameResult", gameResultString});

      Object[][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);

      runScripts(evt, subtypes[8], vars);
    }

    @Override
    protected Object[][] getAvailableVars(String[] eventSubtypes) {
      Vector varsVector = new Vector(29);
      Game game =
          new Game(
              Game.MY_GAME,
              new Position(),
              0,
              "AlexTheGreat",
              "Kasparov",
              new FischerTimeControl(5 * 60 * 1000, 2000),
              1800,
              2852,
              "blah",
              "Blitz",
              true,
              true,
              "C",
              "GM",
              false,
              Player.WHITE_PLAYER);

      int gameType = game.getGameType();
      String gameTypeString;
      switch (gameType) {
        case Game.MY_GAME:
          gameTypeString = "my";
          break;
        case Game.OBSERVED_GAME:
          gameTypeString = "observed";
          break;
        case Game.ISOLATED_BOARD:
          gameTypeString = "isolated";
          break;
        default:
          throw new IllegalStateException("Unknown game type: " + gameType);
      }

      TimeControl timeControl = game.getTimeControl();
      boolean isTimeOdds =
          (timeControl instanceof OddsTimeControl) && ((OddsTimeControl) timeControl).isOdds();

      Integer whiteTime, whiteInc, blackTime, blackInc;
      whiteTime = whiteInc = blackTime = blackInc = null;
      if (game.getTimeControl() instanceof FischerTimeControl) {
        FischerTimeControl tc = (FischerTimeControl) game.getTimeControl();
        whiteTime = new Integer(tc.getInitial() / (1000 * 60));
        whiteInc = new Integer(tc.getIncrement() / 1000);
        blackTime = whiteTime;
        blackInc = whiteInc;
      } else if (timeControl instanceof OddsTimeControl) {
        OddsTimeControl tc = (OddsTimeControl) game.getTimeControl();
        if ((tc.getWhiteTimeControl() instanceof FischerTimeControl)
            && (tc.getBlackTimeControl() instanceof FischerTimeControl)) {
          FischerTimeControl wtc = (FischerTimeControl) tc.getWhiteTimeControl();
          FischerTimeControl btc = (FischerTimeControl) tc.getBlackTimeControl();

          whiteTime = new Integer(wtc.getInitial());
          whiteInc = new Integer(wtc.getIncrement());
          blackTime = new Integer(btc.getInitial());
          blackInc = new Integer(btc.getIncrement());
        }
      }

      varsVector.addElement(new Object[] {"gameType", gameTypeString});
      varsVector.addElement(new Object[] {"initialPosition", game.getInitialPosition()});
      varsVector.addElement(new Object[] {"variant", game.getVariant().getName()});

      Player userPlayer = game.getUserPlayer();
      varsVector.addElement(new Object[] {"whiteName", game.getWhiteName()});
      varsVector.addElement(new Object[] {"blackName", game.getBlackName()});
      varsVector.addElement(new Object[] {"whiteTime", whiteTime});
      varsVector.addElement(new Object[] {"whiteInc", whiteInc});
      varsVector.addElement(new Object[] {"blackTime", blackTime});
      varsVector.addElement(new Object[] {"blackInc", blackInc});
      varsVector.addElement(new Object[] {"whiteRating", new Integer(game.getWhiteRating())});
      varsVector.addElement(new Object[] {"blackRating", new Integer(game.getBlackRating())});
      varsVector.addElement(new Object[] {"whiteTitle", game.getWhiteTitles()});
      varsVector.addElement(new Object[] {"blackTitle", game.getBlackTitles()});
      varsVector.addElement(
          new Object[] {"isGameRated", game.isRated() ? Boolean.TRUE : Boolean.FALSE});
      varsVector.addElement(new Object[] {"ratingCategory", game.getRatingCategoryString()});
      varsVector.addElement(
          new Object[] {"isPlayed", game.isPlayed() ? Boolean.TRUE : Boolean.FALSE});
      varsVector.addElement(new Object[] {"isTimeOdds", isTimeOdds ? Boolean.TRUE : Boolean.FALSE});

      if (userPlayer != null) {
        if (userPlayer.isWhite()) {
          varsVector.addElement(new Object[] {"myName", game.getWhiteName()});
          varsVector.addElement(new Object[] {"oppName", game.getBlackName()});
          varsVector.addElement(new Object[] {"myTime", whiteTime});
          varsVector.addElement(new Object[] {"myInc", whiteInc});
          varsVector.addElement(new Object[] {"oppTime", blackTime});
          varsVector.addElement(new Object[] {"oppInc", blackInc});
          varsVector.addElement(new Object[] {"myRating", new Integer(game.getWhiteRating())});
          varsVector.addElement(new Object[] {"oppRating", new Integer(game.getBlackRating())});
          varsVector.addElement(new Object[] {"myTitle", game.getWhiteTitles()});
          varsVector.addElement(new Object[] {"oppTitle", game.getBlackTitles()});
        } else {
          varsVector.addElement(new Object[] {"oppName", game.getWhiteName()});
          varsVector.addElement(new Object[] {"myName", game.getBlackName()});
          varsVector.addElement(new Object[] {"oppTime", whiteTime});
          varsVector.addElement(new Object[] {"oppInc", whiteInc});
          varsVector.addElement(new Object[] {"myTime", blackTime});
          varsVector.addElement(new Object[] {"myInc", blackInc});
          varsVector.addElement(new Object[] {"oppRating", new Integer(game.getWhiteRating())});
          varsVector.addElement(new Object[] {"myRating", new Integer(game.getBlackRating())});
          varsVector.addElement(new Object[] {"oppTitle", game.getWhiteTitles()});
          varsVector.addElement(new Object[] {"myTitle", game.getBlackTitles()});
        }

        varsVector.addElement(
            new Object[] {"userPlayer", game.getUserPlayer().toString().toLowerCase()});
      }

      Move move =
          new ChessMove(
              Square.parseSquare("e2"),
              Square.parseSquare("e4"),
              Player.WHITE_PLAYER,
              false,
              false,
              false,
              null,
              4,
              null,
              "e4");

      if (Utilities.contains(eventSubtypes, subtypes[1])) {
        varsVector.addElement(new Object[] {"move", move});
      }

      if (Utilities.contains(eventSubtypes, subtypes[6]))
        varsVector.addElement(new Object[] {"newPosition", new Position()});

      if (Utilities.contains(eventSubtypes, subtypes[2]))
        varsVector.addElement(new Object[] {"takebackCount", new Integer(3)});

      if (Utilities.contains(eventSubtypes, subtypes[4]))
        varsVector.addElement(new Object[] {"illegalMove", move});

      if (Utilities.contains(eventSubtypes, subtypes[5])) {
        varsVector.addElement(
            new Object[] {"player", Player.WHITE_PLAYER.toString().toLowerCase()});
        varsVector.addElement(new Object[] {"time", new Integer(4 * 60 * 1000)});
        varsVector.addElement(new Object[] {"isClockRunning", Boolean.TRUE});
      }

      if (Utilities.contains(eventSubtypes, subtypes[3]))
        varsVector.addElement(new Object[] {"isFlipped", Boolean.TRUE});

      if (Utilities.contains(eventSubtypes, subtypes[7])) {
        varsVector.addElement(new Object[] {"offerType", "draw"});
        varsVector.addElement(new Object[] {"isMade", Boolean.TRUE});
        varsVector.addElement(
            new Object[] {"player", Player.WHITE_PLAYER.toString().toLowerCase()});
      }

      if (Utilities.contains(eventSubtypes, subtypes[8]))
        varsVector.addElement(new Object[] {"gameResult", "win"});

      Object[][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);
      return vars;
    }
  }

  /** A <code>ScriptDispatcher</code> for <code>SeekEvents</code>. */
  private class SeekScriptDispatcher extends ScriptDispatcher implements SeekListener {

    private final String[] subtypes = new String[] {"post", "withdraw"};

    @Override
    protected String[] getEventSubtypesImpl() {
      return subtypes;
    }

    @Override
    public boolean isSupportedBy(Connection conn) {
      return (conn instanceof SeekConnection);
    }

    @Override
    public void registerForEvent(ListenerManager listenerManager) {
      ((SeekListenerManager) listenerManager).addSeekListener(this);
    }

    @Override
    public void unregisterForEvent(ListenerManager listenerManager) {
      ((SeekListenerManager) listenerManager).removeSeekListener(this);
    }

    /** Creates the basic set of variables for the specified SeekEvent. */
    private Vector createVarsVector(SeekEvent evt) {
      Vector vars = new Vector(15);

      Seek seek = evt.getSeek();

      vars.addElement(new Object[] {"seek", seek});
      vars.addElement(new Object[] {"name", seek.getSeekerName()});
      vars.addElement(new Object[] {"title", seek.getSeekerTitle()});
      vars.addElement(new Object[] {"rating", new Integer(seek.getSeekerRating())});
      vars.addElement(
          new Object[] {
            "isProvisional", seek.isSeekerProvisional() ? Boolean.TRUE : Boolean.FALSE
          });
      vars.addElement(
          new Object[] {"isRegistered", seek.isSeekerRegistered() ? Boolean.TRUE : Boolean.FALSE});
      vars.addElement(
          new Object[] {"isComputer", seek.isSeekerComputer() ? Boolean.TRUE : Boolean.FALSE});
      vars.addElement(new Object[] {"ratingCategory", seek.getRatingCategoryString()});
      if (seek.getTimeControl() instanceof FischerTimeControl) {
        FischerTimeControl tc = (FischerTimeControl) seek.getTimeControl();
        vars.addElement(new Object[] {"time", new Integer(tc.getInitial() / (1000 * 60))});
        vars.addElement(new Object[] {"inc", new Integer(tc.getIncrement() / 1000)});
      }
      vars.addElement(new Object[] {"isRated", seek.isRated() ? Boolean.TRUE : Boolean.FALSE});
      String colorString =
          seek.getSoughtColor() == null
              ? null
              : (seek.getSoughtColor().isWhite() ? "white" : "black");
      vars.addElement(new Object[] {"color", colorString});
      vars.addElement(
          new Object[] {"ratingLimited", seek.isRatingLimited() ? Boolean.TRUE : Boolean.FALSE});
      vars.addElement(new Object[] {"minRating", new Integer(seek.getMinRating())});
      vars.addElement(new Object[] {"maxRating", new Integer(seek.getMaxRating())});
      vars.addElement(
          new Object[] {"isManualAccept", seek.isManualAccept() ? Boolean.TRUE : Boolean.FALSE});
      vars.addElement(new Object[] {"isFormula", seek.isFormula() ? Boolean.TRUE : Boolean.FALSE});

      return vars;
    }

    @Override
    public void seekAdded(SeekEvent evt) {
      Vector varsVector = createVarsVector(evt);
      Object[][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);

      runScripts(evt, subtypes[0], vars);
    }

    @Override
    public void seekRemoved(SeekEvent evt) {
      Vector varsVector = createVarsVector(evt);
      Object[][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);

      runScripts(evt, subtypes[1], vars);
    }

    @Override
    protected Object[][] getAvailableVars(String[] eventSubtypes) {
      Vector varsVector = new Vector(25);

      Seek seek =
          new Seek(
              "64",
              getConn().userForName("AlexTheGreat"),
              "C",
              1800,
              false,
              true,
              true,
              true,
              Chess.getInstance(),
              "Blitz",
              new FischerTimeControl(5 * 60 * 1000, 2000),
              true,
              null,
              true,
              1700,
              1900,
              false,
              false);

      varsVector.addElement(new Object[] {"seek", seek});
      varsVector.addElement(new Object[] {"name", seek.getSeekerName()});
      varsVector.addElement(new Object[] {"title", seek.getSeekerTitle()});
      varsVector.addElement(new Object[] {"rating", new Integer(seek.getSeekerRating())});
      varsVector.addElement(
          new Object[] {
            "isProvisional", seek.isSeekerProvisional() ? Boolean.TRUE : Boolean.FALSE
          });
      varsVector.addElement(
          new Object[] {"isRegistered", seek.isSeekerRegistered() ? Boolean.TRUE : Boolean.FALSE});
      varsVector.addElement(
          new Object[] {"isComputer", seek.isSeekerComputer() ? Boolean.TRUE : Boolean.FALSE});
      varsVector.addElement(new Object[] {"ratingCategory", seek.getRatingCategoryString()});
      if (seek.getTimeControl() instanceof FischerTimeControl) {
        FischerTimeControl tc = (FischerTimeControl) seek.getTimeControl();
        varsVector.addElement(new Object[] {"time", new Integer(tc.getInitial() / (1000 * 60))});
        varsVector.addElement(new Object[] {"inc", new Integer(tc.getIncrement() / 1000)});
      }
      varsVector.addElement(
          new Object[] {"isRated", seek.isRated() ? Boolean.TRUE : Boolean.FALSE});
      String colorString =
          seek.getSoughtColor() == null
              ? null
              : (seek.getSoughtColor().isWhite() ? "white" : "black");
      varsVector.addElement(new Object[] {"color", colorString});
      varsVector.addElement(
          new Object[] {"ratingLimited", seek.isRatingLimited() ? Boolean.TRUE : Boolean.FALSE});
      varsVector.addElement(new Object[] {"minRating", new Integer(seek.getMinRating())});
      varsVector.addElement(new Object[] {"maxRating", new Integer(seek.getMaxRating())});
      varsVector.addElement(
          new Object[] {"isManualAccept", seek.isManualAccept() ? Boolean.TRUE : Boolean.FALSE});
      varsVector.addElement(
          new Object[] {"isFormula", seek.isFormula() ? Boolean.TRUE : Boolean.FALSE});

      Object[][] vars = new Object[varsVector.size()][];
      varsVector.copyInto(vars);
      return vars;
    }
  }

  /** A <code>ScriptDispatcher</code> for <code>FriendsEvents</code>. */
  private class FriendsScriptDispatcher extends ScriptDispatcher implements FriendsListener {

    private final String[] subtypes =
        new String[] {"stateChanged", "connected", "disconnected", "added", "removed"};

    @Override
    protected String[] getEventSubtypesImpl() {
      return subtypes;
    }

    @Override
    public boolean isSupportedBy(Connection conn) {
      return (conn instanceof FriendsConnection);
    }

    @Override
    public void registerForEvent(ListenerManager listenerManager) {
      ((FriendsListenerManager) listenerManager).addFriendsListener(this);
    }

    @Override
    public void unregisterForEvent(ListenerManager listenerManager) {
      ((FriendsListenerManager) listenerManager).removeFriendsListener(this);
    }

    @Override
    public void friendStateChanged(FriendsEvent evt) {
      runScripts(evt, subtypes[0], new Object[][] {{"name", evt.getFriend().getName()}});
    }

    @Override
    public void friendConnected(FriendsEvent evt) {
      runScripts(evt, subtypes[1], new Object[][] {{"name", evt.getFriend().getName()}});
    }

    @Override
    public void friendDisconnected(FriendsEvent evt) {
      runScripts(evt, subtypes[2], new Object[][] {{"name", evt.getFriend().getName()}});
    }

    @Override
    public void friendAdded(FriendsEvent evt) {
      runScripts(evt, subtypes[3], new Object[][] {{"name", evt.getFriend().getName()}});
    }

    @Override
    public void friendRemoved(FriendsEvent evt) {
      runScripts(evt, subtypes[4], new Object[][] {{"name", evt.getFriend().getName()}});
    }

    @Override
    protected Object[][] getAvailableVars(String[] eventSubtypes) {
      return new Object[][] {{"name", "AlexTheGreat"}};
    }
  }
}
