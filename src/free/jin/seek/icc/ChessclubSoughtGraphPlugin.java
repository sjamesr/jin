/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2004 Alexander Maryanovsky.
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

package free.jin.seek.icc;

import java.awt.event.ActionEvent;

import free.jin.action.JinAction;
import free.jin.seek.SoughtGraphPlugin;


/**
 * This is an ICC specific version of <code>SoughtGraphPlugin</code>.
 */
 
public class ChessclubSoughtGraphPlugin extends SoughtGraphPlugin{
  
  
  
  /**
   * Overrides <code>start</code> to export the 5-min and 1-min actions.
   */
  
  public void start(){
    super.start();
    
    exportAction(new OneMinuteAction());
    exportAction(new FiveMinuteAction());
    exportAction(new FifteenMinuteAction());
  }
  
  
  
  /**
   * A Jin action which issues the "1-minute" command.
   */
  
  private class OneMinuteAction extends JinAction{
    
    
    
    /**
     * Returns the string <code>"1-minute"</code>.
     */
    
    public String getId(){
      return "1-minute";
    }
    
    
    
    /**
     * Issues the "1-minute" command.
     */
    
    public void actionPerformed(ActionEvent e){
      getConn().sendCommand("multi 1-minute");
    }
    
    
    
  }
  
  
  
  /**
   * A Jin action which issues the "5-minute" command.
   */
  
  private class FiveMinuteAction extends JinAction{
    
    
    
    /**
     * Returns the string <code>"5-minute"</code>.
     */
    
    public String getId(){
      return "5-minute";
    }
    
    
    
    /**
     * Issues the "5-minute" command.
     */
    
    public void actionPerformed(ActionEvent e){
      getConn().sendCommand("multi 5-minute");
    }
    
    
    
  }
  
  
  
  /**
   * A Jin action which issues the "15-minute" command.
   */
  
  private class FifteenMinuteAction extends JinAction{
    
    
    
    /**
     * Returns the string <code>"15-minute"</code>.
     */
    
    public String getId(){
      return "15-minute";
    }
    
    
    
    /**
     * Issues the "15-minute" command.
     */
    
    public void actionPerformed(ActionEvent e){
      getConn().sendCommand("multi 15-minute");
    }
    
    
    
  }

  
  
  
}
