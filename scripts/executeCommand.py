#!/usr/bin/env jython

import string
import sys
from AdminServerSession import runAdminCommands

def executeCommand( sendCommand, verbose, opts, args ) :
    cellName=args[0]
    listOfCommands=args[1:]
    for command in listOfCommands:
        if not command: continue
        print "Executing command %s on cell %s"%(command,cellName,)
        try:
            rc = sendCommand(cellName,command)
            print rc
        except:
            print "Command %s on cell %s failed"%(command,cellName,)
            continue

if __name__ == '__main__':
    runAdminCommands( executeCommand )
    sys.exit( 0 )
