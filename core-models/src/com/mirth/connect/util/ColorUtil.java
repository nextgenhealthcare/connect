/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

// https://en.wikipedia.org/wiki/Web_colors#Color_table
public final class ColorUtil {

    private static Color Red = new Color(255, 0, 0);
    private static Color Maroon = new Color(128, 0, 0);
    private static Color Yellow = new Color(255, 255, 0);
    private static Color Olive = new Color(128, 128, 0);
    private static Color Lime = new Color(0, 255, 0);
    private static Color Green = new Color(0, 128, 0);
    private static Color Aqua = new Color(0, 255, 255);
    private static Color Teal = new Color(0, 128, 128);
    private static Color Blue = new Color(0, 0, 255);
    private static Color Navy = new Color(0, 0, 128);
    private static Color Fuchsia = new Color(255, 0, 255);
    private static Color Purple = new Color(128, 0, 128);

    private static Color[] colorPalette = new Color[] { Red, Maroon, Yellow, Olive, Lime, Green,
            Aqua, Teal, Blue, Navy, Fuchsia, Purple };

    private static int selection = 0;

    public static Color getNewColor() {
        return colorPalette[selection++ % colorPalette.length];
    }

    public static String convertToHex(Color color) {
        return color != null ? String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()) : "";
    }

    public static Color getForegroundColor(Color backgroundColor) {
        double luminance = 1 - ((0.299 * backgroundColor.getRed() + 0.587 * backgroundColor.getGreen() + 0.114 * backgroundColor.getBlue()) / 255.0);
        return luminance < 0.5 ? Color.BLACK : Color.WHITE;
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }

    public static BufferedImage tint(BufferedImage loadImg, Color backgroundColor, Color foregroundColor) {
        BufferedImage img = new BufferedImage(loadImg.getWidth(), loadImg.getHeight(), BufferedImage.TRANSLUCENT);
        final float tintOpacity = 0.7f;
        Graphics2D g2d = img.createGraphics();

        g2d.drawImage(loadImg, null, 0, 0);
        g2d.setColor(new Color(backgroundColor.getRed() / 255f, backgroundColor.getGreen() / 255f, backgroundColor.getBlue() / 255f, tintOpacity));

        Raster data = loadImg.getData();
        for (int x = data.getMinX(); x < data.getWidth(); x++) {
            for (int y = data.getMinY(); y < data.getHeight(); y++) {
                int[] pixel = data.getPixel(x, y, new int[4]);
                if (pixel[3] > 0) {
                    g2d.fillRect(x, y, 1, 1);
                }
            }
        }

        g2d.dispose();
        return img;
    }
}
