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


public class LinearSpacePanel extends JPanel implements ListDataListener {

    private static Logger _logger;

    private JLabel [] _label = new JLabel[5] ;
    private ListModel _model = null ;
    private SrmLinearSpaceComponent _drawing = null ;

    private class OurIcon implements Icon {
        private int   _dim   = 18 ;
        private Color _color = null ;
        private Color _color2 = null ;
        private OurIcon( Color color ){ _color = color ; }
        private OurIcon( Color color1 , Color color2 ){
            _color  = color1 ;
            _color2 = color2 ;
        }
        public void paintIcon( Component c , Graphics g , int x , int y ){
            Dimension size = c.getSize() ;
            g.setColor( _color ) ;
            //int dim = size.height ;

            g.fillRect( 2 , 2 , size.height-4 , size.height-4 ) ;

            if( _color2 != null ){
                g.setColor( _color2 ) ;
                g.fillRect( 4 , 4 , size.height-8 , size.height-8 ) ;
            }
        }
        public int getIconWidth(){ return _dim ; }
        public int getIconHeight(){ return _dim ; }
    }
    private void updateValues(){
        _label[0].setText(_str_total+"="+longToByteString((Long)_model.getElementAt(0))) ;
        _label[1].setText(_str_used_in+"="+longToByteString((Long)_model.getElementAt(1)));
        _label[2].setText(_str_free_in+"="+longToByteString((Long)_model.getElementAt(2)));
        _label[3].setText(_str_used_out+"="+longToByteString((Long)_model.getElementAt(3)));
        _label[4].setText(_str_free_out+"="+longToByteString((Long)_model.getElementAt(4)));
        repaint();
    }
    public void setModel( ListModel model ){
        if( _model != null )_model.removeListDataListener( this ) ;
        _model = model ;
        if( _model != null )_model.addListDataListener( this ) ;
    }
    public void contentsChanged( ListDataEvent event ){
        _logger.debug("Content Changed in LinearSpacePanel : "+event ) ;
        updateValues();
        repaint() ;
    }
    public void intervalAdded( ListDataEvent event ){
    }
    public void intervalRemoved( ListDataEvent event ){
    }
    private final static String _str_total    = "Total" ;
    private final static String _str_used_in  = "Used in token(s)" ;
    private final static String _str_free_in  = "Free in token(s)" ;
    private final static String _str_used_out = "Used else" ;
    private final static String _str_free_out = "Free else" ;

    private void setDefaultAgenda(){
        _label[0].setText(_str_total) ;
        _label[1].setText(_str_used_in);
        _label[2].setText(_str_free_in);
        _label[3].setText(_str_used_out);
        _label[4].setText(_str_free_out);

    }
    public LinearSpacePanel() {

        _logger = LoggerFactory.getLogger(LinearSpacePanel.class);

        setLayout(new BorderLayout(10, 10));

//      setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder( Color.blue , 1 ) , "Pool Spaces" ) ) ;
        setBorder(BorderFactory.createEtchedBorder());

        _label[0] = new JLabel(new OurIcon(Color.green), JLabel.LEFT);
        _label[1] = new JLabel(new OurIcon(Color.blue, Color.red), JLabel.LEFT);
        _label[2] = new JLabel(new OurIcon(Color.blue, Color.yellow), JLabel.LEFT);
        _label[3] = new JLabel(new OurIcon(Color.green, Color.red), JLabel.LEFT);
        _label[4] = new JLabel(new OurIcon(Color.green, Color.yellow), JLabel.LEFT);

        setDefaultAgenda();

        _model = new LinearSpaceListModel();
        _model.addListDataListener(this);
        //setModel(_model) ;

        _drawing = new SrmLinearSpaceComponent();
        _drawing.setModel(_model);


        //JPanel right = new JPanel( new GridLayout(1,0) ) ;
        JPanel right = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        for (int i = 0; i < 5; i++) right.add(_label[i]);

        add(right, "South");
        add(_drawing, "Center");


    }
    public void setSpaces(long total ,
                          long tokenUsed , long tokenFree ,
                          long outsideUsed , long outsideFree ){

        ((LinearSpaceListModel)_model).setSpaces(total,tokenUsed,tokenFree,outsideUsed,outsideFree);
        repaint();
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

    public static void main(String s[]) throws Exception  {

        _logger.debug("Starting ...");
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        };



        JFrame f = new JFrame("xxx");
        f.addWindowListener(l);

        final LinearSpacePanel spaces = new LinearSpacePanel() ;
        f.getContentPane().add(  spaces ,  "North" );
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
                                    spaces.setSpaces(1000L,100L,200L,300L,400L);
                                }
                            }
                    ) ;
                }
            }
        }
        ).start() ;

    }

}
