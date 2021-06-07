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
package free.jin.seek;

import free.chess.Player;
import free.chess.WildVariant;
import free.jin.I18n;
import free.jin.Preferences;
import free.jin.SeekConnection;
import free.jin.UserSeek;
import free.jin.plugin.Plugin;
import free.jin.plugin.PluginUIContainer;
import free.util.AWTUtilities;
import free.util.swing.MoreLessOptionsButton;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

/** A panel which lets the user select the seek options. */
public class IssueSeekPanel extends JPanel {

  /** The connection. */
  private final SeekConnection conn;

  /** Our preferences. */
  private final Preferences prefs;

  // The various UI elements.
  private final TimeControlsSelection timeControls;
  private final RatednessSelection ratedness;
  private final VariantSelection variant;
  private final PieceColorSelection pieceColor;
  private final OpponentRatingRangeSelection oppRatingRange;
  private final ManualAcceptSelection manualAccept;
  private final UseFormulaSelection useFormula;
  private final MoreLessOptionsButton moreLess;
  private final JButton issueSeek;

  /**
   * Creates a new <code>IssueSeekPanel</code> with the specified arguments and a <code>Preferences
   * </code> object to load/save settings from/to.
   */
  public IssueSeekPanel(Plugin plugin, final PluginUIContainer container, Preferences prefs) {
    if (plugin == null) throw new IllegalArgumentException("plugin may not be null");
    if (container == null) throw new IllegalArgumentException("container may not be null");
    if (prefs == null) throw new IllegalArgumentException("prefs may not be null");
    if (!(plugin.getConn() instanceof SeekConnection))
      throw new IllegalArgumentException("Connection must be an instance of SeekConnection");

    this.conn = (SeekConnection) plugin.getConn();
    this.prefs = prefs;

    I18n i18n = getI18n();

    WildVariant[] variants = conn.getSupportedVariants();

    String color = prefs.getString("color", "auto");
    Player pieceColorPref =
        "auto".equals(color)
            ? null
            : ("white".equals(color) ? Player.WHITE_PLAYER : Player.BLACK_PLAYER);

    boolean isMinLimited = prefs.getBool("minRatingLimited", false);
    boolean isMaxLimited = prefs.getBool("maxRatingLimited", false);
    int minRating = Math.max(0, prefs.getInt("minRating", 0));
    int maxRating = Math.min(9999, prefs.getInt("maxRating", 9999));

    // Create ui elements
    timeControls = new TimeControlsSelection(prefs.getInt("time", 10), prefs.getInt("inc", 0));
    ratedness = new RatednessSelection(prefs.getBool("isRated", true), plugin.getUser().isGuest());
    variant = new VariantSelection(variants, prefs.getString("variant", "Chess"));
    pieceColor = new PieceColorSelection(pieceColorPref);
    oppRatingRange =
        new OpponentRatingRangeSelection(isMinLimited, minRating, isMaxLimited, maxRating);
    manualAccept = new ManualAcceptSelection(prefs.getBool("manualAccept", false));
    useFormula = new UseFormulaSelection(prefs.getBool("useFormula", true));
    issueSeek = i18n.createButton("issueSeekButton");
    moreLess =
        new MoreLessOptionsButton(
            prefs.getBool("isMore", false),
            new Component[] {
              variant.getLabel(),
              variant.getBox(),
              pieceColor.getLabel(),
              pieceColor.getBox(),
              ratedness.getBox(),
              manualAccept.getBox(),
              useFormula.getBox(),
              oppRatingRange.getMinimumLimitedBox(),
              oppRatingRange.getMinimumLimitSpinner(),
              oppRatingRange.getMaximumLimitedBox(),
              oppRatingRange.getMaximumLimitSpinner()
            });

    moreLess.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (moreLess.isMore() && container.isVisible()) {
              // Need to wait for all delayed layout to finish
              SwingUtilities.invokeLater(
                  new Runnable() {
                    @Override
                    public void run() {
                      Container contentPane = container.getContentPane();
                      if (!AWTUtilities.fitsInto(
                          contentPane.getMinimumSize(), contentPane.getSize())) container.pack();
                    }
                  });
            }
          }
        });

    issueSeek.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            IssueSeekPanel.this.conn.issue(getSeek());
          }
        });

    createUI();
  }

  /** Returns <code>true</code>. */
  @Override
  public boolean isFocusCycleRoot() {
    return true;
  }

  /** Returns the <code>I18n</code> for this class. */
  private I18n getI18n() {
    return I18n.get(IssueSeekPanel.class);
  }

  /** Saves the panel's preferences. */
  public void savePrefs() {
    UserSeek seek = getSeek();

    prefs.setInt("time", seek.getTime());
    prefs.setInt("inc", seek.getInc());
    prefs.setBool("isRated", seek.isRated());
    prefs.setString("variant", seek.getVariant().getName());
    Player color = seek.getColor();
    prefs.setString("color", color == null ? "auto" : color.isWhite() ? "white" : "black");
    prefs.setBool("minRatingLimited", oppRatingRange.isMinimumLimited());
    prefs.setBool("maxRatingLimited", oppRatingRange.isMaximumLimited());
    prefs.setInt("minRating", oppRatingRange.getMinimum());
    prefs.setInt("maxRating", oppRatingRange.getMaximum());
    prefs.setBool("manualAccept", seek.isManualAccept());
    prefs.setBool("useFormula", seek.isFormula());
    prefs.setBool("isMore", moreLess.isMore());
  }

  /** Creates the ui of this panel, laying out all the ui elements. */
  private void createUI() {
    GroupLayout layout = new GroupLayout(this);
    setLayout(layout);
    layout.setAutocreateContainerGaps(true);

    layout.setHorizontalGroup(
        layout
            .createParallelGroup()
            .add(
                layout
                    .createSequentialGroup()
                    .add(
                        layout
                            .createParallelGroup(GroupLayout.TRAILING, false)
                            .add(timeControls.getTimeLabel())
                            .add(timeControls.getIncrementLabel())
                            .add(variant.getLabel())
                            .add(pieceColor.getLabel())
                            .add(oppRatingRange.getMinimumLimitedBox())
                            .add(oppRatingRange.getMaximumLimitedBox()))
                    .addPreferredGap(LayoutStyle.RELATED)
                    .add(
                        layout
                            .createParallelGroup(GroupLayout.LEADING, false)
                            .add(
                                layout
                                    .createSequentialGroup()
                                    .add(
                                        layout
                                            .createParallelGroup(GroupLayout.LEADING, false)
                                            .add(
                                                timeControls.getTimeSpinner(),
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Integer.MAX_VALUE)
                                            .add(
                                                timeControls.getIncrementSpinner(),
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Integer.MAX_VALUE)
                                            .add(
                                                oppRatingRange.getMinimumLimitSpinner(),
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE)
                                            .add(
                                                oppRatingRange.getMaximumLimitSpinner(),
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                Integer.MAX_VALUE))
                                    .addPreferredGap(LayoutStyle.RELATED)
                                    .add(
                                        layout
                                            .createParallelGroup(GroupLayout.LEADING, true)
                                            .add(timeControls.getTimeUnitsLabel())
                                            .add(timeControls.getIncrementUnitsLabel())))
                            .add(variant.getBox())
                            .add(pieceColor.getBox())
                            .add(ratedness.getBox())
                            .add(manualAccept.getBox())
                            .add(useFormula.getBox())))
            .add(
                layout
                    .createSequentialGroup()
                    .addPreferredGap(LayoutStyle.RELATED, 1, Integer.MAX_VALUE)
                    .add(moreLess)
                    .addPreferredGap(LayoutStyle.RELATED)
                    .add(issueSeek)));

    layout.setVerticalGroup(
        layout
            .createSequentialGroup()
            .add(
                layout
                    .createParallelGroup(GroupLayout.BASELINE)
                    .add(timeControls.getTimeLabel())
                    .add(timeControls.getTimeSpinner())
                    .add(timeControls.getTimeUnitsLabel()))
            .addPreferredGap(LayoutStyle.RELATED)
            .add(
                layout
                    .createParallelGroup(GroupLayout.BASELINE)
                    .add(timeControls.getIncrementLabel())
                    .add(timeControls.getIncrementSpinner())
                    .add(timeControls.getIncrementUnitsLabel()))
            .addPreferredGap(LayoutStyle.UNRELATED)
            .add(
                layout
                    .createParallelGroup(GroupLayout.BASELINE)
                    .add(variant.getLabel())
                    .add(variant.getBox()))
            .addPreferredGap(LayoutStyle.UNRELATED)
            .add(
                layout
                    .createParallelGroup(GroupLayout.BASELINE)
                    .add(pieceColor.getLabel())
                    .add(pieceColor.getBox()))
            .addPreferredGap(LayoutStyle.UNRELATED)
            .add(ratedness.getBox())
            .addPreferredGap(LayoutStyle.UNRELATED)
            .add(manualAccept.getBox())
            .addPreferredGap(LayoutStyle.UNRELATED)
            .add(useFormula.getBox())
            .addPreferredGap(LayoutStyle.UNRELATED)
            .add(
                layout
                    .createParallelGroup(GroupLayout.BASELINE)
                    .add(oppRatingRange.getMinimumLimitedBox())
                    .add(oppRatingRange.getMinimumLimitSpinner()))
            .addPreferredGap(LayoutStyle.RELATED)
            .add(
                layout
                    .createParallelGroup(GroupLayout.BASELINE)
                    .add(oppRatingRange.getMaximumLimitedBox())
                    .add(oppRatingRange.getMaximumLimitSpinner()))
            .addPreferredGap(LayoutStyle.UNRELATED, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE)
            .add(layout.createParallelGroup(GroupLayout.BASELINE).add(moreLess).add(issueSeek)));
  }

  /** Returns the currently specified <code>UserSeek</code>. */
  private UserSeek getSeek() {
    int time = timeControls.getTime();
    int inc = timeControls.getIncrement();
    boolean isRated = ratedness.isRated();
    WildVariant wild = variant.getVariant();
    Player color = pieceColor.getColor();

    int minRating =
        oppRatingRange.isMinimumLimited() ? oppRatingRange.getMinimum() : Integer.MIN_VALUE;
    int maxRating =
        oppRatingRange.isMaximumLimited() ? oppRatingRange.getMaximum() : Integer.MAX_VALUE;

    boolean isManualAccept = manualAccept.isManualAccept();
    boolean isUseFormula = useFormula.useFormula();

    return new UserSeek(
        time, inc, isRated, wild, color, minRating, maxRating, isManualAccept, isUseFormula);
  }
}
