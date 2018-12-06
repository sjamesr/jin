package free.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * An "arrow" icon, such as the one frequently used to indicate a drop down menu.
 */
public class ArrowIcon extends ColoredIcon {

  /**
   * Creates a new <code>ArrowIcon</code> of the specified size and color.
   */
  public ArrowIcon(int size, Color color) {
    super(new Dimension(size, size), color);
  }

  /**
   * Draws the "arrow".
   */
  @Override
  public void paintShape(Component c, Graphics g, int x, int y) {
    int halfSize = getIconWidth() / 2;
    int yOffset = (halfSize + 1) / 2;
    g.fillPolygon(
        new int[] {x, x + halfSize, x + 2 * halfSize},
        new int[] {y + yOffset, y + yOffset + halfSize, y + yOffset},
        3);
  }
}
