// $Id: JCostPanel.java,v 1.4 2006/11/12 16:38:41 cvs Exp $
//
package org.dcache.gui.pluggins;
//
import diskCacheV111.poolManager.PoolManagerCellInfo;
import org.pcells.services.connection.DomainConnection;
import org.pcells.services.connection.DomainConnectionListener;
import org.pcells.services.connection.DomainEventListener;
import org.pcells.services.gui.CellBorder;
import org.pcells.services.gui.CellGuiSkinHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

public class      JCostPanel
        extends    CellGuiSkinHelper.CellPanel
        implements DomainConnectionListener,
        DomainEventListener{

    private Logger _logger;

    private DomainConnection _connection = null ;

    private JPoolGroupList _groupList   = null ;
    private JCostDisplay _costDisplay = new JCostDisplay() ;
    private Controller       _controller  = new Controller() ;

    public JCostPanel( DomainConnection connection ) {

        _logger = LoggerFactory.getLogger(JCostPanel.class);

        _connection = connection;
        BorderLayout l = new BorderLayout();
        l.setVgap(10);
        l.setHgap(10);
        setLayout(l);

        setBorder(new CellBorder("Cost Report", 35));

        _groupList = new JPoolGroupList(connection);
        _groupList.addActionListener(_controller);
        _groupList.setProgressBar(_controller._progressBar);
        _costDisplay.setSorting(JCostDisplay.CPU);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                _groupList,
                _costDisplay);

        CellGuiSkinHelper.setComponentProperties(split);

        add(split, "Center");
        add(_controller, "South");

        split.resetToPreferredSizes();

        connection.addDomainEventListener(this);
    }
    private class Controller
            extends CellGuiSkinHelper.CellPanel
            implements ActionListener ,
            DomainConnectionListener{

        private JButton _scanButton = new JButton("Scan Costs");
        private JLabel  _cpuLabel   = new JLabel( "Cpu"   , JLabel.CENTER ) ;
        private JLabel  _spaceLabel = new JLabel( "Space" , JLabel.CENTER ) ;
        private JTextField   _cpuText    = new JTextField("1.0000");
        private JTextField   _spaceText  = new JTextField("1.0000");
        private JProgressBar _progressBar = new JProgressBar() ;
        private JLabel       _messageLabel = new JLabel();
        private JComboBox    _sortingBox   = null ;

        private Controller(){

            Object [] items = { "Cpu" , "Space" , "Merge" } ;
            _sortingBox  = new JComboBox(items) ;
            _sortingBox.setSelectedItem("Cpu");

            _scanButton.addActionListener(this) ;
            _sortingBox.addActionListener(this);
            _cpuText.addActionListener(this);
            _spaceText.addActionListener(this);
            _scanButton.setOpaque(false);

            GridLayout gl = new GridLayout(0,1) ;
            gl.setVgap(10) ;
            gl.setHgap(10);
            setLayout(gl);


            BorderLayout bl = new BorderLayout();
            bl.setVgap(10) ;
            bl.setHgap(10);

            JPanel top = new CellGuiSkinHelper.CellPanel(bl);

            top.add( _scanButton , "West" );
            top.add( _progressBar , "Center" ) ;
            top.add( _sortingBox , "East"   ) ;

            add( top ) ;


            bl = new BorderLayout();
            bl.setVgap(10) ;
            bl.setHgap(10);
            JPanel bottom = new CellGuiSkinHelper.CellPanel(bl);

            bottom.add( _messageLabel , "Center" ) ;

            gl = new GridLayout(1,0);
            gl.setVgap(10) ;
            gl.setHgap(10);

            JPanel bottomWest = new CellGuiSkinHelper.CellPanel(gl);
            bottomWest.add( _cpuLabel ) ;
            bottomWest.add( _cpuText  ) ;
            bottomWest.add( _spaceLabel ) ;
            bottomWest.add( _spaceText ) ;

            bottom.add( bottomWest , "West" ) ;

            add( bottom ) ;
        }
        public void actionPerformed( ActionEvent event ){
            Object source = event.getSource() ;
//	 _logger.debug("action : "+source.getClass().getName());
            if( source == _scanButton ){
                _scanButton.setEnabled(false);
                runCostScan() ;
            }else if( source == _groupList ){
                displayCost() ;
            }else if( source instanceof JTextField ){
                JTextField tf = (JTextField)source ;
                String value = tf.getText() ;
                _logger.debug("Text found : "+value ) ;
                try{
                    Double.parseDouble(value) ;
                    switchToMerged();
                }catch(Exception ee ){
                    tf.setText("1.0000");
                }
            }else if( source == _sortingBox ){
                String sort = _sortingBox.getSelectedItem().toString() ;
                if( sort.equals( "Cpu" ) )
                    _costDisplay.setSorting(JCostDisplay.CPU) ;
                else if( sort.equals("Space") )
                    _costDisplay.setSorting(JCostDisplay.SPACE ) ;
                else if( sort.equals("Merge") ){
                    switchToMerged();
                }
            }
        }
        private void switchToMerged(){
            try{
                double cpu   = Double.parseDouble(_cpuText.getText());
                double space = Double.parseDouble(_spaceText.getText());
                _costDisplay.setMerge( space , cpu ) ;
                _sortingBox.setSelectedItem("Merge");
            }catch(Exception ee ){
                _logger.error("Problem parsing cpu/space");
            }
        }
        private void askForPoolDecision(){
            try{
                _connection.sendObject( "PoolManager" ,
                        "set pool decision" ,
                        this ,
                        4 );
            }catch(Exception ee ){
                _logger.error("Exception send "+ee ) ;
            }

        }
        public void domainAnswerArrived( Object obj , int subid ){
            _logger.debug( "Answer ("+subid+") : "+obj.toString() ) ;
            if( ! ( obj instanceof String ) )return ;
            try{
                StringTokenizer st = new StringTokenizer(obj.toString(),";") ;
                while( st.hasMoreTokens() ){
                    String assign = st.nextToken() ;
                    StringTokenizer st2 = new StringTokenizer(assign,"=") ;
                    String key = st2.nextToken() ;
                    String value = st2.nextToken() ;
                    if( key.equals("scf") ){
                        Double.parseDouble(value) ;
                        _spaceText.setText(value) ;
                    }else if( key.equals("ccf") ){
                        Double.parseDouble(value) ;
                        _cpuText.setText(value) ;
                    }
                }
            }catch(Exception ee ){
                _logger.error("Excpetion in interpreting cost string : "+ee);
            }
        }
        private void runCostScan(){
            new Thread( new ScannerThread() ,"Scanner").start() ;
        }
        public void runCostDone(){
            _scanButton.setEnabled(true) ;
        }

    }
    private class ScannerThread
            implements Runnable,
            DomainConnectionListener {

        private ArrayList _costList = new ArrayList() ;
        private Object    _ioLock   = new Object() ;
        private int       _state    = 0 ;
        private PoolManagerCellInfo _poolInfo = null ;
        private int       _outStanding = 0 ;
        private String    _ok = null ;

        public void domainAnswerArrived( Object obj , int subid ){
            synchronized( _ioLock ){
                if( obj instanceof PoolManagerCellInfo ){
                    _poolInfo = (PoolManagerCellInfo)obj ;
                    _state++ ;
                }else if( obj instanceof Object [] ){
                    Object [] x = (Object [])obj ;
                    if( ( x.length > 3 ) && ( x[2] != null ) && ( x[3] != null ) ){
                        _costList.add( obj ) ;
                    }
                    _outStanding-- ;
                    _controller._progressBar.setValue(_costList.size());
                }else{
                    if( _state == 1 ){
                        _ok = "Unexpected Message arrived while waiting for PoolManagerInfo "+
                                obj.getClass().getName() ;
                        _logger.debug(_ok);
                    }else{
                        _logger.error("Unexpected Message arrived "+obj.getClass().getName()) ;
                        _outStanding-- ;
                    }
                }
                _ioLock.notifyAll() ;
            }
        }
        public void run(){

            try{
                String [] poolList = null ;
                synchronized( _ioLock ){
                    _state = 1 ;
                    _connection.sendObject( "PoolManager" ,
                            "xgetcellinfo" ,
                            this ,
                            4 ) ;
                    _ioLock.wait(10000L);
                    if( _poolInfo == null )
                        throw new
                                Exception("Problem with cellinfo from PoolManager "+
                                ( _ok != null ? _ok : "" ) );


                }
                poolList = (String[]) _poolInfo.getPoolCells().toArray();
                if( poolList == null )
                    throw new
                            Exception("Got empty pool list");

                _controller._progressBar.setMinimum(0) ;
                _controller._progressBar.setMaximum(poolList.length);
                _controller._progressBar.setValue(0);
                for( int i = 0 ; i < poolList.length ; i++ ){
                    synchronized( _ioLock ){
                        _logger.debug("Going to send to "+poolList[i]);
                        _connection.sendObject( "PoolManager" ,
                                "xcm ls "+poolList[i] ,
                                this ,
                                4 ) ;
                        _outStanding++ ;
                        _logger.debug("Send ok to "+poolList[i]);
                    }
                    //Thread.currentThread().sleep(100);
                }
                synchronized( _ioLock ){
                    long waitUntil = System.currentTimeMillis() + 20000L ;
                    while( ( _outStanding > 0 ) &&
                            ( System.currentTimeMillis() < waitUntil ) )
                        _ioLock.wait(waitUntil-System.currentTimeMillis()) ;


                }
	    /*
            _displayArea.setText("");
            Iterator i = _costList.iterator() ;
            while( i.hasNext() ){
               Object [] array = (Object [] )i.next() ;
               StringBuffer sb = new StringBuffer() ;
               for( int j = 0 ; j < array.length ; j++ )
                   sb.append(array[j].toString()).append("  ") ;
               append(sb.toString()+"\n");
            }
	    */
                _currentCostList = _costList ;
                displayCost();
            }catch(Exception ee ){
                _logger.error("Problem in scan : "+ee ) ;
                ee.printStackTrace() ;

            }finally{
                _controller.runCostDone() ;
            }
        }
    }
    private java.util.List _currentCostList = null ;
    private void displayCost(){

        if( _currentCostList == null )return ;

        Set poolSet = _groupList.getPoolSet() ;

        if( ( poolSet == null ) || ( poolSet.size() == 0  ) ){
            _costDisplay.setList( _currentCostList ) ;
            return ;
        }

        Iterator i = _currentCostList.iterator() ;
        java.util.List list = new ArrayList() ;
        while( i.hasNext() ){
            Object [] o = (Object [])i.next() ;
            if( poolSet.contains(o[0].toString()) )
                list.add(o) ;
        }
      /*
      i = list.iterator() ;
      while(i.hasNext()){
         Object [] o = (Object [])i.next() ;
         System.out.print( "Select : ");
	 for( int x = 0 ; x < o.length ; x++ )
         System.out.print( " "+o[x]);
	 _logger.debug("");
      }
      */
        _costDisplay.setList( list ) ;
    }
    private class WestPanel extends CellGuiSkinHelper.CellPanel {
        private WestPanel(){
            BorderLayout l = new BorderLayout() ;
            l.setVgap(10) ;
            l.setHgap(10);
            setLayout(l) ;
        }
    }
    public void connectionOpened( DomainConnection connection ){
        _logger.debug("Connection opened");
        _controller.askForPoolDecision() ;
    }
    public void connectionClosed( DomainConnection connection ){
        _logger.debug("Connection closed" ) ;
    }
    public void connectionOutOfBand( DomainConnection connection, Object obj ){
        _logger.debug("Connection connectionOutOfBand "+obj ) ;
    }
    public void domainAnswerArrived( Object obj , int subid ){
        _logger.debug( "Answer ("+subid+") : "+obj.toString() ) ;
    }


}
