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
import java.awt.event.KeyListener;
import java.awt.event.KeyAdapter;


/**
 * Implements decent switching between internal frames in a given
 * desktop pane. The main functionality of this class is tracking the order in
 * which the frames are traversed.
 */

public class InternalFrameSwitcher implements ContainerListener{



  /**
   * The desktop pane.
   */

  private final JDesktopPane desktop;



  /**
   * A vector of the current internal frames.
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
    this.desktop = desktop;

    desktop.addContainerListener(this);
  }



  /**
   * A dummy KeyListener used to workaround a bug.
   */

  private final KeyListener dummyKeyListener = new KeyAdapter(){};



  /**
   * Are we running under JDK 1.1?
   */

  private static final boolean isJava1_1 =
    (System.getProperty("java.version").compareTo("1.2") < 0);



  /**
   * Adds the added <code>JInternalFrame</code> to the list of frames.
   */

  public void componentAdded(ContainerEvent evt){
    if (ignoreContainerEvents)
      return;

    if (evt.getChild() instanceof JInternalFrame){
      JInternalFrame internalFrame = (JInternalFrame)evt.getChild();

      // We do this because without this, the FocusManager isn't called.
      // The call forces the frame to call enableEvents(AWTEvent.KEY_EVENT_MASK)
      // and since that method is protected we can't call it directly.
      if (isJava1_1)
        internalFrame.addKeyListener(dummyKeyListener);

      frames.addElement(internalFrame);
    }
  }



  /**
   * Removes the removed <code>JInternalFrame</code> from the list of frames.
   */

  public void componentRemoved(ContainerEvent evt){
    if (ignoreContainerEvents)
      return;


    // See comment in componentAdded(ContainerEvent)
    if (evt.getChild() instanceof JInternalFrame){
      JInternalFrame internalFrame = (JInternalFrame)evt.getChild();

      if (isJava1_1)
        internalFrame.addKeyListener(dummyKeyListener);

      frames.removeElement(internalFrame);
    }
  }




  /**
   * Switches to the next frame, relative to the currently selected frame.
   */

  public void selectNext(){
    JInternalFrame newSelectedFrame = null;

    int framesCount = frames.size();
    if (framesCount == 1)
      newSelectedFrame = (JInternalFrame)frames.elementAt(0);
    else if (framesCount != 0){
      int selected = getSelected();
      if (selected == -1){
        for (int i = 0; i < framesCount; i++){
          JInternalFrame frame = (JInternalFrame)frames.elementAt(i);
          if (frame.isVisible() && !frame.isIcon()){
            newSelectedFrame = frame;
            break;
          }
        }
      }
      else{
        int i = (selected == framesCount - 1) ? 0 : selected + 1;
        while (i != selected){
          JInternalFrame frame = (JInternalFrame)frames.elementAt(i);
          if (frame.isVisible() && !frame.isIcon()){
            newSelectedFrame = frame;
            break;
          }
          i++;
          if (i == framesCount)
            i = 0;
        }
      }
    }

    if (newSelectedFrame != null){    
      try{
        ignoreContainerEvents = true;
        newSelectedFrame.setSelected(true);
        ignoreContainerEvents = false;
        return;
      } catch (PropertyVetoException e){}
    }
  }



  /**
   * Switches to the previous frame, relative to the currently selected frame.
   */

  public void selectPrevious(){
    JInternalFrame newSelectedFrame = null;

    
    int framesCount = frames.size();
    if (framesCount == 1)
      newSelectedFrame = (JInternalFrame)frames.elementAt(0);
    else if (framesCount != 0){
      int selected = getSelected();
      if (selected == -1){
        for (int i = 0; i < framesCount; i++){
          JInternalFrame frame = (JInternalFrame)frames.elementAt(i);
          if (frame.isVisible() && !frame.isIcon()){
            newSelectedFrame = frame;
            break;
          }
        }
      }
      else{
        int i = (selected == 0) ? framesCount - 1 : selected - 1;
        while (i != selected){
          JInternalFrame frame = (JInternalFrame)frames.elementAt(i);
          if (frame.isVisible() && !frame.isIcon()){
            newSelectedFrame = frame;
            break;
          }
          i--;
          if (i == -1)
            i = framesCount - 1;
        }
      }
    }

    if (newSelectedFrame != null){    
      try{
        ignoreContainerEvents = true;
        newSelectedFrame.setSelected(true);
        ignoreContainerEvents = false;
        return;
      } catch (PropertyVetoException e){}
    }
  }




  /**
   * Finds the currently selected frame's index or -1 if none.
   */

  private int getSelected(){
    for (int i = 0; i < frames.size(); i++){
      JInternalFrame frame = (JInternalFrame)frames.elementAt(i);
      if (frame.isSelected())
        return i;
    }

    return -1;
  }



}
