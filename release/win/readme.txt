
****************************
*********** Java ***********
****************************

There are generally two versions of Java that Jin can use - Microsoft VM and
Standard Java. Here are the important differences:

Microsoft VM:
* Already installed on most Windows computers.
* Broken on some windows computers and will not run Jin at all, break and
  display garbage at times or completely hang your computer.
* Quite fast and uses less memory than standard Java.
* Has less features, which is sometimes reflected in Jin's features. 
  For example, when Jin runs under Microsoft VM it will not support:
  1. PNG based piece sets or board patterns.
  2. Antialiased text in the console.
  3. Real fullscreen mode for the board - it will instead try to display a
     window which spans the entire screen, which is similar but not the same.
  4. Modern Look and Feels.

Standard Java:
* Usually needs to be installed. You can download it from http://www.java.com/
  (don't get distracted by the pretty pictures - click the "Get it now" button).
* Stable and less likely to crash.
* Usually less responsive than Microsoft VM, although it gets better with every
  version.
* Lets Jin use all of its features.


***********************************
*********** Running Jin ***********
***********************************

Once you've installed Jin, there are three ways to run it:

1. Via the jin.exe file or the "Jin" item in the Start Menu - this will first
   try to run Jin with standard Java, and if that fails with Microsoft VM.
   If both fail, you will get an error about reinstalling the application, but
   the problem is most likely that you don't have either version of Java
   installed.
2. Via the jin_ms.exe file or the "Jin (Microsoft VM)" item in the Start Menu -
   this will run Jin with Microsoft VM.
3. By double clicking jin.jar or via the "Jin (Standard Java)" item in the
   Start Menu - this will run Jin with standard Java, if you have that
   installed.
