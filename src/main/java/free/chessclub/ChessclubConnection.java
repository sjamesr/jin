/**
 * The chessclub.com connection library. More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002-2003 Alexander Maryanovsky. All rights reserved.
 *
 * The chessclub.com connection library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * The chessclub.com connection library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with the
 * chessclub.com connection library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package free.chessclub;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PushbackInputStream;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import free.chessclub.level1.Packet;
import free.chessclub.level2.Datagram;
import free.chessclub.level2.DatagramEvent;
import free.chessclub.level2.DatagramListener;
import free.util.Connection;
import free.util.EventListenerList;
import free.util.FormatException;

/**
 * This class is responsible for connecting to the chessclub.com server, logging on and further
 * processing of information as it arrives from the server. Before using this class you should read
 * <A HREF="ftp://ftp.chessclub.com/pub/icc/formats/formats.txt">ftp://ftp.chessclub.com/pub/icc/
 * formats/formats.txt</A> which describes ICC server's output. All the processXXX methods receive
 * arguments with the meaning and format described there unless specified otherwise in the method
 * documentation.
 */
public class ChessclubConnection extends free.util.Connection {

  /**
   * The standard delimiter, used to delimit both level1 and level2.
   */
  private static final char STANDARD_DELIMITER = '\u0019';

  /**
   * The start-of-level1-packet delimiter.
   */
  private static final char PACKET_START_DELIMITER = '[';

  /**
   * The end-of-level1-packet delimiter.
   */
  private static final char PACKET_END_DELIMITER = ']';

  /**
   * The start-of-level2-datagram delimiter.
   */
  private static final char DATAGRAM_START_DELIMITER = '(';

  /**
   * The end-of-datagram delimiter.
   */
  private static final char DATAGRAM_END_DELIMITER = ')';

  /**
   * Maps rating keys to their english names.
   */
  private final Hashtable ratingCategoryNames = new Hashtable();

  /**
   * Maps wild variant indices to their english names.
   */
  private final Hashtable variantNames = new Hashtable();

  /**
   * The PrintStream where this <code>ChessclubConnection</code> logs the commands we send to the
   * server and information we receive from it. May be <code>null</code> if we're not logging the
   * above information.
   */
  private final PrintStream logStream;

  /**
   * The level2 settings requested by the client. The bit at each index specifies whether the
   * Datagram with that index will be turned on.
   * <P>
   * Note that these aren't the "real" settings because the settings only take effect once the
   * server reads them and responds with a DG_SET2. The "real" level 2 settings are stored in
   * level2settings.
   */
  private BitSet requestedLevel2Settings = new BitSet();

  /**
   * The current board sending "style".
   */
  private int style = 1;

  /**
   * The value we're supposed to assign to the interface variable during login.
   */
  private String interfaceVar = "Java chessclub.com library (http://www.jinchess.com/)";

  /**
   * The current setting of level1.
   */
  private int level1State = 0;

  /**
   * The "real" level 2 settings on which currently the client and the server agree.
   */
  private BitSet level2Settings = new BitSet();

  /**
   * This is set to <code>true</code> when the "level2settings=..." line has been sent.
   */
  private boolean level2SettingsSent = false;

  /**
   * The queue of commands we are to send on-login.
   */
  private LinkedList onLoginCommandQueue = new LinkedList();

  /**
   * A list of listeners to our datagram events, lazily instantiated.
   */
  private final EventListenerList[] datagramListeners =
      new EventListenerList[Datagram.MAX_DG_ID + 1];

  /**
   * Creates a new ChessclubConnection with a chessclub.com server, the ChessclubConnection is
   * initially unconnected. After creating the ChessclubConnection, you can set the various settings
   * (level2 settings for example) and then call the <code>connectAndLogin</code> method.
   *
   * @param requestedUsername The requested username, note that the actual username is unknown until
   * after the login.
   * @param password The password of the account.
   * @param logStream The PrintStream where this ChessclubConnection will log all information sent
   * by the server and commands sent by this <code>ChessclubConnection</code>. Pass
   * <code>null</code> if you don't want logging.
   *
   * @see #setDGState(int, boolean)
   */
  public ChessclubConnection(String requestedUsername, String password, PrintStream logStream) {
    super(requestedUsername, password);

    this.logStream = logStream;

    // We need this to get the real username
    setDGState(Datagram.DG_WHO_AM_I, true);

    // We need this to set the login error message properly
    setDGState(Datagram.DG_LOGIN_FAILED, true);

    // We need this to know the key to rating name mapping.
    setDGState(Datagram.DG_RATING_TYPE_KEY, true);

    // We need this to know the wild number to wild name mapping.
    setDGState(Datagram.DG_WILD_KEY, true);

    // We need this to know which datagrams are actually on.
    setDGState(Datagram.DG_SET2, true);
  }

  /**
   * Adds the specified <code>DatagramListener</code> to receive notifications when datagrams with
   * the specified id arrive. Registering the first listener for a certain type of datagram will
   * cause that datagram to be turned on with the server. This is true even for datagrams which are
   * never sent to the client, but instead act as flags for other datagrams (DG_MOVE_SMITH for
   * example), which means you may have to register listeners for datagrams you never indend to
   * receive. It may seem a bit silly, but it's cleaner than making <code>setDGState</code> public.
   * This also means that when processing the fields of a datagram, you should always be prepared to
   * handle all the optional fields, even if you did not request them (someone else might have
   * requested them). You can check which of the optional fields are on via the
   * <code>isDGOn(int)</code> method.
   */
  public void addDatagramListener(DatagramListener dgListener, int dgId) {
    if (datagramListeners[dgId] == null) {
      datagramListeners[dgId] = new EventListenerList();
      setDGState(dgId, true);
    }

    datagramListeners[dgId].add(DatagramListener.class, dgListener);
  }

  /**
   * Removes the specified <code>DatagramListener</code> from receiving notifications when datagrams
   * with the specified id arrive. Unregistering the last listener for a certain (non essential)
   * type of datagram will cause that datagram to be turned off with the server. This is true even
   * for datagrams which are never sent to the client, but instead act as flags for other datagrams
   * (DG_MOVE_SMITH for example), which means you may have to unregister listeners for datagrams you
   * never indend to receive. It may seem a bit silly, but it's cleaner than making
   * <code>setDGState</code> public.
   */
  public void removeDatagramListener(DatagramListener dgListener, int dgId) {
    if (datagramListeners[dgId] == null) return;

    datagramListeners[dgId].remove(DatagramListener.class, dgListener);

    if (datagramListeners[dgId].getListenerCount() == 0) {
      datagramListeners[dgId] = null;
      setDGState(dgId, false);
    }
  }

  /**
   * Fires a <code>DatagramEvent</code> for the specified <code>Datagram</code> to all registered
   * listeners.
   */
  protected void fireDatagramEvent(Datagram datagram, String clientTag) {
    DatagramEvent evt = new DatagramEvent(this, datagram, clientTag);

    EventListenerList listenerList = datagramListeners[datagram.getId()];
    if (listenerList != null) {
      Object[] listeners = listenerList.getListenerList();
      for (int i = 1; i < listeners.length; i += 2)
        ((DatagramListener) listeners[i]).datagramReceived(evt);
    }
  }

  /**
   * Sets the level1 state. Note that we currently do not support states where the 2nd lowest bit is
   * set (the one which causes ^Y< and ^Y> delimiters to be sent).
   */
  public final synchronized void setLevel1(int level1State) {
    if ((level1State & 2) != 0)
      throw new IllegalArgumentException(
          "level1 states with the 2nd lowest bit set are unsupported");

    this.level1State = level1State;

    if (isLoggedIn()) sendCommand("set level1 " + level1State, false, true, null);
  }

  /**
   * Returns the current level1 state. See
   * <code>ftp://ftp.chessclub.com/pub/icc/formats/formats.txt</code> for documentation about
   * level1.
   */
  public final synchronized int getLevel1() {
    return level1State;
  }

  /**
   * Sets the given level2 datagram on or off. If the ChessclubConnection is already logged in, then
   * the <code>set-2 [DG number] [0/1]</code> string is sent to the server, otherwise the setting is
   * saved, and in the login procedure all the level2 settings are sent on the login line in the
   * <code>level2settings=0011011011...</code> format. Note that some datagrams are necessary for
   * the correct behaviour of this class, and cannot be turned off (DG_WHO_AM_I and DG_SET2 for
   * example). This method is no longer the "normal" way of controlling datagrams' state. In fact,
   * you should not control the datagrams' state directly at all - instead, register a
   * <code>DatagramListener</code> with the id of the datagram you want to receive and it will be
   * turned on for you automatically. This is true even for datagrams which are never sent to the
   * client, but instead act as flags for other datagrams (DG_MOVE_SMITH for example). It may seem a
   * bit silly, but it's cleaner than making this method public.
   *
   * @param dgNumber The number of the datagram.
   * @param state Whether turn the datagram on or off.
   *
   * @return Whether the state of the datagram was modified successfully. This always returns true
   * when setting a datagram on, and only returns false when trying to set an essential datagram
   * off.
   *
   * @see #isEssentialDG(int)
   * @see #isDGOn(int)
   */
  protected final synchronized boolean setDGState(int dgNumber, boolean state) {
    if ((state == false) && isEssentialDG(dgNumber)) return false;

    if (state) requestedLevel2Settings.set(dgNumber);
    else requestedLevel2Settings.clear(dgNumber);

    if (level2SettingsSent) {
      if (isLoggedIn())
        sendCommand("set-2 " + dgNumber + " " + (state ? "1" : "0"), false, true, null);
      // Otherwise, we will fix it in onLogin(). We don't do it here because it's
      // not a good idea to send anything in the middle of the login procedure.
    } else {
      if (state) level2Settings.set(dgNumber);
      else level2Settings.clear(dgNumber);
    }

    return true;
  }

  /**
   * Sets the given datagram on again. This is needed because some datagrams won't correctly keep
   * you up-to-date with the current state of events, and you need (sigh, this is definitely not
   * safe) to set them again to get a refresh (DG_NOTIFY_ARRIVED for example).
   *
   * @throws IllegalStateException if the datagram is not on already or if we're not logged in yet.
   */
  protected synchronized void setDGOnAgain(int dgNumber, String tag) {
    if (!isDGOn(dgNumber))
      throw new IllegalStateException("Cannot set on again a datagram which is not on");
    if (!isLoggedIn())
      throw new IllegalStateException("Cannot set on again a datagram when not yet logged in");

    sendCommand("set-2 " + dgNumber + " 1", false, true, tag);
  }

  /**
   * Same as <code>setDGOnAgain</code>, but without the tag.
   */
  protected void setDGOnAgain(int dgNumber) {
    setDGOnAgain(dgNumber, null);
  }

  /**
   * Sets the interface variable to have the given value. This works only if the ChessclubConnection
   * is not logged on yet, otherwise, throws an IllegalArgumentException. The actual interface
   * variable will be set during the login procedure.
   */
  public final synchronized void setInterface(String interfaceVar) {
    if (isLoggedIn()) throw new IllegalStateException();

    this.interfaceVar = interfaceVar;
  }

  /**
   * Sets the style. If the ChessclubConnection is already logged in, then a
   * "set-quietly style <style>" command is send immediately, otherwise, the setting is saved and
   * sent immediately after logging in. If the <code>getEssentialStyle()</code> method returns a
   * nonzero value different from the specified style, this method will return <code>false</code>;
   * otherwise, it returns <code>true</code>.
   */
  public final synchronized boolean setStyle(int style) {
    int essentialStyle = getEssentialStyle();
    if ((essentialStyle != -1) && (essentialStyle != style)) return false;

    this.style = style;
    if (isLoggedIn()) sendCommand("set-quietly style " + style, false, true, null);

    return true;
  }

  /**
   * Returns true if the datagram with the given id is essential for the normal operation of the
   * instance and therefore cannot be turned off. Overriding methods MUST check with the superclass
   * method before returning false.
   */
  protected boolean isEssentialDG(int dgNumber) {
    switch (dgNumber) {
      case Datagram.DG_WHO_AM_I:
      case Datagram.DG_LOGIN_FAILED:
      case Datagram.DG_RATING_TYPE_KEY:
      case Datagram.DG_SET2:
        return true;
      default:
        return false;
    }
  }

  /**
   * Returns the style which is essential for the normal operation of this class and therefore
   * cannot be changed. Returns -1 if the style setting is not essential. Overriding methods MUST
   * check with the superclass method before returning a value. This method returns -1.
   */
  protected int getEssentialStyle() {
    return -1;
  }

  /**
   * Returns true if the given level2 datagram is turned on. Note that this method returns the
   * actual level2 settings, not the ones requested by the client. These may differ because during
   * the time from the moment when the client sends a request to turn on/off a datagram and until
   * the server receives it, the server will keep sending DGs thinking that particular DG is in its
   * previous state.
   *
   * @param dg The datagram number whose status you want to check.
   *
   * @see #setDGState(int,boolean)
   */
  public synchronized boolean isDGOn(int dg) {
    return level2Settings.get(dg);
  }

  /**
   * Sends the login information to the server.
   */
  @Override
  protected void sendLoginSequence() {
    if ((getPassword() == null) || (getPassword().length() == 0))
      sendCommandImpl(getRequestedUsername(), true);
    else sendCommandImpl(getRequestedUsername() + " " + getPassword(), false);
  }

  /**
   * Invoked when a connection to the server is established. Sends level2settings information to the
   * server.
   */
  @Override
  protected void handleConnected() {
    sendCommandImpl("level1=" + level1State, true);

    int largestSetDGNumber = level2Settings.size();
    while ((largestSetDGNumber >= 0) && !level2Settings.get(largestSetDGNumber))
      largestSetDGNumber--;
    if (largestSetDGNumber >= 0) {
      StringBuffer buf = new StringBuffer("level2settings=");
      for (int i = 0; i <= largestSetDGNumber; i++) {
        buf.append(level2Settings.get(i) ? "1" : "0");
      }
      sendCommandImpl(buf.toString(), true);
      level2SettingsSent = true;
    }

    super.handleConnected();
  }

  /**
   * Sets the various things we need to set on login.
   */
  @Override
  protected void handleLoginSucceeded() {
    synchronized (this) {
      // Apply any level2 changes which might have occurred when we were waiting
      // for login.
      for (int i = 0; i < requestedLevel2Settings.size(); i++) {
        boolean state = requestedLevel2Settings.get(i);
        if (state != level2Settings.get(i))
          sendCommand("set-2 " + i + " " + (state ? "1" : "0"), false, true, null);
      }

      sendCommand("set-quietly prompt 0", false, true, null);
      sendCommand("set-quietly highlight 0", false, true, null);
      sendCommand("set-quietly style " + style, false, true, null);
      sendCommand("set-quietly interface " + interfaceVar, false, true, null);

      for (Iterator i = onLoginCommandQueue.iterator(); i.hasNext(); ) {
        String command = (String) i.next();
        sendCommand(command, false, false, null);
      }
      onLoginCommandQueue.clear();
    }

    super.handleLoginSucceeded();
  }

  /**
   * Handles the specified message.
   */
  @Override
  protected void handleMessage(Object message) {
    handleMessage(message, null);
  }

  /**
   * Overrides {@link Connection#createInputStream(InputStream)} to wrap the specified
   * <code>InputStream</code> in a <code>BufferedInputStream</code> and a
   * <code>PushbackInputStream</code>.
   */
  @Override
  protected InputStream createInputStream(InputStream in) {
    return new PushbackInputStream(new BufferedInputStream(in), 2);
  }

  /**
   * Reads either a line of plain text, a level1 packet or a level2 datagram from the server.
   */
  @Override
  protected Object readMessage(InputStream in) throws IOException {
    PushbackInputStream pin = (PushbackInputStream) in;

    while (true) {
      int b = pin.read();

      if (b < 0) // Clean disconnection
      return null;

      if (b
          == STANDARD_DELIMITER) { // May be a level1 packet or datagram (but may also be just a line with this character
        int next = pin.read();
        if (next < 0) throw new EOFException("EOF after STANDARD_DELIMITER");

        pin.unread(next);
        pin.unread(b);

        if (next == PACKET_START_DELIMITER) return readPacket(pin);
        else if (next == DATAGRAM_START_DELIMITER) return readDatagram(pin);
        else if (next == PACKET_END_DELIMITER) // This means a clean logout
        return null;
      } else {
        pin.unread(b);
        String line = readLine(pin);
        return filterLine(line);
      }
    }
  }

  /**
   * Reads and parses a level1 packet from the specified input stream.
   *
   * @throws IOException if an I/O error occurs while reading the datagram
   */
  private Packet readPacket(PushbackInputStream in) throws IOException {
    StringBuffer header = new StringBuffer();

    // Read until the newline
    while (true) {
      int b = in.read();
      if (b < 0) throw new EOFException("EOF in a packet");

      if (b == '\n') break;

      header.append((char) b);
    }

    // In contradiction to formats.txt, the servers seems to end the header line with \r\n
    int len = header.length();
    if (header.charAt(len - 1) == '\r') header.setLength(len - 1);

    StringTokenizer tokenizer = new StringTokenizer(header.substring(2), " ");
    int commandCode = Integer.parseInt(tokenizer.nextToken());
    String playerName = tokenizer.nextToken();
    String clientTag = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;

    List items = new LinkedList();
    // Read until the matching ^Y]
    while (true) {
      int first = in.read();
      if (first < 0) throw new EOFException("EOF in a packet");

      int second = in.read();
      if (second < 0) throw new EOFException("EOF in a packet");

      if ((first == STANDARD_DELIMITER) && (second == PACKET_END_DELIMITER)) break;

      in.unread(second);
      in.unread(first);

      items.add(readMessage(in));
    }

    return new Packet(commandCode, playerName, clientTag, items.toArray(new Object[items.size()]));
  }

  /**
   * Reads and parses a level2 datagram from the specified input stream.
   *
   * @throws IOException if an I/O error occurs while reading the datagram
   * @throws FormatException if the data read from the input stream can't be parsed as a datagram.
   */
  private Datagram readDatagram(InputStream in) throws IOException {
    StringBuffer buf = new StringBuffer();

    int lastChar = -1;

    // Read until ^Y) is encountered
    while (true) {
      int b = in.read();
      if (b < 0) throw new EOFException("EOF in a datagram");

      buf.append((char) b);

      if ((lastChar == STANDARD_DELIMITER) && (b == DATAGRAM_END_DELIMITER))
        return Datagram.parseDatagram(
            buf.substring(2, buf.length() - 2)); // Strip off the delimiters

      lastChar = b;
    }
  }

  /**
   * Read a line of plain text from the server.
   */
  private String readLine(PushbackInputStream in) throws IOException {
    StringBuffer buf = new StringBuffer();

    while (true) {
      int b = in.read();

      if (b < 0) break;

      // End of line
      if (b == '\n') break;

      // '\r' is also a line delimiter
      if (b == '\r') {
        b = in.read(); // Eat the following '\n', if any
        if ((b > 0) && (b != '\n')) in.unread(b);
        break;
      }

      // Don't read any datagrams
      if (b == STANDARD_DELIMITER) {
        in.unread(b);
        break;
      }

      buf.append((char) b);
    }

    return buf.toString();
  }

  /**
   * Filters certain characters and character sequences (such as BEL and the prompt) from plain
   * lines of text. Returns the string after filter, or <code>null</code> if the line should be
   * completely ignored.
   */
  private String filterLine(String line) {
    int len = line.length();

    // Keep initially empty lines.
    if (len == 0) return "";

    StringBuffer buf = new StringBuffer();

    int i = 0;

    // Skip leading prompt
    while (line.startsWith("aics% ", i)) i += "aics% ".length();

    for (; i < len; i++) {
      char c = line.charAt(i);

      // Ignore BEL
      if (c == 7) continue;

      buf.append(c);
    }

    // Ignore lines which have been completely filtered out
    if (buf.length() == 0) return null;

    return buf.toString();
  }

  /**
   * Sends the "exit" command to the server, when logged in.
   */
  public void exit() {
    sendCommand("exit", true, true, null);
  }

  /**
   * Sends a command to the server with the specified options.
   *
   * @param command The command.
   * @param whenLoggedIn If set and we are not yet logged in, wait until login and then send the
   * command. If unset, the command is sent immediately.
   * @param avoidAliasing Avoid triggering any aliases with the command.
   * @param tag The client tag ("arbitrary-string" in formats.txt) with which we tag the command;
   * <code>null</code> if none.
   */
  public synchronized void sendCommand(
      String command, boolean whenLoggedIn, boolean avoidAliasing, String tag) {
    command = makeCommand(command, avoidAliasing, tag);

    if (isLoggedIn() || !whenLoggedIn) sendCommandImpl(command, true);
    else onLoginCommandQueue.addLast(command);
  }

  /**
   * Creates the actual command to be sent based on the specified options.
   */
  private synchronized String makeCommand(String command, boolean avoidAliasing, String tag) {
    if (avoidAliasing && !(command.startsWith("multi ") || (command.indexOf(';') != -1)))
      command = "multi " + command;

    if (tag != null) {
      int level1 = getLevel1();
      if ((level1 & 5) != level1)
        throw new IllegalArgumentException("Wrong level1 (" + level1 + ") for tagged commands");

      command = "`" + tag + "`" + command;
    }

    return command;
  }

  /**
   * Sends the given command to the server, optionally logging it to the log stream.
   */
  private synchronized void sendCommandImpl(String command, boolean log) {
    if (!isConnected()) throw new IllegalStateException("Not connected");

    if (log && (logStream != null)) logStream.println("SENDING COMMAND: " + command);

    try {
      OutputStream out = getOutputStream();
      out.write(command.getBytes("ISO8859_1"));
      out.write('\n');
      out.flush();
    } catch (IOException e) {
      connectionInterrupted(e);
    }
  }

  /**
   * Returns the name of the rating category with the given index. Note that this is unknown until
   * the login procedure is done (which may be even after login() returns, but it's pretty much
   * guaranteed to be known before you receive notification of any datagrams, except for some
   * special ones, like DG_WHO_AM_I). Returns null if no such rating category exists, or it's
   * unknown yet.
   */
  public String getRatingCategoryName(int index) {
    return (String) ratingCategoryNames.get(new Integer(index));
  }

  /**
   * Returns the name of the wild variant with the given number.Note that this is unknown until the
   * login procedure is done (which may be even after login() returns, but it's pretty much
   * guaranteed to be known before you receive notification of any datagrams, except for some
   * special ones, like DG_WHO_AM_I). Returns null if no such wild variant exists, or it's unknown
   * yet.
   */
  public String getVariantName(int number) {
    return (String) variantNames.get(new Integer(number));
  }

  /**
   * Passes the message to one of:
   * <ul>
   * <li>{@link #handlePacket(Packet, String)}
   * <li>{@link #handleDatagram(Datagram, String))}
   * <li>{@link #handleLine(String, String)}
   * </ul>
   */
  protected void handleMessage(Object message, String clientTag) {
    if (message == null) // It's null if it was filtered (by filterLine, for example)
    return;

    if (message instanceof String) handleLine((String) message, clientTag);
    else if (message instanceof Packet) handlePacket((Packet) message, clientTag);
    else if (message instanceof Datagram) handleDatagram((Datagram) message, clientTag);
    else
      throw new IllegalArgumentException(
          "Unrecognized message type: " + message.getClass().getName());
  }

  /**
   * Invoked when a level1 packet arrives from the server.
   *
   * @param packet The packet.
   * @param clientTag The client tag of the containing packet, if any.
   */
  private void handlePacket(Packet packet, String clientTag) {
    for (int i = 0; i < packet.getItemCount(); i++)
      handleMessage(packet.getItem(i), clientTag == null ? packet.getClientTag() : clientTag);
    // Really, though, only the outermost packet may have a client tag
  }

  /**
   * This method is called when a new level2 datagram arrives from the server.
   *
   * @param datagram The level2 datagram that was received.
   * @param clientTag The client tag of the containing packet, if any.
   *
   * @see #processDatagram(Datagram)
   */
  private final void handleDatagram(Datagram datagram, String clientTag) {
    if (logStream != null) logStream.println(datagram);

    int id = datagram.getId();
    if ((id == Datagram.DG_WHO_AM_I) && !isLoggedIn()) loginSucceeded(datagram.getString(0));
    else if ((id == Datagram.DG_LOGIN_FAILED) && !isLoggedIn()) loginFailed(datagram.getString(1));
    else if (id == Datagram.DG_RATING_TYPE_KEY) {
      int index = datagram.getInteger(0);
      String name = datagram.getString(1);
      ratingCategoryNames.put(new Integer(index), name);
    } else if (id == Datagram.DG_WILD_KEY) {
      int number = datagram.getInteger(0);
      String name = datagram.getString(1);
      variantNames.put(new Integer(number), name);
    } else if (id == Datagram.DG_SET2) {
      int dgType = datagram.getInteger(0);
      boolean state = datagram.getBoolean(1);
      if (state) level2Settings.set(dgType);
      else level2Settings.clear(dgType);
    }

    fireDatagramEvent(datagram, clientTag);
  }

  /**
   * This method is called when a new line of plain text arrives from the server.
   *
   * @param line The line that was received, '\n' not included.
   * @param clientTag The client tag of the containing packet, if any.
   *
   * @see #processLine(String)
   */
  private final void handleLine(String line, String clientTag) {
    if (logStream != null) logStream.println(line);

    processLine(line, clientTag);
  }

  /**
   * This method is called to process a single line of text. It does nothing and is intended as a
   * notification method for subclasses.
   *
   * @param line The line that was received, '\n' not included.
   * @param clientTag The client tag of the containing packet, if any.
   */
  protected void processLine(String line, String clientTag) {}
}
