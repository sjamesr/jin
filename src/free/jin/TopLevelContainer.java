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

package free.jin;

import javax.swing.JMenuBar;
import javax.swing.JFrame;
import java.awt.Container;
import java.awt.Frame;


/**
 * Defines the methods that the main container for Jin must implement. This
 * basically exists to provide a uniform interface for using different top level
 * containers, such as <code>JFrame</code>, <code>JApplet</code> etc. I didn't
 * use <code>javax.swing.RootPaneContainer</code> because it doesn't provide
 * menubar functionality (or some other future functionality required by Jin).
 * The class also provides several wrapper implementations of the interface.
 */

public abstract class TopLevelContainer{
  
  
  
  /**
   * Sets the title of the top level container. This may be ignored by the
   * implementation if it has no notion of a title.
   */
   
  public abstract void setTitle(String title);



  /**
   * Sets the menu bar to the specified one.
   */

  public abstract void setMenuBar(JMenuBar menubar);



  /**
   * Returns the current menu bar.
   */

  public abstract JMenuBar getMenuBar();



  /**
   * Sets the content pane to the specified container.
   */

  public abstract void setContentPane(Container container);



  /**
   * Returns the current content pane.
   */

  public abstract Container getContentPane();



  /**
   * Returns the topmost component, a frame. This should only be used as the
   * parent of dialogs and other windows.
   */

  public abstract Frame getTopMostFrame();



  /**
   * Returns a <code>TopLevelContainer</code> for the specified
   * <code>JFrame</code> with the specified title. The title will be used as a
   * constant postfix for the real title (as set via the <code>setTitle</code>
   * method).
   */

  public static TopLevelContainer getFor(final JFrame frame, String titlePostfix){
    return new JFrameTopLevelContainer(frame, titlePostfix);
  }
  
  
  
  
  /**
   * A TopLevelContainer implementation for a JFrame.
   */
   
  private static class JFrameTopLevelContainer extends TopLevelContainer{
    
    
    /**
     * The frame.
     */
     
    private final JFrame frame;
    
    
    /**
     * The title postfix. We use this to set the title to
     * <code>[requested title] - [postfix title]</code> when
     * <code>setTitle</code> is called. 
     */
     
    private final String titlePostfix;
    
    
    /**
     * Creates a new <code>FrameTopLevelContainer</code> for the specified
     * <code>JFrame</code> and title postfix.
     */
     
    public JFrameTopLevelContainer(JFrame frame, String titlePostfix){
      this.frame = frame;
      this.titlePostfix = titlePostfix;
    }
    
    
    
    // Implementation

    public void setTitle(String title){
      if ((title == null) || "".equals(title))
        frame.setTitle(titlePostfix);
      else
        frame.setTitle(title + " - " + titlePostfix);
    }    
    public void setMenuBar(JMenuBar menubar){frame.setJMenuBar(menubar);}
    public JMenuBar getMenuBar(){return frame.getJMenuBar();}
    public void setContentPane(Container container){frame.setContentPane(container);}
    public Container getContentPane(){return frame.getContentPane();}
    public Frame getTopMostFrame(){return frame;}

    
  }
   


}