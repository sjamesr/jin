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

import free.jin.SeekConnection;
import free.jin.event.ConnectionEvent;
import free.jin.event.ConnectionListener;
import free.jin.event.SeekEvent;
import free.jin.event.SeekListener;
import free.jin.plugin.*;
import free.jin.seek.event.SeekSelectionEvent;
import free.jin.seek.event.SeekSelectionListener;
import free.jin.ui.UIProvider;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Toolkit;
import java.net.URL;


/**
 * The plugin which implements the SoughtGraph. Even though I haven't put this
 * plugin in one of the server specific packages, it's pretty specific to the
 * ICS derived servers (ICC, FICS, chess.net).
 */

public class SoughtGraphPlugin extends Plugin implements SeekListener, SeekSelectionListener,
    PluginUIListener, ConnectionListener{



  /**
   * The SoughtGraph.
   */

  protected SoughtGraph soughtGraph;



  /**
   * The container of the sought graph.
   */

  protected PluginUIContainer soughtGraphContainer;



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
    soughtGraphContainer = createContainer("", UIProvider.HIDEABLE_CONTAINER_MODE);
    soughtGraphContainer.setTitle("Seek Graph");

    URL iconImageURL = SoughtGraphPlugin.class.getResource("icon.gif");
    if (iconImageURL!= null)
      soughtGraphContainer.setIcon(Toolkit.getDefaultToolkit().getImage(iconImageURL));

    soughtGraphContainer.addPluginUIListener(this);


    soughtGraph = createSoughtGraph();

    Container content = soughtGraphContainer.getContentPane();
    content.setLayout(new BorderLayout());
    content.add(soughtGraph, BorderLayout.CENTER);
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
    SeekConnection conn = (SeekConnection)getConn();
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
    ((SeekConnection)getConn()).acceptSeek(evt.getSeek());
  }
  
  
  
  /*
   * ConnectionListener implementation.
   */
  
  public void connectionAttempted(ConnectionEvent evt){}
  public void connectionEstablished(ConnectionEvent evt){}
  public void connectionLoggedIn(ConnectionEvent evt){}
  
  
  
  /**
   * Remove all seeks on disconnection. This just seems to make more sense than
   * leaving them on.   
   */
  
  public void connectionLost(ConnectionEvent evt){
    soughtGraph.removeAllSeeks();
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
