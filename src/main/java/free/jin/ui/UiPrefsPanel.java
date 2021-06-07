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

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

/** A panel for selecting Jin UI preferences. */
public class UiPrefsPanel extends CompositePreferencesPanel {

  /** Creates a new <code>UiPrefsPanel</code>. */
  public UiPrefsPanel() {
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    addPanel(new LookAndFeelPrefPanel(), "lookAndFeelPanel");
    addPanel(new WindowingModePrefPanel(), "windowingModePanel");
  }

  /** Adds the specified panel to the UI. */
  @Override
  protected void addPanelToUi(PreferencesPanel panel, String name, String tooltip) {
    panel.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(name), BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    panel.setAlignmentY(Component.TOP_ALIGNMENT);
    add(panel);
  }

  /** Returns <code>true</code>. */
  @Override
  public boolean applyRequiresRestart() {
    return true;
  }
}
