/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2003 Alexander Maryanovsky. All rights reserved.
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.ui;

import free.jin.Jin;
import free.util.AWTUtilities;
import free.util.swing.SwingUtils;
import free.util.swing.WrapLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import org.jdesktop.layout.LayoutStyle;

/**
 * The superclass for panels which ask the user to choose something or fill in information. They are
 * usually placed in a modal dialog, but for flexibility implemented as panels. This class provides
 * an implementation of the common methods.
 */
public abstract class DialogPanel extends JPanel {

  /** The title of the panel. */
  private final String title;

  /** The dialog in which we're displayed. */
  private JDialog dialog = null;

  /** The parent component which we pass to <code>UIProvider.show(DialogPanel, Component)</code>. */
  private Component hintParent = null;

  /** The default button. */
  private JButton defaultButton = null;

  /** The result - the information specified by the user. */
  private Object result;

  /** Has the result been set? */
  private boolean resultSet = false;

  /** Creates a new <code>DialogPanel</code>. */
  public DialogPanel() {
    this(null);
  }

  /** Creates a new <code>DialogPanel</code> with the specified title. */
  public DialogPanel(String title) {
    this.title = title;
  }

  /** Returns the title of the panel. */
  protected String getTitle() {
    return title;
  }

  /**
   * Returns the result that should be returned if the user cancels the dialog. The default value
   * returns <code>null</code>.
   */
  protected Object getCancelResult() {
    return null;
  }

  /**
   * Sets the default button of this panel. This method is needed since subclasses don't have direct
   * access to the rootpane and thus can't set the default button.
   */
  protected void setDefaultButton(JButton button) {
    this.defaultButton = button;
  }

  /**
   * Displays the panel in the specified dialog centered relative to the specified <code>hintParent
   * </code>.
   */
  public void show(JDialog dialog, Component hintParent) {
    this.dialog = dialog;

    JPanel content = new JPanel(WrapLayout.getInstance());
    LayoutStyle layoutStyle = LayoutStyle.getSharedInstance();
    content.setBorder(
        BorderFactory.createEmptyBorder(
            layoutStyle.getContainerGap(content, SwingConstants.NORTH, dialog),
            layoutStyle.getContainerGap(content, SwingConstants.WEST, dialog),
            layoutStyle.getContainerGap(content, SwingConstants.SOUTH, dialog),
            layoutStyle.getContainerGap(content, SwingConstants.EAST, dialog)));

    content.add(this);
    dialog.setContentPane(content);

    configureDialog(dialog);

    AWTUtilities.centerWindow(dialog, hintParent);

    this.resultSet = false;

    dialog.setVisible(true);
  }

  /** Configures the dialog in which we'll be displayed. */
  protected void configureDialog(JDialog dialog) {
    SwingUtils.registerEscapeCloser(dialog);
    dialog.setModal(true);
    dialog.setTitle(getTitle());
    dialog.setResizable(false);
    dialog.getRootPane().setDefaultButton(defaultButton);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
  }

  /** Resizes the container of the panel to match the its preferred size. */
  public void resizeContainerToFit() {
    if (dialog != null) dialog.pack();
  }

  /**
   * Displays this <code>DialogPanel</code> and returns the result. It is recommended for subclasses
   * to add their own method to call this one but cast the result to the specific type.
   */
  public Object askResult() {
    Jin.getInstance().getUIProvider().showDialog(this, hintParent);

    if (resultSet) return result;
    else return getCancelResult();
  }

  /**
   * Sets the hint component - the specified component is used as a hint as to where to display the
   * <code>DialogPanel</code>. This method always returns <code>this</code>.
   */
  public final DialogPanel setHintParent(Component hintParent) {
    this.hintParent = hintParent;
    return this;
  }

  /**
   * Closes the dialog (or whatever container is used) and causes the <code>askResult</code> to
   * return the specified result.
   */
  protected void close(Object result) {
    this.result = result;
    this.resultSet = true;
    dialog.dispose();
    dialog = null;
  }

  /** An action listener useful for closing the panel with a result known up-front. */
  protected class ClosingListener implements ActionListener {

    /** The result value. */
    private final Object result;

    /** Creates a new <code>ClosingListener</code> with the specified result object. */
    public ClosingListener(Object result) {
      this.result = result;
    }

    /** Invoked <code>close</code> with the result object specified in the constructor. */
    @Override
    public void actionPerformed(ActionEvent evt) {
      close(result);
    }
  }
}
