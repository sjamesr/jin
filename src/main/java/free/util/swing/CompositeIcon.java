package free.util.swing;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/** An icon which composes two specified icons. */
public class CompositeIcon implements Icon {

  /** The background icon. */
  private final Icon backgroundIcon;

  /** The foreground icon. */
  private final Icon foregroundIcon;

  /**
   * Creates a new <code>CompositeIcon</code> with the specified background and foreground icons.
   */
  public CompositeIcon(Icon backgroundIcon, Icon foregroundIcon) {
    if (backgroundIcon == null)
      throw new IllegalArgumentException("backgroundIcon may not be null");
    if (foregroundIcon == null)
      throw new IllegalArgumentException("foregroundIcon may not be null");

    this.backgroundIcon = backgroundIcon;
    this.foregroundIcon = foregroundIcon;
  }

  /** Returns the maximum of the widths of the composed icons. */
  @Override
  public int getIconWidth() {
    return Math.max(backgroundIcon.getIconWidth(), foregroundIcon.getIconWidth());
  }

  /** Returns the maximum of the heights of the composed icons. */
  @Override
  public int getIconHeight() {
    return Math.max(backgroundIcon.getIconHeight(), foregroundIcon.getIconHeight());
  }

  /** Paints the composed icons. */
  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    backgroundIcon.paintIcon(c, g, x, y);
    foregroundIcon.paintIcon(c, g, x, y);
  }
}
