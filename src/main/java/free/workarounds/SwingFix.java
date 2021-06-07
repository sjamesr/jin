/**
 * The workarounds library. More information is available at http://www.jinchess.com/. Copyright (C)
 * 2002 Alexander Maryanovsky. All rights reserved.
 *
 * <p>The workarounds library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 *
 * <p>The workarounds library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * <p>You should have received a copy of the GNU Lesser General Public License along with the
 * workarounds library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite
 * 330, Boston, MA 02111-1307 USA
 */
package free.workarounds;

import java.awt.Color;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RepaintManager;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

/**
 * A class which statically applies various fixes/workarounds for swing. To use this, simply include
 * Class.forName("free.workarounds.SwingFix") somewhere in your application.
 */
public class SwingFix {

  /**
   * Adds key bindings for ctrl+c, ctrl+x, ctrl+v, and COPY, CUT and PASTE keys on Sun keyboards
   * which are missing from the Motif Look and Feel. <A
   * HREF="http://developer.java.sun.com/developer/bugParade/bugs/4106281.html">
   * http://developer.java.sun.com/developer/bugParade/bugs/4106281.html</A> Note: also adds
   * Command-C, Command-V and Command-X keybindings for MacOS users.
   */
  static {
    String cutAction = DefaultEditorKit.cutAction;
    String copyAction = DefaultEditorKit.copyAction;
    String pasteAction = DefaultEditorKit.pasteAction;

    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

    JTextComponent.KeyBinding cut =
        new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_CUT, 0), cutAction);
    JTextComponent.KeyBinding copy =
        new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_COPY, 0), copyAction);
    JTextComponent.KeyBinding paste =
        new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_PASTE, 0), pasteAction);
    JTextComponent.KeyBinding ctrlX =
        new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_X, mask), cutAction);
    JTextComponent.KeyBinding ctrlC =
        new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_C, mask), copyAction);
    JTextComponent.KeyBinding ctrlV =
        new JTextComponent.KeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_V, mask), pasteAction);

    JTextComponent.KeyBinding[] extraBindings =
        new JTextComponent.KeyBinding[] {cut, copy, paste, ctrlX, ctrlC, ctrlV};

    Keymap defaultKeyMap = JTextComponent.getKeymap(JTextComponent.DEFAULT_KEYMAP);

    JTextComponent tempC = new JTextField();
    JTextComponent.loadKeymap(defaultKeyMap, extraBindings, tempC.getActions());
  }

  /**
   * Sets some acceptable margins on JTextField and JPasswordField because they're 0,0,0,0 by
   * default.
   */
  static {
    UIManager.put("TextField.margin", new Insets(0, 2, 0, 2));
    UIManager.put("PasswordField.margin", new Insets(0, 2, 0, 2));
  }

  /**
   * Fix the color of the scrollbar track to something different than the default panel color -
   * otherwise it's invisible sometimes. This changes the color on both Motif and Windows L&Fs,
   * breaking the Motif L&F, but fixing the Windows L&F, so will change it only if we're on windows
   * (the OS, not the L&F).
   */
  static {
    String osName = System.getProperty("os.name");
    if (osName.indexOf("Windows") != -1) {
      UIManager.put("ScrollBar.track", new Color(230, 230, 230));
    }
  }

  /**
   * Disables swing double buffering under Mac OS X since OS X does double buffering all by itself.
   */
  static {
    try {
      String osName = System.getProperty("os.name");
      if (osName.indexOf("Mac OS X") != -1) {
        // I know passing null may not be such a good idea, but the component
        // argument is unused anyway, and the static method can't be "overriden"
        // even though the documentation says it can :-).
        RepaintManager.currentManager(null).setDoubleBufferingEnabled(false);
      }
    } catch (RuntimeException e) {
      e.printStackTrace();
    }
  }
}
