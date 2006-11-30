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

package free.jin.board.fics;

import free.jin.plugin.PluginUIEvent;
import free.freechess.Ivar;
import free.jin.board.BoardManager;
import free.jin.board.BoardPanel;
import free.jin.freechess.JinFreechessConnection;
import free.jin.Game;
import free.jin.Connection;
import free.jin.Preferences;
import free.jin.event.GameStartEvent;
import free.jin.event.GameEndEvent;
import free.jin.freechess.FreechessListenerManager;
import free.jin.freechess.event.IvarStateChangeListener;
import free.jin.freechess.event.IvarStateChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;


/**
 * A freechess.org specific extension of the BoardManager plugin.
 */

public class FreechessBoardManager extends BoardManager implements IvarStateChangeListener{



  /**
   * The current primary (observed) game.
   */

  private Object primaryObservedGameID = null;



  /**
   * The current primary (played) game.
   */

  private Object primaryPlayedGameID = null;


  
  /**
   * Creates a new <code>FreechessBoardManager</code>. Registers a listener for
   * the moveSendingMode property to reflect its value in the premove ivar.
   */
   
  public FreechessBoardManager(){
    addPropertyChangeListener(new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
        if ("moveSendingMode".equals(evt.getPropertyName())){
          Integer oldValue = (Integer)evt.getOldValue();
          Integer newValue = (Integer)evt.getNewValue();
          Integer premoveValue = new Integer(PREMOVE_MOVE_SENDING_MODE);
          
          if (!newValue.equals(oldValue) &&
              (premoveValue.equals(newValue) || premoveValue.equals(oldValue))){
            JinFreechessConnection conn = (JinFreechessConnection)getConn();
            conn.setIvarState(Ivar.PREMOVE, premoveValue.equals(newValue));
          }
        }
      }
    });
  }


  
  /**
   * Registers our own listeners.
   */

  protected void registerConnListeners(){
    super.registerConnListeners();
    
    FreechessListenerManager listenerManager = 
      ((JinFreechessConnection)getConn()).getFreechessListenerManager();

    listenerManager.addIvarStateChangeListener(this);
  }


  
  /**
   * Unregisters the listeners we've registered in
   * <code>registerConnListeners</code>.
   */

  protected void unregisterConnListeners(){
    super.unregisterConnListeners();
    
    FreechessListenerManager listenerManager = 
      ((JinFreechessConnection)getConn()).getFreechessListenerManager();

    listenerManager.removeIvarStateChangeListener(this);
  }
  

  
  /**
   * Overrides BoardManager.createBoardPanel() to return a FreechessBoardPanel.
   */

  protected BoardPanel createBoardPanel(Game game){
    BoardPanel boardPanel = new FreechessBoardPanel(this, game);

    return boardPanel;
  }




  /**
   * Overrides the superclass' method to set the primary game properly.
   */

  public void pluginUIActivated(PluginUIEvent e){
    BoardPanel boardPanel = (BoardPanel)containersToBoardPanels.get(e.getSource());
    if (boardPanel == null) // This means that the frame is in the process 
      return;               // of being initialized and isn't ready yet.

    Game game = boardPanel.getGame();
    Object gameID = game.getID();
    if (boardPanel.isActive()){
      int gameType = game.getGameType();
      Connection conn = getConn();

      if (gameType == Game.OBSERVED_GAME){
        if (!gameID.equals(primaryObservedGameID)){
          conn.sendCommand("$$primary "+gameID);
          primaryObservedGameID = gameID;
        }
      }
      else if (gameType == Game.MY_GAME){
        if (!gameID.equals(primaryPlayedGameID)){
          conn.sendCommand("$$goboard "+gameID);
          primaryPlayedGameID = gameID;
        }
      }
    }

    super.pluginUIActivated(e);
  }


  /**
   * Overrides the superclass' method to set <code>primaryObservedGameID</code>
   * and <code>primaryPlayedGameID</code> properly.
   */

  public void gameStarted(GameStartEvent evt){
    Game game = evt.getGame();
    int gameType = game.getGameType();
    Object gameID = game.getID();

    if ((gameType == Game.OBSERVED_GAME) && (primaryObservedGameID == null))
      primaryObservedGameID = gameID;
    else if ((gameType == Game.MY_GAME) && (primaryPlayedGameID == null))
      primaryPlayedGameID = gameID;

    super.gameStarted(evt);
  }



  /**
   * Overrides the superclass' method to set <code>primaryObservedGameID</code>
   * and <code>primaryPlayedGameID</code> properly.
   */

  public void gameEnded(GameEndEvent evt){
    Game game = evt.getGame();
    int gameType = game.getGameType();
    Object gameID = game.getID();

    if ((gameType == Game.OBSERVED_GAME) && gameID.equals(primaryObservedGameID))
      primaryObservedGameID = null;
    else if ((gameType == Game.MY_GAME) && gameID.equals(primaryPlayedGameID))
      primaryPlayedGameID = null;

    super.gameEnded(evt);
  }

  

  /**
   * <code>IvarStateChangeListener</code> implementation.
   */
   
  public void ivarStateChanged(IvarStateChangeEvent evt){
    // Did the user try to set iv_premove manually to false?
    if ((evt.getIvar() == Ivar.PREMOVE) && (evt.getState() == false) &&
      (getMoveSendingMode() == PREMOVE_MOVE_SENDING_MODE)){

      setMoveSendingMode(LEGAL_CHESS_MOVE_SENDING_MODE);
    }
      // Punish the bastard ;-)
  }
  
  
  
  /**
   * Sets the initial value of autoflag to 1. This is really the server's job,
   * but since FICS refuses to do the reasonable thing, we must.
   */
  
  public void loginSucceeded(Connection conn){
    super.loginSucceeded(conn);
    
    Preferences prefs = getPrefs();
    if (prefs.getBool("setDefaultAutoflagValue", true)){
      conn.sendCommand("$set autoflag 1");
      prefs.setBool("setDefaultAutoflagValue", false);
    }
  }


  
}
