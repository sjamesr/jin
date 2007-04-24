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

package free.jin.seek;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import free.chess.Player;
import free.chess.WildVariant;
import free.jin.Connection;
import free.jin.FriendsConnection;
import free.jin.I18n;
import free.jin.MatchConnection;
import free.jin.Preferences;
import free.jin.ServerUser;
import free.jin.UserMatchOffer;
import free.jin.UsernamePolicy;
import free.jin.event.FriendsEvent;
import free.jin.event.FriendsListener;
import free.jin.plugin.Plugin;
import free.util.Named;
import free.util.TableLayout;



/**
 * A panel which lets the user match an opponent.
 */

public class IssueMatchPanel extends JPanel{
  
  
  
  /**
   * The plugin we're part of.
   */
  
  private final Plugin plugin;
  
  
  
  /**
   * Our preferences.
   */
  
  private final Preferences prefs;
  
  
  
  // The various UI elements.
  private final OpponentSelection opponentSelection;
  private final TimeControlsSelection timeControlsSelection;
  private final JPanel advancedPanel;
  private final RatednessSelection ratednessSelection;
  private final VariantSelection variantSelection;
  private final PieceColorSelection pieceColorSelection;
  private final MoreLessButton moreLessButton;
  private final JButton issueMatchButton;
  
  
  
  /**
   * Creates a new <code>IssueMatchPanel</code> for the specified plugin and
   * with the specified <code>Preferences</code> object to load settings from.
   */
  
  public IssueMatchPanel(Plugin plugin, Preferences prefs){
    if (plugin == null)
      throw new IllegalArgumentException("plugin may not be null");
    if (prefs == null)
      throw new IllegalArgumentException("prefs may not be null");
    if (!(plugin.getConn() instanceof MatchConnection))
      throw new IllegalArgumentException("Connection must be an instance of MatchConnection");
    
    this.plugin = plugin;
    this.prefs = prefs;
    
    String lastOppName = prefs.getString("lastOpponent", null);
    ServerUser lastOpp = lastOppName == null ? null :
      plugin.getConn().userForName(lastOppName);
    
    WildVariant [] variants = plugin.getConn().getSupportedVariants();
    
    String color = prefs.getString("color", "auto");
    Player pieceColor = "auto".equals(color) ? null :
      ("white".equals(color) ? Player.WHITE_PLAYER : Player.BLACK_PLAYER);
    
    I18n i18n = I18n.get(IssueMatchPanel.class);
    
    opponentSelection = new OpponentSelection(getEasyOpponentsModel(), lastOpp);
    timeControlsSelection = new TimeControlsSelection(prefs.getInt("time", 10), prefs.getInt("inc", 0));
    advancedPanel = new JPanel();
    ratednessSelection = new RatednessSelection(prefs.getBool("isRated", true), plugin.getUser().isGuest());
    variantSelection = new VariantSelection(variants, prefs.getString("variant", "Chess"));
    pieceColorSelection = new PieceColorSelection(pieceColor);
    moreLessButton = new MoreLessButton(advancedPanel, prefs.getBool("isMore", false));
    issueMatchButton = i18n.createButton("issueMatchButton");
    
    issueMatchButton.setEnabled(isSelectionValid());
    opponentSelection.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        System.out.println("Opponent: " + opponentSelection.getOpponentName());
        issueMatchButton.setEnabled(isSelectionValid());
      }
    });
    
    issueMatchButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        MatchConnection conn = (MatchConnection)IssueMatchPanel.this.plugin.getConn();
        conn.issue(getMatchOffer());
      }
    });
    
    createUI();
  }
  
  
  
  /**
   * Sets the current opponent to the specified player (may be <code>null</code>
   * to indicate a blank value). 
   */
  
  public void setOpponent(ServerUser opponent){
    opponentSelection.setOpponent(opponent);
  }
  
  
  
  /**
   * Returns the list of opponents to make easily accessible for matching.
   */
  
  private ListModel getEasyOpponentsModel(){
    Connection conn = plugin.getConn();
     
    if (conn instanceof FriendsConnection)
      return new OnlineFriendsListModel((FriendsConnection)conn);
    else
      return new DefaultListModel();
  }
  
  
  
  /**
   * Creates the UI.
   */
  
  private void createUI(){
    setLayout(new TableLayout(1, 4, 6));
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    buttonPanel.add(moreLessButton);
    buttonPanel.add(issueMatchButton);
    
    opponentSelection.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    timeControlsSelection.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    // Advanced options panel
    advancedPanel.setLayout(new TableLayout(1, 4, 6));

    ratednessSelection.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    variantSelection.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    pieceColorSelection.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    buttonPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    advancedPanel.add(ratednessSelection);
    advancedPanel.add(variantSelection);
    advancedPanel.add(pieceColorSelection);
    
    add(opponentSelection);
    add(timeControlsSelection);
    add(advancedPanel);
    add(buttonPanel);
  }
  
  
  
  /**
   * Saves the panel's preferences.
   */
  
  public void savePrefs(){
    prefs.setString("lastOpponent", opponentSelection.getOpponentName());
    prefs.setInt("time", timeControlsSelection.getTime());
    prefs.setInt("inc", timeControlsSelection.getIncrement());
    prefs.setBool("isRated", ratednessSelection.isRated());
    prefs.setString("variant", variantSelection.getVariant().getName());
    Player color = pieceColorSelection.getColor();
    prefs.setString("color", color == null ? "auto" : color.isWhite() ? "white" : "black");
    prefs.setBool("isMore", moreLessButton.isMore());
  }
  
  
  
  /**
   * Checks the validity of the current selection.
   */
  
  private boolean isSelectionValid(){
    UsernamePolicy policy = plugin.getUser().getServer().getUsernamePolicy();
    return policy.invalidityReason(opponentSelection.getOpponentName()) == null;
  }
  
  
  
  /**
   * Returns the currently specified match offer.
   */
  
  private UserMatchOffer getMatchOffer(){
    ServerUser opp = plugin.getConn().userForName(opponentSelection.getOpponentName());
    int time = timeControlsSelection.getTime();
    int inc = timeControlsSelection.getIncrement();
    boolean isRated = ratednessSelection.isRated();
    WildVariant variant = variantSelection.getVariant();
    Player color = pieceColorSelection.getColor();
    
    return new UserMatchOffer(opp, time, inc, isRated, variant, color);
  }
  
  
  
  /**
   * A list model for the list of friends online.
   */
  
  private static class OnlineFriendsListModel extends AbstractListModel{
    
    
    
    /**
     * The current list of friends.
     */
    
    private final List onlineFriends;
    
    
    
    /**
     * Creates a new <code>OnlineFriendsListModel</code> for the specified
     * <code>FriendsConnection</code>.
     */
    
    public OnlineFriendsListModel(FriendsConnection conn){
      onlineFriends = new LinkedList();
      
      conn.getFriendsListenerManager().addFriendsListener(new FriendsListener(){
        public void friendAdded(FriendsEvent evt){
          update(evt.getFriendsConnection());
        }
        public void friendConnected(FriendsEvent evt){
          update(evt.getFriendsConnection());
        }
        public void friendDisconnected(FriendsEvent evt){
          update(evt.getFriendsConnection());
        }
        public void friendRemoved(FriendsEvent evt){
          update(evt.getFriendsConnection());
        }
        public void friendStateChanged(FriendsEvent evt){
          update(evt.getFriendsConnection());
        }
      });
    }
    
    
    
    /**
     * Updates the our list of online friends.
     */
    
    private void update(FriendsConnection conn){
      onlineFriends.clear();
      onlineFriends.addAll(conn.getOnlineFriends());
      onlineFriends.retainAll(conn.getFriends());
      Collections.sort(onlineFriends, Named.ALPHABETIC_NAME_COMPARATOR);
      
      fireContentsChanged(this, 0, onlineFriends.size());
    }
    
    
    
    /**
     * Returns the <code>index</code>th online friend.
     */
    
    public Object getElementAt(int index){
      return onlineFriends.get(index).toString();
    }
    
    
    
    /**
     * Returns the number of friends online.
     */
    
    public int getSize(){
      return onlineFriends.size();
    }
    
    
    
  }

  
  
  
}
