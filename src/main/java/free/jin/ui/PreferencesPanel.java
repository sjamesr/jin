/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2002, 2003 Alexander Maryanovsky. All rights reserved.
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

import free.jin.BadChangesException;
import free.jin.I18n;
import free.util.swing.PreferredSizedPanel;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

/** The superclass of panels containing preferences UI for plugins. */
public abstract class PreferencesPanel extends PreferredSizedPanel {

  /**
   * An action listener whose sole function is to invoke {@link #fireStateChanged()}. Useful for
   * <code>PreferencesPanel</code>s with components firing action events when the user changes a
   * setting.
   */
  protected final ActionListener proxyActionListener =
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
          fireStateChanged();
        }
      };

  /**
   * A change listener whose sole function is to invoke {@link #fireStateChanged()}. Useful for
   * <code>PreferencesPanel</code>s with components firing change events when the user changes a
   * setting.
   */
  protected final ChangeListener proxyChangeListener =
      new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent evt) {
          fireStateChanged();
        }
      };

  /**
   * A list selection listener whose sole function is to invoke {@link #fireStateChanged()}. Useful
   * for <code>PreferencesPanel</code>s with lists.
   */
  protected final ListSelectionListener proxyListSelectionListener =
      new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent evt) {
          fireStateChanged();
        }
      };

  /** The sole ChangeEvent we need. */
  private final ChangeEvent changeEvent = new ChangeEvent(this);

  /**
   * Applies the changes done by the user.
   *
   * @throws BadChangesException if the changes done by the user are invalid.
   */
  public abstract void applyChanges() throws BadChangesException;

  /**
   * Returns whether applying the settings on this panel requires restarting the application. The
   * default implementation returns <code>false</code>.
   */
  public boolean applyRequiresRestart() {
    return false;
  }

  /**
   * Adds a ChangeListener to the list of listeners receiving notifications when the preferences
   * change.
   */
  public void addChangeListener(ChangeListener listener) {
    listenerList.add(ChangeListener.class, listener);
  }

  /**
   * Removes the given Changelistener from the list of listeners receiving notifications when the
   * preferences change.
   */
  public void removeChangeListener(ChangeListener listener) {
    listenerList.remove(ChangeListener.class, listener);
  }

  /**
   * Fires a ChangeEvent to all interested listeners. This method should be called by subclasses
   * whenever the user changes one of the settings.
   */
  protected void fireStateChanged() {
    Object[] listeners = listenerList.getListenerList();
    for (int i = 0; i < listeners.length; i += 2) {
      if (listeners[i] == ChangeListener.class) {
        ChangeListener listener = (ChangeListener) listeners[i + 1];
        listener.stateChanged(changeEvent);
      }
    }
  }

  /** Shows an error dialog and focuses the error component, if any. */
  public void badChangeAttempted(BadChangesException e) {
    I18n i18n = I18n.get(PreferencesPanel.class);
    OptionPanel.error(
        i18n.getString("badChangesDialog.title"), e.getMessage(), PreferencesPanel.this);

    Component errorComponent = e.getErrorComponent();
    if (errorComponent != null) {
      errorComponent.requestFocusInWindow();
      if (errorComponent instanceof JTextComponent) ((JTextComponent) errorComponent).selectAll();
    }
  }
}
