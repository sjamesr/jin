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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.*;

import free.util.AWTUtilities;
import free.util.PlatformUtils;
import free.util.models.BooleanListener;
import free.util.models.BooleanModel;
import free.util.models.ConstBooleanModel;
import free.workarounds.FixedJPanel;


/**
 * A <code>JPanel</code> which allows its content (always a single component)
 * to be displayed in fullscreen mode. Note: this class only works properly on
 * JDK 1.4 or later.
 */

public class FullscreenPanel extends FixedJPanel{



  /**
   * Are we running under JDK 1.4 or later?
   */

  private static final boolean SUFFICIENT_JAVA_VERSION = PlatformUtils.isJavaBetterThan("1.4");



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
   
  private Object originalFrameState;
  
  
  
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
    JButton restore = new JButton("Restore normal mode");
    restore.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        getFullscreenModeModel().setOff();
      }
    });
    restore.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    
    JLabel label = new JLabel("This panel is in fullscreen mode.");
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
      throw new IllegalStateException("You may not add components to FullscreenPanel");
  }



  /**
   * Overrides the superclass method to disallow removing any components.
   */

  public void remove(int index){
    if (beingModified)
      super.remove(index);
    else
      throw new IllegalStateException("You may not remove components from FullscreenPanel");
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
    if (!SUFFICIENT_JAVA_VERSION){
      System.err.println("Ignored request to modify fullscreen mode: only available for JRE 1.4 or later");
      return;
    }
    
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
      Class awtPermissionClass = Class.forName("java.awt.AWTPermission");
      Constructor awtPermissionCtor = awtPermissionClass.getConstructor(new Class[]{String.class});
      Object fullscreenExclusivePermission = awtPermissionCtor.newInstance(new Object[]{"fullScreenExclusive"});
      Class permissionClass = Class.forName("java.security.Permission");
      Method checkPermissionMethod = SecurityManager.class.getMethod("checkPermission", new Class[]{permissionClass});
      try{
        checkPermissionMethod.invoke(sm, new Object[]{fullscreenExclusivePermission});
      } catch (InvocationTargetException e){
          if (e.getTargetException() instanceof SecurityException)
            return false;
          else
            e.printStackTrace();
        }
    } catch (ClassNotFoundException e){e.printStackTrace();}
      catch (NoSuchMethodException e){e.printStackTrace();}
      catch (InstantiationException e){e.printStackTrace();}
      catch (IllegalAccessException e){e.printStackTrace();}
      catch (InvocationTargetException e){e.printStackTrace();}
      
    return true;
  }



  /**
   * Attempts to set the specified window in real fullscreen mode via the
   * JDK 1.4 fullscreen API.
   */

  private JFrame setRealFullscreen(){
    try{
      originalFrame = SwingUtils.frameForComponent(this);
      JFrame frame = new JFrame(originalFrame == null ? "" : originalFrame.getTitle());
      if (originalFrame != null)
        frame.setIconImage(originalFrame.getIconImage());        

      Method setUndecorated = 
        Frame.class.getMethod("setUndecorated", new Class[]{boolean.class});
      Method setResizable = 
        Frame.class.getMethod("setResizable", new Class[]{boolean.class});

      setUndecorated.invoke(frame, new Object[]{Boolean.TRUE});
      setResizable.invoke(frame, new Object[]{Boolean.FALSE});

      Class windowClass = Window.class;
      Class graphicsConfigurationClass = Class.forName("java.awt.GraphicsConfiguration");
      Class graphicsDeviceClass = Class.forName("java.awt.GraphicsDevice");
      Method getGraphicsConfiguration = 
        windowClass.getMethod("getGraphicsConfiguration", (Class[])null);
      Method getDevice = graphicsConfigurationClass.getMethod("getDevice", new Class[0]);
      Method setFullScreenWindow =
        graphicsDeviceClass.getMethod("setFullScreenWindow", new Class[]{Window.class});

      Object graphicsConfiguration = 
        getGraphicsConfiguration.invoke(SwingUtils.frameForComponent(this), new Object[0]);
      Object graphicsDevice = getDevice.invoke(graphicsConfiguration, new Object[0]);
      
      if (originalFrame != null){
        Method getExtState = Frame.class.getMethod("getExtendedState", new Class[0]);
        originalFrameState = getExtState.invoke(originalFrame, new Object[0]);
  
        originalFrame.setVisible(false);
        setFullScreenWindow.invoke(graphicsDevice, new Object[]{frame});
      }

      return frame;
    } catch (Exception e){e.printStackTrace();}

    return null;
  }




  /**
   * Fakes a fullscreen mode, merely setting the specified window's size to the
   * size of the screen.
   */

  private JFrame setFakeFullscreen(){
    try{
      originalFrame = SwingUtils.frameForComponent(this);
      JFrame frame = new JFrame(originalFrame == null ? "" : originalFrame.getTitle());
      
      Method setUndecorated = 
        Frame.class.getMethod("setUndecorated", new Class[]{boolean.class});
      Method setResizable = 
        Frame.class.getMethod("setResizable", new Class[]{boolean.class});
  
      setUndecorated.invoke(frame, new Object[]{Boolean.TRUE});
      setResizable.invoke(frame, new Object[]{Boolean.FALSE});
      
      Rectangle screenBounds = AWTUtilities.getUsableScreenBounds();
      frame.setBounds(screenBounds);
      frame.setVisible(true);
      frame.toFront();
  
      return frame;
    } catch (Exception e){e.printStackTrace();}
    return null;
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
    try{
      Class windowClass = Window.class;
      Class graphicsConfigurationClass = Class.forName("java.awt.GraphicsConfiguration");
      Class graphicsDeviceClass = Class.forName("java.awt.GraphicsDevice");
      Method getGraphicsConfiguration = 
        windowClass.getMethod("getGraphicsConfiguration", new Class[0]);
      Method getDevice = graphicsConfigurationClass.getMethod("getDevice", new Class[0]);
      Method setFullScreenWindow =
        graphicsDeviceClass.getMethod("setFullScreenWindow", new Class[]{Window.class});

      Object graphicsConfiguration = getGraphicsConfiguration.invoke(fullscreenFrame, new Object[0]);
      Object graphicsDevice = getDevice.invoke(graphicsConfiguration, new Object[0]);
      setFullScreenWindow.invoke(graphicsDevice, new Object[]{null});

      fullscreenFrame.dispose();

      
      if (originalFrame != null){
        Method setExtState =
          Frame.class.getMethod("setExtendedState", new Class[]{int.class});
        setExtState.invoke(originalFrame, new Object[]{originalFrameState});
        originalFrame.setVisible(true);
      }
    } catch (Exception e){e.printStackTrace();}
  }



  /**
   * Restores a fake fullscreen window.
   */

  private void makeFakeNormal(){
    fullscreenFrame.dispose();
  }



}