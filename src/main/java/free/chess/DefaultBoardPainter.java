/**
 * The chess framework library. More information is available at http://www.jinchess.com/. Copyright
 * (C) 2002 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The chess framework library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * <p>The chess framework library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with the chess
 * framework library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite
 * 330, Boston, MA 02111-1307 USA
 */
package free.chess;

import java.awt.Component;
import java.awt.Graphics;

/**
 * The default <code>BoardPainter implementation used by <code>JBoard</code>.
 */
public class DefaultBoardPainter implements BoardPainter {

  /** The delegate. */
  private final BoardPainter delegate = new PlainBoardPainter();

  /** Since <code>DefaultBoardPainter</code> is immutable, simply returns </code>this</code>. */
  @Override
  public BoardPainter freshInstance() {
    return new DefaultBoardPainter();
  }

  /** Paints the board. */
  @Override
  public void paintBoard(Graphics g, Component component, int x, int y, int width, int height) {
    delegate.paintBoard(g, component, x, y, width, height);
  }
}
