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

package free.jin.chessclub.console;

import free.jin.console.Console;
import free.jin.console.ConsoleTextField;
import free.jin.plugin.Plugin;
import java.util.Vector;

/**
 * An extension of free.jin.console.Console which adds some ICC specific features.
 */

public class ChessclubConsole extends Console{


  /**
   * A history of people who have told us anything.
   */

  private final Vector tellers = new Vector();




  /**
   * Creates a new Console with the given user Plugin.
   */

  public ChessclubConsole(Plugin userPlugin){
    super(userPlugin);
  }



  /**
   * Creates the JTextField in which the user can input commands to be sent to
   * the server. Overrides Console.createInputComponent() since we need a special
   * JTextField for ICC.
   */

  protected ConsoleTextField createInputComponent(){
    ConsoleTextField textField = new ChessclubConsoleTextField(this);
    return textField;
  }




  /**
   * Gets called when a tell by the given player is received. This method saves
   * the name of the teller so it can be later retrieved when F9 is hit.
   */

  public void tellReceived(String teller){
    tellers.removeElement(teller);
    tellers.insertElementAt(teller, 0);
  }



  /**
   * Returns the nth (from the end) person who told us something via "tell",
   * "say" or "atell"  which went into this console. The index is 0 based. 
   * Sorry about the name of the method but I didn't think getColocutor()
   * was much better :-)
   */

  public String getTeller(int n){
    return (String)tellers.elementAt(n);
  }




  /**
   * Returns the amount of people who have told us anything so far.
   */

  public int getTellerCount(){
    return tellers.size();
  }

}
