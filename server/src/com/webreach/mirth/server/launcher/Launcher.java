/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.server.launcher;

import java.net.URLClassLoader;

public class Launcher {
	public static void main(String[] args) {
		if (args[0] != null) {
			try {
				ClasspathBuilder builder = new ClasspathBuilder(args[0]);
				URLClassLoader classLoader = new URLClassLoader(builder.getClasspath());
				Class mirthClass = classLoader.loadClass("com.webreach.mirth.server.Mirth");
				Thread mirthThread = (Thread) mirthClass.newInstance();
				mirthThread.setContextClassLoader(classLoader);
				mirthThread.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("usage: java Launcher launcher.xml");
		}
	}
}
