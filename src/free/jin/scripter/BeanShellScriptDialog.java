/**
 * Jin - a chess client for internet chess servers.
 * More information is available at http://www.jinchess.com/.
 * Copyright (C) 2003 Alexander Maryanovsky.
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

package free.jin.scripter;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import bsh.EvalError;
import free.jin.I18n;
import free.util.TableLayout;
import free.workarounds.FixedJTextArea;


/**
 * A dialog allowing the user to create a new BeanShellScript.
 */

class BeanShellScriptDialog extends ScriptDialog{
  
  
  
  /**
   * The text area for the code.
   */

  private JTextArea codeArea;


  /**
   * Creates a new <code>BeanShellScriptDialog</code> with the specified parent
   * and <code>Script</code> whose properties are used as default values.
   */

  public BeanShellScriptDialog(Component parent, Scripter scripter, BeanShellScript templateScript){
    super(parent, "", scripter, templateScript);
    
    setTitle(I18n.get(BeanShellScriptDialog.class).getString("title"));
    
    createUI();
  }



  /**
   * Creates the user interface for this dialog.
   */

  protected Container createScriptTypeSpecificUI(){
    I18n i18n = I18n.get(BeanShellScriptDialog.class);
    
    BeanShellScript templateScript = (BeanShellScript)(this.templateScript);
    String defaultCode = (templateScript == null ? "" : templateScript.getCode());

    codeArea = new FixedJTextArea(10, 60);
    codeArea.setText(defaultCode);
    codeArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    codeArea.setTabSize(2);

    JScrollPane codeScrollPane = new JScrollPane(codeArea);
    codeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    codeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    JLabel codeLabel = i18n.createLabel("codeLabel");

    codeLabel.setLabelFor(codeArea);
    codeLabel.setAlignmentY(Component.TOP_ALIGNMENT);

    JButton codeHelp = i18n.createButton("helpButton");
    codeHelp.setDefaultCapable(false);

    codeHelp.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent evt){
        I18n i18n = I18n.get(BeanShellScriptDialog.class);
        i18n.showPlainTextDialog("helpDialog", BeanShellScriptDialog.this);
      }
    });

    JPanel panel = new JPanel(new TableLayout(3, 7, 5));

    panel.add(codeLabel);
    panel.add(codeScrollPane);
    panel.add(codeHelp);

    return panel;
  }




  /**
   * Creates and returns a BeanShellScript from the user specified information.
   */

  protected Script createScriptOnOk(String scriptName, String eventType, String [] selectedSubtypes){
    String code = codeArea.getText();

    try{
      BeanShellScript script = new BeanShellScript(scripter, scriptName, 
        eventType, selectedSubtypes, code);

      if (templateScript != null)
        script.setEnabled(templateScript.isEnabled());
      return script;
    } catch (EvalError e){
        I18n.get(BeanShellScriptDialog.class).error("malformedScriptDialog", this, new Object[]{e.getErrorText()});
        return null;
      }
  }


}
