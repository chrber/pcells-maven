#!/bin/sh
#
#         get our location
#
PRG=`type -p $0` >/dev/null 2>&1
while [ -L "$PRG" ]
 do
   newprg=`expr "\`/bin/ls -l "$PRG"\`" : ".*$PRG -> \(.*\)"`
   expr "$newprg" : / >/dev/null || newprg="`dirname $PRG`/$newprg"
   PRG="$newprg"
 done
#
thisDir=`dirname $PRG`
cd ${thisDir}
thisDir=`pwd`
#
#  check for java
#
javaOrigin=`which java 2>/dev/null`
if [ $? -ne 0 ] ; then
   echo "" >&2
   echo " Java VM not found in PATH" >&2
   echo "" >&2
fi
#
#  check for desktop
#
ourDesktop=$HOME/Desktop
#
if [ ! \( \( -d "${ourDesktop}" \) -a \( -w "${ourDesktop}" \) \) ] ;  then
   echo "" >&2
   echo "Your kde 3 Desktop directory doesn't exists or is not writable" >&2
   echo "" >&2
   exit 3
fi
#
#  the name
#
ourName=pcells-gui
#
rm -rf ${ourDesktop}/${ourName}.desktop
#
(
echo "[Desktop Entry]"
echo "Comment="
echo "Comment[de]="
echo "Encoding=UTF-8"
echo "Exec=${javaOrigin} -jar ${thisDir}/org.pcells.jar"
echo "GenericName="
echo "GenericName[de]="
echo "Icon=${thisDir}/eagle_icon.png"
echo "MimeType="
echo "Name=${ourName}"
echo "Name[de]=${ourName}"
echo "Path="
echo "StartupNotify=false"
echo "Terminal=false"
echo "TerminalOptions="
echo "Type=Application"
echo "X-DCOP-ServiceType=none"
echo "X-KDE-SubstituteUID=false"
echo "X-KDE-Username="
) >${ourDesktop}/${ourName}.desktop
#
exit 0
