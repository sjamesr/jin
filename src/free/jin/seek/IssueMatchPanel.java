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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import free.chess.Player;
import free.chess.WildVariant;
import free.jin.FriendsConnection;
import free.jin.I18n;
import free.jin.MatchOfferConnection;
import free.jin.Preferences;
import free.jin.ServerUser;
import free.jin.UserMatchOffer;
import free.jin.UsernamePolicy;
import free.jin.event.FriendsEvent;
import free.jin.event.FriendsListener;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIContainer;
import free.util.AWTUtilities;
import free.util.Named;
import free.util.swing.MoreLessOptionsButton;



/**
 * A panel which lets the user match an opponent.
 */

public class IssueMatchPanel extends JPanel{
  
  
  
  /**
   * The plugin we're being used by.
   */
  
  private final Plugin plugin;
  
  
  
  /**
   * The connection.
   */
  
  private final MatchOfferConnection conn;
  
  
  
  /**
   * Our preferences.
   */
  
  private final Preferences prefs;
  
  
  
  // The various UI elements.
  private final OpponentSelection opponent;
  private final TimeControlsSelection timeControls;
  private final RatednessSelection ratedness;
  private final VariantSelection variant;
  private final PieceColorSelection pieceColor;
  private final MoreLessOptionsButton moreLess;
  private final JButton issueMatch;
  
  
  
  /**
   * Creates a new <code>IssueMatchPanel</code> with the specified arguments and
   * a <code>Preferences</code> object to load/save settings from/to.
   */
  
  public IssueMatchPanel(Plugin plugin, final PluginUIContainer container, Preferences prefs){
    if (plugin == null)
      throw new IllegalArgumentException("plugin may not be null");
    if (container == null)
      throw new IllegalArgumentException("container may not be null");
    if (prefs == null)
      throw new IllegalArgumentException("prefs may not be null");
    if (!(plugin.getConn() instanceof MatchOfferConnection))
      throw new IllegalArgumentException("Connection must be an instance of MatchOfferConnection");
    
    this.plugin = plugin;
    this.conn = (MatchOfferConnection)plugin.getConn();
    this.prefs = prefs;
    
    
    String lastOppName = prefs.getString("lastOpponent", null);
    ServerUser lastOpp = lastOppName == null ? null : conn.userForName(lastOppName);
    
    WildVariant [] variants = conn.getSupportedVariants();
    
    String color = prefs.getString("color", "auto");
    Player pieceColorPref = "auto".equals(color) ? null :
      ("white".equals(color) ? Player.WHITE_PLAYER : Player.BLACK_PLAYER);
    
    I18n i18n = I18n.get(IssueMatchPanel.class);
    
    opponent = new OpponentSelection(getEasyAccessOpponentsModel(), lastOpp);
    timeControls = new TimeControlsSelection(prefs.getInt("time", 10), prefs.getInt("inc", 0));
    ratedness = new RatednessSelection(prefs.getBool("isRated", true), plugin.getUser().isGuest());
    variant = new VariantSelection(variants, prefs.getString("variant", "Chess"));
    pieceColor = new PieceColorSelection(pieceColorPref);
    issueMatch = i18n.createButton("issueMatchButton");
    moreLess = new MoreLessOptionsButton(prefs.getBool("isMore", false), new Component[]{
      ratedness.getBox(),
      variant.getLabel(), variant.getBox(),
      pieceColor.getLabel(), pieceColor.getBox()
    });
    
    moreLess.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        if (moreLess.isMore() && container.isVisible()){
          // Need to wait for all delayed layout to finish
          SwingUtilities.invokeLater(new Runnable(){
            public void run(){
              Container contentPane = container.getContentPane();
              if (!AWTUtilities.fitsInto(contentPane.getMinimumSize(), contentPane.getSize()))
                container.pack();
            }
          });
        }
      }
    });
    
    issueMatch.setEnabled(isSelectionValid());
    opponent.addChangeListener(new ChangeListener(){
      public void stateChanged(ChangeEvent e){
        issueMatch.setEnabled(isSelectionValid());
      }
    });
    
    issueMatch.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        IssueMatchPanel.this.conn.issue(getMatchOffer());
      }
    });
    
    createUI();
  }
  
  
  
  /**
   * Makes the currently selected opponent the specified one, and transfers
   * focus to the panel. 
   */
  
  public void prepareFor(ServerUser opponentUser){
    opponent.setOpponent(opponentUser);
    issueMatch.requestFocus();
  }
  
  
  
  /**
   * Returns the list of opponents to make easily accessible for matching.
   */
  
  private ListModel getEasyAccessOpponentsModel(){
    if (conn instanceof FriendsConnection)
      return new OnlineFriendsListModel((FriendsConnection)conn);
    else
      return new DefaultListModel();
  }
  
  
  
  /**
   * Creates the UI.
   */
  
  private void createUI(){
    GroupLayout layout = new GroupLayout(this);
    setLayout(layout);
    layout.setAutocreateContainerGaps(true);
    
    layout.setHorizontalGroup(layout.createParallelGroup()
      .add(layout.createSequentialGroup()
        .add(layout.createParallelGroup(GroupLayout.TRAILING, false)
          .add(timeControls.getTimeLabel())
          .add(timeControls.getIncrementLabel())
          .add(variant.getLabel())
          .add(pieceColor.getLabel()))
        .addPreferredGap(LayoutStyle.RELATED)
        .add(layout.createParallelGroup(GroupLayout.LEADING, false)
          .add(layout.createSequentialGroup()
            .add(layout.createParallelGroup(GroupLayout.LEADING, false)
              .add(timeControls.getTimeSpinner(), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE)
              .add(timeControls.getIncrementSpinner(), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE))
            .addPreferredGap(LayoutStyle.RELATED)
            .add(layout.createParallelGroup(GroupLayout.LEADING, false)
              .add(timeControls.getTimeUnitsLabel())
              .add(timeControls.getIncrementUnitsLabel())))
          .add(variant.getBox())
          .add(pieceColor.getBox())
          .add(ratedness.getBox())))
      .add(layout.createSequentialGroup()
        .addPreferredGap(LayoutStyle.RELATED, 1, Integer.MAX_VALUE)
        .add(moreLess).addPreferredGap(LayoutStyle.RELATED).add(issueMatch)));
      
    layout.setVerticalGroup(layout.createSequentialGroup()
      .add(layout.createParallelGroup(GroupLayout.BASELINE)
        .add(timeControls.getTimeLabel()).add(timeControls.getTimeSpinner()).add(timeControls.getTimeUnitsLabel()))
      .addPreferredGap(LayoutStyle.RELATED)
      .add(layout.createParallelGroup(GroupLayout.BASELINE)
        .add(timeControls.getIncrementLabel()).add(timeControls.getIncrementSpinner()).add(timeControls.getIncrementUnitsLabel()))
      .addPreferredGap(LayoutStyle.UNRELATED)
      .add(layout.createParallelGroup(GroupLayout.BASELINE)
        .add(variant.getLabel()).add(variant.getBox()))
      .addPreferredGap(LayoutStyle.UNRELATED)
      .add(layout.createParallelGroup(GroupLayout.BASELINE)
        .add(pieceColor.getLabel()).add(pieceColor.getBox()))
      .addPreferredGap(LayoutStyle.UNRELATED)
      .add(ratedness.getBox())
      .addPreferredGap(LayoutStyle.UNRELATED, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE)
      .add(layout.createParallelGroup(GroupLayout.BASELINE)
        .add(moreLess).add(issueMatch)));
    
    layout.linkSize(new Component[]{moreLess, issueMatch});
  }
  
  
  
  /**
   * Saves the panel's preferences.
   */
  
  public void savePrefs(){
    prefs.setString("lastOpponent", opponent.getOpponentName());
    prefs.setInt("time", timeControls.getTime());
    prefs.setInt("inc", timeControls.getIncrement());
    prefs.setBool("isRated", ratedness.isRated());
    prefs.setString("variant", variant.getVariant().getName());
    Player color = pieceColor.getColor();
    prefs.setString("color", color == null ? "auto" : color.isWhite() ? "white" : "black");
    prefs.setBool("isMore", moreLess.isMore());
  }
  
  
  
  /**
   * Checks the validity of the current selection.
   */
  
  private boolean isSelectionValid(){
    UsernamePolicy policy = plugin.getUser().getServer().getUsernamePolicy();
    return policy.invalidityReason(opponent.getOpponentName()) == null;
  }
  
  
  
  /**
   * Returns the currently specified match offer.
   */
  
  private UserMatchOffer getMatchOffer(){
    ServerUser opp = conn.userForName(opponent.getOpponentName());
    int time = timeControls.getTime();
    int inc = timeControls.getIncrement();
    boolean isRated = ratedness.isRated();
    WildVariant wild = variant.getVariant();
    Player color = pieceColor.getColor();
    
    return new UserMatchOffer(opp, time, inc, isRated, wild, color);
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
