/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2007 Alexander Maryanovsky.
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

package free.jin.console;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import free.jin.Connection;
import free.jin.Game;
import free.jin.I18n;
import free.jin.event.GameAdapter;
import free.jin.event.GameEndEvent;



/**
 * A console designation for chat at a board.
 */

public abstract class GameConsoleDesignation extends AbstractConsoleDesignation{
  
  
  
  /**
   * The game we're covering.
   */
  
  protected final Game game;
  
  
  
  /**
   * Whether the game has ended.
   */
  
  private boolean gameHasEnded = false;
  
  
  
  /**
   * Creates a new <code>GameConsoleDesignation</code>.
   * 
   * @param connection The connection to the server.
   * @param game The game this console designation is for.
   * @param name The name of the console.
   * @param encoding The encoding to use for encoding/decoding messages.
   * @param isConsoleCloseable Whether the console should be closeable. 
   */
  
  public GameConsoleDesignation(Connection connection, Game game, String encoding, boolean isConsoleCloseable){
    super(connection, consoleNameForGame(game, false), encoding, isConsoleCloseable);
    
    if (game == null)
      throw new IllegalArgumentException("game may not be null");
    
    this.game = game;
    
    connection.getListenerManager().addGameListener(new GameAdapter(){
      public void gameEnded(GameEndEvent evt){
        if (getGame().equals(evt.getGame()))
          GameConsoleDesignation.this.gameEnded();
      }
    });
    
    game.addPropertyChangeListener(new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
        setName(consoleNameForGame(GameConsoleDesignation.this.game, gameHasEnded));
      }
    });
  }
  
  
  
  /**
   * Returns the name we should use for the console.
   * 
   * @param game The game.
   * @param gameHasEnded Whether the game has ended.
   */
  
  
  private static String consoleNameForGame(Game game, boolean gameHasEnded){
    if (gameHasEnded)
      return I18n.get(GameConsoleDesignation.class).getFormattedString("endedGameName",
          new Object[]{game.getShortDescription()});
    else
      return game.getShortDescription();
  }
  
  
  
  /**
   * Invoked when the game ends. The default implementation makes the console
   * closeable.
   */
  
  protected void gameEnded(){
    gameHasEnded = true;
    setConsoleCloseable(true);
    setName(consoleNameForGame(game, gameHasEnded));
  }
  
  
  
  /**
   * Returns the game we're covering.
   */
  
  public Game getGame(){
    return game;
  }
  
  
  
}
