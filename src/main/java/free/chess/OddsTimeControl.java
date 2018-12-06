/**
 * The chess framework library. More information is available at http://www.jinchess.com/. Copyright
 * (C) 2007 Alexander Maryanovsky. All rights reserved.
 *
 * The chess framework library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * The chess framework library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with the chess
 * framework library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite
 * 330, Boston, MA 02111-1307 USA
 */
package free.chess;

import free.util.Localization;

/**
 * A <code>TimeControl</code> when the time control for white and black differ.
 */
public class OddsTimeControl extends TimeControl {

  /**
   * The white's time control.
   */
  private final TimeControl whiteTimeControl;

  /**
   * The black's time control.
   */
  private final TimeControl blackTimeControl;

  /**
   * Creates a new <code>OddsTimeControl</code> with the specified white and black time controls.
   */
  public OddsTimeControl(TimeControl whiteTimeControl, TimeControl blackTimeControl) {
    if (whiteTimeControl == null)
      throw new IllegalArgumentException("whiteTimeControl may not be null");
    if (blackTimeControl == null)
      throw new IllegalArgumentException("blackTimeControl may not be null");

    this.whiteTimeControl = whiteTimeControl;
    this.blackTimeControl = blackTimeControl;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getInitialTime(Player player) {
    return (player.isWhite() ? whiteTimeControl : blackTimeControl).getInitialTime(player);
  }

  /**
   * Returns white's time control.
   */
  public TimeControl getWhiteTimeControl() {
    return whiteTimeControl;
  }

  /**
   * Returns black's time control.
   */
  public TimeControl getBlackTimeControl() {
    return blackTimeControl;
  }

  /**
   * Returns whether the two time controls really differ.
   */
  public boolean isOdds() {
    return !whiteTimeControl.equals(blackTimeControl);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getLocalizedShortDescription() {
    return getDescription(
        "shortDescription",
        whiteTimeControl.getLocalizedShortDescription(),
        blackTimeControl.getLocalizedShortDescription());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getLocalizedMediumDescription() {
    return getDescription(
        "mediumDescription",
        whiteTimeControl.getLocalizedMediumDescription(),
        blackTimeControl.getLocalizedMediumDescription());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getLocalizedLongDescription() {
    return getDescription(
        "longDescription",
        whiteTimeControl.getLocalizedLongDescription(),
        blackTimeControl.getLocalizedLongDescription());
  }

  /**
   * Returns a description of the time control obtained using the specified localization key.
   */
  private String getDescription(String key, String whiteDescription, String blackDescription) {
    Localization l10n = LocalizationService.getForClass(OddsTimeControl.class);
    Object[] args = new Object[] {whiteDescription, blackDescription};

    return l10n.getFormattedString(key, args);
  }

  /**
   * Returns whether the specified object equals to this one.
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof OddsTimeControl) || (o == null)) return false;

    OddsTimeControl tc = (OddsTimeControl) o;
    return tc.whiteTimeControl.equals(whiteTimeControl)
        && tc.blackTimeControl.equals(blackTimeControl);
  }

  /**
   * Returns the hash code for this object.
   */
  @Override
  public int hashCode() {
    return whiteTimeControl.hashCode() ^ blackTimeControl.hashCode();
  }
}
