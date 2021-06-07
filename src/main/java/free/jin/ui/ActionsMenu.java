/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2005 Alexander Maryanovsky. All rights reserved.
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

import free.jin.ConnectionManager;
import free.jin.I18n;
import free.jin.Jin;
import free.jin.Session;
import free.jin.SessionEvent;
import free.jin.SessionListener;
import free.jin.action.JinAction;
import javax.swing.JMenu;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/** A menu allowing the user to use any available Actions. */
public class ActionsMenu extends JMenu implements SessionListener, ListDataListener {

  /** Creates a new <code>ActionsMenu</code>. */
  public ActionsMenu() {
    I18n.get(ActionsMenu.class).initAbstractButton(this, "this");
  }

  /** Initializes the menu, registering any needed listeners. */
  @Override
  public void addNotify() {
    super.addNotify();

    ConnectionManager connManager = Jin.getInstance().getConnManager();
    connManager.addSessionListener(this);

    Session session = connManager.getSession();
    if (session != null) {
      ListModel actions = session.getPluginContext().getActions();
      actions.addListDataListener(this);

      updateActionMenuItems(actions);
    }
  }

  /** Unregisters any listeners we've registered. */
  @Override
  public void removeNotify() {
    super.removeNotify();

    ConnectionManager connManager = Jin.getInstance().getConnManager();
    connManager.removeSessionListener(this);

    Session session = connManager.getSession();
    if (session != null) {
      ListModel actions = session.getPluginContext().getActions();
      actions.removeListDataListener(this);

      removeAll();
    }
  }

  /** Sets the action menu items to match the specified list of actions. */
  private void updateActionMenuItems(ListModel actions) {
    removeAll();
    for (int i = 0; i < actions.getSize(); i++) {
      JinAction action = (JinAction) actions.getElementAt(i);
      add(action);
    }
  }

  /*
   * SessionListener implementation. Registers and unregisters us as session
   * listeners and updates the menu items accordingly.
   */

  @Override
  public void sessionEstablished(SessionEvent evt) {
    Session session = evt.getSession();

    ListModel actions = session.getPluginContext().getActions();
    actions.addListDataListener(this);

    updateActionMenuItems(actions);
  }

  @Override
  public void sessionClosed(SessionEvent evt) {
    Session session = evt.getSession();

    ListModel actions = session.getPluginContext().getActions();
    actions.removeListDataListener(this);

    removeAll();
  }

  @Override
  public void sessionStarting(SessionEvent evt) {}

  @Override
  public void sessionClosing(SessionEvent evt) {}

  /*
   * ListDataListener implementation. Synchronizes the menu items with
   * the list of actions
   */

  @Override
  public void intervalAdded(ListDataEvent evt) {
    updateActionMenuItems((ListModel) evt.getSource());
  }

  @Override
  public void intervalRemoved(ListDataEvent evt) {
    updateActionMenuItems((ListModel) evt.getSource());
  }

  @Override
  public void contentsChanged(ListDataEvent evt) {
    updateActionMenuItems((ListModel) evt.getSource());
  }
}
