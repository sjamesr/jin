/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2007 Alexander Maryanovsky. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package free.jin.seek;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import free.jin.I18n;

/**
 * A UI element which allows the user to specify the time controls of a game.
 */
public final class TimeControlsSelection {

  /**
   * The "Time:" label.
   */
  private JLabel timeLabel;

  /**
   * The time control.
   */
  private final JSpinner time;

  /**
   * The time units label.
   */
  private final JLabel timeUnitsLabel;

  /**
   * The "Increment:" label.
   */
  private final JLabel incrementLabel;

  /**
   * The increment control.
   */
  private final JSpinner increment;

  /**
   * The increment units label.
   */
  private final JLabel incrementUnitsLabel;

  /**
   * Creates a new <code>TimeControlsSelection</code> with the specified initial values for time and
   * increment.
   */
  public TimeControlsSelection(int initialTime, int initialIncrement) {
    I18n i18n = I18n.get(TimeControlsSelection.class);

    this.timeLabel = i18n.createLabel("timeLabel");
    this.time = new JSpinner(new SpinnerNumberModel(initialTime, 0, 600, 1));
    this.timeUnitsLabel = i18n.createLabel("timeUnitsLabel");

    this.incrementLabel = i18n.createLabel("incrementLabel");
    this.increment = new JSpinner(new SpinnerNumberModel(initialIncrement, 0, 300, 1));
    this.incrementUnitsLabel = i18n.createLabel("incrementUnitsLabel");

    timeLabel.setLabelFor(time);
    incrementLabel.setLabelFor(increment);
  }

  /**
   * Returns the time label.
   */
  public JLabel getTimeLabel() {
    return timeLabel;
  }

  /**
   * Returns the time spinner.
   */
  public JSpinner getTimeSpinner() {
    return time;
  }

  /**
   * Returns the time units label.
   */
  public JLabel getTimeUnitsLabel() {
    return timeUnitsLabel;
  }

  /**
   * Returns the increment label.
   */
  public JLabel getIncrementLabel() {
    return incrementLabel;
  }

  /**
   * Returns the increment spinner.
   */
  public JSpinner getIncrementSpinner() {
    return increment;
  }

  /**
   * Returns the increment units label.
   */
  public JLabel getIncrementUnitsLabel() {
    return incrementUnitsLabel;
  }

  /**
   * Returns the time selected by the user, in minutes.
   */
  public int getTime() {
    return ((Integer) time.getValue()).intValue();
  }

  /**
   * Returns the increment selected by the user, in seconds.
   */
  public int getIncrement() {
    return ((Integer) increment.getValue()).intValue();
  }
}
