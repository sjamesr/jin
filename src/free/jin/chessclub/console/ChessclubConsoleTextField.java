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

import free.jin.console.ConsoleTextField;
import java.awt.event.KeyEvent;


/**
 * An extension of ConsoleTextField which implements some ICC specific features.
 */

public class ChessclubConsoleTextField extends ConsoleTextField{



  /**
   * The index of the current teller, or -1 if none.
   */

  private int tellerIndex = -1;




  /**
   * Creates a new ChessclubConsoleTextField which will be a part of the given
   * ChessclubConsole.
   */

  public ChessclubConsoleTextField(ChessclubConsole console){
    super(console);
  }



  /**
   * Implements the following functionality:
   * <UL>
   *   <LI> On F9 - Inserts "tell <lastTeller>! " to the beginning of the
   *        textfield if it's not already there.
   * </UL>
   */

  protected void processComponentKeyEvent(KeyEvent evt){
    if (evt.getID()==KeyEvent.KEY_PRESSED){
      switch(evt.getKeyCode()){
        case KeyEvent.VK_F9:
          ChessclubConsole console = (ChessclubConsole)this.console;
          int traversedTellerCount = Integer.parseInt(console.getProperty("f9-traverse-list-size", "5"));

          if (evt.isAltDown()){
            tellerIndex--;
            if (tellerIndex < 0)
              tellerIndex = Math.min(traversedTellerCount, console.getTellerCount()) - 1;
          }
          else{
            tellerIndex++;
            if ((tellerIndex == console.getTellerCount()) || (tellerIndex == traversedTellerCount))
              tellerIndex = 0;
          }

          setText("tell "+console.getTeller(tellerIndex)+"! ");
          break;
        default:
          if (evt.getKeyChar() != KeyEvent.CHAR_UNDEFINED)
            tellerIndex = -1;
          break;
      }
    }

    super.processComponentKeyEvent(evt);
  }

}
