/**
 * The utillib library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2008 Alexander Maryanovsky. All rights reserved.
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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

/**
 * A widget with two buttons - one for adding and one for removing.
 *
 * @author Maryanovsky Alexander
 */
public class AddRemoveButtons extends JComponent {

  /** The base image for the "add" button. */
  private static final Image ADD_IMAGE =
      Toolkit.getDefaultToolkit().getImage(AddRemoveButtons.class.getResource("images/add.png"));

  /** The icon for the "add" button. */
  private static final Icon ADD_ICON = new ImageIcon(ADD_IMAGE);

  /** The disabled icon for the "add" button. */
  private static final Icon DISABLED_ADD_ICON =
      new ImageIcon(IconImageFilters.getDisabled(ADD_IMAGE));

  /** The base image for the "remove" button. */
  private static final Image REMOVE_IMAGE =
      Toolkit.getDefaultToolkit().getImage(AddRemoveButtons.class.getResource("images/remove.png"));

  /** The icon for the "remove" button. */
  private static final Icon REMOVE_ICON = new ImageIcon(REMOVE_IMAGE);

  /** The disabled icon for the "remove" button. */
  private static final Icon DISABLED_REMOVE_ICON =
      new ImageIcon(IconImageFilters.getDisabled(REMOVE_IMAGE));

  /** The "add" button. */
  private final JButton add;

  /** The "remove" button. */
  private final JButton remove;

  /** Creates a new <code>AddRemoveButtons</code> widget. */
  public AddRemoveButtons() {
    this.add = new JButton();
    this.remove = new JButton();

    configureButtons();
    createUi();
  }

  /**
   * Creates a new <code>AddRemoveButtons</code> widget with the specified actions which are invoked
   * when the buttons are clicked.
   */
  public AddRemoveButtons(Action addAction, Action removeAction) {
    this.add = new JButton(addAction);
    this.remove = new JButton(removeAction);

    configureButtons();
    createUi();
  }

  /**
   * Creates a new <code>AddRemoveButtons</code> widget with the specified action listeners which
   * are invoked when the buttons are clicked.
   */
  public AddRemoveButtons(ActionListener addListener, ActionListener removeListener) {
    this.add = new JButton();
    this.remove = new JButton();

    add.addActionListener(addListener);
    remove.addActionListener(removeListener);

    configureButtons();
    createUi();
  }

  /** Returns the "Add" button. */
  public JButton getAddButton() {
    return add;
  }

  /** Returns the "Remove" button. */
  public JButton getRemoveButton() {
    return remove;
  }

  /** Configures the buttons. */
  private void configureButtons() {
    add.putClientProperty("hideActionText", Boolean.TRUE);
    remove.putClientProperty("hideActionText", Boolean.TRUE);

    add.setIcon(ADD_ICON);
    add.setDisabledIcon(DISABLED_ADD_ICON);

    remove.setIcon(REMOVE_ICON);
    remove.setDisabledIcon(DISABLED_REMOVE_ICON);

    add.setText(null);
    remove.setText(null);

    add.setFocusable(false);
    remove.setFocusable(false);
  }

  /** Creates the UI of the widget. */
  private void createUi() {
    add.putClientProperty("JButton.buttonType", "toolbar");
    remove.putClientProperty("JButton.buttonType", "toolbar");

    add.setMargin(new Insets(0, 0, 0, 0));
    remove.setMargin(new Insets(0, 0, 0, 0));

    // setMargin doesn't seem to be enough for Ocean or Windows L&Fs
    add.setMinimumSize(new Dimension(ADD_IMAGE.getWidth(null), ADD_IMAGE.getHeight(null)));
    remove.setMinimumSize(new Dimension(REMOVE_IMAGE.getWidth(null), REMOVE_IMAGE.getHeight(null)));

    setLayout(new GridLayout(1, 2, -1, 0));
    add(add);
    add(remove);

    setPreferredSize(getMinimumSize());
    setMaximumSize(getMinimumSize());
  }
}
