/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
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
import free.jin.User;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginContext;
import free.jin.plugin.UnsupportedContextException;
import free.jin.SeekJinConnection;
import free.jin.event.SeekListener;
import free.jin.event.SeekEvent;
import free.jin.seek.event.SeekSelectionListener;
import free.jin.seek.event.SeekSelectionEvent;
import free.util.StringParser;
import free.util.StringEncoder;
import free.util.GraphicsUtilities;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.net.URL;


/**
 * The plugin which implements the SoughtGraph. Even though I haven't put this
 * plugin in one of the server specific packages, it's pretty specific to the
 * ICS derived servers (ICC, FICS, chess.net).
 */

public class SoughtGraphPlugin extends Plugin implements SeekListener, SeekSelectionListener{



  /**
   * The SoughtGraph.
   */

  protected SoughtGraph soughtGraph;



  /**
   * The JInternalFrame which contains the sought graph.
   */

  protected JInternalFrame soughtGraphFrame;




  /**
   * The JRadioButtonMenuItem indicating whether the sought graph is visible.
   */

  private JRadioButtonMenuItem visibleRB; 




  /**
   * The JRadioButtonMenuItem indicating whether the sought graph is invisible.
   */

  private JRadioButtonMenuItem nonVisibleRB;




  /**
   * We set this to true when we modify the visibility of the graph to avoid
   * handling echo events.
   */

  private boolean changingGraphVisibility = false;





  /**
   * Sets the plugin context - if the connection is not an instance of
   * SeekJinConnection, this method throws an UnsupportedContextException.
   */

  public void setContext(PluginContext context) throws UnsupportedContextException{
    if (!(context.getConnection() instanceof SeekJinConnection))
      throw new UnsupportedContextException("The connection does not implement seeking functionality");

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
    saveState();
    unregisterListeners();
    closeSoughtGraph();
  }





  /**
   * Initializes the sought graph.
   */

  protected void initSoughtGraph(){
    soughtGraph = createSoughtGraph();
    soughtGraphFrame = createSoughtGraphFrame();
    soughtGraphFrame.setVisible(false); // To make sure it's initially invisible even in 1.1/1.2

    Container content = soughtGraphFrame.getContentPane();
    content.setLayout(new BorderLayout());
    content.add(soughtGraph, BorderLayout.CENTER);

    JDesktopPane desktop = getPluginContext().getMainFrame().getDesktop();
    desktop.add(soughtGraphFrame);

    Rectangle desktopBounds = new Rectangle(desktop.getSize());
    String boundsString = getProperty("frame-bounds");
    Rectangle bounds = null;
    if (boundsString!=null)
      bounds = StringParser.parseRectangle(boundsString);

    if (bounds==null){
//      soughtGraphFrame.setBounds(desktopBounds.width/4, desktopBounds.height/4, desktopBounds.width*3/4, desktopBounds.height*3/4);
      soughtGraphFrame.setBounds(desktopBounds.width - 650, desktopBounds.height - 450, 650, 450);
    }
    else
      soughtGraphFrame.setBounds(bounds);

    boolean isMaximized = Boolean.valueOf(getProperty("maximized","false")).booleanValue();
    if (isMaximized){
      try{
        soughtGraphFrame.setMaximum(true);
      } catch (java.beans.PropertyVetoException e){}
    }

    boolean isIconified = Boolean.valueOf(getProperty("iconified","false")).booleanValue();
    if (isIconified){
      try{
        soughtGraphFrame.setIcon(true);
      } catch (java.beans.PropertyVetoException e){}
    }

    JComponent icon = soughtGraphFrame.getDesktopIcon();
    String iconBoundsString = getProperty("frame-icon-bounds");
    if (iconBoundsString!=null){
      Rectangle iconBounds = StringParser.parseRectangle(iconBoundsString);
      icon.setBounds(iconBounds);
    }

    if (Boolean.valueOf(getProperty("visible","true")).booleanValue())
      showSoughtGraphFrame();


    /* See http://developer.java.sun.com/developer/bugParade/bugs/4176136.html for the 
       reason I do this instead of adding an InternalFrameListener like a sane person. */
    soughtGraphFrame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
    soughtGraphFrame.addVetoableChangeListener(new VetoableChangeListener(){

      public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException{
        if (pce.getPropertyName().equals(JInternalFrame.IS_CLOSED_PROPERTY)&&
            pce.getOldValue().equals(Boolean.FALSE)&&pce.getNewValue().equals(Boolean.TRUE)){

          hideSoughtGraphFrame();
          throw new PropertyVetoException("Canceled closing", pce);
        }
      }
    });
  }




  /**
   * Closes the sought graph and undoes everything done by the initSoughtGraph
   * method.
   */

  protected void closeSoughtGraph(){
    hideSoughtGraphFrame();
    soughtGraphFrame.dispose();
    soughtGraphFrame = null;
    soughtGraph = null;
  }




  /**
   * Creates and returns the sought graph.
   */

  protected SoughtGraph createSoughtGraph(){
    return new SoughtGraph(this);
  }




  /**
   * Creates and returns the JInternalFrame which will contain the sought graph.
   */

  protected JInternalFrame createSoughtGraphFrame(){
    JInternalFrame frame = new JInternalFrame("Seek graph", false, true, false, true);

    String iconImageName = getProperty("icon-image");
    if (iconImageName != null){
      URL iconImageURL = SoughtGraphPlugin.class.getResource(iconImageName);
      if (iconImageURL!= null)
        frame.setFrameIcon(new ImageIcon(iconImageURL));
    }

    return frame;
  }




  /**
   * Shows the sought graph frame. Showing the sought graph frame should only be
   * done through this method, since it also registers this SoughtGraphPlugin as
   * a SeekListener. I haven't used a ComponentListener to automatically 
   * register the listener because it seems to be broken in JDK 1.1
   */

  protected void showSoughtGraphFrame(){
    if (soughtGraphFrame.isVisible())
      return;

    changingGraphVisibility = true;

    soughtGraphFrame.setVisible(true);

    if ((visibleRB != null) && (!visibleRB.isSelected())) // It may be null if the graph is shown before it's created.
      visibleRB.setSelected(true);
    SeekJinConnection conn = (SeekJinConnection)getConnection();
    conn.getSeekJinListenerManager().addSeekListener(SoughtGraphPlugin.this);

    changingGraphVisibility = false;
  }




  /**
   * Hides the sought graph frame. Hiding the sought graph frame should only be
   *
   done through this method, since it also unregisters this SoughtGraphPlugin as
   * a SeekListener. I haven't used a ComponentListener to automatically 
   * unregister the listener because it seems to be broken in JDK 1.1
   */

  protected void hideSoughtGraphFrame(){
    if (!soughtGraphFrame.isVisible())
      return;

    changingGraphVisibility = true;

    soughtGraphFrame.setVisible(false);
    soughtGraph.removeAllSeeks();

    if ((visibleRB != null) && (visibleRB.isSelected())) // It may be null if the graph is shown before it's created.
      nonVisibleRB.setSelected(true);
    SeekJinConnection conn = (SeekJinConnection)getConnection();
    conn.getSeekJinListenerManager().removeSeekListener(SoughtGraphPlugin.this);

    changingGraphVisibility = false;
  }




  /**
   * Saves the state of this plugin into the user's properties.
   */

  protected void saveState(){
    User user = getUser();
    String prefix = getID()+".";

    Rectangle soughtGraphFrameBounds = soughtGraphFrame.getBounds();
    // If something bad happened, let's not save that state.
    if ((soughtGraphFrameBounds.width>10)&&(soughtGraphFrameBounds.height>10))
      user.setProperty(prefix+"frame-bounds",StringEncoder.encodeRectangle(soughtGraphFrameBounds),false);

    boolean isMaximized = soughtGraphFrame.isMaximum();
    user.setProperty(prefix+"maximized", String.valueOf(isMaximized), false);

    boolean isIconified = soughtGraphFrame.isIcon();
    user.setProperty(prefix+"iconified", String.valueOf(isIconified), false);

    boolean isVisible = soughtGraphFrame.isVisible();
    user.setProperty(prefix+"visible", String.valueOf(isVisible), false);
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
    SeekJinConnection conn = (SeekJinConnection)getConnection();

    soughtGraph.removeSeekSelectionListener(this);
    conn.getSeekJinListenerManager().removeSeekListener(this); // Just in case.
  }




  /**
   * Creates and returns the JMenu for this plugin.
   */

  public JMenu createPluginMenu(){
    JMenu myMenu = new JMenu(getName());

    visibleRB = new JRadioButtonMenuItem("Graph shown", new Boolean(getProperty("visible","true")).booleanValue());
    visibleRB.setMnemonic('s');
    visibleRB.addChangeListener(new ChangeListener(){
      
      public void stateChanged(ChangeEvent evt){
        if (changingGraphVisibility)
          return;

        if (visibleRB.isSelected())
          showSoughtGraphFrame();
        else
          hideSoughtGraphFrame();
      }
  
    });

    nonVisibleRB = new JRadioButtonMenuItem("Graph hidden", !visibleRB.isSelected());
    nonVisibleRB.setMnemonic('h');

    ButtonGroup visibilityGroup = new ButtonGroup();
    visibilityGroup.add(visibleRB);
    visibilityGroup.add(nonVisibleRB);

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
   * Seek. This method asks the SeekJinConnection to accept the selected seek.
   */

  public void seekSelected(SeekSelectionEvent evt){
    ((SeekJinConnection)getConnection()).acceptSeek(evt.getSeek());
  }

}
