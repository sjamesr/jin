/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
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

import java.awt.FontMetrics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import free.jin.chessclub.JinChessclubConnection;
import free.jin.console.Console;
import free.jin.console.ConsoleTextPane;
import free.util.GraphicsUtilities;


/**
 * An extension of <code>ConsoleTextPane</code> adding ICC specific
 * functionality.
 */

public class ChessclubConsoleTextPane extends ConsoleTextPane{



  /**
   * Creates a new <code>ChessclubConsoleTextPane</code> with the specified
   * <code>Console</code>.
   */

  public ChessclubConsoleTextPane(Console console){
    super(console);
    
    addComponentListener(new ComponentAdapter(){
      
      private int currentHeight = -1;
      
      public void componentResized(ComponentEvent evt){
        if (!isShowing())
          return;
        
        adjustHeight();
      }
      
      public void componentShown(ComponentEvent evt){
        adjustHeight();
      }
      
      private void adjustHeight(){
        FontMetrics metrics = GraphicsUtilities.getFontMetrics(getFont());
        JinChessclubConnection conn = 
          (JinChessclubConnection)ChessclubConsoleTextPane.this.console.getConsoleManager().getConn();
        
        int height = Math.max(5, Math.min(300, getVisibleRect().height/metrics.getHeight()));
        // We don't subtract anything from heigt/fontHeight because the server seems to
        // send a few lines less than the actual console height
        
        if (height != currentHeight){
          currentHeight = height;
          conn.sendCommandWhenLoggedIn("set-quietly height " + height);
        }
      }
      
    });
  }



  /**
   * Overrides </code>ConsoleTextPane.isWordChar(char)</code> to specify that
   * a dash character is also a word character. We do this because ICC handles
   * may include a dash and we want them to be easily selectable.
   */

  protected boolean isWordChar(char c){
    return super.isWordChar(c) || (c == '-');
  }



}
