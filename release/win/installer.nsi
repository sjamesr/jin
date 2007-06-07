# Parameters:
# dir - The directory whose entire contents will be in the install file.
#       This MUST include the trailing slash or backslash.
# file - The name of the installer file.
# name - The name of the application (capitalized properly).
# ver - The version of the application.
#
# The installer should be run with NOCD flag set.

Name "${name} ${ver}"
OutFile ${file}
InstallDir $PROGRAMFILES\${name}
DirText "Select installation directory"

Page directory "" "" leavingDir
Page instfiles

Function leavingDir
  IfFileExists $INSTDIR delete_dir
  Goto end

  delete_dir:
    StrCmp $INSTDIR $PROGRAMFILES\${name} really_delete
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



Section "App"
  SetOutPath $INSTDIR
  File /r ${dir}

  WriteUninstaller uninstall.exe
  
  IfFileExists $SMPROGRAMS\${name} delete_start_menu
  Goto no_delete_start_menu
  
  delete_start_menu:
    RMDir /r $SMPROGRAMS\${name}
    Goto no_delete_start_menu 
  
  no_delete_start_menu:
    CreateDirectory $SMPROGRAMS\${name}
    CreateShortCut $SMPROGRAMS\${name}\${name}.lnk $INSTDIR\${name}.exe
    CreateShortCut $SMPROGRAMS\${name}\Uninstall.lnk $INSTDIR\uninstall.exe
SectionEnd


Section "Uninstall"
  RMDir /r $INSTDIR
  RMDir /r $SMPROGRAMS\${name}
SectionEnd
