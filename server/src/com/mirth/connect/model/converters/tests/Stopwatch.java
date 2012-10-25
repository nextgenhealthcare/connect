/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model.converters.tests;
/**
* Allows timing of the execution of any block of code.
*/
public final class Stopwatch {

  /**
  * An example of the use of this class to
  * time the execution of String manipulation code.
  */
  public static void main (String... arguments) {
    Stopwatch stopwatch = new Stopwatch();

    stopwatch.start();

    //do stuff
    StringBuffer messageOne = new StringBuffer();
    int numIterations = 5000;
    for(int idx=0; idx < numIterations; ++idx){
      messageOne.append("blah");
    }

    stopwatch.stop();
    //Note that there is no need to call a method to get the duration,
    //since toString is automatic here
    System.out.println("The reading for String Buffer is: " + stopwatch);

    //reuse the same stopwatch to measure an alternative implementation
    //Note that there is no need to call a reset method.
    stopwatch.start();

    //do stuff again
    String messageTwo = null;
    for(int idx=0; idx < numIterations; ++idx){
      messageTwo = messageTwo + "blah";
    }

    stopwatch.stop();
    //perform a numeric comparsion
    if ( stopwatch.toValue() > 5 ) {
      System.out.println("The reading is high: " + stopwatch);
    }
    else {
      System.out.println("The reading is low: " + stopwatch);
    }
  }

  /**
  * Start the stopwatch.
  *
  * @throws IllegalStateException if the stopwatch is already running.
  */
  public void start(){
    if ( fIsRunning ) {
      throw new IllegalStateException("Must stop before calling start again.");
    }
    //reset both start and stop
    fStart = System.currentTimeMillis();
    fStop = 0;
    fIsRunning = true;
    fHasBeenUsedOnce = true;
  }

  /**
  * Stop the stopwatch.
  *
  * @throws IllegalStateException if the stopwatch is not already running.
  */
  public void stop() {
    if ( !fIsRunning ) {
      throw new IllegalStateException("Cannot stop if not currently running.");
    }
    fStop = System.currentTimeMillis();
    fIsRunning = false;
  }

  /**
  * Express the "reading" on the stopwatch.
  *
  * @throws IllegalStateException if the Stopwatch has never been used,
  * or if the stopwatch is still running.
  */
  public String toString() {
    validateIsReadable();
    StringBuffer result = new StringBuffer();
    result.append(fStop - fStart);
    result.append(" ms");
    return result.toString();
  }

  /**
  * Express the "reading" on the stopwatch as a numeric type.
  *
  * @throws IllegalStateException if the Stopwatch has never been used,
  * or if the stopwatch is still running.
  */
  public long toValue() {
    validateIsReadable();
    return fStop - fStart;
  }

  // PRIVATE ////
  private long fStart;
  private long fStop;

  private boolean fIsRunning;
  private boolean fHasBeenUsedOnce;

  /**
  * Throws IllegalStateException if the watch has never been started,
  * or if the watch is still running.
  */
  private void validateIsReadable() {
    if ( fIsRunning ) {
      String message = "Cannot read a stopwatch which is still running.";
      throw new IllegalStateException(message);
    }
    if ( !fHasBeenUsedOnce ) {
      String message = "Cannot read a stopwatch which has never been started.";
      throw new IllegalStateException(message);
    }
  }
}