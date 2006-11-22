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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import free.util.AWTUtilities;
import free.util.Localization;
import free.util.models.BooleanListener;
import free.util.models.BooleanModel;
import free.util.models.ConstBooleanModel;
import free.workarounds.FixedJPanel;


/**
 * A <code>JPanel</code> which allows its content (always a single component)
 * to be displayed in fullscreen mode.
 */

public class FullscreenPanel extends FixedJPanel{



  /**
   * The target component.
   */

  private final Component target;
  
  
  
  /**
   * The panel displayed in place of the target.
   */
  
  private final JPanel restorePanel;
  
  
  
  /**
   * Are we allowed to use fullscreen exclusive mode?
   */
  
  private boolean allowExclusiveMode;
  
  
  
  /**
   * The model specifying whether we're in fullscreen mode.
   */
  
  private final BooleanModel fullscreenModeModel;



  /**
   * Are we in the process of entering/exiting fullscreen mode.
   */

  private boolean beingModified = false;



  /**
   * The frame which contains this panel when it's in fullscreen mode.
   */

  private JFrame fullscreenFrame = null;
  
  
  
  /**
   * Are we in real fullscreen mode (as opposed to fake fullscreen mode)? Only
   * relevant if we really are in fullscreen mode
   */
  
  private boolean isRealFullscreen;
  
  
  
  /**
   * The extended state of the original parent frame. This is needed as a
   * workaround for the maximized state sometimes being reset when coming back
   * from fullscreen mode.
   */
   
  private int originalFrameState;
  
  
  
  /**
   * The original parent frame. We need to keep this because the fullscreen
   * panel might get removed from its parent, but we still need to make the
   * original parent frame visible when the target panel is made "normal" again.
   */
   
  private Frame originalFrame;
  
  
  
  /**
   * Creates a new <code>FullscreenPanel</code> with the specified target
   * component.
   */

  public FullscreenPanel(Component target){
    super(new BorderLayout());

    this.target = target;

    beingModified = true;
    add(target, BorderLayout.CENTER);
    beingModified = false;
    
    fullscreenModeModel = new BooleanModel(false);
    fullscreenModeModel.addListener(new BooleanListener(){
      public void modelChanged(ConstBooleanModel model){
        setFullscreen(model.isOn());
      }
    });
    restorePanel = createRestorePanel();
  }
  
  
  
  /**
   * Creates the panel displayed in place of the target component when in
   * fullscreen mode.
   */
  
  private JPanel createRestorePanel(){
    Localization l10n = LocalizationService.getForClass(FullscreenPanel.class);
    JButton restore = new JButton(l10n.getString("restoreNormalModeButton.text")); //$NON-NLS-1$
    restore.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        getFullscreenModeModel().setOff();
      }
    });
    restore.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    
    JLabel label = new JLabel(l10n.getString("fullscreenInfoLabel.text")); //$NON-NLS-1$
    label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(Box.createVerticalGlue());
    panel.add(label);
    panel.add(Box.createVerticalStrut(10));
    panel.add(restore);
    panel.add(Box.createVerticalGlue());
    
    return panel;
  }
  



  /**
   * Overrides the superclass method to disallow adding any components.
   */

  public void add(Component comp, Object constraints, int index){
    if (beingModified)
      super.add(comp, constraints, index);
    else
      throw new IllegalStateException("You may not add components to FullscreenPanel"); //$NON-NLS-1$
  }



  /**
   * Overrides the superclass method to disallow removing any components.
   */

  public void remove(int index){
    if (beingModified)
      super.remove(index);
    else
      throw new IllegalStateException("You may not remove components from FullscreenPanel"); //$NON-NLS-1$
  }
  
  
  
  /**
   * Sets whether we are allowed to use fullscreen exclusive mode (if
   * available). If not, fullscreen mode will always be faked via an undecorated
   * frame placed over the entire screen.  
   */
  
  public void setAllowExclusiveMode(boolean allowExclusiveMode){
    this.allowExclusiveMode = allowExclusiveMode;
  }



  /**
   * Sets this panel's fullscreen mode.
   */

  private void setFullscreen(boolean on){
    beingModified = true;
    if (on)
      makeFullscreen();
    else
      makeNormal();
    beingModified = false;
  }




  /**
   * Returns the <code>BooleanModel</code> specifying whether we're in
   * fullscreen mode.
   */

  public BooleanModel getFullscreenModeModel(){
    return fullscreenModeModel;
  }



  /**
   * Puts this panel into fullscreen mode. 
   */

  private void makeFullscreen(){
    remove(target);
    add(restorePanel);
    validate();

    if (allowExclusiveMode && securityManagerAllowsFullscreen()){
      fullscreenFrame = setRealFullscreen();
      isRealFullscreen = true;
    }
    else
      fullscreenFrame = setFakeFullscreen();

    fullscreenFrame.getContentPane().setLayout(new BorderLayout());
    fullscreenFrame.getContentPane().add(target, BorderLayout.CENTER);
    fullscreenFrame.getContentPane().validate();
  }
  
  
  
  /**
   * Returns whether the security manager allows fullscreen mode.
   */
   
  private static boolean securityManagerAllowsFullscreen(){
    SecurityManager sm = System.getSecurityManager();
    if (sm == null)
      return true;
    
    try{
      sm.checkPermission(new AWTPermission("fullScreenExclusive")); //$NON-NLS-1$
    } catch (SecurityException e){
        return false;
      }
      
    return true;
  }



  /**
   * Attempts to set the specified window in real fullscreen mode via the
   * JDK 1.4 fullscreen API.
   */

  private JFrame setRealFullscreen(){
    originalFrame = SwingUtils.frameForComponent(this);
    JFrame frame = new JFrame(originalFrame == null ? "" : originalFrame.getTitle()); //$NON-NLS-1$
    if (originalFrame != null)
      frame.setIconImage(originalFrame.getIconImage());        

    frame.setUndecorated(true);
    frame.setResizable(false);

    GraphicsDevice graphicsDevice = SwingUtils.frameForComponent(this).getGraphicsConfiguration().getDevice();

    if (originalFrame != null){
      originalFrameState = originalFrame.getExtendedState();
  
      originalFrame.setVisible(false);
      graphicsDevice.setFullScreenWindow(frame);
    }

    return frame;
  }




  /**
   * Fakes a fullscreen mode, merely setting the specified window's size to the
   * size of the screen.
   */

  private JFrame setFakeFullscreen(){
    originalFrame = SwingUtils.frameForComponent(this);
    JFrame frame = new JFrame(originalFrame == null ? "" : originalFrame.getTitle()); //$NON-NLS-1$
      
    frame.setUndecorated(true);
    frame.setResizable(false);
      
    Rectangle screenBounds = AWTUtilities.getUsableScreenBounds();
    frame.setBounds(screenBounds);
    frame.setVisible(true);
    frame.toFront();
  
    return frame;
  }


  

  /**
   * Puts this panel into normal (non-fullscreen) mode.
   */

  private void makeNormal(){
    fullscreenFrame.getContentPane().remove(target);

    if (isRealFullscreen)
      makeRealNormal();
    else
      makeFakeNormal();

    remove(restorePanel);
    add(target, BorderLayout.CENTER);
    validate();

    fullscreenFrame = null;
  }



  /**
   * Restores a real fullscreen window.
   */

  private void makeRealNormal(){
    fullscreenFrame.getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
    fullscreenFrame.dispose();

    if (originalFrame != null){
      originalFrame.setExtendedState(originalFrameState);
      originalFrame.setVisible(true);
    }
  }



  /**
   * Restores a fake fullscreen window.
   */

  private void makeFakeNormal(){
    fullscreenFrame.dispose();
  }



}