# Parameters:
# dir - The directory whose entire contents will be in the install file.
#       This MUST include the trailing slash or backslash.
# file - The name of the installer file.
# ver - The Jin version.
#
# The installer should be run with NOCD flag set.

Name "Jin ${ver}"
OutFile ${file}
InstallDir $PROGRAMFILES\Jin
DirText "Select installation directory"

Page directory "" "" leavingDir
Page instfiles

Function leavingDir
  IfFileExists $INSTDIR delete_dir
  Goto end

  delete_dir:
    StrCmp $INSTDIR $PROGRAMFILES\Jin really_delete
    MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION "The specified installation directory already exists, do you wish to delete its contents and continue?" IDOK really_delete
    Abort
    Goto end

    really_delete:
      RMDir /r $INSTDIR
      IfErrors delete_fail
      Goto end

  delete_fail:
    MessageBox MB_OK|MB_ICONSTOP \
    "The selected installation directory already exists and cannot be deleted.$\nPlease close the application that uses it or select a different directory."
    Abort
    Goto end

  end:

FunctionEnd



Section "Jin"
  SetOutPath $INSTDIR
  File /r ${dir}*.*

  WriteUninstaller uninstall.exe
  
  IfFileExists $SMPROGRAMS\Jin delete_start_menu
  Goto no_delete_start_menu
  
  delete_start_menu:
    RMDir /r $SMPROGRAMS\Jin
    Goto no_delete_start_menu 
  
  no_delete_start_menu:
  CreateDirectory $SMPROGRAMS\Jin
  CreateShortCut $SMPROGRAMS\Jin\Jin.lnk $INSTDIR\jin.exe
  CreateShortCut "$SMPROGRAMS\Jin\Jin (Standard Java).lnk" $INSTDIR\jin.jar
  CreateShortCut "$SMPROGRAMS\Jin\Jin (Microsoft VM).lnk" $INSTDIR\jin_ms.exe
  CreateShortCut $SMPROGRAMS\Jin\Readme.lnk $INSTDIR\readme.txt
  CreateShortCut $SMPROGRAMS\Jin\Uninstall.lnk $INSTDIR\uninstall.exe
SectionEnd


Section "Uninstall"
  RMDir /r $INSTDIR
  RMDir /r $SMPROGRAMS\Jin
SectionEnd


