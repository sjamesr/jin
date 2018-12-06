/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2005 Alexander Maryanovsky. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import free.jin.BadChangesException;
import free.jin.I18n;

/**
 * A preferences panel which is a collection of other preferences panels.
 */
public abstract class CompositePreferencesPanel extends PreferencesPanel {

  /**
   * A list of the underlying panels.
   */
  protected final List panels = new LinkedList();

  /**
   * Keeps track which panels have changes to apply.
   */
  private final Set changedPanels = new HashSet();

  /**
   * Adds an underlying preferences panel.
   */
  protected final void addPanel(PreferencesPanel panel, String panelTitle, String panelToolTip) {
    panels.add(panel);

    panel.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            fireStateChanged();
            changedPanels.add(e.getSource());
          }
        });

    addPanelToUi(panel, panelTitle, panelToolTip);
  }

  /**
   * Adds an underlying preferences panel, retrieving its properties (such as name and tooltip)
   * using the specified key from the <code>I18n</code> object associated with the actual class of
   * this object (with <code>CompositePreferencesPanel</code> as the base class). The following
   * properties are looked up:
   * <ul>
   * <li><code>[key].title</code> for the panel's title.
   * <li><code>[key].tooltip</code> for the panel's tooltip.
   * </ul>
   */
  protected final void addPanel(PreferencesPanel panel, String i18nKey) {
    I18n i18n = I18n.get(getClass(), CompositePreferencesPanel.class);
    addPanel(panel, i18n.getString(i18nKey + ".title"), i18n.getString(i18nKey + ".tooltip"));
  }

  /**
   * Adds the specified panel to the UI of this panel.
   */
  protected abstract void addPanelToUi(
      PreferencesPanel panel, String panelTitle, String panelToolTip);

  /**
   * Returns whether any of our changed panels require a restart.
   */
  @Override
  public boolean applyRequiresRestart() {
    for (Iterator i = changedPanels.iterator(); i.hasNext(); ) {
      PreferencesPanel panel = (PreferencesPanel) i.next();
      if (panel.applyRequiresRestart()) return true;
    }

    return false;
  }

  /**
   * Applies changes to all the underlying panels.
   */
  @Override
  public void applyChanges() throws BadChangesException {
    for (Iterator i = panels.iterator(); i.hasNext(); ) {
      PreferencesPanel panel = (PreferencesPanel) i.next();
      if (changedPanels.contains(panel)) {
        panel.applyChanges();
        changedPanels.remove(panel);
      }
    }
  }
}
