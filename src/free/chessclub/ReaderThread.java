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

package free.chessclub;

import java.io.*;
import free.chessclub.level2.*;
import free.util.SafeRunnable;


/**
 * A thread that keeps reading data from the server. There are 2 possible
 * types of data:
 * <OL>
 *   <LI> A simple string ending with '\n' (line).
 *   <LI> A level2 datagram.
 * </OL>
 * The thread will read each data type, parse it if necessary and
 * delegate handling it to a ChessclubConnection (handler).
 * When a line of text is encountered, it is read and the
 * {@link ChessclubConnection#handleLine(String)} method of the handler is 
 * invoked. When a level2 datagram is encountered, it is read, parsed as a 
 * level2 datagram and the {@link ChessclubConnection#handleDatagram(Datagram)} 
 * of the handler is invoked. <p>
 * <B>IMPORTANT</B> The methods of the handler aren't invoked directly - instead,
 * the ChessclubConnection.execRunnable(Runnable) method is called with a Runnable
 * object that invokes the handler's methods. This allows graphical applications to
 * post that runnable on the AWT thread, thus avoiding mutithreading problems and
 * non-graphical applications to invoke the Runnable's run() method directly. 
 *
 * @see free.chessclub.ChessclubConnection
 * @see free.chessclub.level2.Datagram
 */

public class ReaderThread extends Thread{ 


  /**
   * The InputStream we're reading the data from.
   */

  private final PushbackInputStream in; 


  /**
   * The ChessclubConnection that will be handling the data this thread reads from
   * the server.
   */

  private final ChessclubConnection handler;


  /**
   * The amount of ReaderThreads, used to give an appropriate name to
   * the thread.
   */

  private static int threadCount = 0;



  /**
   * Creates a new ReaderThread which will read from the given 
   * InputStream and will delegate the data to the given ChessclubConnection.
   *
   * @param in The InputStream from which the data will be read.
   * @param handler The ChessclubConnection that this ReaderThread
   * will delegate handling the data to.
   */

  public ReaderThread(InputStream in, ChessclubConnection handler){
    super("ReaderThread-"+(threadCount++));
    if (in==null)
      throw new IllegalArgumentException("Null InputStream");
    if (handler==null)
      throw new IllegalArgumentException("Null handler");

    this.in = new PushbackInputStream(in);
    this.handler = handler;
  }





  /**
   * This method does the actual reading from the server and delegating
   * the data to the handler. When the end of the input stream is reached or
   * an IOException is thrown, the {@link ChessclubConnection#handleDisconnection()} method of the 
   * handler is invoked.
   * All handler method invocations are done via ChessclubConnection.execRunnable(Runnable)
   * (defined at {@link free.util.Connection#execRunnable(java.lang.Runnable)}
   */

  public void run(){
    StringBuffer buf = new StringBuffer();
    StringBuffer dgBuf = new StringBuffer();
    try{
      outerLoop: while (handler.isConnected()){
        buf.setLength(0);
        int b = 0;
        while ((char)b!='\n'){
          b = in.read();

          if (b<0){
            handler.execRunnable(new SafeRunnable(){
              public void safeRun(){
                handler.handleDisconnection();
              }
            });
            return;
          }
          buf.append((char)b);


          // Level2 parsing.
          if (b==Datagram.DG_DELIM){
            dgBuf.setLength(0);
            dgBuf.append((char)b);
            buf.setLength(buf.length()-1);
            while (true){
              b = in.read();
              if (b<0){
                handler.execRunnable(new SafeRunnable(){
                  public void safeRun(){
                    handler.handleDisconnection();
                  }
                });
                return;
              }
              if (b==Datagram.DG_DELIM){
                int c = in.read();
                if (Datagram.DG_END.equals(""+(char)b+(char)c)){
                  dgBuf.append(Datagram.DG_END);
                  final Datagram dg = Datagram.parseDatagram(dgBuf.toString());
                  handler.execRunnable(new SafeRunnable(){
                    public void safeRun(){
                      handler.handleDatagram(dg);
                    }
                  });
                  break;
                }
                else{
                  in.unread(c);
                }
              }
              dgBuf.append((char)b);  
            }
          }
          else{
            if (b==7) // Ignore beeps.
              buf.setLength(buf.length()-1);

            // Ignore the prompt, remove this line if it's possible to disable it
            if (buf.toString().equals("aics% ")) 
              continue outerLoop;
          }
        }
        buf.setLength(buf.length()-1);// Remove the '\n'
        final String line = buf.toString();
        handler.execRunnable(new SafeRunnable(){
          public void safeRun(){
            handler.handleLine(line);
          }
        });

      }
    } catch (IOException e){
        if (handler.isConnected())
          e.printStackTrace();
        handler.execRunnable(new SafeRunnable(){
          public void safeRun(){
            handler.handleDisconnection();
          }
        });
         
      }
  }

}
