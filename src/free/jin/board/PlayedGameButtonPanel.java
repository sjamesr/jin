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

package free.jin.board;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import free.jin.event.GameAdapter;
import free.jin.event.GameListener;
import free.jin.event.OfferEvent;
import free.jin.event.GameEndEvent;
import free.jin.Game;
import free.jin.Connection;
import free.jin.plugin.Plugin;
import free.workarounds.FixedJPanel;


/**
 * The panel which contains all the action buttons for a played game of type
 * Game.MY_GAME.
 */

public class PlayedGameButtonPanel extends FixedJPanel implements ActionListener{



  /**
   * The offer state - when the user can merely offer an abort/adjourn/draw by
   * pressing the corresponding button.
   */

  protected static final int OFFER_STATE = 1;


  

  /**
   * The claim state - when the user can claim an abort/adjourn/draw by pressing
   * the corresponding button, without his opponent's consent.
   */

  protected static final int CLAIM_STATE = 2;



  /**
   * The accept state - when the abort/adjourn/draw has been offered by the
   * opponent and the user can accept it by pressing the corresponding button.
   */

  protected static final int ACCEPT_STATE = 3;



  /**
   * The size of the state border.
   */

  private static final int STATE_BORDER_SIZE = 2;




  /**
   * The button border for the offer state.
   */

  private static final Border offerStateBorder = 
    new EmptyBorder(STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE);



  /**
   * The button border for the claim state.
   */

  private static final Border claimStateBorder = new LineBorder(Color.orange, STATE_BORDER_SIZE);



  /**
   * The button border for the accept state.
   */

  private static final Border acceptStateBorder = 
    new LineBorder(Color.green.darker(), STATE_BORDER_SIZE);




  /**
   * The Plugin we're being used by.
   */

  protected final Plugin plugin;




  /**
   * The Game for which this PlayedGameButtonPanel is used.
   */

  protected final Game game;




  /**
   * The component over which JOptionPane dialogs are displayed.
   */

  protected final Component parentComponent;




  /**
   * The "Resign" button.
   */

  protected JButton resignButton;



  /**
   * The panel for the border of the resign button.
   */

  private JPanel resignButtonPanel;



  /**
   * The "Draw" button.
   */

  protected JButton drawButton;



  /**
   * The panel for the border of the draw button.
   */

  private JPanel drawButtonPanel;



  /**
   * The "Abort" button.
   */

  protected JButton abortButton;



  /**
   * The panel for the border of the abort button.
   */

  private JPanel abortButtonPanel;



  /**
   * The "Adjourn" button.
   */

  protected JButton adjournButton;



  /**
   * The panel for the border of the adjourn button.
   */

  private JPanel adjournButtonPanel;



  /**
   * The game listener that gets notified of various game events we're
   * interested in.
   */

  private GameListener gameListener = new GameAdapter(){


    public void offerUpdated(OfferEvent evt){
      if (evt.getGame() != game)
        return;

      // getUserPlayer shouldn't return null here because this panel should only
      // be used for games played by the user.
      if (evt.getPlayer().equals(game.getUserPlayer().getOpponent())){
        switch (evt.getOfferId()){
          case OfferEvent.DRAW_OFFER: drawOfferUpdate(evt.isOffered()); break;
          case OfferEvent.ABORT_OFFER: abortOfferUpdate(evt.isOffered()); break;
          case OfferEvent.ADJOURN_OFFER: adjournOfferUpdate(evt.isOffered()); break;
        }
      }

      super.offerUpdated(evt);
    }

    public void gameEnded(GameEndEvent evt){
      if (evt.getGame() != game)
        return;

      plugin.getConn().getListenerManager().removeGameListener(this);

      super.gameEnded(evt);
    }


  };
  

  /**
   * Creates a new PlayedGameButtonPanel. It will be used by the given Plugin
   * for the given Game. The given parent Component determines over which component
   * JOptionPane dialogs will be displayed.
   */

  public PlayedGameButtonPanel(Plugin plugin, Game game, Component parentComponent){
    this.plugin = plugin;
    this.game = game;
    this.parentComponent = parentComponent;

    init(plugin, game);
  }




  /**
   * Initializes this PlayedGameButtonPanel. This method calls delegates to
   * {@link #createComponents(Plugin, Game)} and
   * {@link #addComponents(Plugin, Game)}
   */

  protected void init(Plugin plugin, Game game){
    createComponents(plugin, game);
    addComponents(plugin, game);

    setDrawState(OFFER_STATE);
    setAbortState(OFFER_STATE);
    setAdjournState(OFFER_STATE);
    setResignState(CLAIM_STATE);

    plugin.getConn().getListenerManager().addGameListener(gameListener);
  }




  /**
   * Gets called when the state of the draw offer (by the opponent) changes.
   */

  protected void drawOfferUpdate(boolean isOffered){
    setDrawState(isOffered ? ACCEPT_STATE : OFFER_STATE);
  }



  /**
   * Gets called when the state of the abort offer (by the opponent) changes.
   */

  protected void abortOfferUpdate(boolean isOffered){
    setAbortState(isOffered ? ACCEPT_STATE : OFFER_STATE);
  }



  /**
   * Gets called when the state of the adjourn offer (by the opponent) changes.
   */

  protected void adjournOfferUpdate(boolean isOffered){
    setAdjournState(isOffered ? ACCEPT_STATE : OFFER_STATE);
  }




  /**
   * Creates all the components of this PlayedGameButtonPanel.
   */

  protected void createComponents(Plugin plugin, Game game){
    Connection conn = plugin.getConn();

    resignButton = createResignButton(plugin, game);
    drawButton = createDrawButton(plugin, game);
    abortButton = conn.isAbortSupported() ? createAbortButton(plugin, game) : null;
    adjournButton = conn.isAdjournSupported() ? createAdjournButton(plugin, game) : null;
  }




  /**
   * Creates the "Resign" button.
   */

  protected JButton createResignButton(Plugin plugin, Game game){
    JButton button = new JButton("Resign");
    button.setFont(new Font("SansSerif", Font.BOLD, 15));
    button.addActionListener(this);
    button.setDefaultCapable(false);
    button.setRequestFocusEnabled(false);

    return button;
  }




  /**
   * Creates the "Draw" button.
   */

  protected JButton createDrawButton(Plugin plugin, Game game){
    JButton button = new JButton("Draw");
    button.setFont(new Font("SansSerif", Font.BOLD, 15));
    button.addActionListener(this);
    button.setDefaultCapable(false);
    button.setRequestFocusEnabled(false);

    return button;
  }




  /**
   * Creates the "Abort" button.
   */

  protected JButton createAbortButton(Plugin plugin, Game game){
    JButton button = new JButton("Abort");
    button.setFont(new Font("SansSerif", Font.BOLD, 15));
    button.addActionListener(this);
    button.setDefaultCapable(false);
    button.setRequestFocusEnabled(false);

    return button;
  }




  /**
   * Creates the "Adjourn" button.
   */

  protected JButton createAdjournButton(Plugin plugin, Game game){
    JButton button = new JButton("Adjourn");
    button.setFont(new Font("SansSerif", Font.BOLD, 15));
    button.addActionListener(this);
    button.setDefaultCapable(false);
    button.setRequestFocusEnabled(false);

    return button;
  }



  /**
   * Sets the draw button's state to the specified value.
   */

  protected void setDrawState(int state){
    switch (state){
      case OFFER_STATE:{
        drawButton.setToolTipText("Offer a draw");
        drawButtonPanel.setBorder(offerStateBorder);
        break;
      }
      case CLAIM_STATE:{
        drawButton.setToolTipText("Claim a draw");
        drawButtonPanel.setBorder(claimStateBorder);
        break;
      }
      case ACCEPT_STATE:{
        drawButton.setToolTipText("Accept a draw");
        drawButtonPanel.setBorder(acceptStateBorder);
        break;
      }
      default:
        throw new IllegalArgumentException("Unrecognized state: "+state);
    }
  }




  /**
   * Sets the abort button's state to the specified value.
   */

  protected void setAbortState(int state){
    if (abortButton == null)
      return;

    switch (state){
      case OFFER_STATE:{
        abortButton.setToolTipText("Offer to abort the game");
        abortButtonPanel.setBorder(offerStateBorder);
        break;
      }
      case CLAIM_STATE:{
        abortButton.setToolTipText("Abort the game");
        abortButtonPanel.setBorder(claimStateBorder);
        break;
      }
      case ACCEPT_STATE:{
        abortButton.setToolTipText("Agree to abort the game");
        abortButtonPanel.setBorder(acceptStateBorder);
        break;
      }
      default:
        throw new IllegalArgumentException("Unrecognized state: "+state);
    }
  }




  /**
   * Sets the adjourn button's state to the specified value.
   */

  protected void setAdjournState(int state){
    if (adjournButton == null)
      return;

    switch (state){
      case OFFER_STATE:{
        adjournButton.setToolTipText("Offer to adjourn the game");
        adjournButtonPanel.setBorder(offerStateBorder);
        break;
      }
      case CLAIM_STATE:{
        adjournButton.setToolTipText("Adjourn the game");
        adjournButtonPanel.setBorder(claimStateBorder);
        break;
      }
      case ACCEPT_STATE:{
        adjournButton.setToolTipText("Agree to adjourn the game");
        adjournButtonPanel.setBorder(acceptStateBorder);
        break;
      }
      default:
        throw new IllegalArgumentException("Unrecognized state: "+state);
    }
  }



  /**
   * Sets the resign button's state to the specified value. This button may only
   * be in the claim state.
   */

  protected void setResignState(int state){
    switch (state){
      case OFFER_STATE: 
        throw new IllegalArgumentException("The resign button may only be in claim state");
      case CLAIM_STATE:{
        resignButton.setToolTipText("Resign the game");
        resignButtonPanel.setBorder(new EmptyBorder(
          STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE));
        break;
      }
      case ACCEPT_STATE:
        throw new IllegalArgumentException("The resign button may only be in claim state");
      default:
        throw new IllegalArgumentException("Unrecognized state: "+state);
    }
  }



  /**
   * Adds all the components to this PlayedGameButtonPanel.
   */

  protected void addComponents(Plugin plugin, Game game){
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    resignButtonPanel = new JPanel(new BorderLayout());
    resignButtonPanel.add(resignButton, BorderLayout.CENTER);

    drawButtonPanel = new JPanel(new BorderLayout());
    drawButtonPanel.add(drawButton, BorderLayout.CENTER);

    Box upperBox = Box.createHorizontalBox();
    upperBox.add(drawButtonPanel);
    upperBox.add(Box.createHorizontalStrut(10));
    upperBox.add(resignButtonPanel);

    add(upperBox);

    if ((abortButton != null) || (adjournButton != null)){
      Box lowerBox = Box.createHorizontalBox();
      if (abortButton != null){
        abortButtonPanel = new JPanel(new BorderLayout());
        abortButtonPanel.add(abortButton, BorderLayout.CENTER);

        lowerBox.add(abortButtonPanel);
        lowerBox.add(Box.createHorizontalStrut(10));
      }
      if (adjournButton != null){
        adjournButtonPanel = new JPanel(new BorderLayout());
        adjournButtonPanel.add(adjournButton, BorderLayout.CENTER);

        lowerBox.add(adjournButtonPanel);
      }

      add(Box.createVerticalStrut(10));
      add(lowerBox);
    }

  }



  /**
   * ActionListener implementation. Executes the appropriate command depending
   * on the button that was pressed.
   */

  public void actionPerformed(ActionEvent evt){
    Object source = evt.getSource();

    Connection conn = plugin.getConn();
    if (source==resignButton){
      int result = JOptionPane.showConfirmDialog(parentComponent, "Are you sure you want to resign?", "Resign?", JOptionPane.YES_NO_OPTION);
      if (result==JOptionPane.YES_OPTION)
        conn.resign(game);
    }
    else if (source==drawButton){
      conn.requestDraw(game);
    }
    else if (source==abortButton){
      conn.requestAbort(game);
    }
    else if (source==adjournButton){
      conn.requestAdjourn(game);
    }
  }




  /**
   * Overrides getMaximumSize() to return the value of getPreferredSize().
   */

  public Dimension getMaximumSize(){
    return getPreferredSize();
  }


}
