package org.dcache.gui.pluggins.poolManager ;

import org.pcells.services.gui.util.DefaultPieChartModel;
import org.pcells.services.gui.util.JPieChart;
import org.pcells.services.gui.util.PieChartModel;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class SrmSpacePie extends JPanel  {

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
      new SpaceItem( "Total"     , Color.black ) ,  
      new SpaceItem( "Data in reservation"       , Color.blue    , 1000L ) ,
      new SpaceItem( "Reserved but not used"     , Color.yellow  , 1000L ) ,
      new SpaceItem( "Data not in reservation"   , Color.green , 1000L ) ,
      new SpaceItem( "Not used and not reserved" , Color.white , 1000L ) ,
   } ;
   private JPieChart _pie        = null ;
   private JPanel    _labelPanel = null ;
   private DefaultPieChartModel _pieModel = null ;
   
   public SrmSpacePie(){
   
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
   public void setSpaces( long total , 
                          long reservedAndUsed , 
			  long reservedNotUsed , 
			  long usedNotReserved ,
			  long free  ){
       _spaceItems[0].setLongValue( total ) ;
       _spaceItems[1].setLongValue( reservedAndUsed ) ;
       _spaceItems[2].setLongValue( reservedNotUsed ) ;  
       _spaceItems[3].setLongValue( usedNotReserved ) ;  
       _spaceItems[4].setLongValue( free ) ;  
       _pieModel.fireContentsChanged( _pie );
   
   }
   public PieChartModel getModel(){ return _pieModel ; }
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
