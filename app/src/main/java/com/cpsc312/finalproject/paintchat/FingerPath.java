/**
 * This program runs a paint app, which can import and export images.
 * Final project, CPSC 312
 * Icons from Material.io
 * Would not have been possible without the help from:
 *   - https://android.jlelse.eu/learn-to-create-a-paint-application-for-android-5b16968063f8
 *   - Stack Exchange
 *
 * @author Maxwell Sherman
 *   - DrawView
 *   - Layouts
 *   - Image saving
 *   - Slideshow/documentation
 * @author Andrew Italo
 *   - Image sharing
 *   - Main menu logic
 *   - Using the last image
 *   - Getting the image inside DrawView
 * These were merely areas of focus earlier on in development.
 * A significant portion of the time was spent working together on the same code.
 *
 * @version v1.0
 */
package com.cpsc312.finalproject.paintchat;

import android.graphics.Path;

public class FingerPath {
    public int color;
    public int strokeWidth;
    public Path path;

    /**
     * Explicit value constructor
     * @param color color of path
     * @param strokeWidth width of path
     * @param path Path object
     */
    public FingerPath(int color, int strokeWidth, Path path) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}
