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


package com.webreach.mirth.managers.types;

import java.io.Serializable;

import com.webreach.mirth.managers.types.mirth.Channel;
import com.webreach.mirth.managers.types.mirth.Endpoint;
import com.webreach.mirth.managers.types.mirth.Filter;
import com.webreach.mirth.managers.types.mirth.Transformer;
import com.webreach.mirth.managers.types.mirth.User;

public abstract class MirthElement implements Serializable, Cloneable {
	
	public boolean equals(Object o) {
		if (this instanceof Channel) {
			if (this == o)
				return true;
			else if (o instanceof Channel) {
				Channel lhs = (Channel) this;
				final Channel rhs = (Channel) o;
	
				return (lhs.getId() == rhs.getId());
			}
		} else if (this instanceof Endpoint) {
			if (this == o)
				return true;
			else if (o instanceof Endpoint) {
				Endpoint lhs = (Endpoint) this;
				final Endpoint rhs = (Endpoint) o;
	
				return (lhs.getId() == rhs.getId());
			}
		} else if (this instanceof Filter) {
			if (this == o)
				return true;
			else if (o instanceof Filter) {
				Filter lhs = (Filter) this;
				final Filter rhs = (Filter) o;
	
				return (lhs.getId() == rhs.getId());
			}
		} else if (this instanceof Transformer) {
			if (this == o)
				return true;
			else if (o instanceof Transformer) {
				Transformer lhs = (Transformer) this;
				final Transformer rhs = (Transformer) o;
	
				return (lhs.getId() == rhs.getId());
			}
		} else if (this instanceof User) {
			if (this == o)
				return true;
			else if (o instanceof User) {
				User lhs = (User) this;
				final User rhs = (User) o;
	
				return (lhs.getId() == rhs.getId());
			}
		}
	
		return false;
	}
}
