/*
 * Copyright (C) 2000-2001 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=BTE
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * See COPYING.TXT for details.
 */

package com.Ostermiller.bte;

/**
 * An exception that occurs when an element in a template cannot 
 * be compiled.
 */
public class CompileException extends Exception {
    /**
     * Create a new compile exception.
     */
    public CompileException(){
        super();
    }
    
    /**
     * Create a new compile exception
     * with the given message.
     *
     * @param s message for the new exception.
     */
    public CompileException(String s){
        super(s);
    }
}
