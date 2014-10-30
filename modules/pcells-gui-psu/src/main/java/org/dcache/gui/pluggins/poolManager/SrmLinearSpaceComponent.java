
package org.dcache.gui.pluggins.poolManager ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class SrmLinearSpaceComponent extends JComponent implements ListDataListener {

    private Logger _logger = LoggerFactory.getLogger(SrmLinearSpaceComponent.class);

    private long _inTokenUsed = 0L ;
    private long _inTokenFree = 0L ;
    private long _outTokenUsed = 0L ;
    private long _outTokenFree = 0L ;
    private long _totalSpace   = 0L ;

    private ListModel _model  = null ;

    private Color _inTokenUsedColor = Color.red ;
    private Color _inTokenFreeColor = Color.yellow ;
    private Color _outTokenUsedColor = Color.red ;
    private Color _outTokenFreeColor = Color.yellow ;
    private Color _totalSpaceColor  = Color.green ;
    private Color _tokenColor       = Color.blue ;

    private int dY  = 10 ;
    private int ddY = 4 ;
    private Insets _inset = new Insets(10,10,10,10) ;

    private void updateValues(){

        int size = 0 ;
        if( ( _model == null ) || ( ( size = _model.getSize() ) < 5 ) ){
            _logger.error("Model content either null or < 5");
            return ;
        }
        try{
            _totalSpace   = ((Long)_model.getElementAt(0)).longValue() ;
            _inTokenUsed  = ((Long)_model.getElementAt(1)).longValue() ;
            _inTokenFree  = ((Long)_model.getElementAt(2)).longValue() ;
            _outTokenUsed = ((Long)_model.getElementAt(3)).longValue() ;
            _outTokenFree = ((Long)_model.getElementAt(4)).longValue() ;

            _logger.debug("Spaces : " + _totalSpace + " " + _inTokenUsed + " " + _inTokenFree + " " + _outTokenUsed + " " + _outTokenFree);

        }catch(Exception ee){
            _logger.error("Problem getting model data : " + ee);
        }

    }
    private void resetValue(){

    }
    public Dimension getMinimumSize(){
        return new Dimension(0,dY+4*ddY+_inset.top+_inset.bottom) ;
    }
    public Dimension getPreferredSize(){
        return new Dimension(0,dY+4*ddY+_inset.top+_inset.bottom) ;
    }
    public void setModel( ListModel model ){
        if( _model != null )_model.removeListDataListener( this ) ;
        _model = model ;
        if( _model != null )_model.addListDataListener( this ) ;
    }
    public void contentsChanged( ListDataEvent event ){
        _logger.debug("Content Changed SrmLinearComponent : " + event) ;
        updateValues();
        repaint() ;
    }
    public void intervalAdded( ListDataEvent event ){
    }
    public void intervalRemoved( ListDataEvent event ){
    }
    public void paintComponent( Graphics gin ){

        Graphics2D g = (Graphics2D) gin ;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Dimension d = getSize();
        _logger.debug("paintComponent : " + d);
        long totalMax = _inTokenUsed + _inTokenFree + _outTokenUsed + _outTokenFree ;
        totalMax = Math.max( totalMax , _totalSpace ) ;

        int internalLength = d.width - _inset.right - _inset.left ;

        if( totalMax == 0 )return ;

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



        //g.drawRect( _inset.left , _inset.top ,
        //            d.width - _inset.left - _inset.right ,
//		   d.height - _inset.top - _inset.bottom ) ;

        //g.drawRoundRect( d.width/2 , 0 , 200 , 200 , 20 , 20 ) ;
    }


    public static void main(String s[]) throws Exception  {

        System.out.println("Starting ...");
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        };

        final LinearSpaceListModel lm = new LinearSpaceListModel() ;

        SrmLinearSpaceComponent ssc = new SrmLinearSpaceComponent() ;
        ssc.setModel(lm) ;


        JFrame f = new JFrame("xxx");
        f.addWindowListener(l);

        JPanel x = new JPanel( new BorderLayout(10,10) ) ;
        x.add(  ssc ,  "North" );
        x.add(  new SrmLinearSpaceComponent(),  "Center" );
        f.getContentPane().add( x,  "Center" );
        f.pack();
        f.setSize(new Dimension(900,500));
        f.setVisible(true);


        new Thread( new Runnable(){
            public void run(){
                try{
                    Thread.sleep(10000L);
                }catch(Exception ee ){
                }finally{
                    SwingUtilities.invokeLater(
                            new Runnable(){
                                public void run(){
                                    lm.setSpaces( 1000L , 100L , 200L , 300L , 400L ) ;
                                }
                            }
                    ) ;
                }
            }
        }
        ).start() ;

    }
}

