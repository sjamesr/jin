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

import javax.swing.JComboBox;
import javax.swing.JLabel;

import free.chess.WildVariant;
import free.jin.I18n;
import free.util.NamedWrapper;
import free.workarounds.FixedJComboBox;

/**
 * A UI element which allows the user to select a chess variant.
 */
public final class VariantSelection {

  /**
   * The label.
   */
  private final JLabel label;

  /**
   * The combo box displaying the list of variants.
   */
  private final JComboBox box;

  /**
   * Creates a new <code>VariantSelection</code> with the specified variants to select from and the
   * initially selected variant.
   */
  public VariantSelection(WildVariant[] variants, String selectedVariantName) {
    I18n i18n = I18n.get(VariantSelection.class);

    this.label = i18n.createLabel("");
    this.box = new FixedJComboBox(nameWrap(variants));

    label.setLabelFor(box);
    box.setEditable(false);
    box.setSelectedIndex(variantIndexByName(variants, selectedVariantName));
  }

  /**
   * Returns the label.
   */
  public JLabel getLabel() {
    return label;
  }

  /**
   * Returns the combo box.
   */
  public JComboBox getBox() {
    return box;
  }

  /**
   * Wraps each specified variant in a <code>NamedWrapper</code> with the localized name of the
   * variant.
   */
  private NamedWrapper[] nameWrap(WildVariant[] variants) {
    I18n i18n = I18n.get(VariantSelection.class);

    NamedWrapper[] wrappers = new NamedWrapper[variants.length];
    for (int i = 0; i < variants.length; i++) {
      WildVariant variant = variants[i];
      String variantName = i18n.getString("variant." + variant.getName(), variant.getName());
      wrappers[i] = new NamedWrapper(variant, variantName);
    }

    return wrappers;
  }

  /**
   * Returns the currently selected variant.
   */
  public WildVariant getVariant() {
    NamedWrapper wrapper = (NamedWrapper) box.getSelectedItem();
    return (WildVariant) wrapper.getTarget();
  }

  /**
   * Returns the index variant with the specified name among the specified list of variants.
   */
  private int variantIndexByName(WildVariant[] variants, String name) {
    for (int i = 0; i < variants.length; i++) if (variants[i].getName().equals(name)) return i;

    return -1;
  }
}
