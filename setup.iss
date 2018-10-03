#ifndef AppVersion
  #define AppVersion GetFileVersion(SourcePath+'..\..\ditaot.exe')
  #define AppVersion Copy(AppVersion, 1, RPos('.', AppVersion) - 1)
  #define tmpvar Copy(AppVersion, RPos('.', AppVersion) + 1, 3)
  #if tmpvar == "0"
    #define AppVersion Copy(AppVersion, 1, RPos('.', AppVersion) - 1)
  #endif
  #undef tmpvar
;  #define AppVersion AppVersion+'-beta'
#endif

#define AppName "Dita Open Toolkit"
#define NameShort "DitaOT"
#define DirName "Dita-OT"
#define AppVerName AppName + " " + AppVersion
#define AppPublisher "Dita OT Volunteers"
#define AppURL "https://www.dita-ot.org/"
#define AppExeName "startcmd.bat"
#define CurYear GetDateTimeString('yyyy', '', '')

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"


[Setup]
AppId=ditaot
AppName={#AppName}
AppVerName={#AppVerName}
AppCopyright=Copyright (c) 2005-{#CurYear} by Dita OT Volunteers
AppPublisher={#AppPublisher}
AppPublisherURL={#AppURL}
AppSupportURL={#AppURL}
AppUpdatesURL={#AppURL}
UninstallDisplayIcon={app}\ditaot.exe

VersionInfoVersion={#GetFileVersion(SourcePath+'..\..\ditaot.exe')}
VersionInfoTextVersion={#GetFileVersion(SourcePath+'..\..\ditaot.exe')}

DefaultGroupName={#AppName}
AllowNoIcons=yes
LicenseFile=LICENSE
OutputDir={#OutputDir}
OutputBaseFilename=ditaot-{#AppVersion}-setup

Compression=lzma2/ultra64
InternalCompressLevel=ultra64
SolidCompression=yes

PrivilegesRequired=poweruser
ChangesAssociations=yes
ChangesEnvironment=true

#if "user" == InstallTarget
DefaultDirName={userpf}\{#DirName}
PrivilegesRequired=lowest
#else
DefaultDirName={pf}\{#DirName}
#endif

WizardImageFile=compiler:\WizModernImage-IS.bmp
WizardSmallImageFile=compiler:\WizModernSmallImage-IS.bmp

[CustomMessages]
AddToPath=Add {#AppName} to the path.
Other=Other options:
UninstallProgram=Uninstall %1
LaunchProgram=Launch %1
ProgramOnTheWeb=%1 on the Web
CreateQuickLaunchIcon=Create a &Quick Launch icon
CreateDesktopIcon=Create a &desktop icon
AdditionalIcons=Additional icons:
NameAndVersion=%1 version %2

[Tasks]
Name: "addtopath"; Description: "{cm:AddToPath}"; GroupDescription: "{cm:Other}"
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: checkedonce; Check: IsNotUpdate
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked; OnlyBelowVersion: 0,6.1

[Types]
Name: "full"; Description: "Full installation"
Name: "compact"; Description: "Compact installation"

[Components]
Name: "app"; Description: "Main application files"; Types: full compact; Flags: fixed
Name: "docs"; Description: "Local documentation"; Types: full

[Dirs]
Name: "{app}\bin"; Components: app
Name: "{app}\config"; Components: app
Name: "{app}\docsrc"; Components: docs
Name: "{app}\lib"; Components: app
Name: "{app}\plugins"; Components: app
Name: "{app}\xsl"; Components: app

[Files]
Source: "build\tmp\dist\startcmd.bat"; DestDir: "{app}"; Flags: ignoreversion; Components: app
Source: "build\tmp\dist\LICENSE"; DestDir: "{app}"; DestName: "LICENSE.txt"; Flags: ignoreversion; Components: app
Source: "build\tmp\dist\build*.xml"; DestDir: "{app}"; Flags: ignoreversion; Components: docs
Source: "build\tmp\dist\catalog-dita*.xml"; DestDir: "{app}"; Flags: ignoreversion; Components: app
Source: "build\tmp\dist\integrator.xml"; DestDir: "{app}"; Flags: ignoreversion; Components: app

; Dirs
Source: "build\tmp\dist\bin\*.bat"; DestDir: "{app}\bin"; Flags: ignoreversion recursesubdirs createallsubdirs; Components: app
Source: "build\tmp\dist\config\*"; DestDir: "{app}\config"; Flags: ignoreversion recursesubdirs createallsubdirs; Components: app
; Source: "build\tmp\dist\docsrc\*"; DestDir: "{app}\docsrc"; Flags: ignoreversion recursesubdirs createallsubdirs; Components: docs
Source: "build\tmp\dist\lib\*"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs createallsubdirs; Components: app
Source: "build\tmp\dist\plugins\*"; DestDir: "{app}\plugins"; Flags: ignoreversion recursesubdirs createallsubdirs; Components: app
Source: "build\tmp\dist\xsl\*"; DestDir: "{app}\xsl"; Flags: ignoreversion recursesubdirs createallsubdirs; Components: app

[Icons]
Name: "{group}\{#AppName}"; Filename: "{app}\{#AppExeName}"; WorkingDir: {app}
Name: "{group}\License"; Filename: "{app}\LICENSE.txt"; WorkingDir: {app}
Name: "{group}\Home page"; Filename: "{#AppURL}"; WorkingDir: {app}
Name: "{group}\{cm:UninstallProgram,{#AppName}}"; Filename: "{uninstallexe}"; WorkingDir: {app}
Name: "{commondesktop}\{#AppName}"; Filename: "{app}\{#AppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\bin\dita.bat"; Description: "{cm:LaunchProgram,{#AppName}}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: filesandordirs ; Name: "{localappdata}\{#AppName}"

[Registry]
#if "user" == InstallTarget
#define SoftwareClassesRootKey "HKCU"
#else
#define SoftwareClassesRootKey "HKLM"
#endif
; Environment
#if "user" == InstallTarget
#define EnvironmentRootKey "HKCU"
#define EnvironmentKey "Environment"
#define Uninstall64RootKey "HKCU64"
#define Uninstall32RootKey "HKCU32"
#else
#define EnvironmentRootKey "HKLM"
#define EnvironmentKey "System\CurrentControlSet\Control\Session Manager\Environment"
#define Uninstall64RootKey "HKLM64"
#define Uninstall32RootKey "HKLM32"
#endif

Root: {#EnvironmentRootKey}; Subkey: "{#EnvironmentKey}"; ValueType: expandsz; ValueName: "Path"; ValueData: "{olddata};{app}\bin"; Tasks: addtopath; Check: NeedsAddPath(ExpandConstant('{app}\bin'))

[Code]
function NeedsAddPath(Param: string): boolean;
var
  OrigPath: string;
begin
  if not RegQueryStringValue({#EnvironmentRootKey}, '{#EnvironmentKey}', 'Path', OrigPath)
  then begin
    Result := True;
    exit;
  end;
  Result := Pos(';' + Param + ';', ';' + OrigPath + ';') = 0;
end;
{ Both DecodeVersion and CompareVersion functions where taken from the  wiki }
procedure DecodeVersion (verstr: String; var verint: array of Integer);
var
  i,p: Integer; s: string;
begin
  { initialize array }
  verint := [0,0,0,0];
  i := 0;
  while ((Length(verstr) > 0) and (i < 4)) do
  begin
    p := pos ('.', verstr);
    if p > 0 then
    begin
      if p = 1 then s:= '0' else s:= Copy (verstr, 1, p - 1);
      verint[i] := StrToInt(s);
      i := i + 1;
      verstr := Copy (verstr, p+1, Length(verstr));
    end
    else
    begin
      verint[i] := StrToInt (verstr);
      verstr := '';
    end;
  end;

end;

function CompareVersion (ver1, ver2: String) : Integer;
var
  verint1, verint2: array of Integer;
  i: integer;
begin

  SetArrayLength (verint1, 4);
  DecodeVersion (ver1, verint1);

  SetArrayLength (verint2, 4);
  DecodeVersion (ver2, verint2);

  Result := 0; i := 0;
  while ((Result = 0) and ( i < 4 )) do
  begin
    if verint1[i] > verint2[i] then
      Result := 1
    else
      if verint1[i] < verint2[i] then
        Result := -1
      else
        Result := 0;
    i := i + 1;
  end;

end;

function WizardNotSilent(): Boolean;
begin
  Result := not WizardSilent();
end;

// Updates
function IsBackgroundUpdate(): Boolean;
begin
  Result := ExpandConstant('{param:update|false}') <> 'false';
end;

function IsNotUpdate(): Boolean;
begin
  Result := not IsBackgroundUpdate();
end;

function InitializeSetup(): Boolean;
var
  ErrorCode: Integer;
  JavaVer : String;
  Result1 : Boolean;
begin
    RegQueryStringValue(HKLM, 'SOFTWARE\JavaSoft\Java Runtime Environment', 'CurrentVersion', JavaVer);
    Result := false;
    if Length( JavaVer ) > 0 then
    begin
        if CompareVersion(JavaVer,'1.8') >= 0 then
        begin
            Result := true;
        end;
    end;
    if Result = false then
    begin
        Result1 := MsgBox('This tool requires Java Runtime Environment v1.8 or older to run. Please download and install JRE and run this setup again.' + #13 + #10 + 'Do you want to download it now?',
          mbConfirmation, MB_YESNO) = idYes;
        if Result1 = true then
        begin
            ShellExec('open',
              'http://www.java.com/en/download/manual.jsp#win',
              '','',SW_SHOWNORMAL,ewNoWait,ErrorCode);
        end;
    end;
end;
