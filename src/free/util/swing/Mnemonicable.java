package free.util.swing;



/**
 * A tagging interface for components which display text and can have a mnemonic.
 */

public interface Mnemonicable{
  
  
  
  /**
   * Sets the text of the component.
   */
  
  void setText(String text);
  
  
  
  /**
   * Sets the displayed mnemonic index.
   */
  
  void setDisplayedMnemonicIndex(int index);
  
  
  
}
