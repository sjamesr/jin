/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2007 Alexander Maryanovsky. All rights reserved.
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
package free.jin.console;

import free.jin.Connection;
import free.jin.event.ChatEvent;
import free.jin.event.JinEvent;
import free.jin.event.PlainTextEvent;
import free.util.AbstractNamed;
import free.util.TextUtilities;
import free.util.Utilities;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.SwingPropertyChangeSupport;

/** A base implementation of <code>ConsoleDesignation</code> with some useful facilities. */
public abstract class AbstractConsoleDesignation implements ConsoleDesignation {

  /** The connection to the server. */
  protected final Connection connection;

  /** Whether to count unseen messages and display their number in the designation name. */
  private final boolean countUnseenMessages;

  /** The name of the designation. */
  private String name;

  /**
   * The encoding used to encode/decode certain messages sent to/received from the server. A <code>
   * null</code> value indicates that the console manager's encoding should be used.
   */
  private final String encoding;

  /** Whether the console is closeable. */
  private boolean isConsoleCloseable;

  /** Our command types. */
  private final List commandTypes = new LinkedList();

  /** The console we're responsible for. */
  private Console console;

  /** The number of unseen (added when the console is invisible) messages we've received. */
  private int unseenMessageCount = 0;

  /** Our property change support. */
  private final PropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

  /**
   * Creates a new <code>AbstractConsoleDesignation</code>.
   *
   * @param connection The connection to the server.
   * @param name The name of the console.
   * @param encoding The encoding to use for encoding/decoding messages.
   * @param isConsoleCloseable Whether the console should be closeable.
   */
  public AbstractConsoleDesignation(
      Connection connection, String name, String encoding, boolean isConsoleCloseable) {
    this(connection, false, name, encoding, isConsoleCloseable);
  }

  /**
   * Creates a new <code>AbstractConsoleDesignation</code>.
   *
   * @param connection The connection to the server.
   * @param countUnseenMessages Whether to count the number of unseen messages and display it in the
   *     designation's name.
   * @param name The name of the console.
   * @param encoding The encoding to use for encoding/decoding messages.
   * @param isConsoleCloseable Whether the console should be closeable.
   */
  public AbstractConsoleDesignation(
      Connection connection,
      boolean countUnseenMessages,
      String name,
      String encoding,
      boolean isConsoleCloseable) {

    if (connection == null) throw new IllegalArgumentException("connection may not be null");

    this.connection = connection;
    this.countUnseenMessages = countUnseenMessages;
    this.name = name;
    this.encoding = encoding;
    this.isConsoleCloseable = isConsoleCloseable;
  }

  /**
   * The tag subclasses should use for tagging commands and messages sent to the server.
   *
   * @see #sendTaggedCommand(String)
   */
  protected String getTag() {
    return Integer.toHexString(System.identityHashCode(this));
  }

  /** Sets our console. */
  @Override
  public void setConsole(Console console) {
    this.console = console;

    console.addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent e) {
            consoleShown();
          }

          @Override
          public void componentHidden(ComponentEvent e) {
            consoleHidden();
          }
        });

    joinForums();
  }

  /** Returns the console we're responsible for. */
  public Console getConsole() {
    return console;
  }

  /**
   * This method should be overridden by subclasses to join any forums this designation accepts
   * which need to be joined explicitly.
   */
  protected void joinForums() {}

  /** Sets the number of unseen messages and updates the title accordingly. */
  protected final void setUnseenMessageCount(int unseenMessageCount) {
    String oldName = getName();
    this.unseenMessageCount = unseenMessageCount;
    String newName = getName();
    propertyChangeSupport.firePropertyChange("name", oldName, newName);
  }

  /** Invoked when the console we're responsible for is shown. */
  protected void consoleShown() {
    if (countUnseenMessages) setUnseenMessageCount(0);
  }

  /** Invoked when the console we're responsible for is hidden. */
  protected void consoleHidden() {}

  /** {@inheritDoc} */
  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /** Removes a property change listener. */
  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /** Returns the name of this designation. */
  @Override
  public String getName() {
    if (countUnseenMessages && (unseenMessageCount != 0))
      return name + " (" + unseenMessageCount + ")";
    else return name;
  }

  /**
   * Sets the name of this designation. If <code>countUnseenMessages</code> is set, the specified
   * name is treated only as a base-name.
   */
  protected void setName(String name) {
    if (Utilities.areEqual(this.name, name)) return;

    String oldName = getName();
    this.name = name;

    propertyChangeSupport.firePropertyChange("name", oldName, getName());
  }

  /**
   * Encodes the specified string for sending to the server, according to the connection's encoding
   * and the encoding of this console designation.
   */
  protected final String encode(String s) {
    return TextUtilities.convert(s, getActualEncoding(), connection.getTextEncoding());
  }

  /**
   * Decodes the specified message received from the server according to the connection's encoding
   * and the encoding of this console designation.
   */
  protected final String decode(String s) {
    return TextUtilities.convert(s, connection.getTextEncoding(), getActualEncoding());
  }

  /**
   * Returns the encoding actually used by this console designation. If the designation's encoding
   * is <code>null</code>, this returns the console manager's encoding. Note that in order for this
   * to work, the console must have been already set (via {@link #setConsole(Console)}).
   */
  private String getActualEncoding() {
    return encoding == null ? console.getConsoleManager().getEncoding() : encoding;
  }

  /** Returns whether the console is closeable. */
  @Override
  public boolean isConsoleCloseable() {
    return isConsoleCloseable;
  }

  /** Sets the closeable status of the console. */
  protected void setConsoleCloseable(boolean isConsoleCloseable) {
    if (this.isConsoleCloseable == isConsoleCloseable) return;

    boolean oldValue = this.isConsoleCloseable;
    this.isConsoleCloseable = isConsoleCloseable;

    propertyChangeSupport.firePropertyChange("consoleCloseable", oldValue, isConsoleCloseable);
  }

  /**
   * Sends the specified command, tagged with {@link AbstractConsoleDesignation#getTag()}, to the
   * server.
   */
  protected final void sendCommand(String command) {
    connection.sendTaggedCommand(command, getTag());
  }

  /**
   * Splits the reception of an event into two phases - accepting (or declining) it and, if
   * accepted, sending it to the console.
   */
  @Override
  public boolean receive(JinEvent evt) {
    if (accept(evt)) {
      append(evt);

      if (countUnseenMessages && !console.isVisible())
        setUnseenMessageCount(unseenMessageCount + 1);

      return true;
    } else return false;
  }

  /**
   * Returns whether the specified event's client tag is the same tag as ours.
   *
   * @see getTag()
   */
  protected boolean isTaggedByUs(JinEvent evt) {
    String tag = getTag();
    return (tag != null) && (tag.equals(evt.getClientTag()));
  }

  /** Returns whether the specified event is accepted by this <code>ConsoleDesignation</code>. */
  protected abstract boolean accept(JinEvent evt);

  /**
   * Appends the specified event to the console, causing it to be displayed there in some manner.
   * The default implementation passes the event to either {@link #appendChat(ChatEvent)} or {@link
   * #appendPlainText(PlainTextEvent)}, based on the class of the event.
   */
  protected void append(JinEvent evt) {
    if (evt instanceof PlainTextEvent) appendPlainText((PlainTextEvent) evt);
    else if (evt instanceof ChatEvent) appendChat((ChatEvent) evt);
  }

  /** Appends the specified chat event to the console. */
  protected void appendChat(ChatEvent evt) {
    Console console = getConsole();
    console.addToOutput(evt, getActualEncoding());
  }

  /** Appends the text of the specified plain text event to the console. */
  protected void appendPlainText(PlainTextEvent evt) {
    getConsole().addToOutput(decode(evt.getText()), "plain");
  }

  /**
   * Adds a command type to the list of command types that can be issued from this console. Note
   * that this method is only effective before an actual console with this designation has been
   * set-up, so the preferred place to invoke it is in the constructor (of the subclass).
   */
  protected void addCommandType(CommandType commandType) {
    commandTypes.add(commandType);
  }

  /** Returns the command types we're capable of sending. */
  @Override
  public final List getCommandTypes() {
    return Collections.unmodifiableList(commandTypes);
  }

  /** A skeleton implementation of a command type. */
  protected abstract class AbstractCommandType extends AbstractNamed implements CommandType {

    /** Whether user-text is encoded before passing it to the {@linkplain #send(String)} method. */
    private final boolean encodeUserText;

    /**
     * Creates a new <code>AbstractCommandType</code> with the specified name, which will
     * potentially encode user-text with the designation's encoding before passing it to the
     * {@linkplain #send(String)} method.
     */
    public AbstractCommandType(String name, boolean encodeUserText) {
      super(name);

      this.encodeUserText = encodeUserText;
    }

    /**
     * Creates a new <code>AbstractCommandType</code> with the specified name. The new <code>
     * AbstractCommandType</code> will automatically encode user-text with the designation's
     * encoding before passing it to the {@linkplain #send(String)} method.
     */
    public AbstractCommandType(String name) {
      this(name, true);
    }

    /**
     * Invoked when a command is issued by the user with this command type. If <code>userText</code>
     * is prefixed with a forward slash, the slash is stripped and the remaining command is passed
     * to <code>sendCommand</code> and, if the <code>doNotEcho</code> flag is unset, to <code>
     * echoCommand</code>. Otherwise, it is passed to <code>send</code> and, if the <code>doNotEcho
     * </code> flag is unset, to <code>echo</code>.
     */
    @Override
    public final void handleCommand(String userText, boolean doNotEcho) {
      boolean escaped = userText.startsWith("/");
      if (escaped) userText = userText.substring(1);

      if (escaped) {
        sendCommand(encodeUserText ? encode(userText) : userText);
        if (!doNotEcho) echoCommand(userText);
      } else {
        send(encodeUserText ? encode(userText) : userText);
        if (!doNotEcho) echo(userText);
      }
    }

    /**
     * Echoes the specified command to the console. The default implementation simply appends the
     * command's text to the console.
     */
    protected void echoCommand(String command) {
      Console console = getConsole();
      console.addToOutput(command, console.getUserTextType());
    }

    /**
     * "Sends" the specified user text. Usually, this meands sending some command to the server;
     * what exactly, depends on the nature of the console.
     *
     * @param userText The text entered by the user.
     * @param connection The connection to the server.
     */
    protected abstract void send(String userText);

    /**
     * Echoes the text entered by the user to the console. The default implementation does nothing.
     *
     * @param userText The text entered by the user.
     * @param user The user we're logged in with.
     */
    protected void echo(String userText) {}
  }
}
