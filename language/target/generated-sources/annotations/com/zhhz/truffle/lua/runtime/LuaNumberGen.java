// CheckStyle: start generated
package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.LibraryExport;
import com.oracle.truffle.api.nodes.DenyReplace;
import java.math.BigInteger;

@GeneratedBy(LuaNumber.class)
final class LuaNumberGen {

    static  {
        LibraryExport.register(LuaNumber.class, new InteropLibraryExports());
    }

    private LuaNumberGen() {
    }

    @GeneratedBy(LuaNumber.class)
    private static final class InteropLibraryExports extends LibraryExport<InteropLibrary> {

        private InteropLibraryExports() {
            super(InteropLibrary.class, LuaNumber.class, false, false, 0);
        }

        @Override
        protected InteropLibrary createUncached(Object receiver) {
            assert receiver instanceof LuaNumber;
            InteropLibrary uncached = new Uncached(receiver);
            return uncached;
        }

        @Override
        protected InteropLibrary createCached(Object receiver) {
            assert receiver instanceof LuaNumber;
            return new Cached(receiver);
        }

        @GeneratedBy(LuaNumber.class)
        private static final class Cached extends LuaValueGen.InteropLibraryExports.Cached {

            protected Cached(Object receiver) {
                super(receiver);
            }

            @Override
            public boolean isNumber(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).isNumber();
            }

            @Override
            public boolean fitsInByte(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).fitsInByte();
            }

            @Override
            public boolean fitsInShort(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).fitsInShort();
            }

            @Override
            public boolean fitsInInt(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).fitsInInt();
            }

            @Override
            public boolean fitsInLong(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).fitsInLong();
            }

            @Override
            public boolean fitsInFloat(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).fitsInFloat();
            }

            @Override
            public boolean fitsInDouble(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).fitsInDouble();
            }

            @Override
            public boolean fitsInBigInteger(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).fitsInBigInteger();
            }

            @Override
            public byte asByte(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).asByte();
            }

            @Override
            public short asShort(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).asShort();
            }

            @Override
            public int asInt(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).asInt();
            }

            @Override
            public long asLong(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).asLong();
            }

            @Override
            public float asFloat(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).asFloat();
            }

            @Override
            public double asDouble(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).asDouble();
            }

            @Override
            public BigInteger asBigInteger(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).asBigInteger();
            }

            @Override
            public boolean isString(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).isString();
            }

            @Override
            public String asString(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaNumber) receiver)).asString();
            }

        }
        @GeneratedBy(LuaNumber.class)
        @DenyReplace
        private static final class Uncached extends LuaValueGen.InteropLibraryExports.Uncached {

            protected Uncached(Object receiver) {
                super(receiver);
            }

            @Override
            @TruffleBoundary
            public boolean accepts(Object receiver) {
                return super.accepts(receiver);
            }

            @TruffleBoundary
            @Override
            public boolean isNumber(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .isNumber();
            }

            @TruffleBoundary
            @Override
            public boolean fitsInByte(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .fitsInByte();
            }

            @TruffleBoundary
            @Override
            public boolean fitsInShort(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .fitsInShort();
            }

            @TruffleBoundary
            @Override
            public boolean fitsInInt(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .fitsInInt();
            }

            @TruffleBoundary
            @Override
            public boolean fitsInLong(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .fitsInLong();
            }

            @TruffleBoundary
            @Override
            public boolean fitsInFloat(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .fitsInFloat();
            }

            @TruffleBoundary
            @Override
            public boolean fitsInDouble(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .fitsInDouble();
            }

            @TruffleBoundary
            @Override
            public boolean fitsInBigInteger(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .fitsInBigInteger();
            }

            @TruffleBoundary
            @Override
            public byte asByte(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .asByte();
            }

            @TruffleBoundary
            @Override
            public short asShort(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .asShort();
            }

            @TruffleBoundary
            @Override
            public int asInt(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .asInt();
            }

            @TruffleBoundary
            @Override
            public long asLong(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .asLong();
            }

            @TruffleBoundary
            @Override
            public float asFloat(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .asFloat();
            }

            @TruffleBoundary
            @Override
            public double asDouble(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .asDouble();
            }

            @TruffleBoundary
            @Override
            public BigInteger asBigInteger(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .asBigInteger();
            }

            @TruffleBoundary
            @Override
            public boolean isString(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .isString();
            }

            @TruffleBoundary
            @Override
            public String asString(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaNumber) receiver) .asString();
            }

        }
    }
}
