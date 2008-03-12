/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2005 Alexander Maryanovsky.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package free.jin.ui;

import java.util.Vector;

import free.jin.BadChangesException;
import free.jin.I18n;


/**
 * A preferences panel which is a collection of other preferences panels.
 */

public abstract class CompositePreferencesPanel extends PreferencesPanel{
  
  
  
  /**
   * A list of the underlying panels.
   */
  
  protected final Vector panels = new Vector();
  
  
  
  /**
   * Adds an underlying preferences panel.
   */
  
  protected final void addPanel(PreferencesPanel panel, String panelTitle, String panelToolTip){
    panels.addElement(panel);
    panel.addChangeListener(proxyChangeListener);
    
    addPanelToUi(panel, panelTitle, panelToolTip);
  }
  
  
  
  /**
   * Adds an underlying preferences panel, retrieving its properties (such as
   * name and tooltip) using the specified key from the <code>I18n</code> object
   * associated with the actual class of this object (with
   * <code>CompositePreferencesPanel</code> as the base class).
   * The following properties are looked up:
   * <ul>
   *   <li><code>[key].title</code> for the panel's title.
   *   <li><code>[key].tooltip</code> for the panel's tooltip.
   * </ul>
   */
  
  protected final void addPanel(PreferencesPanel panel, String i18nKey){
    I18n i18n = I18n.get(getClass(), CompositePreferencesPanel.class);
    addPanel(panel, i18n.getString(i18nKey + ".title"), i18n.getString(i18nKey + ".tooltip"));
  }
  
  
  
  /**
   * Adds the specified panel to the UI of this panel.
   */
  
  protected abstract void addPanelToUi(PreferencesPanel panel, String panelTitle, String panelToolTip);

  
  
  /**
   * Applies changes to all the underlying panels.
   */

  public void applyChanges() throws BadChangesException{
    for (int i = 0; i < panels.size(); i++)
      ((PreferencesPanel)panels.elementAt(i)).applyChanges();
  }
  
  
  
}
