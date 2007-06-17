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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import free.jin.Connection;
import free.jin.Game;
import free.jin.I18n;
import free.jin.event.GameAdapter;
import free.jin.event.GameEndEvent;
import free.jin.event.GameListener;
import free.jin.event.OfferEvent;
import free.jin.plugin.Plugin;
import free.jin.ui.OptionPanel;
import free.util.PlatformUtils;
import free.util.TableLayout;
import free.util.swing.SwingUtils;
import free.util.swing.WrapLayout;
import free.workarounds.FixedJPanel;


/**
 * The panel which contains all the action buttons for a played game of type
 * Game.MY_GAME.
 */

public class PlayedGameButtonPanel extends FixedJPanel implements ActionListener{
  
  
  
  /**
   * The offered state - when the offer has already been made by the user.
   */
  
  protected static final int OFFERED_STATE = 0;
  
  
  
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
  
  private static final int STATE_BORDER_SIZE = PlatformUtils.isMacOSX() ? 3 : 6;
  
  
  
  /**
   * The color of the offered state.
   */
  
  private static final Color OFFERED_STATE_COLOR = null;
  
  
  
  /**
   * The button border for the offered state.
   */
  
  private static final Border OFFERED_STATE_BORDER = 
    new EmptyBorder(STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE);
  
  
  
  /**
   * The color of the offer state.
   */
  
  private static final Color OFFER_STATE_COLOR = null;
  
  
  
  /**
   * The button border for the offer state.
   */
  
  private static final Border OFFER_STATE_BORDER = 
    new EmptyBorder(STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE);
  
  
  
  /**
   * The color of the claim state.
   */
  
  private static final Color CLAIM_STATE_COLOR = Color.orange.darker();
  
  
  
  /**
   * The button border for the claim state.
   */
  
  private static final Border CLAIM_STATE_BORDER = new LineBorder(CLAIM_STATE_COLOR, STATE_BORDER_SIZE);
  
  
  
  /**
   * The color of the accept state.
   */
  
  private static final Color ACCEPT_STATE_COLOR = Color.green.darker();
  
  
  
  /**
   * The button border for the accept state.
   */
  
  private static final Border ACCEPT_STATE_BORDER = new LineBorder(ACCEPT_STATE_COLOR, STATE_BORDER_SIZE);
  
  
  
  
  
  
  /**
   * The Plugin we're being used by.
   */
  
  protected final Plugin plugin;
  
  
  
  
  /**
   * The Game for which this PlayedGameButtonPanel is used.
   */
  
  protected final Game game;
  
  
  
  
  /**
   * The component over which confirmation dialogs are displayed.
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
   * The takeback1 (1 ply) button.
   */
  
  protected JButton takeback1Button;
  
  
  
  /**
   * The button for the border of the takeback (1) button.
   */
  
  private JPanel takeback1ButtonPanel;
  
  
  
  /**
   * The takeback2 (2 plies) button.
   */
  
  protected JButton takebackNButton;
  
  
  
  /**
   * The button for the border of the multiple takeback button.
   */
  
  private JPanel takebackNButtonPanel;
  
  
  
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
      boolean isOppsOffer = evt.getPlayer().equals(game.getUserPlayer().getOpponent());
      switch (evt.getOfferId()){
        case OfferEvent.DRAW_OFFER:
          drawOfferUpdate(isOppsOffer, evt.isOffered()); break;
        case OfferEvent.ABORT_OFFER:
          abortOfferUpdate(isOppsOffer, evt.isOffered()); break;
        case OfferEvent.ADJOURN_OFFER:
          adjournOfferUpdate(isOppsOffer, evt.isOffered()); break;
        case OfferEvent.TAKEBACK_OFFER:
          takebackOfferUpdate(isOppsOffer, evt.isOffered(), evt.getTakebackCount()); break;
      }
      
      super.offerUpdated(evt);
    }
    
    public void gameEnded(GameEndEvent evt){
      if (evt.getGame() != game)
        return;
      
      drawButton.setEnabled(false);
      resignButton.setEnabled(false);
      if (abortButton != null)
        abortButton.setEnabled(false);
      if (adjournButton != null)
        adjournButton.setEnabled(false);
      if (takeback1Button != null)
        takeback1Button.setEnabled(false);
      if (takebackNButton != null)
        takebackNButton.setEnabled(false);
      
      plugin.getConn().getListenerManager().removeGameListener(this);
      
      super.gameEnded(evt);
    }
    
    
  };
  
  
  /**
   * Creates a new PlayedGameButtonPanel. It will be used by the given Plugin
   * for the given Game. The given parent Component determines over which component
   * confirmation dialogs will be displayed.
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
    setTakeback1State(OFFER_STATE);
    setTakebackNState(OFFER_STATE, 2);
    
    plugin.getConn().getListenerManager().addGameListener(gameListener);
  }
  
  
  
  
  /**
   * Gets called when the state of the draw offer (by the opponent) changes.
   */
  
  protected void drawOfferUpdate(boolean isOppsOffer, boolean isOffered){
    setDrawState(isOffered ? (isOppsOffer ? ACCEPT_STATE : OFFERED_STATE) : OFFER_STATE);
  }
  
  
  
  /**
   * Gets called when the state of the abort offer (by the opponent) changes.
   */
  
  protected void abortOfferUpdate(boolean isOppsOffer, boolean isOffered){
    setAbortState(isOffered ? (isOppsOffer ? ACCEPT_STATE : OFFERED_STATE) : OFFER_STATE);
  }
  
  
  
  /**
   * Gets called when the state of the adjourn offer (by the opponent) changes.
   */
  
  protected void adjournOfferUpdate(boolean isOppsOffer, boolean isOffered){
    setAdjournState(isOffered ? (isOppsOffer ? ACCEPT_STATE : OFFERED_STATE) : OFFER_STATE);
  }
  
  
  
  /**
   * A list of the ply counts for all of the user's current outstanding
   * takeback offers.
   */
  
  private final Vector userTakebacks = new Vector();
  
  
  
  /**
   * A list of the ply counts for all of the opponent's current outstanding
   * takeback offers.
   */
  
  private final Vector oppTakebacks = new Vector();
  
  
  
  /**
   * Gets called when the state of the takeback offer (by the opponent) changes.
   */
  
  protected void takebackOfferUpdate(boolean isOppsOffer, boolean isOffered, int plyCount){
    Vector offers = isOppsOffer ? oppTakebacks : userTakebacks;
    if (isOffered)
      offers.addElement(new Integer(plyCount));
    else
      offers.removeElement(new Integer(plyCount));
    
    int newState;
    int plies;
    if (oppTakebacks.isEmpty()){
      if (userTakebacks.isEmpty()){
        newState = OFFER_STATE;
        plies = plyCount;
      }
      else{
        Integer lastOffer = (Integer)userTakebacks.elementAt(userTakebacks.size() - 1);
        newState = OFFERED_STATE;
        plies = lastOffer.intValue();
      }
    }
    else{
      Integer lastOffer = (Integer)oppTakebacks.elementAt(oppTakebacks.size() - 1);
      newState = ACCEPT_STATE;
      plies = lastOffer.intValue();
    }
    
    if (plies == 1)
      setTakeback1State(newState);
    else
      setTakebackNState(newState, newState == OFFER_STATE ? 2 : plies);
  }
  
  
  
  
  /**
   * Creates all the components of this PlayedGameButtonPanel.
   */
  
  protected void createComponents(Plugin plugin, Game game){
    Connection conn = plugin.getConn();
    
    resignButton = createButton("resignButton");
    drawButton = createButton("drawButton");
    abortButton = conn.isAbortSupported() ? createButton("abortButton") : null;
    adjournButton = conn.isAdjournSupported() ? createButton("adjournButton") : null;
    takeback1Button = conn.isTakebackSupported() ? createButton("takebackButton") : null;
    takebackNButton = conn.isMultipleTakebackSupported() ?
        createButton("multipleTakebackButton") : null;
  }
  
  
  
  /**
   * Creates a button with the specified i18n key.
   */
  
  private JButton createButton(String i18nKey){
    I18n i18n = I18n.get(PlayedGameButtonPanel.class);
    JButton button = i18n.createButton(i18nKey);
    button.addActionListener(this);
    button.setDefaultCapable(false);
    button.setRequestFocusEnabled(false);
    
    return button;
  }
  
  
  
  /**
   * Sets the draw button's state to the specified value.
   */
  
  protected void setDrawState(int state){
    setState(drawButton, drawButtonPanel, state, "draw");
  }
  
  
  
  /**
   * Sets the abort button's state to the specified value.
   */
  
  protected void setAbortState(int state){
    setState(abortButton, abortButtonPanel, state, "abort");
  }
  
  
  
  
  /**
   * Sets the adjourn button's state to the specified value.
   */
  
  protected void setAdjournState(int state){
    setState(adjournButton, adjournButtonPanel, state, "adjourn");
  }
  
  
  
  /**
   * Sets the state of the takeback (1) button to the specified state.
   */
  
  protected void setTakeback1State(int state){
    setState(takeback1Button, takeback1ButtonPanel, state, "takeback");
  }
  
  
  
  /**
   * Sets the specified state on the specified button and button panel. 
   */
  
  private void setState(JButton button, JPanel panel, int state, String buttonName){
    if (button == null)
      return;
    
    I18n i18n = I18n.get(PlayedGameButtonPanel.class);
    
    button.setEnabled(state != OFFERED_STATE);    
    
    switch (state){
      case OFFERED_STATE:{
        button.setBackground(OFFERED_STATE_COLOR);
        panel.setBorder(OFFERED_STATE_BORDER);
        break;
      }
      case OFFER_STATE:{
        button.setToolTipText(i18n.getString(buttonName + "Button.offerTooltip"));
        button.setBackground(OFFER_STATE_COLOR);
        panel.setBorder(OFFER_STATE_BORDER);
        break;
      }
      case CLAIM_STATE:{
        button.setToolTipText(i18n.getString(buttonName + "Button.claimTooltip"));
        button.setBackground(CLAIM_STATE_COLOR);
        panel.setBorder(CLAIM_STATE_BORDER);
        break;
      }
      case ACCEPT_STATE:{
        button.setToolTipText(i18n.getString(buttonName + "Button.acceptTooltip"));
        button.setBackground(ACCEPT_STATE_COLOR);
        panel.setBorder(ACCEPT_STATE_BORDER);
        break;
      }
      default:
        throw new IllegalArgumentException("Unrecognized state: " + state);
    }
  }
  
  
  
  /**
   * Sets the state of the takeback (1) button to the specified state.
   */
  
  protected void setTakebackNState(int state, int plyCount){
    if (takebackNButton == null)
      return;
    
    I18n i18n = I18n.get(PlayedGameButtonPanel.class);
    
    Object [] plyCountArr = new Object[]{new Integer(plyCount)};
    
    String buttonTextPattern = i18n.getFormattedString("multipleTakebackButton.text", plyCountArr);
    SwingUtils.applyLabelSpec(takebackNButton, buttonTextPattern);
    takebackNButton.setEnabled(state != OFFERED_STATE);
    takebackNButton.setActionCommand(String.valueOf(plyCount));
    
    
    switch (state){
      case OFFERED_STATE:{
        takebackNButton.setBackground(OFFERED_STATE_COLOR);
        takebackNButtonPanel.setBorder(OFFERED_STATE_BORDER);
        break;
      }
      case OFFER_STATE:{
        takebackNButton.setToolTipText(i18n.getFormattedString("multipleTakebackButton.offerTooltip", plyCountArr));
        takebackNButton.setBackground(OFFER_STATE_COLOR);
        takebackNButtonPanel.setBorder(OFFER_STATE_BORDER);
        break;
      }
      case CLAIM_STATE:{
        takebackNButton.setToolTipText(i18n.getFormattedString("multipleTakebackButton.claimTooltip", plyCountArr));
        takebackNButton.setBackground(CLAIM_STATE_COLOR);
        takebackNButtonPanel.setBorder(CLAIM_STATE_BORDER);
        break;
      }
      case ACCEPT_STATE:{
        takebackNButton.setToolTipText(i18n.getFormattedString("multipleTakebackButton.claimTooltip", plyCountArr));
        takebackNButton.setBackground(ACCEPT_STATE_COLOR);
        takebackNButtonPanel.setBorder(ACCEPT_STATE_BORDER);
        break;
      }
      default:
        throw new IllegalArgumentException("Unrecognized state: " + state);
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
        resignButtonPanel.setBorder(new EmptyBorder(
            STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE));
        break;
      }
      case ACCEPT_STATE:
        throw new IllegalArgumentException("The resign button may only be in claim state");
      default:
        throw new IllegalArgumentException("Unrecognized state: " + state);
    }
  }
  
  
  
  /**
   * Adds all the components to this PlayedGameButtonPanel.
   */
  
  protected void addComponents(Plugin plugin, Game game){
    setLayout(new TableLayout(2, 4, 4));
    
    drawButtonPanel = new JPanel(WrapLayout.getInstance());
    drawButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    drawButtonPanel.add(drawButton, BorderLayout.CENTER);
    add(drawButtonPanel);
    
    resignButtonPanel = new JPanel(WrapLayout.getInstance());
    resignButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    resignButtonPanel.add(resignButton, BorderLayout.CENTER);
    add(resignButtonPanel);
    
    if (abortButton != null){    
      abortButtonPanel = new JPanel(WrapLayout.getInstance());
      abortButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      abortButtonPanel.add(abortButton, BorderLayout.CENTER);
      add(abortButtonPanel);
    }
    
    if (adjournButton != null){
      adjournButtonPanel = new JPanel(WrapLayout.getInstance());
      adjournButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      adjournButtonPanel.add(adjournButton, BorderLayout.CENTER);
      add(adjournButtonPanel);
    }
    
    if (takeback1Button != null){
      takeback1ButtonPanel = new JPanel(WrapLayout.getInstance());
      takeback1ButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      takeback1ButtonPanel.add(takeback1Button, BorderLayout.CENTER);
      add(takeback1ButtonPanel);
    }
    
    if (takebackNButton != null){
      takebackNButtonPanel = new JPanel(WrapLayout.getInstance());
      takebackNButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
      takebackNButtonPanel.add(takebackNButton, BorderLayout.CENTER);
      add(takebackNButtonPanel);
    }
    
    int maxWidth = 0;
    for (int i = 0; i < getComponentCount(); i++){
      Component component = getComponent(i);
      int width = component.getPreferredSize().width;
      if (width > maxWidth)
        maxWidth = width;
    }
    
    add(Box.createHorizontalStrut(maxWidth));
    add(Box.createHorizontalStrut(maxWidth));
  }
  
  
  
  /**
   * ActionListener implementation. Executes the appropriate command depending
   * on the button that was pressed.
   */
  
  public void actionPerformed(ActionEvent evt){
    Object source = evt.getSource();
    
    Connection conn = plugin.getConn();
    if (source == resignButton){
      Object result = I18n.get(PlayedGameButtonPanel.class).confirm(OptionPanel.OK, "resignConfirmation", parentComponent);
      if (result == OptionPanel.OK)
        conn.resign(game);
    }
    else if (source == drawButton)
      conn.requestDraw(game);
    else if (source == abortButton)
      conn.requestAbort(game);
    else if (source == adjournButton)
      conn.requestAdjourn(game);
    else if (source == takeback1Button)
      conn.requestTakeback(game);
    else if (source == takebackNButton){
      int plies = Integer.parseInt(takebackNButton.getActionCommand());
      conn.requestTakeback(game, plies);
    }
  }
  
  
  
  /**
   * Overrides getMaximumSize() to return the value of getPreferredSize().
   */
  
  public Dimension getMaximumSize(){
    return getPreferredSize();
  }
  
  
}
