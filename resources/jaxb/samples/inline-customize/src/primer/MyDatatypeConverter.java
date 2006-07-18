/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package primer;

import java.math.BigInteger;
import javax.xml.bind.DatatypeConverter;

public class MyDatatypeConverter {

  public static short parseIntegerToShort(String value) {
	BigInteger result = DatatypeConverter.parseInteger(value);
	return (short)(result.intValue());
  }

  public static String printShortToInteger(short value) {
        BigInteger result = BigInteger.valueOf(value);
        return DatatypeConverter.printInteger(result);
  }

  public static int parseIntegerToInt(String value) {
	BigInteger result = DatatypeConverter.parseInteger(value);
	return result.intValue();
  }

  public static String printIntToInteger(int value) {
       BigInteger result = BigInteger.valueOf(value);
       return DatatypeConverter.printInteger(result);
  }
};
