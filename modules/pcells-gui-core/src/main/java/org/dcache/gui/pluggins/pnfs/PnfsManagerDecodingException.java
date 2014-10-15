// $Id: PnfsManagerPanel.java,v 1.1 2008/11/09 08:23:58 cvs Exp $
//
package org.dcache.gui.pluggins.pnfs;
//

public class PnfsManagerDecodingException extends Exception {
    private int _lineNumber = 0 ;
    public PnfsManagerDecodingException(String message , int lineNumber ){
         super(message);
        _lineNumber = lineNumber ;
    }
    public int getLine(){ return _lineNumber ; }
}

