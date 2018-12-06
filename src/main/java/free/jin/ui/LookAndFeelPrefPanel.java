/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2005 Alexander Maryanovsky. All rights reserved.
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
package free.jin.ui;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import free.jin.BadChangesException;
import free.jin.I18n;
import free.jin.Jin;
import free.util.PlatformUtils;

/**
 * A preferences panel for selecting the current Swing Look and Feel.
 */
public class LookAndFeelPrefPanel extends PreferencesPanel {

  /**
   * Have we installed the extra look and feels already?
   */
  private static boolean extraLnFsInstalled = false;

  /**
   * The list of available Look and Feels. This actually holds instances of <code>LnF</code>.
   */
  private final JList lookAndFeels;

  /**
   * Creates a new <code>LookAndFeelPrefPanel</code>.
   */
  public LookAndFeelPrefPanel() {
    installExtraLookAndFeels();

    Set installedLnfs =
        new TreeSet(
            new Comparator() {
              @Override
              public int compare(Object arg0, Object arg1) {
                UIManager.LookAndFeelInfo lnf1 = (UIManager.LookAndFeelInfo) arg0;
                UIManager.LookAndFeelInfo lnf2 = (UIManager.LookAndFeelInfo) arg1;

                return lnf1.getName().compareToIgnoreCase(lnf2.getName());
              }
            });
    installedLnfs.addAll(Arrays.asList(UIManager.getInstalledLookAndFeels()));

    List lnfs = new LinkedList();
    for (Iterator i = installedLnfs.iterator(); i.hasNext(); ) {
      UIManager.LookAndFeelInfo lnfInfo = (UIManager.LookAndFeelInfo) i.next();

      // WORKAROUND: GTK Look and Feel is broken for now in 1.5.0 with an applet
      // Remove this when Sun fixes it.
      if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(lnfInfo.getClassName())
          && (System.getSecurityManager() != null)
          && PlatformUtils.isJavaBetterThan("1.5")) continue;

      lnfs.add(new LnF(lnfInfo));
    }

    this.lookAndFeels = new JList(lnfs.toArray());

    // Select the right current look and feel
    String lnfClassName = UIManager.getLookAndFeel().getClass().getName();
    for (int i = 0; i < lookAndFeels.getModel().getSize(); i++) {
      String classname = ((LnF) lookAndFeels.getModel().getElementAt(i)).classname;
      if (lnfClassName.equals(classname)) {
        lookAndFeels.setSelectedIndex(i);
        lookAndFeels.ensureIndexIsVisible(i);
        break;
      }
    }

    createUI();
  }

  /**
   * Installs any extra look and feels Jin is using.
   */
  private static synchronized void installExtraLookAndFeels() {
    if (extraLnFsInstalled) return;
    extraLnFsInstalled = true;

    int extraLooksCount = Integer.parseInt(Jin.getAppProperty("lf.extra.count", "0"));
    for (int i = 0; i < extraLooksCount; i++) {
      String name = Jin.getAppProperty("lf.extra." + i + ".name", null);
      String className = Jin.getAppProperty("lf.extra." + i + ".class", null);
      String minRequiredJavaVer = Jin.getAppProperty("lf.extra." + i + ".minRequiredJava", "0");
      if (PlatformUtils.isJavaBetterThan(minRequiredJavaVer)) {
        try {
          Class.forName(className);
          UIManager.installLookAndFeel(name, className);
        } catch (ClassNotFoundException e) {
          // We used to print a message here, but too many people panic and
          // think something is terribly wrong.
        }
      }
    }
  }

  /**
   * Creates the UI of this panel.
   */
  private void createUI() {
    lookAndFeels.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    lookAndFeels.setVisibleRowCount(Math.max(5, Math.min(lookAndFeels.getModel().getSize(), 10)));

    JScrollPane scrollPane = new JScrollPane(lookAndFeels);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    setLayout(new BorderLayout());
    add(scrollPane, BorderLayout.CENTER);
    add(Box.createHorizontalStrut(150), BorderLayout.SOUTH);

    lookAndFeels.addListSelectionListener(
        new ListSelectionListener() {
          @Override
          public void valueChanged(ListSelectionEvent evt) {
            fireStateChanged();
          }
        });
  }

  /**
   * Applies the selected look and feel selection to the list of tree roots specified in the
   * constructor.
   */
  @Override
  public void applyChanges() throws BadChangesException {
    LnF lnf = (LnF) lookAndFeels.getSelectedValue();
    if (lnf == null) {
      I18n i18n = I18n.get(LookAndFeelPrefPanel.class);
      throw new BadChangesException(i18n.getString("noLookNFeelSelectedErrorMessage"), this);
    }

    Jin.getInstance()
        .getPrefs()
        .setString("lookAndFeel.classname." + PlatformUtils.getOSName(), lnf.classname);
  }

  /**
   * A small class which holds the name and classname of a look and feel, and also returns the name
   * in its <code>toString</code> method. We use instances of this class as the elements in the
   * lookAndFeels list.
   */
  private static class LnF {

    /**
     * The Look and Feel name.
     */
    public final String name;

    /**
     * The Look and Feel classname.
     */
    public final String classname;

    /**
     * Creates a new LnF from the specified <code>UIManager.LookAndFeelInfo</code>.
     */
    public LnF(UIManager.LookAndFeelInfo info) {
      this.name = info.getName();
      this.classname = info.getClassName();
    }

    /**
     * Returns the name of the Look and Feel.
     */
    @Override
    public String toString() {
      return name;
    }
  }
}
