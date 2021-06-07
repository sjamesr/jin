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

import free.jin.I18n;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** A UI element which allows the user to limit the range of the rating of his opponent(s). */
public final class OpponentRatingRangeSelection {

  /** The checkbox specifying whether the range is limited at the bottom. */
  private final JCheckBox minimumLimitedBox;

  /** The model of the minimum rating spinner. */
  private final SpinnerNumberModel minimumModel;

  /** The minimum rating spinner. */
  private final JSpinner minimumSpinner;

  /** The model of the maximum rating spinner. */
  private final SpinnerNumberModel maximumModel;

  /** The checkbox specifying whether the range is limited at the top. */
  private final JCheckBox maximumLimitedBox;

  /** The maximum rating spinner. */
  private final JSpinner maximumSpinner;

  /**
   * Creates a new <code>OpponentRatingRangeSelection</code> with the specified initial state.
   *
   * @isMininimumLimited Whether the range is initially limited at the bottom.
   * @initialMinimum The initial minimum rating value.
   * @isMaximumLimited Whether the range is initially limited at the top.
   * @initialMaximum The initial maximum rating value.
   */
  public OpponentRatingRangeSelection(
      boolean isMinimumLimited, int initialMinimum, boolean isMaximumLimited, int initialMaximum) {

    I18n i18n = I18n.get(OpponentRatingRangeSelection.class);

    this.minimumModel = new SpinnerNumberModel(initialMinimum, 0, 9999, 50);
    this.maximumModel = new SpinnerNumberModel(initialMaximum, 0, 9999, 50);

    this.minimumLimitedBox = i18n.createCheckBox("minimumLimitedCheckBox");
    this.minimumSpinner = new JSpinner(minimumModel);
    this.maximumLimitedBox = i18n.createCheckBox("maximumLimitedCheckBox");
    this.maximumSpinner = new JSpinner(maximumModel);

    minimumLimitedBox.setSelected(isMinimumLimited);
    maximumLimitedBox.setSelected(isMaximumLimited);

    minimumSpinner.setEnabled(minimumLimitedBox.isSelected());
    maximumSpinner.setEnabled(maximumLimitedBox.isSelected());

    minimumModel.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            if (minimumModel.getNumber().intValue() > maximumModel.getNumber().intValue())
              maximumModel.setValue(minimumModel.getNumber());
          }
        });

    maximumModel.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            if (minimumModel.getNumber().intValue() > maximumModel.getNumber().intValue())
              minimumModel.setValue(maximumModel.getNumber());
          }
        });

    minimumLimitedBox.addItemListener(
        new ItemListener() {
          @Override
          public void itemStateChanged(ItemEvent evt) {
            minimumSpinner.setEnabled(minimumLimitedBox.isSelected());
          }
        });

    maximumLimitedBox.addItemListener(
        new ItemListener() {
          @Override
          public void itemStateChanged(ItemEvent evt) {
            maximumSpinner.setEnabled(maximumLimitedBox.isSelected());
          }
        });
  }

  /** Returns the bottom limit enabled checkbox. */
  public JCheckBox getMinimumLimitedBox() {
    return minimumLimitedBox;
  }

  /** Returns the bottom limit spinner. */
  public JSpinner getMinimumLimitSpinner() {
    return minimumSpinner;
  }

  /** Returns the top limit enabled checkbox. */
  public JCheckBox getMaximumLimitedBox() {
    return maximumLimitedBox;
  }

  /** Returns the top limit spinner. */
  public JSpinner getMaximumLimitSpinner() {
    return maximumSpinner;
  }

  /** Returns whether the rating range is limited at the bottom. */
  public boolean isMinimumLimited() {
    return minimumLimitedBox.isSelected();
  }

  /** Returns the lower bound of the range. */
  public int getMinimum() {
    return minimumModel.getNumber().intValue();
  }

  /** Returns whether the rating range is limited at the top. */
  public boolean isMaximumLimited() {
    return maximumLimitedBox.isSelected();
  }

  /** Returns the upper bound of the range. */
  public int getMaximum() {
    return maximumModel.getNumber().intValue();
  }
}
