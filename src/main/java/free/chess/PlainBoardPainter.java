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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

/** An implementation of BoardPainter which fills the squares with a solid color. */
public final class PlainBoardPainter extends AbstractColoredBoardPainter {

  /**
   * Creates a new <code>PlainBoardPainter</code> which paints squares with the given light square
   * color and dark square color.
   */
  public PlainBoardPainter(Color lightColor, Color darkColor) {
    super(lightColor, darkColor);
  }

  /**
   * Creates a new <code>PlainBoardPainter</code> with some default colors for light and dark
   * squares.
   */
  public PlainBoardPainter() {
    this(new Color(255, 207, 144), new Color(143, 96, 79));
  }

  /** Returns a new <code>PlainBoardPainter</code>. */
  @Override
  public BoardPainter freshInstance() {
    return new PlainBoardPainter();
  }

  /** Paints the board at the given location on the given Graphics scaled to the given size. */
  @Override
  public void paintBoard(Graphics g, Component component, int x, int y, int width, int height) {
    int squareWidth = width / 8;
    int squareHeight = height / 8;
    Color lightColor = getLightColor();
    Color darkColor = getDarkColor();

    for (int i = 0; i < 8; i++)
      for (int j = 0; j < 8; j++) {
        if ((i + j) % 2 == 0) g.setColor(lightColor);
        else g.setColor(darkColor);

        g.fillRect(x + i * squareWidth, y + j * squareHeight, squareWidth, squareHeight);
      }
  }
}
