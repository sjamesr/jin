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

package free.jin;

import java.awt.Component;
import java.util.Properties;


/**
 * An interface dialogs collecting login information from the user must 
 * implement. To fully implement the contract, however, the implementing class 
 * must have a no-arg constructor. The dialog goes through the following
 * procedure:
 * <OL>
 *   <LI> An instance is created.
 *   <LI> The setHintUser(Properties) method of the dialog is called to give
 *        it the login properties.
 *   <LI> The show() method is called.
 *   <LI> The isCanceled() method is called to determine whether the user
 *        accepted or canceled the connection. If this method returns false, no
 *        further action is taken.
 *   <LI> The getConnection() method is invoked to obtain an instance of the
 *        Connection to the server. The returned connection needn't be
 *        connected.
 * </OL>
 * Sometime after the show() method returns, the getProperties() method may also
 * be called to obtain and (perhaps?) save the properties (all this only if
 * isCanceled()==false of course).
 */

public interface LoginDialog{

  
  /**
   * Gets called to hint the LoginDialog which user is probably trying to log on.
   * The LoginDialog should probably use this information to populate the
   * controls that allow choosing the login information with default values.
   * It is guaranteed that the given User is for a same server supported by the
   * LoginDialog.
   */

  void setHintUser(User user);



  /**
   * Waits until enough information to create an appropriate Connection instance
   * is available. This usually involves showing the user a (modal?) dialog. The
   * contract of this method is that it blocks until all necessary information
   * has been collected and a call to getConnection() will return a valid
   * instance.
   *
   * @param parentComponent A component over which to open the dialog. This
   * component should usually be used for 2 purposes - determining the location
   * of the dialog on the screen and obtaining the Frame necessary to open a
   * modal dialog.
   */

  void show(Component parentComponent);





  /**
   * Returns true if the user canceled the connection, returns false otherwise.
   */

  boolean isCanceled();




  /**
   * Returns an unconnected JinConnection to the server. This method will be called
   * after the show(Component) method returns and only if isCanceled() returns
   * false.
   */

  JinConnection createConnection();



  /**
   * Returns the User object filled with the information supplied by the user.
   * This method will be called after the show(Component) method returns and only
   * if isCanceled() returns false. This method should either return the User
   * passed to the LoginDialog in the setHintUser(User) method with some of its
   * properties modified, or if the properties were changed significantly, a new
   * User. It's up to the LoginDialog to decide when to reuse the given User and
   * when to create a new one. Usually, you would want to create a new User
   * if the user put in a different username.
   */

  User getUser();

}