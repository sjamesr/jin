/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.hightemplar.com/jin/.
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

package free.jin.gamelogger;

import java.io.*;
import free.jin.event.*;
import free.chess.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.swing.JOptionPane;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginContext;
import free.jin.plugin.UnsupportedContextException;
import free.jin.JinConnection;
import free.jin.PGNJinConnection;
import free.jin.Game;
import bsh.Interpreter;
import bsh.EvalError;



/**
 * A plugin which allows logging games.
 */

public class GameLogger extends Plugin implements GameListener{



  /**
   * The DateFormat used for formatting the Date pgn tag.
   */

  private static final DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");




  /**
   * The DateFormat used for formatting the Time pgn tag.
   */

  private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");




  /**
   * Maps Game objects to GameInfo objects.
   */

  private final Hashtable gamesToGameInfo = new Hashtable();




  /**
   * An array of conditions for logging games. The n-th item specifies that a
   * game matching it should be logged into the file specified by the n-th item
   * in the <code>loggingFilenames</code> array.
   */

  private String [] loggingConditions = null;




  /**
   * An array of file names specifying the files into which games should be
   * logged.
   */

  private String [] loggingFilenames = null;

  



  /**
   * Sets the plugin context - if the connection is not an instance of
   * <code>SANMoveJinConnection</code>, this method throws an
   * <code>UnsupportedContextException</code>.
   */

  public void setContext(PluginContext context) throws UnsupportedContextException{
    if (!(context.getConnection() instanceof PGNJinConnection))
      throw new UnsupportedContextException("The connection doesn't implement the features necessary for game logging");

    super.setContext(context);
  }



  /**
   * Initializes the plugin.
   */

  public void start(){
    registerListeners();
    loadLoggingConditions();
  }




  /**
   * Stops the plugin.
   */

  public void stop(){
    unregisterListeners();
  }




  
  /**
   * Registers all the necessary listeners.
   */

  protected void registerListeners(){
    JinConnection conn = getConnection();
    JinListenerManager listenerManager = conn.getJinListenerManager();

    listenerManager.addGameListener(this);
  }





  /**
   * Unregisters all the listeners registered by <code>registerListeners()</code>.
   */

  protected void unregisterListeners(){
    JinConnection conn = getConnection();
    JinListenerManager listenerManager = conn.getJinListenerManager();

    listenerManager.removeGameListener(this);
  }




  /**
   * Loads the conditions for logging games and the file names to which the
   * games should be logged.
   */

  private void loadLoggingConditions(){
    int conditionsCount = getIntegerProperty("logging.conditions.count", 0);
    loggingConditions = new String[conditionsCount];
    loggingFilenames = new String[conditionsCount];

    for (int i = 0; i < conditionsCount; i++){
      loggingConditions[i] = getProperty("logging.condition-"+(i+1));
      loggingFilenames[i] = getProperty("logging.filename-"+(i+1));
    }
  }




  /**
   * Returns an array of names of the files into which the specified game should
   * be logged. Returns <code>null</code> if the specified game should not be 
   * logged at all.
   */

  private String [] getFilesToLogInto(Game game){
    GameInfo gameInfo = (GameInfo)gamesToGameInfo.get(game);

    Vector files = new Vector();
    for (int i = 0; i < loggingConditions.length; i++){
      String condition = loggingConditions[i];
      Interpreter bsh = new Interpreter();
      boolean isUserWhite = game.getUserPlayer().isWhite();
      try{
        bsh.set("variant", game.getVariant().getName());
        bsh.set("rating", isUserWhite ? game.getBlackRating() : game.getWhiteRating());
        bsh.set("time", isUserWhite ? game.getWhiteTime() : game.getBlackRating());
        bsh.set("inc", isUserWhite ? game.getWhiteInc() : game.getBlackInc());
        bsh.set("etime", isUserWhite ? (game.getWhiteTime() + 2*game.getWhiteInc()/3) : 
                                       (game.getBlackTime() + 2*game.getBlackInc()/3));
        bsh.set("rated", game.isRated());
        bsh.set("title", isUserWhite ? game.getBlackTitles() : game.getWhiteTitles());
        bsh.set("moves", gameInfo.movelist.size());
        bsh.set("result", getResultString(isUserWhite, game.getResult()));

        boolean result = ((Boolean)bsh.eval(condition)).booleanValue();
        if (result)
          files.addElement(loggingFilenames[i]);
      } catch (EvalError e){
          e.printStackTrace();
        }
    }

    if (files.size() == 0)
      return null;

    String [] filenames = new String[files.size()];
    for (int i = 0; i < filenames.length; i++)
      filenames[i] = (String)files.elementAt(i);

    return filenames;
  }




  /**
   * Returns the result string for the specified result code and a boolean
   * value specifying whether the user is playing with the white pieces.
   */

  private static final String getResultString(boolean isUserWhite, int result){
    switch (result){
      case Game.UNKNOWN_RESULT:
        return "unknown";
      case Game.DRAW:
        return "draw";
      case Game.WHITE_WINS:
        if (isUserWhite)
          return "win";
        else
          return "loss";
      case Game.BLACK_WINS:
        if (isUserWhite)
          return "loss";
        else
          return "win";
      default:
        throw new IllegalArgumentException("Bad result value: "+result);
    }
  }




  /**
   * Returns <code>true</code> if the specified game can be logged, that
   * is, do we know how to log it.
   */

  protected boolean canLog(Game game){
    return (game.getVariant() instanceof Chess) && (game.getGameType() == Game.MY_GAME) && game.isPlayed();
  }




  /**
   * Logs the specified game into the specified file.
   */

  private void log(Game game, String filename){
    GameInfo gameInfo = (GameInfo)gamesToGameInfo.get(game);

    try{
      String resultString;
      switch (game.getResult()){
        case Game.WHITE_WINS:
          resultString = "1-0";
          break;
        case Game.BLACK_WINS:
          resultString = "0-1";
          break;
        case Game.DRAW:
          resultString = "1/2-1/2";
          break;
        default:
          resultString = "*";
          break;
      }

      DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename, true)));
      out.writeBytes("[Event \"Casual Game\"]\n");
      out.writeBytes("[Site \""+getUser().getServer().getLongName()+"\"]\n");
      out.writeBytes("[Date \""+dateFormat.format(gameInfo.gameStartDate)+"\"]\n");
      out.writeBytes("[Round \"-\"]\n");
      out.writeBytes("[White \""+game.getWhiteName()+"\"]\n");
      out.writeBytes("[Black \""+game.getBlackName()+"\"]\n");
      out.writeBytes("[Result \""+resultString+"\"]\n");
      out.writeBytes("[Time \""+timeFormat.format(gameInfo.gameStartDate)+"\"]\n");
      if (!game.isTimeOdds()){
        out.writeBytes("[TimeControl \""+game.getWhiteTime()/1000+"+"+game.getWhiteInc()+"\"]\n");
      }
      out.writeBytes("[Mode \"ICS\"]\n");
      if (!gameInfo.initPos.getFEN().equals(Chess.INITIAL_POSITION_FEN)){
        out.writeBytes("[SetUp \"1\"]\n");
        out.writeBytes("[FEN \""+gameInfo.initPos.getFEN()+"\"]\n");
      }

      out.writeBytes("\n");

      Vector movelist = gameInfo.movelist;
      int moveCount = movelist.size();
      StringBuffer lineBuf = new StringBuffer();
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < moveCount; i++){
        buf.setLength(0);
        ChessMove move = (ChessMove)movelist.elementAt(i);
        Player movingPlayer = move.getPlayer();
        String san = move.getSAN();
        if ((i == 0) && movingPlayer.isBlack()){
          buf.append("1... ");
          buf.append(san);
        }
        else{
          if (movingPlayer.isWhite()){
            buf.append(String.valueOf(1+i/2));
            buf.append(". ");
          }
          buf.append(san);
        }
        if (lineBuf.length() + 1 + buf.length() > 80){ // +1 is for the space between them
          out.writeBytes(lineBuf.toString());
          out.writeBytes("\n");
          lineBuf.setLength(0);
          lineBuf.append(buf.toString());
        }
        else{
          if (lineBuf.length() != 0)
            lineBuf.append(" ");
          lineBuf.append(buf.toString());
        }

        buf.setLength(0);
      }

      if (lineBuf.length() + 1 + resultString.length() > 80){ // +1 is for the space between them
        out.writeBytes(lineBuf.toString());
        out.writeBytes("\n");
        out.writeBytes(resultString);
      }
      else{
        out.writeBytes(lineBuf.toString());
        out.writeBytes(" ");
        out.writeBytes(resultString);
      }

      out.writeBytes("\n\n");

      out.close();
    } catch (IOException e){
        e.printStackTrace();
        JOptionPane.showMessageDialog(getPluginContext().getMainFrame(), "Unable to log game ("+e.getMessage()+")", "I/O Error", JOptionPane.ERROR_MESSAGE);
      }
  }



  
  /**
   * Starts logging the moves.
   */

  public void gameStarted(GameStartEvent evt){
    Game game = evt.getGame();
    Position initPos = game.getInitialPosition();
    GameInfo gameInfo = new GameInfo(initPos);
    gamesToGameInfo.put(game, gameInfo);
  }




  /**
   * Saves the ended game.
   */

  public void gameEnded(GameEndEvent evt){
    Game game = evt.getGame();
    if (canLog(game)){
      String [] filenames = getFilesToLogInto(game);
      for (int i = 0; i < filenames.length; i++)
        log(game, filenames[i]);
    }
    gamesToGameInfo.remove(game);
  }




  /**
   * Saves the move.
   */

  public void moveMade(MoveMadeEvent evt){
    Game game = evt.getGame();
    Move move = evt.getMove();
    GameInfo gameInfo = (GameInfo)gamesToGameInfo.get(game);
    gameInfo.movelist.addElement(move);
  }




  /**
   * Clears the move list.
   */

  public void positionChanged(PositionChangedEvent evt){
    Game game = evt.getGame();
    GameInfo gameInfo = (GameInfo)gamesToGameInfo.get(game);
    gameInfo.initPos = evt.getPosition();
  }




  /**
   * Removes the undone moves from the move list.
   */

  public void takebackOccurred(TakebackEvent evt){
    Game game = evt.getGame();
    GameInfo gameInfo = (GameInfo)gamesToGameInfo.get(game);
    Vector movelist = gameInfo.movelist;
    int takebackCount = evt.getTakebackCount();
    for (int i = 0; i < takebackCount; i++)
      movelist.removeElementAt(movelist.size() - 1);
  }




  /**
   * GameListener implementation.
   */

  public void illegalMoveAttempted(IllegalMoveEvent evt){}
  public void clockAdjusted(ClockAdjustmentEvent evt){}
  public void boardFlipped(BoardFlipEvent evt){}




  /**
   * A small class bundling information about a game.
   */

  private static class GameInfo{


    /**
     * The initial position.
     */

    public Position initPos;



    /**
     * The move list.
     */

    public Vector movelist;




    /**
     * A Date object representing the time when the game started.
     */

    public final Date gameStartDate;



    /**
     * Creates a new GameInfo with the specified initial position.
     */

    public GameInfo(Position initPos){
      this.initPos = initPos;
      movelist = new Vector();
      gameStartDate = new Date();
    }

  }

}
