/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util;

import java.io.IOException;
import java.net.Socket;


/**
 * This is the base class for all classes that define a connection to some
 * server. It implements the framework for the connection.
 */

public abstract class Connection{


  /**
   * The requested username, note that the actual username assigned by the
   * server may be unknown until the login procedure is finished.
   */ 

  private final String requestedUsername;



  /**
   * The actual username, this may be unknown (null) until the login procedure
   * is done.
   */

  private volatile String username = null;



  /**
   * The password of the account.
   */

  private final String password;

  

  /**
   * Are we currently connected to the server. The Connection is initially 
   * unconnected, it becomes connected after the socked to the server is
   * created. It becomes disconnected again when either the disconnect method is
   * called or when connection to the server is lost.
   */
  
  private volatile boolean isConnected = false;



  /**
   * Has the login procedure ended already?
   */
   
  private volatile boolean isLoggedIn = false;



  /**
   * The login error message.
   */

  private volatile String loginErrorMessage;



  /**
   * The socket to the server.
   */

  protected volatile Socket sock;



  /**
   * The thread that reads information from the server.
   */

  private Thread readerThread;



  /**
   * Creates a new Connection.
   *
   * @param username The name of the account, note that the actual username may
   * be assigned by the server and will thus be known only after the login
   * procedure is done.
   * @param password The password of the account.
   */

  public Connection(String requestedUsername, String password){
    this.requestedUsername = requestedUsername;
    this.password = password;
  }



  /**
   * Connects and logs in to the server. Returns <code>true</code> if the
   * procedure finished successfully, <code>false</code> otherwise. The reason
   * for login failure may be obtained via the
   * <code>getLoginErrorMessage()</code> method. Note that the connection may
   * be reused if this method fails (either by throwing an exception or
   * returning false), by invoking this method again.
   * <strong>IMORTANT:</strong> This method blocks until the connection is
   * established and the login procedure is finished. Since communication with
   * the reader thread is done via the <code>execRunnable(Runnable)</code>
   * method, and in order to complete the login procedure, communication with it
   * <strong>is<strong> required, this method should NOT be called in the same
   * thread <code>execRunnable(Runnable)</code> executes the runnable. Doing
   * that will cause this method to never return.
   *
   * @param hostname The name of the host to connect to.
   * @param port The port to connect on.
   *
   * @throws IOException if an I/O error occured when connecting to the server.
   * @throws IllegalStateException If a connection was already established.
   *
   * @return whether login succeeded.
   *
   * @see #isConnected()
   * @see #disconnect()
   */

  public boolean connectAndLogin(String hostname, int port) throws IOException{
    synchronized(this){
      if (isConnected())
        throw new IllegalStateException();

      sock = createSocket(hostname, port);

      isConnected = true;
    }

    readerThread = createReaderThread();
    readerThread.start();

    isLoggedIn = login();

    if (!isLoggedIn){
      readerThread = null;
      sock.close();
      isConnected = false;
      return false;
    }
    else if (getUsername() == null)
      throw new IllegalStateException("The login() method MUST set the username if the login procedure finishes successfully");

    execRunnable(new Runnable(){
      public void run(){
        onLogin();
      }
    });

    return true;
  }




  /**
   * Goes through the login procedure and returns <code>true</code> if it's
   * successful, false otherwise. This method is responsible for setting 
   * the username assigned by the server or the login error message via the
   * <code>setUsername(String)</code> and <code>setLoginErrorMessage</code>
   * methods respectively.
   */

  protected abstract boolean login() throws IOException;




  /**
   * Creates and connects a socket to the given host on the given
   * port.
   */

  protected Socket createSocket(String hostname, int port) throws IOException{
    return new Socket(hostname,port);
  }





  /**
   * Creates (but not starts) a new Thread that will do the reading from the server.
   */

  protected abstract Thread createReaderThread() throws IOException;



  
  /**
   * Returns true if this Connection is logged in.
   */

  public boolean isLoggedIn(){
    return isLoggedIn;
  } 



  /**
   * This method is called right after the login, in the handler thread (the
   * one in which things are called when you invoke
   * <code>execRunnable()</code>).
   * The default implementation does nothing.
   */

  protected void onLogin(){}



  /**
   * Returns the hostname to which we're connected, or were connected if we've
   * been disconnected. Returns <code>null</code> if we haven't been connected
   * yet/
   */

  public String getHostname(){
    return sock == null ? null : sock.getInetAddress().getHostName();
  }



  /**
   * Returns the (remote) port on which we're connected, or were connected if
   * we've been disconnected. Returns -1 if we're not connected.
   */

  public int getPort(){
    return sock == null ? -1 : sock.getPort();
  }



  /**
   * Returns the username assigned by the server, this may be null until the 
   * session is logged on.
   *
   * @see #isLoggedIn()
   */

  public String getUsername(){
    return username;
  }




  /**
   * Sets the username assigned by the server. This method may only be called once,
   * and it should be called from the login() method.
   *
   * @see #getUsername()
   */

  protected final synchronized void setUsername(String username){
    if (this.username != null)
      throw new IllegalStateException("A username may only be assigned once");

    this.username = username;
  }




  /**
   * Returns the login error message. Note that this may return null even if the
   * login failed.
   *
   * @throws IllegalStateException if login did not fail.
   */

  public String getLoginErrorMessage(){
    if (isLoggedIn())
      throw new IllegalStateException("The login did not fail");

    return loginErrorMessage;
  }




  /**
   * Sets the login error message to the specified string.
   */

  protected final void setLoginErrorMessage(String message){
    if (loginErrorMessage != null)
      throw new IllegalStateException("The login error message may only be assigned once");
    if (!isConnected())
      throw new IllegalStateException("Must be connected in order to assign login error message");
    if (isLoggedIn())
      throw new IllegalStateException("Must not be logged in in order to assign login error message");

    this.loginErrorMessage = message;
  }






  /**
   * Returns the username requested by the user. This is never null, but note
   * that the server may assign a different username from the requested one.
   */

  public String getRequestedUsername(){
    return requestedUsername;
  }




  /**
   * Returns the password of the account.
   */

  protected String getPassword(){
    return password;
  }




  /**
   * Disconnects from the server.
   *
   * @throws IOException if an I/O error occured when disconnecting from the 
   * server
   *
   * @see #isConnected()
   * @see #connect()
   */
  
  public synchronized void disconnect() throws IOException{
    if (!isConnected())
      throw new IllegalArgumentException();

    isConnected = false;
    isLoggedIn = false;
    readerThread = null;
    sock.close();
  }





  /**
   * Returns <code>true</code> if this Connection is connected to the server, false otherwise.
   *
   * @see #connect()
   * @see #disconnect()
   */

  public boolean isConnected(){
    return isConnected;
  }



  /**
   * This method should be called by the ReaderThread to execute given code
   * on the data handling thread. This is needed to allow graphical applications
   * using the Connection to make sure that their handling code is executed on
   * the AWT thread thus avoiding multi-threading problems.
   * The default implementation of this method, meant for non-graphical
   * applications, such as bots, simply invokes the run() method of the Runnable
   * in the current thread. Note that it's important that the runnable is always
   * run in <B>one</B> thread, so you can't do load balancing from here.
   */

  public void execRunnable(Runnable runnable){
    runnable.run();
  }


}
