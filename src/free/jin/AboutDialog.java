/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2002 Alexander Maryanovsky.
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

package free.jin;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import free.util.WindowDisposingActionListener;
import free.util.IOUtilities;
import java.util.StringTokenizer;


/**
 * The "Help->About" dialog.
 */

public class AboutDialog extends JDialog{



  /**
   * Creates a new AboutDialog.
   */

  public AboutDialog(Frame parent){
    super(parent, "About Jin", true);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    KeyStroke closeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    ActionListener closer = new WindowDisposingActionListener(this);
    getRootPane().registerKeyboardAction(closer, closeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    createUI();
  }




  /**
   * Creates the user interface.
   */

  private void createUI(){
    JPanel contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    setContentPane(contentPane);

    Icon jinIcon = new ImageIcon(AboutDialog.class.getResource("resources/jinicon.gif"));
    JLabel jinLabel = new JLabel("Jin", jinIcon, JLabel.CENTER);
    jinLabel.setFont(new Font("Serif", Font.PLAIN, 36));
    jinLabel.setAlignmentX(CENTER_ALIGNMENT);

    contentPane.add(jinLabel);
    contentPane.add(Box.createVerticalStrut(5));
    contentPane.add(new JSeparator());
    contentPane.add(Box.createVerticalStrut(5));

    String copyright; 
    try{
      copyright = IOUtilities.loadText(AboutDialog.class.getResource("legal/copyright.txt"));
    } catch (java.io.IOException e){
        contentPane.add(new JLabel("Unable to load copyright file"));
        return;
      }
    StringTokenizer copyrightLines = new StringTokenizer(copyright, "\r\n");

    while (copyrightLines.hasMoreTokens()){
      String line = copyrightLines.nextToken();
      JLabel label = new JLabel(line, JLabel.CENTER);
      label.setAlignmentX(CENTER_ALIGNMENT);
      contentPane.add(label);
    }

    contentPane.add(Box.createVerticalStrut(10));

    JLabel loveLabel = new JLabel("This program is dedicated to my Love", JLabel.CENTER);
    loveLabel.setAlignmentX(CENTER_ALIGNMENT);
    contentPane.add(loveLabel);

    contentPane.add(Box.createVerticalStrut(10));

    JButton closeButton = new JButton("OK");
    closeButton.addActionListener(new WindowDisposingActionListener(this));
    closeButton.setAlignmentX(CENTER_ALIGNMENT);
    getRootPane().setDefaultButton(closeButton);
    contentPane.add(closeButton);
  }


}