/*******************************************************************************
 * Copyright (c) 2008 Vlad Dumitrescu and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution.
 * 
 * Contributors:
 *     Vlad Dumitrescu
 *******************************************************************************/
package org.erlide.ui.prefs.tickets;

public class TicketStatus {
	public TicketStatus(boolean ok2, int id2) {
		ok = ok2;
		id = id2;
	}

	public boolean ok;
	public int id;
}