/* ``The contents of this file are subject to the Erlang Public License,
 * Version 1.1, (the "License"); you may not use this file except in
 * compliance with the License. You should have received a copy of the
 * Erlang Public License along with this software. If not, it can be
 * retrieved via the world wide web at http://www.erlang.org/.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Initial Developer of the Original Code is Ericsson Utvecklings AB.
 * Portions created by Ericsson are Copyright 1999, Ericsson Utvecklings
 * AB. All Rights Reserved.''
 *
 *     $Id$
 */
package com.ericsson.otp.erlang;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Provides a Java representation of Erlang lists. Lists are created from zero
 * or more arbitrary Erlang terms.
 * 
 * <p>
 * The arity of the list is the number of elements it contains.
 */
public class OtpErlangList extends OtpErlangObject implements
	Iterable<OtpErlangObject>, Serializable, Cloneable {
    // don't change this!
    static final long serialVersionUID = 5999112769036676548L;

    private static final OtpErlangObject[] NO_ELEMENTS = new OtpErlangObject[0];

    final private OtpErlangObject[] elems;

    private OtpErlangObject lastTail = null;

    /**
     * Create an empty list.
     */
    public OtpErlangList() {
	elems = NO_ELEMENTS;
    }

    /**
     * Create a list of characters.
     * 
     * @param str
     *                the characters from which to create the list.
     */
    public OtpErlangList(final String str) {
	if (str == null || str.length() == 0) {
	    elems = NO_ELEMENTS;
	} else {
	    final int len = str.length();
	    elems = new OtpErlangObject[len];
	    for (int i = 0; i < len; i++) {
		elems[i] = new OtpErlangChar(str.charAt(i));
	    }
	}
    }

    /**
     * Create a list containing one element.
     * 
     * @param elem
     *                the elememet to make the list from.
     */
    public OtpErlangList(final OtpErlangObject elem) {
	elems = new OtpErlangObject[] { elem };
    }

    /**
     * Create a list from an array of arbitrary Erlang terms.
     * 
     * @param elems
     *                the array of terms from which to create the list.
     */
    public OtpErlangList(final OtpErlangObject[] elems) {
	this(elems, 0, elems.length);
    }

    /**
     * Create a list from an array of arbitrary Erlang terms. Tail can be
     * specified, if not null, the list will not be proper.
     * 
     * @param elems
     *                array of terms from which to create the list
     * @param lastTail
     * @throws OtpErlangException
     */
    public OtpErlangList(final OtpErlangObject[] elems,
	    final OtpErlangObject lastTail) throws OtpErlangException {
	this(elems, 0, elems.length);
	if (elems.length == 0 && lastTail != null) {
	    throw new OtpErlangException("Bad list, empty head, non-empty tail");
	}
	this.lastTail = lastTail;
    }

    /**
     * Create a list from an array of arbitrary Erlang terms.
     * 
     * @param elems
     *                the array of terms from which to create the list.
     * @param start
     *                the offset of the first term to insert.
     * @param count
     *                the number of terms to insert.
     */
    public OtpErlangList(final OtpErlangObject[] elems, final int start,
	    final int count) {
	if (elems != null && count > 0) {
	    this.elems = new OtpErlangObject[count];
	    System.arraycopy(elems, start, this.elems, 0, count);
	} else {
	    this.elems = NO_ELEMENTS;
	}
    }

    /**
     * Create a list from a stream containing an list encoded in Erlang external
     * format.
     * 
     * @param buf
     *                the stream containing the encoded list.
     * 
     * @exception OtpErlangDecodeException
     *                    if the buffer does not contain a valid external
     *                    representation of an Erlang list.
     */
    public OtpErlangList(final OtpInputStream buf)
	    throws OtpErlangDecodeException {
	final int arity = buf.read_list_head();
	if (arity > 0) {
	    elems = new OtpErlangObject[arity];
	    for (int i = 0; i < arity; i++) {
		elems[i] = buf.read_any();
	    }
	    /* discard the terminating nil (empty list) or read tail */
	    if (buf.peek() == OtpExternal.nilTag) {
		buf.read_nil();
	    } else {
		lastTail = buf.read_any();
	    }
	} else {
	    elems = NO_ELEMENTS;
	}
    }

    /**
     * Get the arity of the list.
     * 
     * @return the number of elements contained in the list.
     */
    public int arity() {
	return elems.length;
    }

    /**
     * Get the specified element from the list.
     * 
     * @param i
     *                the index of the requested element. List elements are
     *                numbered as array elements, starting at 0.
     * 
     * @return the requested element, of null if i is not a valid element index.
     */
    public OtpErlangObject elementAt(final int i) {
	if (i >= arity() || i < 0) {
	    return null;
	}
	return elems[i];
    }

    /**
     * Get all the elements from the list as an array.
     * 
     * @return an array containing all of the list's elements.
     */
    public OtpErlangObject[] elements() {
	if (arity() == 0) {
	    return NO_ELEMENTS;
	} else {
	    final OtpErlangObject[] res = new OtpErlangObject[arity()];
	    System.arraycopy(elems, 0, res, 0, res.length);
	    return res;
	}
    }

    /**
     * Get the string representation of the list.
     * 
     * @return the string representation of the list.
     */

    @Override
    public String toString() {
	return toString(0);
    }

    protected String toString(final int start) {
	final StringBuffer s = new StringBuffer();
	s.append("[");

	for (int i = start; i < arity(); i++) {
	    if (i > start) {
		s.append(",");
	    }
	    s.append(elems[i].toString());
	}
	if (lastTail != null) {
	    s.append("|").append(lastTail.toString());
	}
	s.append("]");

	return s.toString();
    }

    /**
     * Convert this list to the equivalent Erlang external representation. Note
     * that this method never encodes lists as strings, even when it is possible
     * to do so.
     * 
     * @param buf
     *                An output stream to which the encoded list should be
     *                written.
     * 
     */

    @Override
    public void encode(final OtpOutputStream buf) {
	encode(buf, 0);
    }

    protected void encode(final OtpOutputStream buf, final int start) {
	final int arity = arity() - start;

	if (arity > 0) {
	    buf.write_list_head(arity);

	    for (int i = start; i < arity + start; i++) {
		buf.write_any(elems[i]);
	    }
	}
	if (lastTail == null) {
	    buf.write_nil();
	} else {
	    buf.write_any(lastTail);
	}
    }

    /**
     * Determine if two lists are equal. Lists are equal if they have the same
     * arity and all of the elements are equal.
     * 
     * @param o
     *                the list to compare to.
     * 
     * @return true if the lists have the same arity and all the elements are
     *         equal.
     */

    @Override
    public boolean equals(final Object o) {

	/*
	 * Be careful to use methods even for "this", so that equals work also
	 * for sublists
	 */

	if (!(o instanceof OtpErlangList)) {
	    return false;
	}

	final OtpErlangList l = (OtpErlangList) o;

	final int a = arity();
	if (a != l.arity()) {
	    return false;
	}
	for (int i = 0; i < a; i++) {
	    if (!elementAt(i).equals(l.elementAt(i))) {
		return false; // early exit
	    }
	}
	final OtpErlangObject otherTail = l.getLastTail();
	if (getLastTail() == null && otherTail == null) {
	    return true;
	}
	if (getLastTail() == null) {
	    return false;
	}
	return getLastTail().equals(l.getLastTail());
    }

    public OtpErlangObject getLastTail() {
	return lastTail;
    }

    @Override
    public Object clone() {
	try {
	    return new OtpErlangList(elements(), getLastTail());
	} catch (final OtpErlangException e) {
	    return null;
	}
    }

    public Iterator<OtpErlangObject> iterator() {
	return iterator(0);
    }

    private Iterator<OtpErlangObject> iterator(final int start) {
	return new Itr(start);
    }

    /**
     * @return true if the list is proper, i.e. the last tail is nil
     */
    public boolean isProper() {
	return lastTail == null;
    }

    public OtpErlangObject getHead() {
	if (arity() > 0) {
	    return elems[0];
	}
	return null;
    }

    public OtpErlangObject getTail() {
	return getNthTail(1);
    }

    public OtpErlangObject getNthTail(final int n) {
	final int arity = arity();
	if (arity >= n) {
	    if (arity == n && lastTail != null) {
		return lastTail;
	    } else {
		return new SubList(this, n);
	    }
	}
	return null;
    }

    public static class SubList extends OtpErlangList {
	private static final long serialVersionUID = OtpErlangList.serialVersionUID;

	private final int start;

	private final OtpErlangList parent;

	private SubList(final OtpErlangList parent, final int start) {
	    super();
	    this.parent = parent;
	    this.start = start;
	}

	@Override
	public int arity() {
	    return parent.arity() - start;
	}

	@Override
	public OtpErlangObject elementAt(final int i) {
	    return parent.elementAt(i + start);
	}

	@Override
	public OtpErlangObject[] elements() {
	    final int n = parent.arity() - start;
	    final OtpErlangObject[] res = new OtpErlangObject[n];
	    for (int i = 0; i < res.length; i++) {
		res[i] = parent.elementAt(i + start);
	    }
	    return res;
	}

	@Override
	public boolean isProper() {
	    return parent.isProper();
	}

	@Override
	public OtpErlangObject getHead() {
	    return parent.elementAt(start);
	}

	@Override
	public OtpErlangObject getNthTail(final int n) {
	    return parent.getNthTail(n + start);
	}

	@Override
	public String toString() {
	    return parent.toString(start);
	}

	@Override
	public void encode(final OtpOutputStream stream) {
	    parent.encode(stream, start);
	}

	@Override
	public OtpErlangObject getLastTail() {
	    return parent.getLastTail();
	}

	@Override
	public Iterator<OtpErlangObject> iterator() {
	    return parent.iterator(start);
	}
    }

    private class Itr implements Iterator<OtpErlangObject> {
	/**
	 * Index of element to be returned by subsequent call to next.
	 */
	private int cursor;

	private Itr(final int cursor) {
	    this.cursor = cursor;
	}

	public boolean hasNext() {
	    return cursor < elems.length;
	}

	public OtpErlangObject next() {
	    try {
		return elems[cursor++];
	    } catch (final IndexOutOfBoundsException e) {
		throw new NoSuchElementException();
	    }
	}

	public void remove() {
	    throw new UnsupportedOperationException(
		    "OtpErlangList cannot be modified!");
	}
    }
}
