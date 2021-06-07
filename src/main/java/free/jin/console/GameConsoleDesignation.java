/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2007 Alexander Maryanovsky. All rights reserved.
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
package free.jin.console;

import free.jin.Connection;
import free.jin.Game;
import free.jin.I18n;
import free.jin.event.ChatEvent;
import free.jin.event.GameAdapter;
import free.jin.event.GameEndEvent;
import free.jin.event.IllegalMoveEvent;
import free.jin.event.JinEvent;
import free.jin.event.TakebackEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/** A console designation for chat at a board. */
public abstract class GameConsoleDesignation extends AbstractConsoleDesignation {

  /** The game we're covering. */
  protected final Game game;

  /** Whether the game has ended. */
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
  public GameConsoleDesignation(
      Connection connection, Game game, String encoding, boolean isConsoleCloseable) {
    super(connection, consoleNameForGame(game, false), encoding, isConsoleCloseable);

    if (game == null) throw new IllegalArgumentException("game may not be null");

    this.game = game;

    connection
        .getListenerManager()
        .addGameListener(
            new GameAdapter() {
              @Override
              public void gameEnded(GameEndEvent evt) {
                if (getGame().equals(evt.getGame())) GameConsoleDesignation.this.gameEnded(evt);
              }

              @Override
              public void illegalMoveAttempted(IllegalMoveEvent evt) {
                if (getGame().equals(evt.getGame()))
                  GameConsoleDesignation.this.illegalMoveAttempted(evt);
              }

              @Override
              public void takebackOccurred(TakebackEvent evt) {
                if (getGame().equals(evt.getGame()))
                  GameConsoleDesignation.this.takebackOccurred(evt);
              }
            });

    game.addPropertyChangeListener(
        new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent evt) {
            setName(consoleNameForGame(GameConsoleDesignation.this.game, gameHasEnded));

            if ("resultCode".equals(evt.getPropertyName()))
              getConsole().addToOutput(getGame().getGameEndReasonDescription(), "gameInfo");
          }
        });
  }

  /**
   * Returns the name we should use for the console.
   *
   * @param game The game.
   * @param gameHasEnded Whether the game has ended.
   */
  private static String consoleNameForGame(Game game, boolean gameHasEnded) {
    if (gameHasEnded)
      return I18n.get(GameConsoleDesignation.class)
          .getFormattedString("endedGameName", new Object[] {game.getShortDescription()});
    else return game.getShortDescription();
  }

  /** Invoked when the game ends. */
  protected void gameEnded(GameEndEvent evt) {
    gameHasEnded = true;
    setConsoleCloseable(true);
    setName(consoleNameForGame(game, gameHasEnded));
  }

  /** Invoked when an illegal move is attempted in the game. */
  protected void illegalMoveAttempted(IllegalMoveEvent evt) {
    I18n i18n = I18n.get(GameConsoleDesignation.class);

    String i18nKey;
    switch (evt.getReasonCode()) {
      case IllegalMoveEvent.ILLEGAL_MOVE:
        i18nKey = "illegalMove";
        break;
      case IllegalMoveEvent.NOT_YOUR_TURN:
        i18nKey = "notYourTurn";
        break;
      case IllegalMoveEvent.OTHER:
        i18nKey = "other";
        break;
      default:
        throw new IllegalStateException("Bad reason code value: " + evt.getReasonCode());
    }
    i18nKey = "moveRejectedMessage." + i18nKey;

    Object[] args = new Object[] {evt.getMove().toString()};
    getConsole().addToOutput(i18n.getFormattedString(i18nKey, args), "gameInfo");
  }

  /** Invoked when a takeback occurs in the game. */
  protected void takebackOccurred(TakebackEvent evt) {
    if (!game.isPlayed()) return;

    I18n i18n = I18n.get(GameConsoleDesignation.class);
    String i18nKey = "takebackMessage";
    Object[] args = new Object[] {String.valueOf(evt.getTakebackCount())};
    getConsole().addToOutput(i18n.getFormattedString(i18nKey, args), "gameInfo");
  }

  /** Returns the game we're covering. */
  public Game getGame() {
    return game;
  }

  /**
   * Accepts chata events of category <code>GAME_CHAT_CATEGORY</code> where the forum is the game
   * we're covering.
   */
  @Override
  protected boolean accept(JinEvent evt) {
    if (isTaggedByUs(evt)) return true;

    if (!(evt instanceof ChatEvent)) return false;

    ChatEvent chatEvent = (ChatEvent) evt;
    return (chatEvent.getCategory() == ChatEvent.GAME_CHAT_CATEGORY)
        && game.equals(chatEvent.getForum());
  }
}
