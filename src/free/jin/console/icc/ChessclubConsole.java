/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
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

package free.jin.console.icc;

import javax.swing.text.BadLocationException;

import free.jin.console.Console;
import free.jin.console.ConsoleTextField;
import free.jin.console.ConsoleTextPane;


/**
 * An extension of free.jin.console.Console which adds some ICC specific
 * features.
 */

public class ChessclubConsole extends Console{



  /**
   * Creates a new <code>ChessclubConsole</code> to be used with the specified
   * <code>ChessclubConsoleManager</code>. 
   */

  public ChessclubConsole(ChessclubConsoleManager consoleManager){
    super(consoleManager);
  }



  /**
   * Creates the <code>ConsoleTextField</code> in which the user can input
   * commands to be sent to the server. Overrides
   * <code>Console.createInputComponent()</code> since we need a
   * special <code>ConsoleTextField</code> for ICC.
   */

  protected ConsoleTextField createInputComponent(){
    return new ChessclubConsoleTextField(this);
  }




  /**
   * Overrides <code>Console.createOutputComponent()</code> since we need a
   * special <code>ConsoleTextPane</code> for ICC.
   */

  protected ConsoleTextPane createOutputComponent(){
    return new ChessclubConsoleTextPane(this);
  }
  
  
  
  /**
   * Works around the issue with specially layed out finger noted, such as
   * "finger Live" by splitting lines with lots of spaces followed by a ':'.
   * See http://sourceforge.net/tracker/index.php?func=detail&aid=675197&group_id=50386&atid=459537
   * for more information.
   */
   
  protected void addToOutputImpl(String text, String textType) throws BadLocationException{
    // As of 02.06.2005, the ICC server does the wrapping thing itself 
//    String delim = "        :";
//    int index;
//    while ((index = text.indexOf(delim)) != -1){
//      String line = TextUtilities.trimRight(text.substring(0, index));
//      super.addToOutputImpl(line, textType);
//      text = text.substring(index + delim.length() - 3);
//    }
    
    super.addToOutputImpl(text, textType);
  }

  

}
