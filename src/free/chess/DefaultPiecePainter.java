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

package free.chess;

import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.image.ImageObserver;
import free.util.ImageUtilities;


/**
 * The default PiecePainter implementation used in JBoard.
 * This PiecePainter supports the pieces in all wild variants defined in the
 * free.chess package and all its subpackages. This class currently uses
 * free.chess.art.EboardVectorPiecePainter to paint the pieces, but this is
 * an implementation detail which you should not rely on. The artwork belongs
 * solely to the author(s) of eboard. For additional information see
 * <A HREF="http://eboard.sourceforge.net/">the eboard website</A>
 */

public class DefaultPiecePainter implements PiecePainter{


  
  /**
   * The piece painter we're exploiting.
   */

  private final PiecePainter painter;



  /**
   * Creates a new DefaultPiecePainter.
   */

  public DefaultPiecePainter(){
    painter = new free.chess.art.EboardVectorPiecePainter();
  }




  /**
   * Simply delegates to the exploited piece painter.
   */

  public void scaleHint(int width, int height){
    painter.scaleHint(width, height);
  }




  /**
   * Returns the preferred piece size of the exploited piece painter.
   */

  public Dimension getPreferredPieceSize(){
    return painter.getPreferredPieceSize();
  }




  /**
   * Delegates painting to the exploited piece painter.
   */

  public void paintPiece(Piece piece, Graphics g, ImageObserver imageObserver, int x, int y, int width, int height){
    painter.paintPiece(piece, g, imageObserver, x, y, width, height);
  }
  

}
