/**
 * The chessclub.com connection library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002-2003 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The chessclub.com connection library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The chessclub.com connection library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the chessclub.com connection library; if not, write to the Free
 * Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.chessclub;

import java.io.*;
import java.util.*;
import free.chessclub.level2.*;
import free.util.EventListenerList;


/**
 * This class is responsible for connecting to the chessclub.com
 * server, logging on and further processing of information as it
 * arrives from the server. Before using this class you should read 
 * <A HREF="ftp://ftp.chessclub.com/pub/icc/formats/formats.txt">ftp://ftp.chessclub.com/pub/icc/formats/formats.txt</A>
 * which describes ICC server's output. All the processXXX methods receive arguments
 * with the meaning and format described there unless specified otherwise
 * in the method documentation.
 */

public class ChessclubConnection extends free.util.Connection{




  /**
   * Maps rating keys to their english names.
   */

  private final Hashtable ratingCategoryNames = new Hashtable();




  /**
   * Maps wild variant indices to their english names.
   */

  private final Hashtable variantNames = new Hashtable();




  /**
   * The PrintStream where this ChessclubConnection echoes the information sent by the 
   * server and the commands sent by it to the standard output stream. Null
   * if it doesn't echo.
   */

  private final PrintStream echoStream;

  


  /**
   * The level2 settings requested by the client. The bit at each index
   * specifies whether the Datagram with that index
   * will be turned on. <P>
   * Note that these aren't the "real" settings because the settings only take
   * effect once the server reads them and responds with a DG_SET2. The
   * "real" level 2 settings are stored in level2settings.
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
   * The "real" level 2 settings on which currently the client and the server
   * agree.
   */

  private BitSet level2Settings = new BitSet();




  /**
   * This is set to <code>true</code> when the "level2settings=..." line has
   * been sent.
   */

  private boolean level2SettingsSent = false;



  /**
   * The OutputStream to the server.
   */

  private OutputStream out;



  /**
   * The lock we synchronize the login on.
   */

  private final Object loginLock = new String("Login Lock");



  /**
   * A list of listeners to our datagram events, lazily instantiated.
   */
   
  private final EventListenerList [] datagramListeners = new EventListenerList[Datagram.MAX_DG_ID + 1];



  /**
   * Creates a new ChessclubConnection with a chessclub.com server, the 
   * ChessclubConnection is initially unconnected. After creating the 
   * ChessclubConnection, you can set the various settings (level2 settings for 
   * example) and then call the <code>connectAndLogin</code> method.
   *
   * @param username The requested username, note that the actual username is
   * unknown until after the login.
   * @param password The password of the account.
   * @param echoStream The PrintStream where this ChessclubConnection will echo
   * all information sent by the server and commands sent by this
   * ChessclubConnection. Pass null if you don't want echoing.
   *
   * @see #setDGState(int, boolean)
   */

  public ChessclubConnection(String username, String password, PrintStream echoStream){
    super(username, password);

    this.echoStream = echoStream;

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
   * Adds the specified <code>DatagramListener</code> to receive notifications
   * when datagrams with the specified id arrive. Registering the first listener
   * for a certain type of datagram will cause that datagram to be turned on
   * with the server. This is true even for datagrams which are never sent to
   * the client, but instead act as flags for other datagrams (DG_MOVE_SMITH for
   * example), which means you may have to register listeners for datagrams you
   * never indend to receive. It may seem a bit silly, but it's cleaner than
   * making <code>setDGState</code> public. This also means that when processing
   * the fields of a datagram, you should always be prepared to handle all
   * the optional fields, even if you did not request them (someone else might
   * have requested them). You can check which of the optional fields are on
   * via the <code>isDGOn(int)</code> method.
   */
   
  public void addDatagramListener(DatagramListener dgListener, int dgId){
    if (datagramListeners[dgId] == null){
      datagramListeners[dgId] = new EventListenerList();
      setDGState(dgId, true);
    }
    
    datagramListeners[dgId].add(DatagramListener.class, dgListener);
  }
  
  
  
  /**
   * Removes the specified <code>DatagramListener</code> from receiving
   * notifications when datagrams with the specified id arrive. Unregistering
   * the last listener for a certain (non essential) type of datagram will cause
   * that datagram to be turned off with the server. This is true even for
   * datagrams which are never sent to the client, but instead act as flags for
   * other datagrams (DG_MOVE_SMITH for example), which means you may have to
   * unregister listeners for datagrams you never indend to receive. It may seem
   * a bit silly, but it's cleaner than making <code>setDGState</code> public.
   */
   
  public void removeDatagramListener(DatagramListener dgListener, int dgId){
    if (datagramListeners[dgId] == null)
      return;
    
    datagramListeners[dgId].remove(DatagramListener.class, dgListener);
    
    if (datagramListeners[dgId].getListenerCount() == 0){
      datagramListeners[dgId] = null;
      setDGState(dgId, false);
    }
  }
  
  
  
  /**
   * Fires a <code>DatagramEvent</code> for the specified <code>Datagram</code>
   * to all registered listeners.
   */
   
  protected void fireDatagramEvent(Datagram datagram){
    DatagramEvent evt = new DatagramEvent(this, datagram);
    
    EventListenerList listenerList = datagramListeners[datagram.getId()];
    if (listenerList != null){
      Object [] listeners = listenerList.getListenerList();
      for (int i = 1; i < listeners.length; i += 2)
        ((DatagramListener)listeners[i]).datagramReceived(evt);
    }
  }
  

   
   
   /**
   * Sets the given level2 datagram on or off. If the ChessclubConnection is
   * already logged in, then the <code>set-2 [DG number] [0/1]</code> string
   * is sent to the server, otherwise the setting is saved, and in the login
   * procedure all the level2 settings are sent on the login line in the
   * <code>level2settings=0011011011...</code> format.
   * Note that some datagrams are necessary for the correct behaviour of this
   * class, and cannot be turned off (DG_WHO_AM_I and DG_SET2 for example).
   * This method is no longer the "normal" way of controlling datagrams' state.
   * In fact, you should not control the datagrams' state directly at all -
   * instead, register a <code>DatagramListener</code> with the id of the
   * datagram you want to receive and it will be turned on for you
   * automatically. This is true even for datagrams which are never sent to the
   * client, but instead act as flags for other datagrams (DG_MOVE_SMITH for
   * example). It may seem a bit silly, but it's cleaner than making this method
   * public.
   *
   * @param dgNumber The number of the datagram.
   * @param state Whether turn the datagram on or off.
   *
   * @return Whether the state of the datagram was modified successfully. This
   * always returns true when setting a datagram on, and only returns false
   * when trying to set an essential datagram off.
   *
   * @see #isEssentialDG(int)
   * @see #isDGOn(int)
   */

  protected final synchronized boolean setDGState(int dgNumber, boolean state){
    if ((state == false) && isEssentialDG(dgNumber))
      return false;

    if (state)
      requestedLevel2Settings.set(dgNumber);
    else
      requestedLevel2Settings.clear(dgNumber);

    if (level2SettingsSent){
      if (isLoggedIn())
        sendCommand("set-2 "+dgNumber+" "+(state ? "1" : "0"));
      // Otherwise, we will fix it in onLogin(). We don't do it here because it's
      // not a good idea to send anything in the middle of the login procedure.
    }
    else{
      if (state)
        level2Settings.set(dgNumber);
      else
        level2Settings.clear(dgNumber);
    }

    return true;
  }





  /**
   * Sets the given datagram on again. This is needed because some datagrams
   * won't correctly keep you up-to-date with the current state of events, and
   * you need (sigh, this is definitely not safe) to set them again to get a
   * refresh (DG_NOTIFY_ARRIVED for example). 
   *
   * @throws IllegalStateException if the datagram is not on already or if we're
   * not logged in yet.
   */

  protected synchronized void setDGOnAgain(int dgNumber){
    if (!isDGOn(dgNumber))
      throw new IllegalStateException("Cannot set on again a datagram which is not on");
    if (!isLoggedIn())
      throw new IllegalStateException("Cannot set on again a datagram when not yet logged in");

    sendCommand("set-2 " + dgNumber + " 1");
  }




  /**
   * Sets the interface variable to have the given value. This works only if the
   * ChessclubConnection is not logged on yet, otherwise, throws an 
   * IllegalArgumentException. The actual interface variable will be set during 
   * the login procedure.
   */

  public final synchronized void setInterface(String interfaceVar){
    if (isLoggedIn())
      throw new IllegalStateException();
    this.interfaceVar = interfaceVar;
  }





  /**
   * Sets the style. If the ChessclubConnection is already logged in, then
   * a "set-quietly style <style>" command is send immediately, otherwise, the setting
   * is saved and sent immediately after logging in. If the <code>getEssentialStyle()</code>
   * mehod returns a value different than 0 and different from the given style,
   * this method will throw an IllegalAccessException.
   */

  public final synchronized boolean setStyle(int style){
    int essentialStyle = getEssentialStyle();
    if ((essentialStyle != -1) && (essentialStyle != style))
      return false;

    this.style = style;
    if (isLoggedIn())
      sendCommand("set-quietly style " + style);

    return true;
  }



  /**
   * Returns true if the datagram with the given id is essential for the normal
   * operation of the instance and therefore cannot be turned off. Overriding
   * methods MUST check with the superclass method before returning false.
   */

  protected boolean isEssentialDG(int dgNumber){
    switch (dgNumber){
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
   * Returns the style which is essential for the normal operation of this class
   * and therefore cannot be changed. Returns -1 if the style setting is not
   * essential. Overriding methods MUST check with the superclass method before
   * returning a value. This method returns -1.
   */

  protected int getEssentialStyle(){
    return -1;
  }



  /**
   * Returns true if the given level2 datagram is turned on.
   * Note that this method returns the actual level2 settings, not the ones
   * requested by the client. These may differ because during the time from the
   * moment when the client sends a request to turn on/off a datagram and until 
   * the server receives it, the server will keep sending DGs thinking that
   * particular DG is in its previous state.
   *
   * @param dg The datagram number whose status you want to check.
   *
   * @see #setDGState(int,boolean)
   */

  public synchronized boolean isDGOn(int dg){
    return level2Settings.get(dg);
  }

  

  /**
   * Goes through the login procedure on a chessclub.com server.
   *
   * @throws IOException if an I/O error occured when connecting or logging on
   * to the server.
   *
   * @see #isConnected()
   * @see #disconnect()
   */

  protected boolean login() throws IOException{
    out = sock.getOutputStream();

    synchronized(this){
      int largestSetDGNumber = level2Settings.size();
      while ((largestSetDGNumber >= 0) && !level2Settings.get(largestSetDGNumber))
        largestSetDGNumber--;
      if (largestSetDGNumber >= 0){
        StringBuffer buf = new StringBuffer("level2settings=");
        for (int i = 0; i <= largestSetDGNumber; i++){
          buf.append(level2Settings.get(i) ? "1" : "0");
        }
        sendCommand(buf.toString());
        level2SettingsSent = true;
      }

      String requestedUsername = getRequestedUsername();
      String password = getPassword();
      if ((password == null) || (password.length() == 0))
        sendCommand(requestedUsername);
      else
        sendCommand(requestedUsername + " " + password, false);
    }

    synchronized(loginLock){
      try{
        loginLock.wait(); // Wait until we receive DG_WHO_AM_I or DG_LOGIN_FAILED
      } catch (InterruptedException e){
          throw new InterruptedIOException(e.getMessage());
        } 
    }

    // We always set the login error message on error, so this is a valid way
    // to check whether there was an error.
    return getLoginErrorMessage() == null; 
  }




  /**
   * Sets the various things we need to set on login.
   */

  protected void onLogin(){
    super.onLogin();

    synchronized(this){
      // Apply any level2 changes which might have occurred when we were waiting
      // for login.
      for (int i = 0; i < requestedLevel2Settings.size(); i++){
        boolean state = requestedLevel2Settings.get(i);
        if (state != level2Settings.get(i))
          sendCommand("set-2 "+ i +" " + (state ? "1" : "0"));
      }

      sendCommand("set-quietly prompt 0");
      sendCommand("set-quietly style " + style);
      sendCommand("set-quietly interface " + interfaceVar);
    }
  }



  /**
   * If the connection is currently connected, sends the "exit" command to the
   * server. Otherwise the call is simply ignored.
   */

  public void quit(){
    if (isConnected())
      sendCommand("exit");
  }



  /**
   * Creates a new ReaderThread that will do the reading from the server.
   */

  protected Thread createReaderThread() throws IOException{
    return new ReaderThread(new BufferedInputStream(sock.getInputStream()), this);
  }


  
  /**
   * Sends the specified command to the server.
   */
   
  public void sendCommand(String command){
    sendCommand(command, true);
  }
  


  /**
   * Sends the given command to the server, optionally echoing it to System.out.
   */

  public synchronized void sendCommand(String command, boolean echo){
    if (echo && (echoStream != null))
      echoStream.println("SENDING COMMAND: " + command);

    if (!isConnected())
      throw new IllegalStateException("Not connected");
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
   * Returns the name of the rating category with the given index. Note that
   * this is unknown until the login procedure is done (which may be even after
   * login() returns, but it's pretty much guaranteed to be known before you
   * receive notification of any datagrams, except for some special ones, 
   * like DG_WHO_AM_I). Returns null if no such rating category exists, or it's
   * unknown yet.
   */

  public String getRatingCategoryName(int index){
    return (String)ratingCategoryNames.get(new Integer(index));
  }



  /**
   * Returns the name of the wild variant with the given number.Note that
   * this is unknown until the login procedure is done (which may be even after
   * login() returns, but it's pretty much guaranteed to be known before you
   * receive notification of any datagrams, except for some special ones, 
   * like DG_WHO_AM_I). Returns null if no such wild variant exists, or it's
   * unknown yet.
   */

  public String getVariantName(int number){
    return (String)variantNames.get(new Integer(number));
  }



  /**
   * This method is called by the ReaderThread when a new level2 datagram 
   * arrives from the server.
   *
   * @param datagram The level2 datagram that was received.
   *
   * @see #processDatagram(Datagram)
   */

  public final void handleDatagram(Datagram datagram){
    if (echoStream != null)
      echoStream.println(datagram);

    int id = datagram.getId();
    if ((id == Datagram.DG_WHO_AM_I) && !isLoggedIn()){
      synchronized(loginLock){
        setUsername(datagram.getString(0));
        loginLock.notify();
      }
    }
    else if ((id == Datagram.DG_LOGIN_FAILED) && !isLoggedIn()){
      synchronized(loginLock){
        setLoginErrorMessage(datagram.getString(1));
        loginLock.notify();
      }
    }
    else if (id == Datagram.DG_RATING_TYPE_KEY){
      int index = datagram.getInteger(0);
      String name = datagram.getString(1);
      ratingCategoryNames.put(new Integer(index), name);
    }
    else if (id == Datagram.DG_WILD_KEY){
      int number = datagram.getInteger(0);
      String name = datagram.getString(1);
      variantNames.put(new Integer(number), name);
    }
    else if (id == Datagram.DG_SET2){
      int dgType = datagram.getInteger(0);
      boolean state = datagram.getBoolean(1);
      if (state)
        level2Settings.set(dgType);
      else
        level2Settings.clear(dgType);
    }

    fireDatagramEvent(datagram);
  }
  
  
  
  /**
   * This method is called by the ReaderThread when a new line of plain text
   * arrives from the server.
   *
   * @param line The line that was received, '\n' not included.
   *
   * @see #processLine(String)
   */

  public final void handleLine(String line){
    if (echoStream != null)
      echoStream.println(line);
    processLine(line);
  }





  /**
   * This method is called to process a single line of text.
   *
   * @param line The line that was received, '\n' not included.
   */

  protected void processLine(String line){
    
  }




  /**
   * This method is called by the ReaderThread when the connection the server
   * is terminated.
   */

  final synchronized void handleDisconnection(){
    if (echoStream != null)
      echoStream.println("DISCONNECTED");

    if (isConnected())
      try{
        disconnect();
      } catch (IOException e){
          e.printStackTrace();
        }

    processDisconnection();
  }




  /**
   * This method is called to process disconnection from the server.
   */

  protected void processDisconnection(){

  }


}
