/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package samples.ubl.report.facade;

import java.text.DateFormat;

import java.util.Calendar;
import java.util.Iterator;

import org.oasis.ubl.commonaggregatecomponents.BuyerPartyType;
import org.oasis.ubl.commonaggregatecomponents.PartyNameType;
import org.oasis.ubl.commonaggregatecomponents.SellerPartyType;
import org.oasis.ubl.commonbasiccomponents.Name;

import org.oasis.ubl.order.Order;

/**
 * The <code>OrderFacade</code> class provides a set of read-only methhods for
 * accessing data in a UBL order.
 *
 * @author Sun Microsystems, Inc.
 * @version 1.0
 */
public class OrderFacade {

    Order order = null;

    /**
     * Creates a new <code>OrderFacade</code> instance.
     *
     * @param order an <code>Order</code> value
     */
    public OrderFacade(Order order) {
        this.order = order;
    }

    /**
     * Returns a <code>String</code> representing the name of a person familiar
     * with this order.
     *
     * @return a <code>String</code> value representing the name of a person
     * familiar with <code>Order</code>
     */
    public String getBuyerContact() {
        BuyerPartyType party = order.getBuyerParty();
        return ((Name) party.getParty().getPartyName().getName().get(0)).getValue();
    }

    /**
     * Returns a <code>String</code> representing the name of the entity placing
     * this order.
     *
     * @return a <code>String</code> value representing the name of the entity
     * placing this order
     */
    public String getBuyerName() {
        BuyerPartyType party = order.getBuyerParty();
        return ((Name) party.getParty().getPartyName().getName().get(0)).getValue();
     }

    /**
     * Returns the first <code>AddressFacade</code> in list order contained by
     * the <code>BuyerPartyType</code> representing the entity placing this
     * order.
     *
     * @return an <code>AddressFacade</code> value representing the address of
     * the entity placing this order
     */
    public AddressFacade getBuyerAddress() {
        return new AddressFacade(order.getBuyerParty().getParty().getAddress());
    }


    /**
     * Returns a <code>String</code> representing the name of the entity
     * fulfilling this order.
     *
     * @return a <code>String</code> value representing the name of the entity
     * fulfilling this order
     */
    public String getSellerName() {
        SellerPartyType party = order.getSellerParty();
        return ((Name) party.getParty().getPartyName().getName().get(0)).getValue();
    }


    /**
     * Returns the first <code>PartyNameType</code> in list order contained by
     * the <code>SellerPartyType</code> representing the entity fulfilling this
     * order.
     *
     * @param seller a <code>SellerPartyType</code> representing the entity
     * fulfilling this order
     * @return a <code>PartyNameType</code> value representing the name of the
     * entity fulfilling this order
     */
    private PartyNameType getSellerParty(SellerPartyType seller) {
        return seller.getParty().getPartyName();
    }

    /**
     * Returns the first <code>AddressFacade</code> in list order contained by
     * the <code>SellerPartyType</code> representing the entity fulfilling this
     * order.
     *
     * @return an <code>AddressFacade</code> value representing the address of the
     * entity fulfilling this order
     */
    public AddressFacade getSellerAddress() {
        return new AddressFacade(order.getSellerParty().getParty().getAddress());
    }

    /**
     * Returns an UBL <code>Order</code> issue date in the <code>LONG</code>
     * format as defined by <code>java.text.DateFormat</code>.
     *
     * @return a <code>String</code> value representing the issue date of this
     * UBL <code>Order</code>
     */
    public String getLongDate() {
        DateFormat form = DateFormat.getDateInstance(DateFormat.LONG);
        Calendar cal = getCalendar();
        form.setTimeZone(cal.getTimeZone());
        return form.format(cal.getTime());
    }

    /**
     * Returns a <code>Calendar</code> representing the issue date of this UBL
     * <code>Order</code>.
     *
     * @return a <code>Calendar</code> representing the issue date of this UBL order
     */
    private Calendar getCalendar() {
        Calendar date = null;
        return order.getIssueDate() != null
            ? order.getIssueDate().getValue()
            : date;
    }

    /**
     * Returns an iterator over orders line items.
     *
     * @return an Iterator over OrderLineTypeFacade.
     */
    public Iterator getLineItemIter() {
        return new OrderLineTypeFacade.Iterator(order.getOrderLine());
    }
}
