/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2007 Alexander Maryanovsky. All rights reserved.
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

import free.util.imagefilters.IconImageFilters;
import java.awt.Image;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * A button which, by default behaves like an icon button - it only draws the icon and text, if any,
 * has 0 margins and is not focusable. Additionally, it automatically creates variations of the icon
 * for rollover, pressed and disabled icons.
 */
public class IconButton extends JButton {

  /** Creates an <code>IconButton</code> with the specified icon. */
  public IconButton(Icon icon) {
    this(null, icon);
  }

  /** Creates an <code>IconButton</code> with the specified text and icon. */
  public IconButton(String text, Icon icon) {
    super(text, icon);
    init();
  }

  /** Creates an <code>IconButton</code> configured from the specified action. */
  public IconButton(Action action) {
    super(action);
    init();
  }

  /** Initializes the button's properties. */
  private void init() {
    SwingUtils.confIconButton(this, null);
  }

  /**
   * Sets the icon of the button. If the icon is an <code>ImageIcon</code> variants of it are
   * automatically created for the rollover, pressed and disabled icons.
   */
  @Override
  public void setIcon(Icon icon) {
    if (icon instanceof ImageIcon) {
      Image image = ((ImageIcon) icon).getImage();

      setRolloverIcon(new ImageIcon(IconImageFilters.getRollover(image)));
      setPressedIcon(new ImageIcon(IconImageFilters.getPressed(image)));
      setDisabledIcon(new ImageIcon(IconImageFilters.getDisabled(image)));
    }

    super.setIcon(icon);
  }
}
