/**
 * Jin - a chess client for internet chess servers. More information is available at
 * http://www.jinchess.com/. Copyright (C) 2003 Alexander Maryanovsky. All rights reserved.
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
package free.jin.ui;

import free.jin.I18n;
import free.jin.Jin;
import free.util.AWTUtilities;
import free.util.IOUtilities;
import free.util.swing.LinkLabel;
import free.util.swing.PlainTextDialog;
import free.util.swing.UrlDisplayingAction;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/** Displays licensing and copyright information about Jin and bundled software. */
public class LicensePanel extends DialogPanel {

  /** The font of the text area displaying the license text. */
  private static final Font LICENSE_TEXT_FONT = new Font("Monospaced", Font.PLAIN, 12);

  /** The text of the GPL, loaded lazily. */
  private String gplText = null;

  /** The text of the LGPL, loaded lazily. */
  private String lgplText = null;

  /** The text of the Creative Commons Attribution-ShareAlike 2.5 license, loaded lazily. */
  private String ccsa25Text = null;

  /** The text of the jgoodies copyright, loaded lazily. */
  private String jgoodiesCopyrightText = null;

  /** The text of the BeanShell license/copyright notice, loaded lazily. */
  private String beanshellCopyrightText = null;

  /** Creates a new <code>LicensePanel</code>. */
  public LicensePanel() {
    createUI();
  }

  /** The component to get the focus. */
  private Component focusComponent;

  /**
   * A workaround for ESCAPE not working in this dialog, under MS VM at least, because we assign
   * tooltips to some of the labels which makes them grab the focus.
   */
  @Override
  public void paint(Graphics g) {
    super.paint(g);

    if (focusComponent != null) {
      focusComponent.requestFocus();
      focusComponent = null;
    }
  }

  /** Displays the panel. */
  public void display() {
    super.askResult();
  }

  /** Returns the title of this <code>DialogPanel</code>. */
  @Override
  protected String getTitle() {
    return I18n.get(LicensePanel.class)
        .getFormattedString("title", new Object[] {Jin.getAppName()});
  }

  /** Creates the user interface. */
  private void createUI() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    ActionListener gplActionListener =
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            if (gplText == null) {
              try {
                gplText = IOUtilities.loadText(Jin.class.getResource("legal/gpl.txt"), true);
              } catch (IOException e) {
                e.printStackTrace();
                return;
              }
            }

            PlainTextDialog textDialog =
                new PlainTextDialog(LicensePanel.this, "The GNU General Public License", gplText);
            textDialog.setTextAreaFont(LICENSE_TEXT_FONT);
            AWTUtilities.centerWindow(textDialog, getParent());
            textDialog.setVisible(true);
          }
        };

    ActionListener lgplActionListener =
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            if (lgplText == null) {
              try {
                lgplText = IOUtilities.loadText(Jin.class.getResource("legal/lgpl.txt"), true);
              } catch (IOException e) {
                e.printStackTrace();
                return;
              }
            }

            PlainTextDialog textDialog =
                new PlainTextDialog(
                    LicensePanel.this, "The GNU Lesser General Public License", lgplText);
            textDialog.setTextAreaFont(LICENSE_TEXT_FONT);
            AWTUtilities.centerWindow(textDialog, getParent());
            textDialog.setVisible(true);
          }
        };

    ActionListener ccsa25ActionListener =
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            if (ccsa25Text == null) {
              try {
                ccsa25Text =
                    IOUtilities.loadText(Jin.class.getResource("legal/cc-sa-2.5.txt"), true);
              } catch (IOException e) {
                e.printStackTrace();
                return;
              }

              PlainTextDialog textDialog =
                  new PlainTextDialog(
                      LicensePanel.this,
                      "Creative Commons Attribution-ShareAlike 2.5 License",
                      ccsa25Text);
              textDialog.setTextAreaFont(LICENSE_TEXT_FONT);
              AWTUtilities.centerWindow(textDialog, getParent());
              textDialog.setVisible(true);
            }
          }
        };

    ActionListener jgoodiesActionListener =
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            if (jgoodiesCopyrightText == null) {
              try {
                jgoodiesCopyrightText =
                    IOUtilities.loadText(Jin.class.getResource("legal/jgoodies.txt"), true);
              } catch (IOException e) {
                e.printStackTrace();
                return;
              }
            }

            PlainTextDialog textDialog =
                new PlainTextDialog(
                    LicensePanel.this, "The JGoodies License (BSD)", jgoodiesCopyrightText);
            textDialog.setTextAreaFont(LICENSE_TEXT_FONT);
            AWTUtilities.centerWindow(textDialog, getParent());
            textDialog.setVisible(true);
          }
        };

    ActionListener beanshellActionListener =
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent evt) {
            if (beanshellCopyrightText == null) {
              try {
                beanshellCopyrightText =
                    IOUtilities.loadText(Jin.class.getResource("legal/beanshell.txt"), true);
              } catch (IOException e) {
                e.printStackTrace();
                return;
              }
            }

            PlainTextDialog textDialog =
                new PlainTextDialog(
                    LicensePanel.this, "The BeanShell License", beanshellCopyrightText);
            textDialog.setTextAreaFont(LICENSE_TEXT_FONT);
            AWTUtilities.centerWindow(textDialog, getParent());
            textDialog.setVisible(true);
          }
        };

    I18n i18n = I18n.get(LicensePanel.class);

    Object[] appName = new Object[] {Jin.getAppName()};

    JPanel jinPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    jinPanel.add(
        new JLabel("<html>" + i18n.getFormattedString("jinGpl.text", appName) + "&nbsp;</html>"));
    LinkLabel jinGPLLabel = new LinkLabel(i18n.getString("gpl.link"));
    jinGPLLabel.addActionListener(gplActionListener);
    jinPanel.add(jinGPLLabel);
    jinPanel.add(new JLabel("<html>.</html>"));
    add(jinPanel);
    add(Box.createVerticalStrut(5));

    String websiteURL = Jin.getAppProperty("websiteURL", null);
    JPanel jinWebsitePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    jinWebsitePanel.add(
        new JLabel(
            "<html>" + i18n.getFormattedString("jinWebsite.text", appName) + "&nbsp;</html>"));
    LinkLabel jinWebsiteLabel = new LinkLabel(i18n.getFormattedString("jinWebsite.link", appName));
    jinWebsiteLabel.setToolTipText(websiteURL);
    jinWebsiteLabel.addActionListener(new UrlDisplayingAction(websiteURL));
    jinWebsitePanel.add(jinWebsiteLabel);
    jinWebsitePanel.add(new JLabel("<html>.</html>"));
    add(jinWebsitePanel);

    add(Box.createVerticalStrut(10));
    add(new JSeparator());
    add(Box.createVerticalStrut(10));

    if (new Boolean(Jin.getAppProperty("usesBeanshell", "false")).booleanValue()) {
      JPanel beanshellPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      beanshellPanel.add(
          new JLabel("<html>" + i18n.getFormattedString("uses.text", appName) + "&nbsp;</html>"));
      LinkLabel beanshellWebsiteLabel = new LinkLabel(i18n.getString("beanshell.link"));
      beanshellWebsiteLabel.setToolTipText("http://www.beanshell.org");
      beanshellWebsiteLabel.addActionListener(new UrlDisplayingAction("http://www.beanshell.org"));
      beanshellPanel.add(beanshellWebsiteLabel);
      beanshellPanel.add(
          new JLabel("<html>" + i18n.getString("licensedUnder.text") + "&nbsp;</html>"));
      LinkLabel beanshellLicenseLabel = new LinkLabel(i18n.getString("lgpl.link"));
      beanshellLicenseLabel.addActionListener(beanshellActionListener);
      beanshellPanel.add(beanshellLicenseLabel);
      beanshellPanel.add(new JLabel("<html>.</html>"));
      add(beanshellPanel);
      add(Box.createVerticalStrut(5));
    }

    JPanel swingLayoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    swingLayoutPanel.add(
        new JLabel("<html>" + i18n.getFormattedString("uses.text", appName) + "&nbsp;</html>"));
    LinkLabel swingLayoutWebsiteLabel = new LinkLabel(i18n.getString("swingLayout.link"));
    swingLayoutWebsiteLabel.setToolTipText("https://swing-layout.dev.java.net/");
    swingLayoutWebsiteLabel.addActionListener(
        new UrlDisplayingAction("https://swing-layout.dev.java.net/"));
    swingLayoutPanel.add(swingLayoutWebsiteLabel);
    swingLayoutPanel.add(
        new JLabel("<html>" + i18n.getString("licensedUnder.text") + "&nbsp;</html>"));
    LinkLabel swingLayoutLicenseLabel = new LinkLabel(i18n.getString("lgpl.link"));
    swingLayoutLicenseLabel.addActionListener(lgplActionListener);
    swingLayoutPanel.add(swingLayoutLicenseLabel);
    swingLayoutPanel.add(new JLabel("<html>.</html>"));
    add(swingLayoutPanel);
    add(Box.createVerticalStrut(5));

    if (new Boolean(Jin.getAppProperty("includesXBoardPieceSet", "false")).booleanValue()) {
      JPanel xboardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      xboardPanel.add(
          new JLabel(
              "<html>"
                  + i18n.getFormattedString("includesPieceSetFrom.text", appName)
                  + "&nbsp;</html>"));
      LinkLabel xboardWebsiteLabel = new LinkLabel(i18n.getString("xboard.link"));
      xboardWebsiteLabel.setToolTipText("http://www.tim-mann.org/xboard.html");
      xboardWebsiteLabel.addActionListener(
          new UrlDisplayingAction("http://www.tim-mann.org/xboard.html"));
      xboardPanel.add(xboardWebsiteLabel);
      xboardPanel.add(
          new JLabel("<html>" + i18n.getString("licensedUnder.text") + "&nbsp;</html>"));
      LinkLabel xboardLicenseLabel = new LinkLabel(i18n.getString("gpl.link"));
      xboardLicenseLabel.addActionListener(gplActionListener);
      xboardPanel.add(xboardLicenseLabel);
      xboardPanel.add(new JLabel("<html>.</html>"));
      add(xboardPanel);
      add(Box.createVerticalStrut(5));
    }

    JPanel eboardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    eboardPanel.add(
        new JLabel(
            "<html>"
                + i18n.getFormattedString("includesPieceSetFrom.text", appName)
                + "&nbsp;</html>"));
    LinkLabel eboardWebsiteLabel = new LinkLabel(i18n.getString("eboard.link"));
    eboardWebsiteLabel.setToolTipText("http://eboard.sourceforge.net/");
    eboardWebsiteLabel.addActionListener(new UrlDisplayingAction("http://eboard.sourceforge.net/"));
    eboardPanel.add(eboardWebsiteLabel);
    eboardPanel.add(new JLabel("<html>" + i18n.getString("licensedUnder.text") + "&nbsp;</html>"));
    LinkLabel eboardLicenseLabel = new LinkLabel(i18n.getString("lgpl.link"));
    eboardLicenseLabel.addActionListener(lgplActionListener);
    eboardPanel.add(eboardLicenseLabel);
    eboardPanel.add(new JLabel("<html>.</html>"));
    add(eboardPanel);
    add(Box.createVerticalStrut(5));

    JPanel blitzinPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    blitzinPanel.add(
        new JLabel(
            "<html>"
                + i18n.getFormattedString("includesICCStuff.text", appName)
                + "&nbsp;</html>"));
    LinkLabel blitzinWebsiteLabel = new LinkLabel(i18n.getString("icc.link"));
    blitzinWebsiteLabel.setToolTipText("http://www.chessclub.com");
    blitzinWebsiteLabel.addActionListener(new UrlDisplayingAction("http://www.chessclub.com"));
    blitzinPanel.add(blitzinWebsiteLabel);
    blitzinPanel.add(new JLabel("<html>" + i18n.getString("usedWithPermission.text") + "</html>"));
    add(blitzinPanel);
    add(Box.createVerticalStrut(5));

    JPanel maurizioMongePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    maurizioMongePanel.add(
        new JLabel(
            "<html>"
                + i18n.getFormattedString("includesPieceSetsBy.text", appName)
                + "&nbsp;</html>"));
    LinkLabel maurizioMongeWebsiteLabel = new LinkLabel(i18n.getString("maurizioMonge.link"));
    maurizioMongeWebsiteLabel.setToolTipText(
        "http://linuz.sns.it/~monge/wiki/index.php/Chess_pieces");
    maurizioMongeWebsiteLabel.addActionListener(
        new UrlDisplayingAction("http://linuz.sns.it/~monge/wiki/index.php/Chess_pieces"));
    maurizioMongePanel.add(maurizioMongeWebsiteLabel);
    maurizioMongePanel.add(
        new JLabel("<html>" + i18n.getString("licensedUnder.text") + "&nbsp;</html>"));
    LinkLabel maurizioMongeLicenseLabel = new LinkLabel(i18n.getString("lgpl.link"));
    maurizioMongeLicenseLabel.addActionListener(lgplActionListener);
    maurizioMongePanel.add(maurizioMongeLicenseLabel);
    maurizioMongePanel.add(new JLabel("<html>.</html>"));
    add(maurizioMongePanel);
    add(Box.createVerticalStrut(5));

    try {
      // Throws ClassNotFoundException if not found
      Class.forName("com.incors.plaf.kunststoff.KunststoffLookAndFeel");

      JPanel kunststoffPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      kunststoffPanel.add(
          new JLabel(
              "<html>"
                  + i18n.getFormattedString("distributedWith.text", appName)
                  + "&nbsp;</html>"));
      LinkLabel kunststoffWebsiteLabel = new LinkLabel(i18n.getString("kunststoff.link"));
      kunststoffWebsiteLabel.setToolTipText("http://www.incors.org/");
      kunststoffWebsiteLabel.addActionListener(new UrlDisplayingAction("http://www.incors.org/"));
      kunststoffPanel.add(kunststoffWebsiteLabel);
      kunststoffPanel.add(
          new JLabel("<html>" + i18n.getString("licensedUnder.text") + "&nbsp;</html>"));
      LinkLabel kunststoffLicenseLabel = new LinkLabel(i18n.getString("lgpl.link"));
      kunststoffLicenseLabel.addActionListener(lgplActionListener);
      kunststoffPanel.add(kunststoffLicenseLabel);
      kunststoffPanel.add(new JLabel("<html>.</html>"));
      add(kunststoffPanel);
      add(Box.createVerticalStrut(5));
    } catch (ClassNotFoundException e) {
    }

    try {
      // Throws ClassNotFoundException if not found
      Class.forName("net.sourceforge.mlf.metouia.MetouiaLookAndFeel");

      JPanel metouialfPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      metouialfPanel.add(
          new JLabel(
              "<html>"
                  + i18n.getFormattedString("distributedWith.text", appName)
                  + "&nbsp;</html>"));
      LinkLabel metouialfWebsiteLabel = new LinkLabel(i18n.getString("metouia.link"));
      metouialfWebsiteLabel.setToolTipText("http://mlf.sourceforge.net/");
      metouialfWebsiteLabel.addActionListener(
          new UrlDisplayingAction("http://mlf.sourceforge.net/"));
      metouialfPanel.add(metouialfWebsiteLabel);
      metouialfPanel.add(
          new JLabel("<html>" + i18n.getString("licensedUnder.text") + "&nbsp;</html>"));
      LinkLabel metouialfLicenseLabel = new LinkLabel(i18n.getString("lgpl.link"));
      metouialfLicenseLabel.addActionListener(lgplActionListener);
      metouialfPanel.add(metouialfLicenseLabel);
      metouialfPanel.add(new JLabel("<html>.</html>"));
      add(metouialfPanel);
      add(Box.createVerticalStrut(5));
    } catch (ClassNotFoundException e) {
    }

    try {
      // Throws ClassNotFoundException if not found
      Class.forName("com.birosoft.liquid.LiquidLookAndFeel");

      JPanel liquidlfPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      liquidlfPanel.add(
          new JLabel(
              "<html>"
                  + i18n.getFormattedString("distributedWith.text", appName)
                  + "&nbsp;</html>"));
      LinkLabel liquidlfWebsiteLabel = new LinkLabel(i18n.getString("liquid.link"));
      liquidlfWebsiteLabel.setToolTipText("http://liquidlnf.sourceforge.net/");
      liquidlfWebsiteLabel.addActionListener(
          new UrlDisplayingAction("http://liquidlnf.sourceforge.net/"));
      liquidlfPanel.add(liquidlfWebsiteLabel);
      liquidlfPanel.add(
          new JLabel("<html>" + i18n.getString("licensedUnder.text") + "&nbsp;</html>"));
      LinkLabel liquidlfLicenseLabel = new LinkLabel(i18n.getString("lgpl.link"));
      liquidlfLicenseLabel.addActionListener(lgplActionListener);
      liquidlfPanel.add(liquidlfLicenseLabel);
      liquidlfPanel.add(new JLabel("<html>.</html>"));
      add(liquidlfPanel);
      add(Box.createVerticalStrut(5));
    } catch (ClassNotFoundException e) {
    }

    try {
      // Throws ClassNotFoundException if not found
      Class.forName("com.jgoodies.looks.plastic.PlasticLookAndFeel");

      JPanel jgoodieslfPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      jgoodieslfPanel.add(
          new JLabel(
              "<html>"
                  + i18n.getFormattedString("distributedWith.text", appName)
                  + "&nbsp;</html>"));
      LinkLabel jgoodieslfWebsiteLabel = new LinkLabel(i18n.getString("jGoodiesLooks.link"));
      jgoodieslfWebsiteLabel.setToolTipText("http://jgoodies.dev.java.net");
      jgoodieslfWebsiteLabel.addActionListener(
          new UrlDisplayingAction("http://jgoodies.dev.java.net"));
      jgoodieslfPanel.add(jgoodieslfWebsiteLabel);
      jgoodieslfPanel.add(
          new JLabel("<html>" + i18n.getString("licensedUnder.text") + "&nbsp;</html>"));
      LinkLabel jgoodieslfLicenseLabel = new LinkLabel(i18n.getString("bsd.link"));
      jgoodieslfLicenseLabel.addActionListener(jgoodiesActionListener);
      jgoodieslfPanel.add(jgoodieslfLicenseLabel);
      jgoodieslfPanel.add(new JLabel("<html>.</html>"));
      add(jgoodieslfPanel);
      add(Box.createVerticalStrut(5));
    } catch (ClassNotFoundException e) {
    }

    JPanel tangoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    tangoPanel.add(
        new JLabel(
            "<html>" + i18n.getFormattedString("usesIconsBy.text", appName) + "&nbsp;</html>"));
    LinkLabel tangoWebsiteLabel = new LinkLabel(i18n.getString("tango.link"));
    tangoWebsiteLabel.setToolTipText("http://tango.freedesktop.org/");
    tangoWebsiteLabel.addActionListener(new UrlDisplayingAction("http://tango.freedesktop.org/"));
    tangoPanel.add(tangoWebsiteLabel);
    tangoPanel.add(new JLabel("<html>" + i18n.getString("licensedUnder.text") + "&nbsp;</html>"));
    LinkLabel tangoLicenseLabel = new LinkLabel(i18n.getString("ccsa25.link"));
    tangoLicenseLabel.addActionListener(ccsa25ActionListener);
    tangoPanel.add(tangoLicenseLabel);
    tangoPanel.add(new JLabel("<html>.</html>"));
    add(tangoPanel);
    add(Box.createVerticalStrut(5));

    String logoId = Jin.getAppProperty("app.logo.id", "jin");
    if (logoId.equals("jin")) {
      JPanel denisDesLauriersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      denisDesLauriersPanel.add(
          new JLabel(
              "<html>" + i18n.getFormattedString("logoDesignedBy", appName) + "&nbsp;</html>"));
      LinkLabel denisDesLauriersWebsiteLabel =
          new LinkLabel(i18n.getString("denisDesLauriers.link"));
      denisDesLauriersWebsiteLabel.setToolTipText("http://www.chess-art.com");
      denisDesLauriersWebsiteLabel.addActionListener(
          new UrlDisplayingAction("http://www.chess-art.com"));
      denisDesLauriersPanel.add(denisDesLauriersWebsiteLabel);
      denisDesLauriersPanel.add(
          new JLabel("<html>" + i18n.getString("licensedUnder.text") + "&nbsp;</html>"));
      LinkLabel logoLicenseLabel = new LinkLabel(i18n.getString("gpl.link"));
      logoLicenseLabel.addActionListener(gplActionListener);
      denisDesLauriersPanel.add(logoLicenseLabel);
      denisDesLauriersPanel.add(new JLabel("<html>.</html>"));
      add(denisDesLauriersPanel);
    } else if (logoId.equals("sonia")) {
      JPanel logoCreditPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      logoCreditPanel.add(
          new JLabel(
              "<html>"
                  + i18n.getFormattedString("logoInspiredByMaurizioMonge", appName)
                  + "&nbsp;</html>"));
      LinkLabel fantasySetWebsiteLabel = new LinkLabel(i18n.getString("fantasySetWebsite.link"));
      fantasySetWebsiteLabel.setToolTipText(
          "http://linuz.sns.it/~monge/wiki/index.php/Chess_pieces#Fantasy");
      fantasySetWebsiteLabel.addActionListener(
          new UrlDisplayingAction(
              "http://linuz.sns.it/~monge/wiki/index.php/Chess_pieces#Fantasy"));
      logoCreditPanel.add(fantasySetWebsiteLabel);
      logoCreditPanel.add(new JLabel("<html>.</html>"));
      add(logoCreditPanel);
    }

    add(Box.createVerticalStrut(30));

    JButton closeButton = i18n.createButton("closeButton");
    closeButton.addActionListener(new ClosingListener(null));
    closeButton.setAlignmentX(CENTER_ALIGNMENT);
    add(closeButton);

    setDefaultButton(closeButton);

    focusComponent = closeButton;
  }
}
