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

import java.awt.*;
import javax.swing.JFrame;
import java.io.IOException;
import java.util.Properties;
import javax.swing.ListModel;
import free.util.IOUtilities;
import free.util.AWTUtilities;


/**
 * Various Jin related utilities.
 */

public class JinUtilities{
  
  
  
  /**
   * Loads and returns the application properties.
   */
  
  static Properties loadAppProps() throws IOException{
    return IOUtilities.loadProperties(JinUtilities.class.getResourceAsStream("resources/app.props"));
  }



  /**
   * Saves the geometry of the specified frame into the specified preferences
   * with preference names prefixed with the specified string.
   */

  static void saveFrameGeometry(JFrame frame, Preferences prefs, String prefNamePrefix){
    
    // Save bounds on screen
    Point frameLocation = frame.isVisible() ? frame.getLocationOnScreen() : frame.getLocation();
    Dimension frameSize = frame.getSize();
    prefs.setRect(prefNamePrefix + ".bounds", new Rectangle(frameLocation, frameSize));
    
    // Save maximized state
    int state = AWTUtilities.getExtendedFrameState(frame);
    prefs.setBool(prefNamePrefix + ".maximized.vert", (state & Frame.MAXIMIZED_VERT) != 0);
    prefs.setBool(prefNamePrefix + ".maximized.horiz", (state & Frame.MAXIMIZED_HORIZ) != 0);
  }
  
  

  /**
   * Returns <code>true</code> if the specified frame bounds are reasonably
   * placed on a screen of the specified dimensions. This is used to avoid
   * situations where a frame is displayed outside of the screen where the user
   * can't change its size and/or move it (can happen for example if the
   * resolution is changed between runs).
   */

  private static boolean frameBoundsOk(Dimension screenSize, Rectangle frameBounds){
    if (frameBounds.x + frameBounds.width < 50)
      return false;
    if (frameBounds.y < -10)
      return false;
    if (frameBounds.width < 30)
      return false;
    if (frameBounds.height < 40)
      return false;
    if (frameBounds.x > screenSize.width - 10)
      return false;
    if (frameBounds.y > screenSize.height - 20)
      return false;

    return true;
  }
  
  
  
  
  /**
   * Restores the geometry of the specified frame from the specified
   * preferences. The prefix specifies the prefix to use when obtaining the
   * frame geometry from the preferences.
   */
   
  static void restoreFrameGeometry(JFrame frame, Preferences prefs, String prefNamePrefix){
    Dimension screenSize = frame.getToolkit().getScreenSize();
    Rectangle defaultFrameBounds = new Rectangle(
      screenSize.width/16, screenSize.height/16, screenSize.width*7/8, screenSize.height*7/8);
    
      
    // Restore bounds      
    Rectangle frameBounds = prefs.getRect(prefNamePrefix + ".bounds", defaultFrameBounds);
    frameBounds = frameBoundsOk(screenSize, frameBounds) ? frameBounds : defaultFrameBounds;
    frame.setBounds(frameBounds);

    
    // Restore maximized state 
    boolean vertMaximized = prefs.getBool(prefNamePrefix + ".maximized.vert", false);
    boolean horizMaximized = prefs.getBool(prefNamePrefix + ".maximized.horiz", false);

    // Bugfix for Java bug 4464714 - setExtendedState only works once the
    // the window is realized.
    if (frame.getPeer() == null)
      frame.addNotify();
   
    int state = ((vertMaximized ? Frame.MAXIMIZED_VERT : 0) | (horizMaximized ? Frame.MAXIMIZED_HORIZ : 0));
    AWTUtilities.setExtendedFrameState(frame, state);
  }



  /**
   * Returns the server with the specified id. Returns <code>null</code> if no
   * such server found.
   */

  public static Server getServerById(JinContext context, String id){
    Server [] servers = context.getServers();
    for (int i = 0; i < servers.length; i++)
      if (servers[i].getId().equals(id))
        return servers[i];

    return null;
  }



  /**
   * Returns the user with the specified username on the specified server or
   * <code>null</code> if no such user exists.
   */

  public static User getUser(JinContext context, Server server, String username){
    ListModel users = context.getUsers();
    for (int i = 0; i < users.getSize(); i++){
      User user = (User)users.getElementAt(i);
      if ((user.getServer() == server) && 
          server.getUsernamePolicy().isSame(username, user.getUsername()))
        return user;
    }

    return null;
  }



  /**
   * Returns whether the specified User represents a "known" account, that is,
   * it appears in the list returned by <code>context.getUsers()</code>.
   */

  public static boolean isKnownUser(JinContext context, User user){
    ListModel users = context.getUsers();
    for (int i = 0; i < users.getSize(); i++)
      if (users.getElementAt(i).equals(user))
        return true;

    return false;
  }



}