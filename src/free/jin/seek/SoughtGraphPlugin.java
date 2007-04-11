/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

package free.jin.seek;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import free.jin.Connection;
import free.jin.I18n;
import free.jin.Preferences;
import free.jin.Seek;
import free.jin.SeekConnection;
import free.jin.action.JinAction;
import free.jin.event.ConnectionListener;
import free.jin.event.SeekEvent;
import free.jin.event.SeekListener;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginContext;
import free.jin.plugin.PluginUIContainer;
import free.jin.plugin.PluginUIEvent;
import free.jin.plugin.PluginUIListener;
import free.jin.seek.event.SeekSelectionEvent;
import free.jin.seek.event.SeekSelectionListener;
import free.jin.ui.UIProvider;
import free.util.TableLayout;


/**
 * The plugin which implements the SoughtGraph. Even though I haven't put this
 * plugin in one of the server specific packages, it's pretty specific to the
 * ICS derived servers (ICC, FICS, chess.net).
 */

public class SoughtGraphPlugin extends Plugin implements SeekListener, SeekSelectionListener,
    PluginUIListener, ConnectionListener{
  
  
  
  /**
   * The panel for issuing seeks.
   */
  
  private IssueSeekPanel issueSeekPanel;
  
  
  
  /**
   * The seek graph.
   */

  private SoughtGraph soughtGraph;



  /**
   * The container of our UI.
   */

  private PluginUIContainer uiContainer;



  /**
   * Sets the plugin context - return <code>false</code> if the connection is
   * not an instance of <code>SeekConnection</code>.
   */

  public boolean setContext(PluginContext context){
    if (!(context.getConnection() instanceof SeekConnection))
      return false;

    return super.setContext(context);
  }


 
  /**
   * Starts this plugin.
   */

  public void start(){
    createUI();
    registerListeners();
    exportAction(new FindGameAction());
  }



  /**
   * Stops this plugin.
   */

  public void stop(){
    unregisterListeners();
    issueSeekPanel.saveSeekOptions();
  }



  /**
   * Creates the UI.
   */

  protected void createUI(){
    I18n i18n = getI18n();
    
    int xGap = 10;
    int yGap = 10;
    
    uiContainer = createContainer("", UIProvider.HIDEABLE_CONTAINER_MODE);
    uiContainer.setTitle(i18n.getString("uiContainerTitle"));

    URL iconImageURL = SoughtGraphPlugin.class.getResource("icon.gif");
    if (iconImageURL!= null)
      uiContainer.setIcon(Toolkit.getDefaultToolkit().getImage(iconImageURL));

    uiContainer.addPluginUIListener(this);

    issueSeekPanel = new IssueSeekPanel(this, 
        Preferences.createWrapped(getPrefs(), "issueSeekPanel."));
    soughtGraph = new SoughtGraph(this);
    
    
    JPanel issueSeekPanelWrapper = new JPanel(new TableLayout(1, xGap, yGap));
    issueSeekPanelWrapper.setAlignmentY(JComponent.TOP_ALIGNMENT);
    issueSeekPanelWrapper.add(i18n.createLabel("issueSeekLabel"));
    issueSeekPanelWrapper.add(issueSeekPanel);
    
    JPanel soughtGraphWrapper = new JPanel(new BorderLayout(xGap, yGap));
    soughtGraphWrapper.add(i18n.createLabel("soughtGraphLabel"), BorderLayout.PAGE_START);
    soughtGraphWrapper.add(soughtGraph, BorderLayout.CENTER);
    soughtGraph.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
    
    
    JPanel content = new JPanel(new BorderLayout(xGap, yGap));
    content.add(issueSeekPanelWrapper, BorderLayout.LINE_START);
    content.add(soughtGraphWrapper, BorderLayout.CENTER);
    
    content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    uiContainer.getContentPane().setLayout(new BorderLayout());
    uiContainer.getContentPane().add(content, BorderLayout.CENTER);
    
  }
  
  

  /**
   * Gets called when the seek graph container is made visible.
   */

  public void pluginUIShown(PluginUIEvent evt){
    SeekConnection conn = (SeekConnection)getConn();
    
    for (Iterator i = conn.getSeeks().iterator(); i.hasNext();){
      Seek seek = (Seek)i.next();
      soughtGraph.addSeek(seek);
    }
    
    conn.getSeekListenerManager().addSeekListener(this);
  }



  /**
   * Gets called when the seek graph container is made invisible.
   */

  public void pluginUIHidden(PluginUIEvent evt){
    soughtGraph.removeAllSeeks();

    SeekConnection conn = (SeekConnection)getConn();
    conn.getSeekListenerManager().removeSeekListener(this);
  }
  
  
  
  public void pluginUIClosing(PluginUIEvent evt){}
  public void pluginUIActivated(PluginUIEvent evt){}
  public void pluginUIDeactivated(PluginUIEvent evt){}
  public void pluginUIDisposed(PluginUIEvent evt){}
  public void pluginUITitleChanged(PluginUIEvent evt){}
  public void pluginUIIconChanged(PluginUIEvent evt){}
  




  /**
   * Registers the necessary listeners.
   */

  protected void registerListeners(){
    soughtGraph.addSeekSelectionListener(this);
    getConn().getListenerManager().addConnectionListener(this);
  }




  /**
   * Unregisters all the listeners registered by this SoughtGraphPlugin.
   */

  protected void unregisterListeners(){
    SeekConnection conn = (SeekConnection)getConn();

    soughtGraph.removeSeekSelectionListener(this);
    conn.getSeekListenerManager().removeSeekListener(this); // Just in case.
    getConn().getListenerManager().removeConnectionListener(this);
  }
  
  
  
  /**
   * SeekListener implementation. Gets called when a seek is added.
   */

  public void seekAdded(SeekEvent evt){
    soughtGraph.addSeek(evt.getSeek());
  }



  /**
   * SeekListener implementation. Gets called when a seek is removed.
   */

  public void seekRemoved(SeekEvent evt){
    soughtGraph.removeSeek(evt.getSeek());
  }



  /**
   * SeekSelectionListener implementation. Gets called when the user selects a
   * Seek. This method asks the SeekConnection to accept the selected seek.
   */

  public void seekSelected(SeekSelectionEvent evt){
    Seek seek = evt.getSeek();
    SeekConnection conn = (SeekConnection)getConn();
    
    // Is it our own seek?
    if (seek.getSeeker().equals(getConn().getUser()))
      conn.withdraw(seek);
    else
      conn.accept(seek);
  }
  
  
  
  /**
   * Remove all seeks on disconnection. This just seems to make more sense than
   * leaving them on.   
   */
  
  public void connectionLost(Connection conn){
    soughtGraph.removeAllSeeks();
  }
  
  
  
  // The rest of ConnectionListener's methods.
  public void connectingFailed(Connection conn, String reason){}
  public void connectionAttempted(Connection conn, String hostname, int port){}
  public void connectionEstablished(Connection conn){}
  public void loginFailed(Connection conn, String reason){}
  public void loginSucceeded(Connection conn){}



  /**
   * Returns the string <code>"seek"</code>.
   */

  public String getId(){
    return "seek";
  }



  /**
   * Returns the name of this plugin.
   */

  public String getName(){
    return getI18n().getString("pluginName");
  }
  
  
  
  /**
   * An action which displays/hides our UI.
   */
  
  private class FindGameAction extends JinAction{
    
    
    
    /**
     * Creates a new <code>FindGameAction</code>.
     */
    
    public FindGameAction(){
      super(true);
    }
    
    
    
    /**
     * Returns the id of this action.
     */
      
    public String getId(){
      return "findgame";
    }
    
    
    
    /**
     * Displays or hides the UI.
     */
    
    public void actionPerformed(ActionEvent evt){
      uiContainer.setActive(true);
    }
    
    
    
  }



}
