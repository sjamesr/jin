/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util.swing;

import javax.swing.*;
import java.beans.PropertyVetoException;
import java.util.Vector;
import java.awt.event.ContainerListener;
import java.awt.event.ContainerEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameEvent;


/**
 * Implements decent switching between internal frames in a given
 * desktop pane. The main functionality of this class is tracking the order in
 * which the frames are traversed. Note that this class doesn't automaticall
 * switch between the frames - you need to listen to the appropriate events and
 * call <code>selectNext</code> and <code>selectPrevious</code> yourself.
 */

public class InternalFrameSwitcher implements ContainerListener, InternalFrameListener{



  /**
   * A vector of the current internal frames, in the right order.
   */

  private final Vector frames = new Vector();



  /**
   * We set this flag to true when activating frames because for some reason
   * JInternalFrame.setSelected(true) causes the frame to be readded.
   */

  private boolean ignoreContainerEvents = false;

                                    

  /**
   * Creates a new <code>InternalFrameSwitcher</code> with the specified 
   * desktop pane.
   */

  public InternalFrameSwitcher(JDesktopPane desktop){
    desktop.addContainerListener(this);
  }


  
  /**
   * Adds the added <code>JInternalFrame</code> to the list of frames.
   */

  public void componentAdded(ContainerEvent evt){
    if (ignoreContainerEvents)
      return;

    if (evt.getChild() instanceof JInternalFrame){
      JInternalFrame internalFrame = (JInternalFrame)evt.getChild();

      frames.addElement(internalFrame);
      internalFrame.addInternalFrameListener(this);
    }
  }



  /**
   * Removes the removed <code>JInternalFrame</code> from the list of frames.
   */

  public void componentRemoved(ContainerEvent evt){
    if (ignoreContainerEvents)
      return;


    if (evt.getChild() instanceof JInternalFrame){
      JInternalFrame internalFrame = (JInternalFrame)evt.getChild();

      frames.removeElement(internalFrame);
      internalFrame.removeInternalFrameListener(this);
    }
  }
  
  
  
  /**
   * Moves the source frame to the top of the frame list.
   */
  
  public void internalFrameActivated(InternalFrameEvent e){
    JInternalFrame f = (JInternalFrame)e.getSource();
    
    frames.removeElement(f);
    frames.insertElementAt(f, 0);
  }
  
  
  
  // InternalFrameListener implementation
  public void internalFrameClosed(InternalFrameEvent e){}
  public void internalFrameClosing(InternalFrameEvent e){} 
  public void internalFrameDeactivated(InternalFrameEvent e){} 
  public void internalFrameDeiconified(InternalFrameEvent e){} 
  public void internalFrameIconified(InternalFrameEvent e){}
  public void internalFrameOpened(InternalFrameEvent e){} 

 

  /**
   * Switches to the frame that should be selected next.
   */

  public void selectNext(){
    // JInternalFrame newSelectedFrame = null;
    
    if (!frames.isEmpty()){
      JInternalFrame f = (JInternalFrame)frames.elementAt(frames.size() - 1);
      try{
        ignoreContainerEvents = true;
        f.setSelected(true);
        ignoreContainerEvents = false;
        return;
      } catch (PropertyVetoException e){}
    }
  }



  /**
   * Switches to the previous selected frame.
   */

  public void selectPrevious(){
    if (!frames.isEmpty()){
      Object f = frames.elementAt(0);
      frames.removeElementAt(0);
      frames.addElement(f);
      
      try{
        ignoreContainerEvents = true;
        ((JInternalFrame)frames.elementAt(0)).setSelected(true);
        ignoreContainerEvents = false;
        return;
      } catch (PropertyVetoException e){}
    }
  }


  
}
