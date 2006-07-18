/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package samples.ubl.report;

import samples.ubl.report.facade.OrderFacade;
import samples.ubl.report.facade.OrderLineTypeFacade;
import samples.ubl.report.facade.AddressFacade;

import java.io.FileInputStream;
import java.io.IOException;

import java.text.NumberFormat;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.oasis.ubl.order.Order;

/*
 * $Id: PrintOrder.java,v 1.1 2004/06/25 21:12:40 kohsuke Exp $
 *
 * Copyright 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
 
/**
 * Unmarshals a UBL order instance and prints some of its data as
 * text to the standard output.
 *
 * @author <a href="mailto:Ed.Mooney@Sun.COM">Ed Mooney</a>
 * @version 1.0
 */
public class PrintOrder {

    /**
     * Unmarshals <code>xml/OfficeSupplyOrderInstance.xml</code>,
     * computes subtotals for each line item, and prints results to the
     * standard output.
     *
     * @param args Ignored.
     */
    public static void main(String[] args) {
        try {
            JAXBContext jc =
                JAXBContext.newInstance("org.oasis.ubl.order:"
                                        + "org.oasis.ubl.commonaggregatecomponents");
            Unmarshaller u = jc.createUnmarshaller();
            Order order =
                (Order) u.unmarshal(new
                                    FileInputStream("cd-UBL-1.0/xml/office/"
                                                    + "UBL-Order-1.0-Office-Example.xml"));

            OrderFacade of = new OrderFacade(order);

            printLetterHead(of);
            printDate(of);
            printBuyer(of);
            printLineItems(of);
        } catch (JAXBException e) {
            e.printStackTrace(System.out);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } // end of try-catch
    }

    /**
     * Prints information about the Seller.
     *
     * @param order a UBL <code>Order</code>
     */
    private static void printLetterHead(OrderFacade order) {
        AddressFacade addr = order.getSellerAddress();
        System.out.println("         "
                           + order.getSellerName()
                           + "\n         "
                           + addr.getStreet()
                           + "\n         "
                           + addr.getCity()
                           + ", "
                           + addr.getState()
                           + "  "
                           + addr.getZip());
    }

    /**
     * Prints the issue date in <code>java.text.DateFormat.LONG</code> format.
     *
     * @param order a UBL <code>Order</code>
     */
    private static void printDate(OrderFacade order) {
        System.out.println("\nDate: "
                           + order.getLongDate());
    }

    /**
     * Prints information about the Buyer.
     *
     * @param order a UBL <code>Order</code>
     */
    private static void printBuyer(OrderFacade order) {
        AddressFacade addr = order.getBuyerAddress();
        System.out.println("\nSold To: "
                           + order.getBuyerContact()
                           + "\n         c/o "
                           + order.getBuyerName()
                           + "\n         "
                           + addr.getStreet()
                           + "\n         "
                           + addr.getCity()
                           + ", "
                           + addr.getState()
                           + "  "
                           + addr.getZip());
    }

    /**
     * Prints information about line items in this order, including extension
     * based on quantity and base price and a total of all extensions.
     *
     * @param order a UBL <code>Order</code>
     */
    private static void printLineItems(OrderFacade order) {
        double total = 0;
        NumberFormat form = NumberFormat.getCurrencyInstance();

        java.util.Iterator iter = order.getLineItemIter();
        for (int i = 0; iter.hasNext(); i++) {
            OrderLineTypeFacade lineItem = (OrderLineTypeFacade) iter.next();

            // Compute subtotal and total
            double price = lineItem.getItemPrice();
            int qty = lineItem.getItemQuantity();
            double subtotal = qty * price;
            total += subtotal;

            System.out.println("\n"
                               + (i + 1)
                               + ". Part No.: "
                               + lineItem.getItemPartNumber()
                               + "\n   Description: "
                               + lineItem.getItemDescription()
                               + "\n   Price: "
                               + form.format(price)
                               + "\n   Qty.: "
                               + qty
                               + "\n   Subtotal: "
                               + form.format(subtotal));
        }
        System.out.println("\nTotal: " + form.format(total));
    }
}
