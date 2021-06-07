/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2002 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The utillib library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * <p>The utillib library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with utillib
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package free.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

/** A subclass of <code>ColoredIcon</code> which draws a rectangle. */
public class RectangleIcon extends ColoredIcon {

  /** Creates a new RectangleIcon of the given size and Color. */
  public RectangleIcon(Dimension size, Color color) {
    super(size, color);
  }

  /** Fills the rectangle. */
  @Override
  public void paintShape(Component component, Graphics g, int x, int y) {
    g.fillRect(x, y, getIconWidth(), getIconHeight());
  }
}
