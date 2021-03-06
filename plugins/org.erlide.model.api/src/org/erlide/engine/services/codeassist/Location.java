package org.erlide.engine.services.codeassist;

import java.util.Objects;

import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

@SuppressWarnings("all")
public class Location {
    private final int offset;

    private final int length;

    public Location(final int offset, final int length) {
        this.offset = offset;
        this.length = length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset, length);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Location other = (Location) obj;
        if (other.offset != offset) {
            return false;
        }
        if (other.length != length) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final ToStringBuilder b = new ToStringBuilder(this);
        b.add("offset", offset);
        b.add("length", length);
        return b.toString();
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }
}
