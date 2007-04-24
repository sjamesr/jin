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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import free.chess.Player;
import free.chess.WildVariant;
import free.jin.I18n;
import free.jin.Preferences;
import free.jin.SeekConnection;
import free.jin.UserSeek;
import free.jin.plugin.Plugin;
import free.util.TableLayout;



/**
 * A panel which lets the user select the seek options.
 */
 
public class IssueSeekPanel extends JPanel{
  
  
  
  /**
   * The plugin we're part of.
   */
  
  private final Plugin plugin;
  
  
  
  /**
   * Our preferences.
   */
  
  private final Preferences prefs;
  
  
  
  // The various UI elements.
  private final TimeControlsSelection timeControlsSelection;
  private final JPanel advancedPanel;
  private final RatednessSelection ratednessSelection;
  private final VariantSelection variantSelection;
  private final PieceColorSelection pieceColorSelection;
  private final OpponentRatingRangeSelection oppRatingRangeSelection;
  private final ManualAcceptSelection manualAcceptSelection;
  private final UseFormulaSelection useFormulaSelection;
  private final MoreLessButton moreLessButton;
  
  private final JButton issueSeekButton;
  
  
  
  
  /**
   * Creates a new <code>IssueSeekPanel</code> for the specified plugin and with
   * the specified <code>Preferences</code> object to load settings from.
   */
   
  public IssueSeekPanel(Plugin plugin, Preferences prefs){
    if (plugin == null)
      throw new IllegalArgumentException("plugin may not be null");
    if (prefs == null)
      throw new IllegalArgumentException("prefs may not be null");
    if (!(plugin.getConn() instanceof SeekConnection))
      throw new IllegalArgumentException("Connection must be an instance of SeekConnection");
    
    this.plugin = plugin;
    this.prefs = prefs;
    
    I18n i18n = getI18n();
    
    WildVariant [] variants = plugin.getConn().getSupportedVariants();
    
    String color = prefs.getString("color", "auto");
    Player pieceColor = "auto".equals(color) ? null :
      ("white".equals(color) ? Player.WHITE_PLAYER : Player.BLACK_PLAYER);
    
    boolean isRatingLimited = prefs.getBool("limitRating", false);
    int minRating = Math.max(0, prefs.getInt("minRating", 0));
    int maxRating = Math.min(9999, prefs.getInt("maxRating", 9999));

    // Create ui elements
    timeControlsSelection = new TimeControlsSelection(prefs.getInt("time", 10), prefs.getInt("inc", 0));
    advancedPanel = new JPanel();
    ratednessSelection = new RatednessSelection(prefs.getBool("isRated", true), plugin.getUser().isGuest());
    variantSelection = new VariantSelection(variants, prefs.getString("variant", "Chess"));
    pieceColorSelection = new PieceColorSelection(pieceColor);
    oppRatingRangeSelection = new OpponentRatingRangeSelection(isRatingLimited, minRating, maxRating);
    manualAcceptSelection = new ManualAcceptSelection(prefs.getBool("manualAccept", false));
    useFormulaSelection = new UseFormulaSelection(prefs.getBool("useFormula", true));
    moreLessButton = new MoreLessButton(advancedPanel, prefs.getBool("isMore", false));
    issueSeekButton = i18n.createButton("issueSeekButton");
    
    issueSeekButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        SeekConnection conn = (SeekConnection)IssueSeekPanel.this.plugin.getConn();
        conn.issue(getSeek());
      }
    });
    
    createUI();
  }
  
  
  
  /**
   * Returns the <code>I18n</code> for this class.
   */
  
  private I18n getI18n(){
    return I18n.get(IssueSeekPanel.class);
  }
  
  
  
  /**
   * Saves the panel's preferences.
   */
  
  public void savePrefs(){
    UserSeek seek = getSeek();
    
    prefs.setInt("time", seek.getTime());
    prefs.setInt("inc", seek.getInc());
    prefs.setBool("isRated", seek.isRated());
    prefs.setString("variant", seek.getVariant().getName());
    Player color = seek.getColor();
    prefs.setString("color", color == null ? "auto" : color.isWhite() ? "white" : "black");
    prefs.setBool("limitRating", oppRatingRangeSelection.isLimited());
    prefs.setInt("minRating", oppRatingRangeSelection.getMinimum());
    prefs.setInt("maxRating", oppRatingRangeSelection.getMaximum());
    prefs.setBool("manualAccept", seek.isManualAccept());
    prefs.setBool("useFormula", seek.isFormula());
    prefs.setBool("isMore", moreLessButton.isMore());
  }

  
  
  
  /**
   * Creates the ui of this panel, laying out all the ui elements.
   */
   
  private void createUI(){
    int xGap = 4; // The standard horizontal gap
    int yGap = 6; // The standard verical gap
    
    JPanel buttonsPanel = new JPanel(new TableLayout(2, xGap, yGap));
    buttonsPanel.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
    buttonsPanel.add(moreLessButton);
    buttonsPanel.add(issueSeekButton);
    
    // Holds the panel displayed when "More Options" is clicked
    JPanel advancedPanelHolder = new JPanel(new BorderLayout());
    advancedPanelHolder.add(advancedPanel);
    
    // Layout the subcontainers in the main container
    setLayout(new TableLayout(1, xGap, yGap));
    setAlignmentX(JComponent.LEFT_ALIGNMENT);
    timeControlsSelection.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    advancedPanelHolder.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    buttonsPanel.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
    
    add(timeControlsSelection);
    add(Box.createVerticalStrut(yGap));
    add(advancedPanelHolder);
    add(buttonsPanel);
    
    
    // Advanced options panel
    advancedPanel.setLayout(new TableLayout(1, xGap, yGap));
    
    ratednessSelection.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    variantSelection.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    pieceColorSelection.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    oppRatingRangeSelection.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    manualAcceptSelection.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    useFormulaSelection.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    
    advancedPanel.add(variantSelection);
    advancedPanel.add(pieceColorSelection);
    advancedPanel.add(ratednessSelection);
    advancedPanel.add(manualAcceptSelection);
    advancedPanel.add(useFormulaSelection);
    advancedPanel.add(oppRatingRangeSelection);
  }
  
  
  
  /**
   * Returns the currently specified <code>UserSeek</code>.
   */
  
  private UserSeek getSeek(){
    int time = timeControlsSelection.getTime();
    int inc = timeControlsSelection.getIncrement();
    boolean isRated = ratednessSelection.isRated();
    WildVariant variant = variantSelection.getVariant();
    Player color = pieceColorSelection.getColor();
    
    int minRating, maxRating;
    if (oppRatingRangeSelection.isLimited()){
      minRating = oppRatingRangeSelection.getMinimum();
      maxRating = oppRatingRangeSelection.getMaximum();
    }
    else{
      minRating = Integer.MIN_VALUE;
      maxRating = Integer.MAX_VALUE;
    }
    
    boolean manualAccept = manualAcceptSelection.isManualAccept();
    boolean useFormula = useFormulaSelection.useFormula();
    
    return new UserSeek(time, inc, isRated, variant, color, minRating,
      maxRating, manualAccept, useFormula);
  }
  
  
  
}
