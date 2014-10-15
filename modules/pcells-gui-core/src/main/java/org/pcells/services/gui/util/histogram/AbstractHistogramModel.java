// $Id: AbstractHistogramModel.java,v 1.1 2008/08/04 18:38:59 cvs Exp $

package org.pcells.services.gui.util.histogram ;

import javax.swing.event.EventListenerList;

public abstract class AbstractHistogramModel
       implements BasicHistogramModel     {


   protected EventListenerList listenerList = new EventListenerList();

   public abstract Object getParameterAt( int i ) ;
   public abstract int getParameterCount() ;
   
   public abstract String   getNameAt( int i ) ;
   public abstract float [] getDataAt( int i ) ;
   public abstract int getDataCount() ;

    /**
     * Adds a listener to the list that's notified each time a change
     * to the data model occurs.
     *
     * @param l the <code>ListDataListener</code> to be added
     */
    public void addHistogramListener(HistogramListener l) {
        listenerList.add(HistogramListener.class, l);
    }


    /**
     * Removes a listener from the list that's notified each time a
     * change to the data model occurs.
     *
     * @param l the <code>ListDataListener</code> to be removed
     */
    public void removeHistogramListener(HistogramListener l) {
        listenerList.remove(HistogramListener.class, l);
    }

    /**
     * Returns an array of all the list data listeners
     * registered on this <code>AbstractListModel</code>.
     *
     * @return all of this model's <code>ListDataListener</code>s,
     *         or an empty array if no list data listeners
     *         are currently registered
     *
     * @see #addListDataListener
     * @see #removeListDataListener
     *
     * @since 1.4
     */
    public HistogramListener[] getHistogramListeners() {
        return (HistogramListener[])listenerList.getListeners(
                HistogramListener.class);
    }


    protected void fireContentsChanged(Object source)
    {
        Object[] listeners = listenerList.getListenerList();
        HistogramEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HistogramListener.class) {
                if (e == null) {
                    e = new HistogramEvent(source, HistogramEvent.CONTENTS_CHANGED);
                }
                ((HistogramListener)listeners[i+1]).histogramContentsChanged(e);
            }
        }
    }
    protected void fireParametersChanged(Object source)
    {
        Object[] listeners = listenerList.getListenerList();
        HistogramEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HistogramListener.class) {
                if (e == null) {
                    e = new HistogramEvent(source, HistogramEvent.PARAMETERS_CHANGED);
                }
                ((HistogramListener)listeners[i+1]).histogramParametersChanged(e);
            }
        }
    }
    protected void fireStructureChanged(Object source)
    {
        Object[] listeners = listenerList.getListenerList();
        HistogramEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HistogramListener.class) {
                if (e == null) {
                    e = new HistogramEvent(source, HistogramEvent.STRUCTURE_CHANGED);
                }
                ((HistogramListener)listeners[i+1]).histogramStructureChanged(e);
            }
        }
    }


}
