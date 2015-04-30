
package org.pcells.services.gui ;

import javax.swing.JComponent;
import javax.swing.JFrame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

public class JTest3 extends JComponent {


    public JTest3(Frame frame , String [] args ) throws IOException{


    }
    private long _inTokenUsed = 100L ;
    private long _inTokenFree = 200L ;
    private long _outTokenUsed = 200L ; 
    private long _outTokenFree = 300L ;
    private long _totalSpace   = 700L ;
    
    private Color _inTokenUsedColor = Color.red ;
    private Color _inTokenFreeColor = Color.yellow ;
    private Color _outTokenUsedColor = Color.red ;
    private Color _outTokenFreeColor = Color.yellow ;
    private Color _totalSpaceColor  = Color.green ;
    private Color _tokenColor       = Color.blue ;
    
    private int dY  = 20 ;
    private int ddY = 10 ;
    private Insets _inset = new Insets(10,10,10,10) ;
    
    public Dimension getMinimumSize(){
       return new Dimension(200,50) ;
    }
    public void paintComponent( Graphics gin ){

        Graphics2D g = (Graphics2D) gin ;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);

       Dimension d = getSize(); 

       long totalMax = _inTokenUsed + _inTokenFree + _outTokenUsed + _outTokenFree ;
       totalMax = Math.max( totalMax , _totalSpace ) ;
       
       int internalLength = d.width - _inset.right - _inset.left ;
       
       int inTokenUsed = (int)( (float)_inTokenUsed/(float)totalMax * (float)internalLength ) ;
       int inTokenFree = (int)( (float)_inTokenFree/(float)totalMax * (float)internalLength ) ;
       int outTokenUsed = (int)( (float)_outTokenUsed/(float)totalMax * (float)internalLength ) ;
       int outTokenFree = (int)( (float)_outTokenFree/(float)totalMax * (float)internalLength ) ;
       int totalSpace   = (int)( (float)_totalSpace/(float)totalMax * (float)internalLength ) ;
       int tokenSpace   = inTokenUsed + inTokenFree ;

       int x = _inset.left ;
       int y = ( d.height - _inset.top - _inset.bottom - dY ) / 2 + _inset.top ;

       
       
       g.setColor( Color.gray ) ;
       g.fillRoundRect( _inset.left + 2 , y - 2 * ddY + 2 , totalSpace , dY + 4 * ddY , dY , dY ) ;
       g.setColor( _totalSpaceColor ) ;
       g.fillRoundRect( _inset.left , y - 2 * ddY , totalSpace , dY + 4 * ddY , dY , dY ) ;

       g.setColor( Color.gray ) ;
       g.fillRoundRect( _inset.left + 2 , y - ddY + 2 , tokenSpace , dY + 2 * ddY , dY , dY ) ;
       g.setColor( _tokenColor ) ;
       g.fillRoundRect( _inset.left , y - ddY , tokenSpace , dY + 2 * ddY , dY , dY ) ;
       
       g.setColor( _inTokenUsedColor ) ;
       g.fillRect( x , y , inTokenUsed , dY ) ;
       x += inTokenUsed ;
       
       g.setColor( _inTokenFreeColor ) ;
       g.fillRect( x , y , inTokenFree , dY ) ;
       
       x += inTokenFree ;
       
       g.setColor( _outTokenUsedColor ) ;
       g.fillRect( x , y , outTokenUsed , dY ) ;
       
       x += outTokenUsed ;
       
       g.setColor( _outTokenFreeColor ) ;
       g.fillRect( x , y , outTokenFree , dY ) ;
       
       

       g.drawRect( _inset.left , _inset.top ,
                   d.width - _inset.left - _inset.right ,
		   d.height - _inset.top - _inset.bottom ) ;
       
       //g.drawRoundRect( d.width/2 , 0 , 200 , 200 , 20 , 20 ) ; 
    }


    public static void main(String s[]) throws Exception  {

        System.out.println("Starting ...");
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        };



       JFrame f = new JFrame("xxx");
       f.addWindowListener(l);
       
       f.getContentPane().add( new JTest3(f,s),  "Center" );
       f.pack();
       f.setSize(new Dimension(900,500));
       f.setVisible(true);
    }
}
