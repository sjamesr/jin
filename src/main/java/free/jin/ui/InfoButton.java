package free.jin.ui;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import free.util.imagefilters.IconImageFilters;

/**
 * A button with a question mark icon. This should be used as the standard "informational" button in
 * Jin.
 *
 * @author Maryanovsky Alexander
 */
public class InfoButton extends JButton {

  /**
   * The info button icon image.
   */
  private static final Image ICON_IMAGE =
      Toolkit.getDefaultToolkit().getImage(InfoButton.class.getResource("info.png"));

  /**
   * The info button icon.
   */
  private static final Icon ICON = new ImageIcon(ICON_IMAGE);

  /**
   * The disabled info button icon.
   */
  private static final Icon DISABLED_ICON = new ImageIcon(IconImageFilters.getDisabled(ICON_IMAGE));

  /**
   * Creates a new <code>InfoButton</code>.
   */
  public InfoButton() {
    setIcon(ICON);
    setDisabledIcon(DISABLED_ICON);

    setFocusable(false);
    setMargin(new Insets(0, 0, 0, 0));

    // setMargin isn't enough for Ocean or Windows L&Fs
    Dimension legendButtonSize = new Dimension(ICON.getIconWidth() + 4, ICON.getIconHeight() + 4);
    setMinimumSize(legendButtonSize);
    setPreferredSize(legendButtonSize);
    setMaximumSize(legendButtonSize);
  }
}
