package org.dcache.gui.pluggins.pools;

import diskCacheV111.pools.PoolCostInfo;
import org.pcells.services.gui.util.DefaultPieChartModel;
import org.pcells.services.gui.util.JPieChart;
import org.pcells.services.gui.util.PieChartModel;

import javax.swing.*;
import java.awt.*;

public class PoolSpacePanel extends JPanel  {

   private class SpaceItem implements PieChartModel.PieChartItem{
   
      private JLabel _title = new JLabel();
      private JLabel _value = new JLabel("              ");
      private Color  _color = null ;
      private long   _longValue = 0L ;
            
      public SpaceItem( String title , Color color ){
         _color = color ;
         _title.setText(title);
         _title.setIcon( new OurIcon( _color ) ) ;
         _title.setIconTextGap(5);
         _value.setHorizontalAlignment( JLabel.RIGHT);
         _value.setText( ""+_longValue);
      }
      public SpaceItem( String title , Color color , long value ){
         this(title,color);
         _longValue = value ;
         _value.setText( ""+_longValue);
      }
      public long getLongValue(){ return _longValue ; }
      public Color getColor(){ return _color ; }
      public String toString(){ return _title.getText() ; }
      public void setLongValue( long value ){
         _longValue = value ;
         _value.setText(longToByteString(_longValue));
      }
   }
   private SpaceItem [] _spaceItems = {
      new SpaceItem( "Total"    , Color.black ) ,  
      new SpaceItem( "Free"     , Color.yellow , 1000L ) ,
      new SpaceItem( "Removable", Color.green , 1000L ) ,
      new SpaceItem( "Pinned"   , Color.magenta ) ,
      new SpaceItem( "Precious" , Color.red )
   } ;
   private JPieChart _pie        = null ;
   private JPanel    _labelPanel = null ;
   private DefaultPieChartModel _pieModel = null ;
   
   public PoolSpacePanel(){
   
       setLayout( new GridBagLayout() ) ;
      
       setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder( Color.blue , 1 ) , "Pool Spaces" ) ) ;
       
       _labelPanel = createLabelPanel() ;
       Dimension d = _labelPanel.getPreferredSize() ;

       _pie = new JPieChart();
       _pie.setPreferredSize( new Dimension( d.height , d.height ) ) ;
       
       _pieModel = (DefaultPieChartModel)_pie.getModel() ;
       for( int i = 1 ; i < _spaceItems.length ; i++ )_pieModel.addElement(_spaceItems[i]);

       GridBagConstraints c = new GridBagConstraints()  ;
       c.insets     = new Insets(4,4,4,4) ;
       c.weightx = 1.0 ;
       c.weighty = 1.0 ;

       
       c.fill  = GridBagConstraints.NONE;
       c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = 0 ;
       c.weightx = 0.0 ;
       
       add( _pie , c ) ;
       
       c.fill  = GridBagConstraints.HORIZONTAL;
       c.gridwidth  = 1 ; c.gridx = 1 ; c.gridy = 0 ;
       c.weightx = 1.0 ;   
          
       add( _labelPanel , c ) ;

   }
   private JPanel createLabelPanel(){
   
       GridBagLayout     lo = new GridBagLayout() ;
       GridBagConstraints c = new GridBagConstraints()  ;
       JPanel    labelPanel = new JPanel( lo ) ;
       labelPanel.setOpaque(false);
       
       c.insets     = new Insets(4,4,4,4) ;
       c.fill       = GridBagConstraints.BOTH;
       c.weightx    = 1.0 ;
       c.weighty    = 1.0 ;
       c.gridheight = 1 ;
       
       for( int i = 0 ; i < _spaceItems.length ; i++ ){
       
          c.gridwidth  = 1 ; c.gridx = 0 ; c.gridy = i ; c.weightx = 0.1 ;
          labelPanel.add( _spaceItems[i]._title , c ) ;
          
          c.gridwidth  = 1 ; c.gridx = 1 ; c.gridy = i ; c.weightx = 0.9 ;
          labelPanel.add( _spaceItems[i]._value  , c ) ;

       }
       return labelPanel ;
   }
   private class OurIcon implements Icon {
      private int   _dim   = 10 ;
      private Color _color = null ;
      private OurIcon( Color color ){ _color = color ; }
      public void paintIcon( Component c , Graphics g , int x , int y ){
         Dimension size = c.getSize() ;
         g.setColor( _color ) ;
         int dim = size.height -6 ;
         g.fillRect( 2 , 2 , size.height-4 , size.height-4 ) ;
      }
      public int getIconWidth(){ return _dim ; }
      public int getIconHeight(){ return _dim ; }
   }
   public void setSpaces( long total , long precious , long free , long removable ){
       _spaceItems[0].setLongValue( total ) ;
       _spaceItems[1].setLongValue( free ) ;
       _spaceItems[2].setLongValue( removable ) ;  // removable
       _spaceItems[3].setLongValue( total-free-removable-precious ) ;  // pinned
       _spaceItems[4].setLongValue( precious ) ;
       
       _pieModel.fireContentsChanged( _pie );
   
   }
   public PieChartModel getModel(){ return _pieModel ; }
   public void setSpaces( PoolCostInfo.PoolSpaceInfo info ){
       long total     = info.getTotalSpace() ;
       long precious  = info.getPreciousSpace() ;
       long free      = info.getFreeSpace() ;
       long removable = info.getRemovableSpace() ;
       setSpaces( total , precious , free , removable );
   }
       private String longToByteString( long value ){
          String b = ""+value ;
          StringBuffer sb = new StringBuffer() ;
          int count = 0 ;
          for( int i = b.length()  - 1 ; i >= 0 ; i-- , count++){
             char c = b.charAt(i) ;
             if( ( count > 0 ) && ( count % 3 ) == 0 )sb.append('.');
             sb.append( c ) ;
             
          }
          return sb.reverse().toString();
       }


}
