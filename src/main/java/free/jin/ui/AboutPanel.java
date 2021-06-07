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

import com.google.common.io.Resources;
import free.jin.I18n;
import free.jin.Jin;
import free.util.IOUtilities;
import java.awt.Font;
import java.util.StringTokenizer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/** A panel displaying information about Jin. Normally displayed in the "Help->About..." dialog. */
public class AboutPanel extends DialogPanel {

  /** Creates a new <code>AboutPanel</code>. */
  public AboutPanel() {
    createUI();
  }

  /** Returns the title of this <code>DialogPanel</code>. */
  @Override
  protected String getTitle() {
    I18n i18n = I18n.get(AboutPanel.class);
    String appName = Jin.getAppName();
    return i18n.getFormattedString("title", new Object[] {appName});
  }

  /** Displays this panel. */
  public void display() {
    super.askResult();
  }

  /** Creates the user interface. */
  private void createUI() {
    I18n i18n = I18n.get(AboutPanel.class);

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    Icon jinIcon = new ImageIcon(Resources.getResource("logo48.png"));
    JLabel jinLabel =
        new JLabel(Jin.getAppName() + " " + Jin.getAppVersion(), jinIcon, SwingConstants.CENTER);
    jinLabel.setFont(new Font("Serif", Font.PLAIN, 36));
    jinLabel.setAlignmentX(CENTER_ALIGNMENT);

    add(jinLabel);
    add(Box.createVerticalStrut(5));
    add(new JSeparator());
    add(Box.createVerticalStrut(5));

    String copyright;
    try {
      copyright = IOUtilities.loadText(Jin.class.getResourceAsStream("legal/copyright.txt"));
    } catch (java.io.IOException e) {
      add(i18n.createLabel("unableToLoadCopyright"));
      return;
    }
    StringTokenizer copyrightLines = new StringTokenizer(copyright, "\r\n");

    Font font = new Font("SansSerif", Font.PLAIN, 12);
    while (copyrightLines.hasMoreTokens()) {
      String line = copyrightLines.nextToken();
      JLabel label = new JLabel(line.trim(), SwingConstants.CENTER);
      label.setAlignmentX(CENTER_ALIGNMENT);
      label.setFont(font);
      add(label);
    }

    add(Box.createVerticalStrut(10));

    JLabel dedicationLabel =
        new JLabel(Jin.getAppProperty("app.dedication", null), SwingConstants.CENTER);
    dedicationLabel.setAlignmentX(CENTER_ALIGNMENT);
    add(dedicationLabel);
    dedicationLabel.setFont(font);

    add(Box.createVerticalStrut(10));

    JButton closeButton = i18n.createButton("closeButton");
    closeButton.addActionListener(new ClosingListener(null));
    closeButton.setAlignmentX(CENTER_ALIGNMENT);
    add(closeButton);

    setDefaultButton(closeButton);
  }
}
