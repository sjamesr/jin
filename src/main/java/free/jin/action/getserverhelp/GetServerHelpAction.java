/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2004 Alexander Maryanovsky. All rights reserved.
 *
 * <p>This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * <p>You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.action.getserverhelp;

import free.jin.action.JinAction;
import java.awt.event.ActionEvent;

/** An action which shows server specific help to the user. */
public class GetServerHelpAction extends JinAction {

  /** Returns the id of the action - "getserverhelp". */
  @Override
  public String getId() {
    return "getserverhelp";
  }

  /** Causes the server specific help to be shown to the user. */
  @Override
  public void actionPerformed(ActionEvent evt) {
    getConn().showServerHelp();
  }
}
