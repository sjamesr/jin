/**
 * The utillib library.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
 * All rights reserved.
 *
 * The utillib library is free software; you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * The utillib library is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with utillib library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package free.util.swing;

import java.awt.Component;
import java.awt.Font;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import free.util.Localization;
import free.util.WindowDisposingListener;
import free.workarounds.FixedJTextArea;
  
  
/**
 * A simple dialog for displaying plain text until the user closes it.
 */

public class PlainTextDialog extends JDialog{



  /**
   * The title of the text (and the dialog).
   */

  private final String title;




  /**
   * The text area displaying the text.
   */

  private final JTextArea textArea;



  /**
   * The "OK" button.
   */

  private final JButton closeButton;



  /**
   * Creates a new LicenseTextDialog with the given parent, title and text.
   * The title may be <code>null</code>
   */

  public PlainTextDialog(Component parent, String title, String text){
    super(SwingUtils.frameForComponent(parent), title == null ? "" : title, true); //$NON-NLS-1$

    this.title = title;

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    SwingUtils.registerEscapeCloser(this);

    textArea = new FixedJTextArea(text, 20, 81);
    
    Localization l10n = LocalizationService.getForClass(PlainTextDialog.class);
    closeButton = new JButton(l10n.getString("closeButton.text")); //$NON-NLS-1$

    createUI();
  }




  /**
   * Sets the font of the text area displaying the text.
   */

  public void setTextAreaFont(Font font){
    textArea.setFont(font);
  }



  /**
   * Creates the UI.
   */

  protected void createUI(){
    JPanel contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    setContentPane(contentPane);

    if (title != null){
      JLabel titleLabel = new JLabel(title, JLabel.CENTER);
      titleLabel.setAlignmentX(CENTER_ALIGNMENT);
      titleLabel.setFont(new Font("Serif", Font.PLAIN, 36)); //$NON-NLS-1$
      contentPane.add(titleLabel);
      contentPane.add(Box.createVerticalStrut(10));
    }

    contentPane.add(Box.createVerticalStrut(10));

    textArea.setRequestFocusEnabled(false);
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    JScrollPane scrollPane = new JScrollPane(textArea);
    contentPane.add(scrollPane);

    contentPane.add(Box.createVerticalStrut(20));

    closeButton.addActionListener(new WindowDisposingListener(this));
    closeButton.setAlignmentX(CENTER_ALIGNMENT);
    getRootPane().setDefaultButton(closeButton);
    contentPane.add(closeButton);
  }



}
