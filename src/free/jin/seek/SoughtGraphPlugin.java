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

import java.awt.*;
import javax.swing.*;
import free.jin.plugin.*;
import free.jin.SeekConnection;
import free.jin.event.SeekListener;
import free.jin.event.SeekEvent;
import free.jin.seek.event.SeekSelectionListener;
import free.jin.seek.event.SeekSelectionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URL;


/**
 * The plugin which implements the SoughtGraph. Even though I haven't put this
 * plugin in one of the server specific packages, it's pretty specific to the
 * ICS derived servers (ICC, FICS, chess.net).
 */

public class SoughtGraphPlugin extends Plugin implements SeekListener, SeekSelectionListener,
    PluginUIListener{



  /**
   * The SoughtGraph.
   */

  protected SoughtGraph soughtGraph;



  /**
   * The container of the sought graph.
   */

  protected PluginUIContainer soughtGraphContainer;




  /**
   * The JRadioButtonMenuItem indicating whether the sought graph is visible.
   */

  private JRadioButtonMenuItem visibleRB; 




  /**
   * The JRadioButtonMenuItem indicating whether the sought graph is invisible.
   */

  private JRadioButtonMenuItem nonVisibleRB;



  /**
   * Sets the plugin context - if the connection is not an instance of
   * SeekJinConnection, this method throws an UnsupportedContextException.
   */

  public void setContext(PluginContext context) throws PluginStartException{
    if (!(context.getConnection() instanceof SeekConnection))
      throw new PluginStartException("The connection does not implement seeking functionality");

    super.setContext(context);
  }


 
  /**
   * Starts this plugin.
   */

  public void start(){
    initSoughtGraph();
    registerListeners();
  }





  /**
   * Stops this plugin.
   */

  public void stop(){
    unregisterListeners();
  }





  /**
   * Initializes the sought graph.
   */

  protected void initSoughtGraph(){
    soughtGraphContainer = createContainer("");
    soughtGraphContainer.setCloseOperation(PluginUIContainer.HIDE_ON_CLOSE);
    soughtGraphContainer.setTitle("Seek Graph");

    URL iconImageURL = SoughtGraphPlugin.class.getResource("icon.gif");
    if (iconImageURL!= null)
      soughtGraphContainer.setIcon(Toolkit.getDefaultToolkit().getImage(iconImageURL));

    soughtGraphContainer.addPluginUIListener(this);


    soughtGraph = createSoughtGraph();

    Container content = soughtGraphContainer.getContentPane();
    content.setLayout(new BorderLayout());
    content.add(soughtGraph, BorderLayout.CENTER);

    if (getPrefs().getBool("isVisible", true))
      soughtGraphContainer.setVisible(true);
  }



  /**
   * Creates and returns the sought graph.
   */

  protected SoughtGraph createSoughtGraph(){
    return new SoughtGraph(this);
  }



  /**
   * Gets called when the seek graph container is made visible.
   */

  public void pluginUIShown(PluginUIEvent evt){
    // It may be null if the graph is shown before it's created.
    if ((visibleRB != null) && (!visibleRB.isSelected())) 
      visibleRB.setSelected(true);

    SeekConnection conn = (SeekConnection)getConn();
    conn.getSeekListenerManager().addSeekListener(this);
  }



  /**
   * Gets called when the seek graph container is made invisible.
   */

  public void pluginUIHidden(PluginUIEvent evt){
    soughtGraph.removeAllSeeks();

    // It may be null if the graph is shown before it's created.
    if ((visibleRB != null) && (visibleRB.isSelected())) 
      nonVisibleRB.setSelected(true);

    SeekConnection conn = (SeekConnection)getConn();
    conn.getSeekListenerManager().removeSeekListener(this);
  }



  public void pluginUIClosing(PluginUIEvent evt){}
  public void pluginUIActivated(PluginUIEvent evt){}
  public void pluginUIDeactivated(PluginUIEvent evt){}



  /**
   * Saves the state of this plugin into the user's properties.
   */

  public void saveState(){
    boolean isVisible = soughtGraphContainer.isVisible();
    getPrefs().setBool("isVisible", isVisible);
  }




  /**
   * Registers the necessary listeners.
   */

  protected void registerListeners(){
    soughtGraph.addSeekSelectionListener(this);
  }




  /**
   * Unregisters all the listeners registered by this SoughtGraphPlugin.
   */

  protected void unregisterListeners(){
    SeekConnection conn = (SeekConnection)getConn();

    soughtGraph.removeSeekSelectionListener(this);
    conn.getSeekListenerManager().removeSeekListener(this); // Just in case.
  }




  /**
   * Creates and returns the JMenu for this plugin.
   */

  public JMenu getPluginMenu(){
    JMenu myMenu = new JMenu(getName());

    visibleRB = new JRadioButtonMenuItem("Graph Shown", getPrefs().getBool("isVisible", true));
    nonVisibleRB = new JRadioButtonMenuItem("Graph Hidden", !visibleRB.isSelected());

    visibleRB.setMnemonic('s');
    nonVisibleRB.setMnemonic('h');

    ButtonGroup visibilityGroup = new ButtonGroup();
    visibilityGroup.add(visibleRB);
    visibilityGroup.add(nonVisibleRB);

    visibleRB.setActionCommand("show");
    nonVisibleRB.setActionCommand("hide");

    ActionListener visibilityListener = new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        String actionCommand = evt.getActionCommand();
        boolean isVisible = "show".equals(actionCommand);
        soughtGraphContainer.setVisible(isVisible);
        getPrefs().setBool("isVisible", isVisible);
      }
    };

    visibleRB.addActionListener(visibilityListener);
    nonVisibleRB.addActionListener(visibilityListener);

    myMenu.add(visibleRB);
    myMenu.add(nonVisibleRB);

    return myMenu;
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
    ((SeekConnection)getConn()).acceptSeek(evt.getSeek());
  }



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
    return "Seek Graph";
  }

  

}
