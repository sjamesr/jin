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

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Implements a TCP/IP bounce utility (proxy). You run bounce specifying which
 * port to listen on, which host and on which port to connect to and it will
 * act as a proxy relaying information between anyone who connects to it and the
 * specified server.
 */

public class Bounce{


  
  /**
   * The main method.
   */

  public static void main(String [] args){
    if (args.length < 3){
      printUsage();
      System.exit(1);
    }

    int localPort;
    try{
      localPort = Integer.parseInt(args[0]);
    } catch (NumberFormatException e){
        System.err.println("Bad local port value: "+args[0]);
        printUsage();
        System.exit(2);
        return;
      }

    String hostname = args[1];

    int remotePort;
    try{
      remotePort = Integer.parseInt(args[2]);
    } catch (NumberFormatException e){
        System.err.println("Bad remote port value: "+args[2]);
        printUsage();
        System.exit(3);
        return;
      }

    try{
      ServerSocket ssock = new ServerSocket(localPort);
      Socket incomingSock = ssock.accept();
      System.out.println("Connection accepted");
      Socket outgoingSock = new Socket(hostname, remotePort);

      InputStream incomingIn = incomingSock.getInputStream();
      OutputStream incomingOut = incomingSock.getOutputStream();
      InputStream outgoingIn = outgoingSock.getInputStream();
      OutputStream outgoingOut = outgoingSock.getOutputStream();

      PumpThread t1 = new PumpThread(incomingIn, outgoingOut);
      PumpThread t2 = new PumpThread(outgoingIn, incomingOut);
      t1.start();
      t2.start();
      new ProcessKillerThread(t1).start();
      new ProcessKillerThread(t2).start();
    } catch (IOException e){
        e.printStackTrace();
        System.exit(3);
      }
  }



  /**
   * Dumps usage information to the standard error stream.
   */

  private static void printUsage(){
    System.err.println("Bounce Utility");
    System.err.println("Copyright (C) 2002 Alexander Maryanovsky");
    System.err.println();
    System.err.println("Usage: java free.util.Bounce localPort hostname remotePort");
    System.out.println();
    System.out.println("Version 1.00 - 18 Aug. 2002");
  }




  /**
   * A thread which waits for the given thread to die and then calls System.exit(0);
   */

  private static class ProcessKillerThread extends Thread{



    /**
     * The thread we're to wait on.
     */

    private final Thread target;




    /**
     * Creates a new ProcessKillerThread with the given Thread to wait on.
     */

    public ProcessKillerThread(Thread target){
      super("ProcessKillerThread("+target.getName()+")");

      this.target = target;

      setDaemon(true);
    }




    /**
     * Waits for the target thread to die, prints a message and calls
     * <code>System.exit(0)</code>
     */

    public void run(){
      try{
        target.join();
      } catch (InterruptedException e){
          e.printStackTrace();
        }

      System.err.println("Connection died");
      System.exit(0);
    }


  }


}