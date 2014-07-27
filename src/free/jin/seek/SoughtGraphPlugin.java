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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import free.jin.Connection;
import free.jin.I18n;
import free.jin.MatchOfferConnection;
import free.jin.Preferences;
import free.jin.Seek;
import free.jin.SeekConnection;
import free.jin.ServerUser;
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
import free.util.swing.WrapLayout;
import free.util.swing.WrapperComponent;
import free.util.swing.tabbedpane.Tab;
import free.util.swing.tabbedpane.TabbedPane;
import free.util.swing.tabbedpane.TabbedPaneEvent;
import free.util.swing.tabbedpane.TabbedPaneListener;
import free.util.swing.tabbedpane.TabbedPaneModel;


/**
 * The plugin which implements the SoughtGraph. Even though I haven't put this
 * plugin in one of the server specific packages, it's pretty specific to the
 * ICS derived servers (ICC, FICS, chess.net).
 */

public class SoughtGraphPlugin extends Plugin implements SeekListener, SeekSelectionListener,
    PluginUIListener, ConnectionListener{
  
  
  
  /**
   * The ID of this plugin.
   */
  
  public static final String PLUGIN_ID = "seek";
  
  
  
  /**
   * The panel for issuing seeks.
   */
  
  private IssueSeekPanel issueSeekPanel;
  
  
  
  /**
   * The panel for issuing match offers. May be <code>null</code> if there is
   * no such panel (if, for example, the connection is not an instance of
   * <code>MatchOfferConnection</code>).
   */
  
  private IssueMatchPanel issueMatchPanel;
  
  
  
  /**
   * The tabbed pane holding the issue seek and match panels. May be
   * <code>null</code> if there is no issueMatchPanel.
   */
  
  private TabbedPane issueTabbedPane;
  
  
  
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
   * Returns the connection to the server, cast to a
   * <code>SeekConnection</code>.
   */
  
  private SeekConnection getSeekConn(){
    return (SeekConnection)getConn();
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
    savePrefs();
  }
  
  
  
  /**
   * Saves the plugin's preferences.
   */
  
  private void savePrefs(){
    issueSeekPanel.savePrefs();
    
    if (issueMatchPanel != null){
      issueMatchPanel.savePrefs();
      
      Preferences prefs = getPrefs();
      prefs.setString("visibleIssuePanel", issueSeekPanel.isVisible() ? "seek" : "match");
    }
  }



  /**
   * Creates the UI.
   */

  protected void createUI(){
    I18n i18n = getI18n();
    
    uiContainer = createContainer("", UIProvider.HIDEABLE_CONTAINER_MODE);
    uiContainer.setTitle(i18n.getString("uiContainerTitle"));

    URL iconImageURL = SoughtGraphPlugin.class.getResource("icon.gif");
    if (iconImageURL!= null)
      uiContainer.setIcon(Toolkit.getDefaultToolkit().getImage(iconImageURL));

    uiContainer.addPluginUIListener(this);

    issueSeekPanel = createIssueSeekPanel();
    issueMatchPanel = createIssueMatchPanel();
    soughtGraph = new SoughtGraph(this);
    
    JLabel soughtGraphLabel = i18n.createLabel("soughtGraphLabel");
    soughtGraphLabel.setFont(soughtGraphLabel.getFont().deriveFont(Font.BOLD));
    
    issueTabbedPane = new TabbedPane(SwingConstants.TOP);
    issueTabbedPane.setAlwaysShowTabs(false);
    
    TabbedPaneModel model = issueTabbedPane.getModel();
    model.addTab(new Tab(issueSeekPanel, i18n.getString("issueSeekTab.text"), null, false));
    if (issueMatchPanel != null)
      model.addTab(new Tab(issueMatchPanel, i18n.getString("issueMatchTab.text"), null, false));
    model.setSelectedIndex(model.indexOfComponent(
        getPrefs().getString("visibleIssuePanel", "seek").equals("seek") ?
            (Component)issueSeekPanel : (Component)issueMatchPanel));
    
    model.addTabbedPaneListener(new TabbedPaneListener(){
      public void tabSelected(TabbedPaneEvent evt){
        Container issuePanel = (Container)evt.getTabbedPaneModel().getTab(evt.getTabIndex()).getComponent();
        Component defaultComponent = issuePanel.getFocusTraversalPolicy().getDefaultComponent(issuePanel);
        if (defaultComponent != null)
          defaultComponent.requestFocusInWindow();
      }
      
      public void tabAdded(TabbedPaneEvent evt){}
      public void tabDeselected(TabbedPaneEvent evt){}
      public void tabRemoved(TabbedPaneEvent evt){}
    });
    
    JLabel issueSeekLabel = null;
    if (issueMatchPanel == null){
      issueSeekLabel = i18n.createLabel("issueSeekLabel");
      issueSeekLabel.setFont(issueSeekLabel.getFont().deriveFont(Font.BOLD));
    }
    
    
    JComponent soughtGraphWrapper = new WrapperComponent(){
      public Dimension getPreferredSize(){
        // The sought graph doesn't really have a preferred size, as much as a
        // preferred width:height ratio
        int height = Math.max(issueTabbedPane.getPreferredSize().height, getMinimumSize().height);
        int width = (int)(1.5*height);
        
        return new Dimension(width, height);
      }
    };
    soughtGraphWrapper.add(soughtGraph);
    soughtGraph.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Panel.background").darker()));
    
    
    JPanel content = new JPanel();
    GroupLayout layout = new GroupLayout(content);
    content.setLayout(layout);
    layout.setAutocreateContainerGaps(true);
    
    if (issueSeekLabel == null){
      layout.setHorizontalGroup(layout.createSequentialGroup()
          .add(issueTabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
          .addPreferredGap(LayoutStyle.UNRELATED)
          .add(layout.createParallelGroup()
            .add(soughtGraphLabel).add(soughtGraphWrapper)));
        
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.TRAILING)
          .add(issueTabbedPane)
          .add(layout.createSequentialGroup()
            .add(soughtGraphLabel)
            .addPreferredGap(LayoutStyle.RELATED)
            .add(soughtGraphWrapper)));
    }
    else{
      layout.setHorizontalGroup(layout.createSequentialGroup()
        .add(layout.createParallelGroup()
          .add(issueSeekLabel)
          .add(issueTabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(LayoutStyle.UNRELATED)
        .add(layout.createParallelGroup()
          .add(soughtGraphLabel).add(soughtGraphWrapper)));
      
      layout.setVerticalGroup(layout.createSequentialGroup()
        .add(layout.createParallelGroup(GroupLayout.BASELINE)
          .add(issueSeekLabel).add(soughtGraphLabel))
        .addPreferredGap(LayoutStyle.RELATED)
        .add(layout.createParallelGroup(GroupLayout.LEADING)
          .add(issueTabbedPane).add(soughtGraphWrapper)));
    }
    
    uiContainer.getContentPane().setLayout(WrapLayout.getInstance());
    uiContainer.getContentPane().add(content);
  }
  
  
  
  /**
   * Creates the <code>IssueSeekPanel</code>. This method allows subclasses to
   * provide their own, custom, versions of <code>IssueSeekPanel</code>.
   */
  
  protected IssueSeekPanel createIssueSeekPanel(){
    return new IssueSeekPanel(this, uiContainer,
        Preferences.createWrapped(getPrefs(), "issueSeekPanel."));
  }
  
  
  
  /**
   * Creates the <code>IssueMatchPanel</code>. This method allows subclasses to
   * provide their own, custom, versions of <code>IssueMatchPanel</code>.
   * Returns <code>null</code> if there is no <code>IssueMatchPanel</code> (if,
   * for example, the connection is not an instance of
   * <code>MatchOfferConnection</code>).
   */
  
  protected IssueMatchPanel createIssueMatchPanel(){
    if (getConn() instanceof MatchOfferConnection)
      return new IssueMatchPanel(this, uiContainer,
          Preferences.createWrapped(getPrefs(), "issueMatchPanel."));
    else
      return null;
  }
  
  
  
  /**
   * Returns whether we have UI for issuing match offers.
   * 
   * @see #displayMatchUI(ServerUser)
   */
  
  public boolean hasMatchUI(){
    return issueMatchPanel != null;
  }
  
  
  
  /**
   * Sets the UI up to issue a match offer to the specified player (may be
   * <code>null</code>, to indicate a blank opponent).
   * 
   * @see #hasMatchUI()
   */
  
  public void displayMatchUI(ServerUser opponent){
    if (!hasMatchUI())
      throw new IllegalArgumentException("No UI for matching");
    
    TabbedPaneModel model = issueTabbedPane.getModel();
    model.setSelectedIndex(model.indexOfComponent(issueMatchPanel));
    uiContainer.setActive(true);
    issueMatchPanel.prepareFor(opponent);
  }
  
  
  
  /**
   * Gets called when the seek graph container is made visible.
   */

  public void pluginUIShown(PluginUIEvent evt){
    SeekConnection conn = getSeekConn();
    
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

    SeekConnection conn = getSeekConn();
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
    soughtGraph.removeSeekSelectionListener(this);
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
    SeekConnection conn = getSeekConn();
    
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
   * Returns the ID of this plugin. See also {@linkplain #PLUGIN_ID}.
   */

  public String getId(){
    return PLUGIN_ID;
  }



  /**
   * An action which displays/hides our UI.
   */
  
  private class FindGameAction extends JinAction implements PluginUIListener{
    
    
    
    /**
     * Creates a new <code>FindGameAction</code>.
     */
    
    public FindGameAction(){
      uiContainer.addPluginUIListener(this);
    }
    
    
    
    /**
     * Returns the id of this action - "findgame".
     */
      
    public String getId(){
      return "findgame";
    }
    
    
    
    /**
     * Displays or hides the UI.
     */
    
    public void actionPerformed(ActionEvent evt){
      if (uiContainer.isVisible())
        uiContainer.setVisible(false);
      else
        uiContainer.setActive(true);
    }
    
    
    
    /**
     * Invoked when the "find game" UI is shown.
     */
    
    public void pluginUIShown(PluginUIEvent evt){
      I18n i18n = I18n.get(FindGameAction.class);
      
      putValue(Action.NAME, i18n.getString("name.hide"));
      putValue(Action.SHORT_DESCRIPTION, i18n.getString("shortDescription.hide"));
    }
    
    
    
    /**
     * Invoked when the "find game" UI is hidden.
     */
    
    public void pluginUIHidden(PluginUIEvent evt){
      I18n i18n = I18n.get(FindGameAction.class);
      
      putValue(Action.NAME, i18n.getString("name"));
      putValue(Action.SHORT_DESCRIPTION, i18n.getString("shortDescription"));
    }
    
    
    
    /**
     * Invoked when the "find game" UI is made active.
     */
    
    public void pluginUIActivated(PluginUIEvent evt){
      Container focusCycleRoot = issueTabbedPane.getFocusCycleRootAncestor();
      if (focusCycleRoot == null) // Weird things can happen on shutdown
        return;
      
      FocusTraversalPolicy policy = focusCycleRoot.getFocusTraversalPolicy();
      if (policy == null)
        return;
      
      Component defaultComponent = policy.getDefaultComponent(issueTabbedPane);
      if (defaultComponent == null)
        return;
      
      defaultComponent.requestFocusInWindow();
    }
    
    public void pluginUIClosing(PluginUIEvent evt){}
    public void pluginUIDeactivated(PluginUIEvent evt){}
    public void pluginUIDisposed(PluginUIEvent evt){}
    public void pluginUITitleChanged(PluginUIEvent evt){}
    public void pluginUIIconChanged(PluginUIEvent evt){}
    
    
    
  }



}
