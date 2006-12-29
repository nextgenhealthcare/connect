package com.truemesh.squiggle.output;

/**
 * Utility to quickly grab the complete String from an object that is Outputtable
 *
 * @author <a href="mailto:joe@truemesh.com">Joe Walnes</a>
 */
public class ToStringer {

    public static String toString(Outputable outputable) {
        Output out = new Output("    ");
        outputable.write(out);
        return out.toString();
    }

}
