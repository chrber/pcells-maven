 package org.dcache.gui.pluggins.pools;

 import org.pcells.services.connection.DomainConnection;
 import org.pcells.services.connection.DomainConnectionListener;
 import org.pcells.services.gui.util.RowObjectTableModel;

 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableColumnModel;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.SimpleDateFormat;
 import java.util.Iterator;

 public class FlushQueueDetailsPanel extends
              JComponent implements
                     ListSelectionListener,
                     ActionListener ,
                     DomainConnectionListener  {

    private SimpleDateFormat   _formatter  = new SimpleDateFormat ("MM.dd HH:mm:ss");

    private JTable   _table          = new JTable() ;
    private JButton  _updateButton   = new JButton("Update");
    private String   _destination    = null ;

    private DomainConnection        _connection  = null ;
    private FlushQueueDetailsModel  _model       = new FlushQueueDetailsModel(  ) ;
    private PoolGenericInfo _poolInfoScanner = new PoolGenericInfo() ;
    private TitledBorder        _title           = null ;

    public class FlushQueueDetailsModel extends RowObjectTableModel {
        public FlushQueueDetailsModel(  ){
           super(new String[]{ "Name" ,
                               "Expires Defined" , "Expires Rest" ,
                               "Pending Defined" , "Pending Rest" ,
                               "Size Defined" , "Size Rest" ,
                               "Active Procs" ,
                               "Active Entries" , "Inactive Entries" ,
                               "Suspended"  });
        }
        public QueueInfo getQueueInfoAt( int pos ){
           return (QueueInfo)getRowAt(pos) ;
        }
    }
    public class QueueInfo implements RowObjectTableModel.SimpleTableRow {
        private String    _name   = null ;
        private Object [] _values = null ;
        private PoolGenericInfo.FlushClass _info = null ;
        private QueueInfo( PoolGenericInfo.FlushClass info ){
           _name = info.getName() ;
           _info = info ;
           init() ;
        }
        private void init(){
           _values = new Object[11] ;
           _values[0] = _name ;
           _values[1] = new Long( _info.getExpirationDefined() ) ;
           _values[2] = new Long( _info.getExpirationRest() ) ;
           _values[3] = new Long( _info.getPendingDefined() ) ;
           _values[4] = new Long( _info.getPendingRest() ) ;
           _values[5] = new Long( _info.getSizeDefined() ) ;
           _values[6] = new Long( _info.getSizeRest() ) ;
           _values[7] = new Integer( _info.getActiveProcesses() ) ;
           _values[8] = new Integer( _info.getActiveEntries() ) ;
           _values[9] = new Integer( _info.getInactiveEntries() ) ;
           _values[10] = new Boolean( _info.isSuspended() ) ;

        }
        public String getName(){ return _name ; }
        public PoolGenericInfo.FlushClass getFlushClass(){ return _info ; }
        public String toString(){ return _name +"="+_info ; }
        public Object getValueAtColumn( int column ){
            return column < _values.length ? _values[column] : null ;
        }
        public Component renderCell(Component component , Object value , boolean isSelected ,
                                    boolean isFocussed , int row , int column ){

           JLabel label = (JLabel)component ;

           boolean suspended = ((Boolean)_values[10]).booleanValue() ;
           boolean expired = ( _info.getExpirationRest() < 0                         ) ||
                             ( _info.getPendingRest()    > _info.getPendingDefined() ) ||
                             ( _info.getSizeRest()       > _info.getSizeDefined()    ) ;

           label.setHorizontalAlignment( JLabel.RIGHT ) ;
           if( suspended ){
              label.setForeground( Color.orange ) ;
           }else if( expired ){
              label.setForeground( Color.red ) ;
           }else{
              label.setForeground( Color.black ) ;
           }
           switch( column ){
              case 0 :
                 if( expired )label.setForeground( Color.red ) ;
              break ;
               case 1 :
                 label.setText( toTimeDifference((Long)value) ) ;
              break ;
              case 2 :
                 label.setText( toTimeDifference((Long)value) ) ;
              break ;
              case 10 :
                 Boolean b = (Boolean)value ;
              break ;
              case 7 :
              case 8 :
              case 9 :
              break ;
              default :
                  label.setText( longToByteString( (Long)value )  ) ;
           }
           return component ;
        }
        private String longToByteString( Long value ){
           String b = value.toString() ;
           StringBuffer sb = new StringBuffer() ;
           int count = 0 ;
           for( int i = b.length()  - 1 ; i >= 0 ; i-- ){
              char c = b.charAt(i) ;
              if( ( count > 0 ) && ( count % 3 ) > 0 )sb.append('.');
              sb.append( c ) ;
           }
           return sb.reverse().toString();
        }
        public String toTimeDifference( Long value ){
           long    x   = ((Long)value).longValue() ;
           boolean neg = x < 0L ;
           x = Math.abs(x)  ;
           String seconds = "" + ( x % 60 ) ;  //seconds
           if( seconds.length() == 1 )seconds = "0"+seconds;
           x = x / 60 ;
           String minutes = "" + ( x % 60 ) ;  //minutes
           if( minutes.length() == 1 )minutes = "0"+minutes;
           x = x / 60 ;
           String hours = "" + ( x % 24 ) ;  //hours
           x = x / 24 ;

           String days = ( x > 0 ) ? ( ""+x+" days" ) : "" ;

           return ( neg ? "- " : "" ) + days +" "+hours+":"+minutes+":"+seconds ;
        }

    }
    public FlushQueueDetailsPanel(  DomainConnection connection  ){

        _connection = connection ;

        setLayout( new BorderLayout() ) ;

        _title = BorderFactory.createTitledBorder(
                 BorderFactory.createLineBorder( Color.blue , 1 ) , "Queue Summary" ) ;

        setBorder( _title ) ;

        _table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION);
        _table.setModel( _model ) ;
        JTableHeader header = _table.getTableHeader() ;
        header.addMouseListener(_model);
        //header.setDefaultRenderer(_model.getRenderer());

        TableColumnModel columnModel = _table.getColumnModel() ;
        for( int i= 0 , n = columnModel.getColumnCount() ; i < n ; i++ ){
            columnModel.getColumn(i).setCellRenderer(_model.getRenderer());
        }


        ListSelectionModel selectionModel = _table.getSelectionModel() ;

        selectionModel.addListSelectionListener(this) ;
        JScrollPane scroll = new JScrollPane( _table ) ;

        scroll.setPreferredSize( new Dimension(0,10) ) ;
        add( scroll , "Center");

        JPanel controls = new JPanel( new FlowLayout(FlowLayout.LEFT,5,5) ) ;

        _updateButton.addActionListener( this ) ;
        _updateButton.setEnabled( true ) ;
        controls.add( _updateButton ) ;

     //   add( controls , "North" ) ;


     }
     public void setDestination( String destination ){
         _model.clear() ;
        _destination = destination ;

        /*
        if( _destination == null ){
           _title.setTitle("Queue Summary") ;
           _updateButton.setEnabled( false ) ;
        }else{
           _title.setTitle("Queue Summary <"+_destination+">"  ) ;
           _updateButton.setEnabled( true ) ;
        }
        */
     }
     public void actionPerformed( ActionEvent event ){

         if( _destination == null )return ;

          try{
             _connection.sendObject( _destination ,
                                     "queue ls -l queue" ,
                                     this ,
                                     10 );
          }catch(Exception ee ){
             System.out.println("Exception send "+ee ) ;
          }
     }
     public void domainAnswerArrived( Object obj , int subid ){

        System.out.println( "Answer ("+subid+") : "+obj.toString() ) ;

      //  DoLaterTransfer doLater = new DoLaterTransfer( obj , subid ) ;
      //  SwingUtilities.invokeLater( doLater ) ;
        if( subid == 10 ){
           java.util.List list = _poolInfoScanner.scanFlushInfo( obj.toString() ) ;
           System.out.println("Start list");
           for( Iterator nn = list.iterator() ; nn.hasNext() ;  ){
               PoolGenericInfo.FlushClass fc = (PoolGenericInfo.FlushClass)nn.next() ;
               System.out.println(fc.toString());
           }
           System.out.println("End list");
           setFlushClassList( list ) ;
        }
     }
     public QueueInfo getQueueInfoAt( int pos ){
         return ((FlushQueueDetailsModel)_model).getQueueInfoAt(pos) ;
     }
     public ListSelectionModel getSelectionModel(){ return _table.getSelectionModel() ; }
     public void valueChanged( ListSelectionEvent event ) {
        Object source = event.getSource() ;
     }
     private void addQueueInfo( QueueInfo info ){
        _model.add(info);
     }
     public void setFlushClassList( java.util.List flushClassList ){

         _model.clear() ;
         for( Iterator i = flushClassList.iterator() ;i.hasNext() ; ){
            addQueueInfo( new QueueInfo( (PoolGenericInfo.FlushClass)i.next()  ) ) ;
         }
         _model.fire() ;
     }
 }

