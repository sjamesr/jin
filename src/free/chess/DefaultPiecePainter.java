/**
 * The chess framework library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The chess framework library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The chess framework library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the chess framework library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.chess;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;


/**
 * The default PiecePainter implementation used in JBoard.
 * This PiecePainter supports the pieces in all wild variants defined in the
 * free.chess package and all its subpackages. This class currently uses
 * <code>free.chess.EboardVectorPiecePainter</code> to paint the pieces, but
 * this is an implementation detail which you should not rely on. The artwork
 * belongs solely to the author(s) of eboard. For additional information see
 * <A HREF="http://eboard.sourceforge.net/">the eboard website</A>
 */

public class DefaultPiecePainter implements PiecePainter{
  
  
  
  /**
   * The delegate.
   */
  
  private PiecePainter delegate = new EboardVectorPiecePainter();
  
  
  
  /**
   * Since <code>DefaultPiecePainter</code> is immutable, simply returns
   * </code>this</code>.
   */
  
  public PiecePainter freshInstance(){
    return this;
  }
  
  
  
  /**
   * Paints the piece.
   */

  public void paintPiece(Piece piece, Graphics g, Component component, Rectangle rect, boolean shaded){
    delegate.paintPiece(piece, g, component, rect, shaded);
  }
  
  
  
}
