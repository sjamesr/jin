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

package free.util;

import java.io.IOException;
import java.net.Socket;


/**
 * This is the base class for all classes that define a connection to some server.
 * It implements the framework for the connection.
 */

public abstract class Connection{


  /**
   * The requested username, note that the actual username assigned by the server
   * may be unknown until the login procedure is finished.
   */ 

  private final String requestedUsername;



  /**
   * The actual username, this may be unknown (null) until the login procedure
   * is done.
   */

  private String username = null;



  /**
   * The password of the account.
   */

  private final String password;


  

  /**
   * The hostname of the server to which we will connect.
   */

  private final String hostname;




  /**
   * The port on which we will connect to the server.
   */

  private final int port;




  /**
   * Are we currently connected to the server. The Connection is initially 
   * unconnected, it becomes connected after the connect() method returns.
   * It becomes disconnected again when either the disconnect method is
   * called or when connection to the server is lost.
   */
  
  private volatile boolean isConnected = false;




  /**
   * Has the login procedure ended already?
   */
   
  private volatile boolean isLoggedIn = false;



  /**
   * The socket to the server.
   */

  protected Socket sock;




  /**
   * The thread that reads information from the server.
   */

  private Thread readerThread;




  /**
   * Creates a new Connection. Note that the Connection is initially unconnected,
   * after creating the session, you can adjust the various Connection settings
   * and then call the connect() method.
   *
   * @param hostname The server we will connect to.
   * @param port The port we will connect on.
   * @param username The name of the account, note that the actual username may be
   * assigned by the server and will thus be known only after the login procedure
   * is done.
   * @param password The password of the account.
   */

  public Connection(String hostname, int port, String requestedUsername, String password){
    this.hostname = hostname;
    this.port = port;
    this.requestedUsername = requestedUsername;
    this.password = password;
  }
 




  /**
   * Connects to the server.  Note that this method blocks until the connection
   * is established and the login procedure is finished. Also note that since
   * communication with the reader thread is done via the execRunnable(Runnable)
   * method, and in order to complete the login procedure, communication with it
   * *is* required, this method should NOT be called in the same thread
   * execRunnable(Runnable) executes the runnable. Doing that will cause this 
   * method to never return.
   *
   * @throws IOException if an I/O error occured when connecting to the server.
   * @throws IllegalStateException If a connection was already established.
   *
   * @see #isConnected()
   * @see #disconnect()
   */

  public synchronized void connect() throws IOException{
    if (isConnected())
      throw new IllegalStateException();
    sock = createSocket(hostname,port);

    isConnected = true;

    readerThread = createReaderThread();
    readerThread.start();

    login();
    if (getUsername()==null)
      throw new Error("The login() method MUST assign a username");
    isLoggedIn = true;
  }




  /**
   * Goes through the login procedure. This method is responsible for setting 
   * the username assigned by the server.
   */

  protected abstract void login() throws IOException;





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

  protected final void setUsername(String username){
    if (this.username!=null)
      throw new IllegalStateException("A username may only be assigned once");

    this.username = username;
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
   * Returns the hostname of the server.
   */

  public String getHostname(){
    return hostname;
  }




  /**
   * Returns the port on which we are connecting.
   */

  public int getPort(){
    return port;
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
    sock = null;
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
   * in the current thread.
   */

  public void execRunnable(Runnable runnable){
    runnable.run();
  }



}
