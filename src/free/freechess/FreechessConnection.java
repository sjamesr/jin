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

package free.freechess;

import java.io.*;
import jregex.*;
import java.util.StringTokenizer;


/**
 * This class implements an easy way to communicate with a freechess.org server.
 * It provides parsing of messages sent by the server and allows receiving
 * notifications of various events in an easy manner.
 */

public class FreechessConnection extends free.util.Connection implements Runnable{



  /**
   * A regular expression string matching a FICS username.
   */

  private static final String usernameRegex = "[A-z]{3,17}";



  /**
   * The lock we wait on when logging.
   */

  private final Object loginLock = new String("Login Lock");




  /**
   * The OutputStream to the server.
   */
  
  private OutputStream out;




  /**
   * The current board sending "style".
   */

  private int style = 1;




  /**
   * The value we're supposed to assign to the interface variable during login.
   */

  private String interfaceVar = "Java freechess.org library by Alexander Maryanovsky";




  /**
   * Creates a new FreechessConnection to the given hostname, on the given port,
   * with the given username and password.
   */

  public FreechessConnection(String hostname, int port, String username, String password){
    super(hostname, port, username, password);
  }




  /**
   * Creates a new ReaderThread that will do the reading from the server.
   */

  protected Thread createReaderThread() throws IOException{
    return new Thread(this);
  }




  /**
   * Sets the style. If the ChessclubConnection is already logged in, then
   * a "set style <style>" command is send immediately, otherwise, the setting
   * is saved and sent immediately after logging in.
   */

  public synchronized final void setStyle(int style){
    this.style = style;
    if (isLoggedIn())
      sendCommand("set style "+style);
  }




  /**
   * Sets the interface variable to have the given value. This works only if the
   * FreechessConnection is not logged on yet, otherwise, throws an 
   * IllegalArgumentException. The actual interface variable will be set during 
   * the login procedure.
   */

  public final synchronized void setInterface(String interfaceVar){
    if (isLoggedIn())
      throw new IllegalStateException();

    this.interfaceVar = interfaceVar;
  }





  /**
   * Logs in.
   */

  protected void login() throws IOException{
    out = sock.getOutputStream();

    sendCommand(getRequestedUsername());
    sendCommand(getPassword());

    synchronized(loginLock){
      try{
        loginLock.wait(); // Wait until we receive the login line.
      } catch (InterruptedException e){
          throw new InterruptedIOException(e.getMessage());
        } 
    }
  }




  /**
   * Sets the various things we need to set on login.
   */

  protected void onLogin(){
    super.onLogin();

    sendCommand("set style "+style);
    sendCommand("set interface "+interfaceVar);
    sendCommand("iset nowrap 1");
    sendCommand("iset lock 1");
  }





  /**
   * Sends the given command to the server. You should not include the end of
   * line symbol in the command.
   */

  public void sendCommand(String command){
    if (!isConnected())
      throw new IllegalStateException("Not connected");

    System.out.print(command+": ");

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
   * This method is called when a line of text that isn't identified as some
   * known type of information arrives from the server.
   */

  protected void processLine(String line){}




  /**
   * This method is called to process disconnection from the server.
   */

  protected void processDisconnection(){}





  /**
   * This method is called by the reader thread when the connection the server
   * is terminated.
   */

  private synchronized void handleDisconnection(){
    if (isConnected())
      try{
        disconnect();
      } catch (IOException e){
          e.printStackTrace();
        }

    processDisconnection();
  }




  /**
   * This method is called by the reader thread when a line of text arrives from
   * the server. The method is responsible for determining the type of the
   * information, parsing it and sending it for further processing.
   */

  private void handleLine(String line){
    if (handleLogin(line))
      return;
    if (handlePersonalTell(line))
      return;

    processLine(line);
  }




  /**
   * The regular expression matching login confirmation lines.
   * Example: "**** Starting FICS session as AlexTheGreat ****"
   */

  private static final Pattern loginPattern =
    new Pattern("^\\*\\*\\*\\* Starting FICS session as ("+usernameRegex+")\\(?.*\\)? \\*\\*\\*\\*");




  /**
   * Called to determine if the given line of text is a login confirming line
   * and to handle that information if it is. Returns <code>true</code> if the
   * given line is a login confirming line, otherwise, returns
   * <code>false</code>.
   */

  private boolean handleLogin(String line){
    Matcher matcher = loginPattern.matcher(line);
    if (!matcher.find())
      return false;

    synchronized(loginLock){
      setUsername(matcher.group(1));
      loginLock.notify();
    }

    return true;
  }




  /**
   * The regular expression matching personal tells.
   */

  private static final Pattern ptellPattern = 
    new Pattern("("+usernameRegex+")(\\(.*\\))? tells you: (.*)");




  /**
   * Called to determine whether the given line of text is a personal tell and
   * to further process it if it is.
   */

  private boolean handlePersonalTell(String line){
    Matcher matcher = ptellPattern.matcher(line);
    if (!matcher.find())
      return false;

    String username = matcher.group(1);
    String titles = matcher.group(2);
    String message = matcher.group(3);

    processPersonalTell(username, titles, message);

    return true;
  }




  /**
   * This method is called when a personal tell arrives. The <code>titles</code>
   * variable contains the titles of the player who send the personal tell in
   * the "(T1)(T2)" format, or <code>null</code> if that player has no titles.
   */

  protected void processPersonalTell(String username, String titles, String message){}





  /**
   * The run() method. This is called by the reader thread. Continuously reads
   * lines of text from the server and sends them for further processing.
   */

  public void run(){
    try{
      final String prompt = "fics% ";
      final int promptLength = prompt.length();

      InputStream in = new BufferedInputStream(sock.getInputStream());

      StringBuffer buf = new StringBuffer();
      int b;
      mainLoop: while ((b = in.read()) != -1){
        System.out.print((char)b);
        if (b == '\r')
          continue;
        else if (b == '\n'){
          String s = buf.toString();
          while (s.startsWith(prompt)){
            s = s.substring(promptLength);
            if (s.length() == 0) // An all prompt line
              continue mainLoop;
          }
          execRunnable(new HandleLineRunnable(s));
          buf.setLength(0);
        }
        else{
          buf.append((char)b);
        }
      }
    } catch (IOException e){}
    execRunnable(new Runnable(){

      public void run(){
        handleDisconnection();
      }

    });
  }




  /**
   * A runnable which takes a String and invokes handleLine(String) with it when
   * run. Used by the reader thread to communicate with the data handling code.
   */

  private class HandleLineRunnable implements Runnable{


    /**
     * The string.
     */

    private final String line;




    /**
     * Creates a new HandleLineRunnable with the specified string.
     */

    public HandleLineRunnable(String line){
      this.line = line;
    }




    /**
     * Calls the outer class' handleLine method with the string given in the
     * constructor.
     */

    public void run(){
      handleLine(line);
    }


  }



}

