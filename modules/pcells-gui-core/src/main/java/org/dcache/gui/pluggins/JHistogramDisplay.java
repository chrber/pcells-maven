 // $Id: JHistogramDisplay.java,v 1.10 2007/04/29 11:37:17 cvs Exp $
//
package org.dcache.gui.pluggins;
//

 import org.pcells.services.gui.CellGuiSkinHelper;
 import org.pcells.services.gui.util.DrawBoardFrame;

 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;

 public class      JHistogramDisplay
        extends    CellGuiSkinHelper.CellPanel
                     {

    private Display            _display       = null ;
    private JPanel             _displayPanel  = null ;
    private HistogramDataModel _dataModel     = null ;
    private boolean            _isExtended    = false ;
    private JFrame             _extendedFrame = null ;
    private String             _title         = "Histogram" ;

    public JHistogramDisplay(){ this(null);}
    public JHistogramDisplay( String title ){
       _title = title == null ? _title : title ;
       setLayout( new BorderLayout( 10 , 10 ) ) ;

       add( _display = new Display() , "Center" ) ;
       _display.addMouseListener( new DisplayMouse() );

    }
    private void switchToExternal(){
       remove( _display ) ;
       doLayout() ;
       repaint();
       if( DrawBoardFrame.__sharedStoryBoard != null ){

          DrawBoardFrame.__sharedStoryBoard.addToDrawboard( _display , _title ) ;

       }else{
          if( _extendedFrame == null ){
             _extendedFrame = new JFrame(_title) ;
             _extendedFrame.getContentPane().setLayout( new BorderLayout(10,10) ) ;

               _displayPanel = new CellGuiSkinHelper.CellPanel( new BorderLayout(10,10) ) ;
               _displayPanel.setBorder( new CellGuiSkinHelper.CellBorder(_title,25) ) ;
               _displayPanel.add( _display , "Center" ) ;
               _displayPanel.doLayout() ;
               _display.update() ;

             _extendedFrame.getContentPane().add( _displayPanel , "Center" ) ;
             _extendedFrame.pack() ;
             _extendedFrame.addWindowListener(
                new WindowAdapter(){
                  public void windowClosing(WindowEvent e) {
                     _isExtended = false ;
                     _extendedFrame.setVisible(false) ;
                     _displayPanel.remove( _display ) ;
                     add( _display , "Center" ) ;
                     doLayout() ;
                     repaint();
                     _display.update() ;
                  }
                }
             );
             _extendedFrame.setLocation(100,100);
             _extendedFrame.setSize(600,400);
           }else{
              _displayPanel.add( _display , "Center" ) ;
              _displayPanel.doLayout() ;
              _display.update() ;
              _extendedFrame.getContentPane().doLayout();
           }
           _extendedFrame.setVisible(true);
        }
    }
    private class DisplayMouse extends MouseAdapter {
       public void mouseClicked( MouseEvent event ){
           if( event.getClickCount() > 1 ){
               if( _isExtended )return ;
               _isExtended = true ;
               switchToExternal();
            }

        }
    }
    public void setDataModel( HistogramDataModel mode ){
       _dataModel = mode ;
       repaint() ;
    }
    public HistogramDataModel getDataModel(){
       return _dataModel ;
    }
    public void prepareHistogram( int [] values , int [] flags , int classes , int binCount){
       HistogramDataModel dataModel = new HistogramDataModel() ;
       dataModel.prepareHistogram(values,flags,classes,binCount);
       setDataModel(dataModel);
       _display.update();
    }
    private class HistogramDataModel {

       private Histogram _currentHistogram = null ;

       private class Histogram {
          private int [][] _displayArrays    = null ;
          private int      _maxDisplayArray = 0 ;
          private long     _secondsPerMasterBin = 0 ;
          private long     _secondsPerBin       = 0 ;
          private BinScale _masterBin = null ;
          private BinScale _bin       = null ;
       }
       private  BinScale [] _binDefinition = {
          new BinScale(                 1 ,  1 , "s"  ) ,
          new BinScale(                 2 ,  2 , "s"  ) ,
          new BinScale(                 5 ,  5 , "s"  ) ,
          new BinScale(                10 , 10 , "s"  ) ,
          new BinScale(                30 , 30 , "s"  ) ,
          new BinScale(                60 ,  1 , "m"  ) ,
          new BinScale(               120 ,  2 , "m"  ) ,
          new BinScale(               240 ,  4 , "m"  ) ,
          new BinScale(               300 ,  5 , "m"  ) ,
          new BinScale(               600 , 10 , "m"  ) ,
          new BinScale(              1800 , 30 , "m"  ) ,
          new BinScale(              3600 ,  1 , "h"  ) ,
          new BinScale(          2 * 3600 ,  2 , "h"  ) ,
          new BinScale(          4 * 3600 ,  4 , "h"  ) ,
          new BinScale(          5 * 3600 ,  5 , "h"  ) ,
          new BinScale(         12 * 3600 , 12 , "h"  ) ,
          new BinScale(         24 * 3600 ,  1 , "d"  ) ,
          new BinScale(     2 * 24 * 3600 ,  2 , "d"  ) ,
          new BinScale(     4 * 24 * 3600 ,  4 , "d"  ) ,
          new BinScale(     7 * 24 * 3600 ,  1 , "w"  ) ,
          new BinScale( 2 * 7 * 24 * 3600 ,  2 , "w"  ) ,
       } ;
       private class BinScale {
          private long   secondsPerBin = 0 ;
          private int    unitCount     = 0 ;
          private String unitName      = null ;
          private BinScale( long secondsPerBin , int unitCount , String unitName ){
             this.secondsPerBin = secondsPerBin ;
             this.unitCount     = unitCount ;
             this.unitName      = unitName ;
          }
          public String toString(){
            return "BinScale("+secondsPerBin+"="+unitCount+" "+unitName+")" ;
          }
       }
       public void prepareHistogram( int [] values , int [] flags , int classes , int binCount){
          _currentHistogram = prepareHistogram(values,flags,classes,binCount,0);
          repaint();
       }
       private Histogram prepareHistogram( int [] values , int [] flags , int classes ,
                                           int binCount , int unit ){

          try{
              String binCountString = System.getProperty("histogram.bins");
              if( binCountString != null )binCount = Integer.parseInt(binCountString) ;
          }catch(Exception ee ){
              System.out.println("histogram.bins bad value (using "+binCount);
          }

          Histogram histogram = new Histogram() ;

          boolean useFlags = flags != null ;
          classes = useFlags ? Math.max( classes , 1 ) : 1 ;
          int vecLength = useFlags ? Math.min( values.length , flags.length ) : values.length ;
          long maxValue = 0;
          for( int i = 0 , n = vecLength ; i < n ; i++ ){
         maxValue   = Math.max( maxValue   , values[i] ) ;
          }

          long secPerBin = maxValue / binCount  ;

          //
          // allows to normalize seconds/bin which isn't actually necessary.
          //
          int pos = 0 ;
          boolean _shouldNormalizeSecondsPerBin = false ;
          if( _shouldNormalizeSecondsPerBin ){
             //
             // takes the next highest value of secondsPerBin
             //
             pos = 0 ;
             for( int n = _binDefinition.length ; pos < n ; pos++ ){
                if( _binDefinition[pos].secondsPerBin > secPerBin )break ;
             }
             // border condition
             pos = pos == _binDefinition.length ? _binDefinition.length - 1 : pos ;

             histogram._bin = _binDefinition[pos] ;
             secPerBin      = _binDefinition[pos].secondsPerBin ;
          }else{
             secPerBin      = secPerBin + 1 ;  // to make sure it's large enough
          }
          //
          // new max values
          //
          maxValue = secPerBin * binCount ;
          //
          //   Distribute values among bin containers.
          //
          int [] [] arrays = new int [classes][] ;
          for( int i = 0 ; i < classes ; i ++ )arrays[i] = new int[binCount] ;

          long largest = secPerBin * binCount ;
          for( int i = 0 , n = vecLength ; i < n ; i++ ){
             long diff = values[i] ;
             pos = (int)((float)diff/(float)largest*(float)(binCount-1)) ;
             pos = Math.min( pos , binCount-1 ) ;

             int flag = useFlags ? flags[i] : 1 ;
             for( int c = 0 ; c < classes ; c ++ ){
                if( ( flag & 1 ) == 1 )arrays[c][pos] ++ ;
                flag >>= 1 ;
             }
          }
          //
          // get the highest Value.
          //
          int maxDisplayArray = 0 ;
          for( int i = 0 , n = arrays[0].length ; i < n ; i++ ){
             for( int j = 0 ; j < classes ; j++ )
                maxDisplayArray = Math.max( maxDisplayArray , arrays[j][i] ) ;
          }
          histogram._maxDisplayArray = maxDisplayArray ;
          histogram._displayArrays   = arrays;
          //
          // find the tick values
          //
          //int  binsPerMasterBin      = binCount / 4 ;
          //
          long secondsPerMasterBin   = maxValue / 4 ;
          //
          // normalize secconds per tick.
          //
          int masterPos = 0 ;
          for( int n = _binDefinition.length ; masterPos < n ; masterPos++ ){
             if( _binDefinition[masterPos].secondsPerBin >= secondsPerMasterBin )break ;
          }
          //
          // make it a bit more than defined
          masterPos = Math.max( masterPos - 1 , 0 );

          histogram._secondsPerMasterBin = _binDefinition[masterPos].secondsPerBin ;
          histogram._masterBin           = _binDefinition[masterPos] ;
          histogram._secondsPerBin       = secPerBin ;

          return histogram ;


       }
    }
    public class Display extends CellGuiSkinHelper.CellPanel {

       private float            _unitPerPixel = (float)0.0 ;
       private int              _leftMargin   = 0 ;
       private Color _black = new Color(190,40,40);
       private Color _blue  = new Color(94, 105, 176);
       private Color _gray  = new Color(120,120,230) ;
       private int [] _counterDefinition = {
          1 , 2 , 5 , 10 , 20 , 50 , 100 , 200 ,
          500 , 1000 , 2000 , 5000 , 10000
       };
       public void update(){ repaint() ; }
       public void paintComponent( Graphics gin ){
          Graphics2D g = (Graphics2D) gin ;
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

          Dimension d = getSize() ;

          g.setColor( getBackground() ) ;
 //         g.setPaint(new GradientPaint((float)0,(float)0,_blue,(float)(d.width),(float)(d.height),_black));
          g.fillRect( 0 , 0 , d.width  , d.height  ) ;

          HistogramDataModel.Histogram histogram = null ;
          if( ( _dataModel == null ) || ( ( histogram = _dataModel._currentHistogram ) == null ) ) return ;

          FontMetrics fm = g.getFontMetrics() ;
          int fontHeight = fm.getAscent() - fm.getDescent() ;

          int [] [] arrays    = histogram._displayArrays ;
          int maxDisplayArray = Math.max( histogram._maxDisplayArray , 1 ) ;

          int leftMargin   = 20 ;
          int rightMargin  = 5 ;
          int tickLength   = 4 ;
          //
          // find y bin size
          //
          int ybin = maxDisplayArray / 4 ;
          int pos  = 0 ;
          for( int n = _counterDefinition.length ; pos < n ; pos++ ){
             if( _counterDefinition[pos] >= ybin )break ;
          }
          pos = Math.max(0,pos-1);
          ybin = _counterDefinition[pos] ;
          //
          // find max x label pixel's
          //
          int maxXLabelPixel = fm.stringWidth(""+(ybin * 4));
          leftMargin += ( maxXLabelPixel + 2 + 4 ) ;
 //         int topMargin    = 10 ;
 //         int bottomMargin = 10 ;

          int height   = d.height - 50 ;
          int baseline = d.height - 10 - fontHeight - 2 - 4 ;
          //
          // the picture
          //
          Color [] colors = { getForeground() ,Color.red,Color.blue,Color.orange,Color.yellow};

          String colorsString = System.getProperty("histogram.color.bin");
          try{
              if( colorsString != null )colors[0] = Color.decode(colorsString) ;
          }catch(Exception ee ){
              System.err.println("Couldn't decode : "+colorsString);
          }

          int pixelsPerBin = ( d.width - leftMargin - rightMargin  ) / arrays[0].length  ;
          int x   = leftMargin  ;
          for( int i = 0 , n = arrays[0].length ; i < n ; i++ , x += pixelsPerBin ){
             for( int c = 0 ; c < arrays.length ; c++ ){
                int y = (int)((float)arrays[c][i]/(float)maxDisplayArray*(float)height);
                g.setColor( colors[c%colors.length] ) ;
                g.fillRect( x , baseline - y , pixelsPerBin-1 ,  y );
             }
          }
          _leftMargin   = leftMargin ;
          _unitPerPixel = (float)height / (float)maxDisplayArray ;
          //
          //  axis's
          //
          g.setColor( getForeground() ) ;
          colorsString = System.getProperty("histogram.color.frame");
          try{
              if( colorsString != null )g.setColor(Color.decode(colorsString)) ;
          }catch(Exception ee ){
              System.err.println("Couldn't decode : "+colorsString);
          }
          //
          // x axis
          //
          g.drawLine( leftMargin - tickLength , baseline ,
                      leftMargin + arrays[0].length * pixelsPerBin , baseline ) ;
          //
          // y axis
          //
          g.drawLine( leftMargin , baseline + tickLength ,
                      leftMargin , baseline - height - tickLength ) ;

          int pixelsPerMasterBin = (int)(
              (float)histogram._secondsPerMasterBin /
              (float)histogram._secondsPerBin  *
              (float)pixelsPerBin ) ;

         //
          // x ticks and labels
          //
          int xoffset = leftMargin + pixelsPerMasterBin ;
          int unitCount = histogram._masterBin.unitCount ;
          for( int i = 0 , n = arrays[0].length * pixelsPerBin ;
               xoffset <= n ;
               i++ , xoffset += pixelsPerMasterBin ){
             g.drawLine( xoffset  , baseline-tickLength ,
                         xoffset  , baseline+tickLength ) ;

             String label = ""  + unitCount +
                            " " + histogram._masterBin.unitName ;

             unitCount += histogram._masterBin.unitCount ;
             int stringWidth = fm.stringWidth(label);
             g.drawString( label ,
                           xoffset - stringWidth/2 ,
                           baseline + tickLength + 2 + fm.getAscent() );
          }

          //
          // draw y ticks
          //
          int yoff = ybin ;
          for( int i = 0 ; yoff <= maxDisplayArray ; i ++ , yoff += ybin ){
             int y = (int)((float)yoff/(float)maxDisplayArray*(float)height);
             g.drawLine( leftMargin - tickLength , baseline - y ,
                         leftMargin + tickLength , baseline - y ) ;
             String label = ""+yoff ;
             g.drawString( label ,
                           leftMargin - tickLength - 2 - fm.stringWidth(label)  ,
                           baseline - y + fontHeight) ;
          }


       }
    }

 }

