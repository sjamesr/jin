/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import free.jin.I18n;
import free.jin.Jin;
import free.util.swing.UrlDisplayingAction;


/**
 * The help menu on Jin's menubar.
 */

public class HelpMenu extends JMenu{
  
  
  
  /**
   * Creates a new <code>HelpMenu</code>.
   */

  public HelpMenu(){
    I18n.get(HelpMenu.class).initAbstractButton(this, "this");
    
    add(createWebsiteMenuItem());
    add(createCreditsMenuItem());
    add(createBugReportMenuItem());
    add(createFeatureRequestMenuItem());
    add(createAboutMenuItem());
  }



  /**
   * Creates the "Jin Website" menu item.
   */

  private JMenuItem createWebsiteMenuItem(){
    JMenuItem item = I18n.get(HelpMenu.class).createMenuItem("jinWebsiteMenuItem");
    item.addActionListener(new UrlDisplayingAction("http://www.jinchess.com"));
    return item;
  }



  /**
   * Creates the "Credits and copyrights" menu item.
   */

  private JMenuItem createCreditsMenuItem(){
    JMenuItem item = I18n.get(HelpMenu.class).createMenuItem("creditsMenuItem");
    item.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        new LicensePanel().display();
      }
    });
    return item;
  }



  /**
   * Creates the "Report a Bug" menu item.
   */

  private JMenuItem createBugReportMenuItem(){
    JMenuItem item = I18n.get(HelpMenu.class).createMenuItem("reportBugMenuItem");
    item.addActionListener(new UrlDisplayingAction("https://sourceforge.net/tracker/?group_id=50386&atid=459537"));
    return item;
  }



  /**
   * Creates the "Suggest a Feature" menu item.
   */

  private JMenuItem createFeatureRequestMenuItem(){
    JMenuItem item = I18n.get(HelpMenu.class).createMenuItem("suggestFeatureMenuItem");
    item.addActionListener(new UrlDisplayingAction("https://sourceforge.net/tracker/?group_id=50386&atid=459540"));
    return item;
  }



  /**
   * Creates the "About..." menu item.
   */

  private JMenuItem createAboutMenuItem(){
    JMenuItem item = I18n.get(HelpMenu.class).createMenuItem("aboutJinMenuItem", new Object[]{Jin.getInstance().getAppName()});
    item.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        new AboutPanel().display();
      }
    });
    return item;
  }



}