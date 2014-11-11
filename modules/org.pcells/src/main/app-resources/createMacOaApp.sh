#!/bin/sh

THISVERSION=2.0.3

echo "Creating Darwin .... "
#
DARWINNAME=pcells-${THISVERSION}.app
#
cp -R ../org/pcells/app/pcells.app pcells.app
cp -R ../org/pcells/app/pcells.app ${DARWINNAME}
sed "s/THISVERSION/$THISVERSION/" <pcells.app/Contents/Info.plist >${DARWINNAME}/Contents/Info.plist
cp ./*.jar ${DARWINNAME}/Contents/Resources/Java/
