// $Id: SectionPanelGroup.java,v 1.1 2008/08/04 19:02:57 cvs Exp $
//
package org.dcache.gui.pluggins.costs ;
//

import diskCacheV111.pools.PoolCostInfo;
import org.pcells.services.gui.util.histogram.SimpleNamedBarHistogram;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


public class SectionPanelGroup extends JTabbedPane {
   
   
      
   private Map<String,PoolCostInfo>  _costMap = new HashMap<String,PoolCostInfo>() ;

   public void setCostDetails( boolean details ){
      int n = getTabCount() ;
      for( int i = 0 ; i < n ; i++ ){
	 ((SectionPanel)getComponentAt(i)).setCostDetails(details);
      }
   }
   public void setCostByMover( boolean details ){
      int n = getTabCount() ;
      for( int i = 0 ; i < n ; i++ ){
	 ((SectionPanel)getComponentAt(i)).setCostByMover(details);
      }
   }
   public void removeAll(){
   
      for( int i = 0 , n = getTabCount() ; i < n ; i++ ){
	 ((SectionPanel)getComponentAt(i)).cleanAll();
      }
   
      super.removeAll() ;
   }
   private void updateCost( Map<String,PoolCostInfo> costMap ){
   
      for( int i = 0 , n = getTabCount() ; i < n ; i++ ){
	 ((SectionPanel)getComponentAt(i)).updateCosts( costMap );
      }

   }
   public void allTabsDone(){
      for( int i = 0 , n = getTabCount() ; i < n ; i++ ){
	 ((SectionPanel)getComponentAt(i)).ready();
      }
   }
   public void setCostMap( Map<String,PoolCostInfo> map ){
      _costMap = map ;
      updateCost(_costMap) ;
   }
   public void addTab( String sectionName , Collection<String> poolList ){
      super.addTab( sectionName , new SectionPanel( sectionName , poolList ) );
   }
   private class SectionPanel 
           extends JPanel 
	   implements ChangeListener {

      
      private String    _name     = null ;

      private LabelSlider _p2pSlider   = new LabelSlider("p2p"  ,"2.0");
      private LabelSlider _scaleSlider = new LabelSlider("Scale","1.0");
      
      private SimpleNamedBarHistogram _drawBoard   = new SimpleNamedBarHistogram() ;
      private PoolCostDataModel       _dataModel   = null ;
      
      
      
      public SectionPanel( String name , Collection<String> poolList ){

	_name     = name ;

	setLayout( new BorderLayout(10,10) ) ;

	JPanel rightSliders = new JPanel( new GridLayout(1,0) ) ;
	rightSliders.add( _scaleSlider ) ;
	// TERMP rightSliders.add( _p2pSlider ) ;
        _p2pSlider.setEnabled(false);
	
	_scaleSlider.addChangeListener(this);
	_p2pSlider.addChangeListener(this);
	
	_drawBoard.setModel( _dataModel   = new PoolCostDataModel( poolList ) ) ;
	
	add( rightSliders , "East" ) ;
	add( _drawBoard   , "Center" ) ;
	

      }
      private void ready(){
         _dataModel.fireStructureChanged() ;
      }
      public void cleanAll(){
      }
      public void setCostDetails( boolean details ){
         _dataModel.setCostDetails(details) ;
      }
      public void setCostByMover( boolean details ){
         _dataModel.setCostByMover(details);
      }
      private void updateCosts( Map<String,PoolCostInfo> costMap ){
          _dataModel.updateCosts( costMap ) ;
      }
      public void redraw(){
         repaint();
      }
      public String getSectionName(){ return _name ; }
      
      public void stateChanged( ChangeEvent event ){
      
	 Object source = event.getSource() ;
	 if( source == _p2pSlider.getSlider() ){
	    //_drawBoard.setP2p( (float)2.0 * (float)_p2pSlider.getValue()/(float)1000 ) ;
	 }else if( source == _scaleSlider.getSlider() ){
	    _dataModel.setScale( (float)Math.max(_scaleSlider.getValue(),1)/(float)1000 ) ;
	 }

      }

   }
   private class LabelSlider extends JPanel {

      private JSlider _slider = new JSlider(JSlider.VERTICAL);

      private LabelSlider( String title , String topLabel ){

	 setLayout( new BorderLayout(4,4) ) ;

	_slider.setMinimum(0) ;
	_slider.setMaximum(1000) ;
	_slider.setValue(1000);
	_slider.setPaintTicks(true);
	_slider.setMajorTickSpacing(100);

	Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
	labelTable.put( new Integer( 0 )   , new JLabel("0.0") );
	labelTable.put( new Integer( 1000 ), new JLabel(topLabel) );

	_slider.setLabelTable( labelTable );
	_slider.setPaintLabels(true);

	add( _slider , "Center" ) ;
	add( new JLabel( title , JLabel.CENTER ) , "North" ) ;

      }
      public void setEnable( boolean mode ){ _slider.setEnabled(mode) ; }
      public void addChangeListener( ChangeListener changeListener ){
	 _slider.addChangeListener(changeListener) ;
      }
      public JSlider getSlider(){ return _slider ; }
      public int getValue(){ return _slider.getValue() ; }
   }
   
}

