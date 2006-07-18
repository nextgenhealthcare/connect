/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package samples.ubl.report.facade;

import org.oasis.ubl.commonaggregatecomponents.OrderLineType;
import org.oasis.ubl.commonaggregatecomponents.BasePriceType;

/**
 * The <code>OrderLineTypeFacade</code> class provides a set of read-only
 * methods for accessing data in a UBL order line.
 *
 * @author Sun Microsystems, Inc.
 * @version 1.0
 */
public class OrderLineTypeFacade {
    private OrderLineType lineItem;
  
    /**
     * Creates a new <code>OrderLineTypeFacade</code> instance.
     *
     * @param olt an <code>OrderLineType</code> value
     */
    public OrderLineTypeFacade(OrderLineType olt) {
        lineItem = olt;
    }

    /**
     * Returns the part number associated with a line item.
     *
     * @return a <code>String</code> representing the part number for this line
     * item
     */
    public String getItemPartNumber() {
        String num = "";
        try {
            num = lineItem.getLineItem().getItem().getSellersItemIdentification().getID().getValue();
        } catch (NullPointerException npe) {
        }
        return num;
    }

    /**
     * Returns the description associated with a line item.
     *
     * @return a <code>String</code> representing the description of this line item
     */
    public String getItemDescription() {
        String descr = "";
        try {
            descr = lineItem.getLineItem().getItem().getDescription().getValue();
        } catch (NullPointerException npe){
        }
        return descr;
    }

    /**
     * Returns the price associated with a line item.
     *
     * @return a <code>double</code> representing the price of this line item
     */
    public double getItemPrice() {
        double price = 0.0;
        try {
            price = getTheItemPrice().getPriceAmount().getValue().doubleValue();
        } catch (NullPointerException npe){
        }
        return price;
    }

    /**
     * Returns the quantity associated with a line item.
     *
     * @return an <code>int</code> representing the quantity of this line item
     */
    public int getItemQuantity() {
        int quantity = 0;
        try {
            quantity = lineItem.getLineItem().getQuantity().getValue().intValue();
        } catch (NullPointerException npe){
        }
        return quantity;
    }

    /**
     * Returns the <code>BasePriceType</code> associated with a line item
     *
     * @return a <code>BasePriceType</code> representing the price of this item
     */
    private BasePriceType getTheItemPrice() {
        return (BasePriceType) lineItem.getLineItem().getItem().getBasePrice().get(0);
    }
 
    static public class Iterator implements java.util.Iterator {
        java.util.Iterator iter;
        
        /** List of OrderLineType */
        public Iterator(java.util.List lst) {
            iter = lst.iterator();
        }
       
        public Object next() {
            return new OrderLineTypeFacade((OrderLineType)iter.next());
        }

        public boolean hasNext() {
            return iter.hasNext();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
