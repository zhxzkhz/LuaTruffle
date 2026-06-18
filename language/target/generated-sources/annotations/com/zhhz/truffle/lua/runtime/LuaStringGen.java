// CheckStyle: start generated
package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.LibraryExport;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.utilities.TriState;

@GeneratedBy(LuaString.class)
final class LuaStringGen {

    static  {
        LibraryExport.register(LuaString.class, new InteropLibraryExports());
    }

    private LuaStringGen() {
    }

    @GeneratedBy(LuaString.class)
    private static final class InteropLibraryExports extends LibraryExport<InteropLibrary> {

        private InteropLibraryExports() {
            super(InteropLibrary.class, LuaString.class, false, false, 0);
        }

        @Override
        protected InteropLibrary createUncached(Object receiver) {
            assert receiver instanceof LuaString;
            InteropLibrary uncached = new Uncached(receiver);
            return uncached;
        }

        @Override
        protected InteropLibrary createCached(Object receiver) {
            assert receiver instanceof LuaString;
            return new Cached(receiver);
        }

        @GeneratedBy(LuaString.class)
        private static final class Cached extends LuaValueGen.InteropLibraryExports.Cached {

            protected Cached(Object receiver) {
                super(receiver);
            }

            @Override
            public boolean isString(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaString) receiver)).isString();
            }

            @Override
            public String asString(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaString) receiver)).asString();
            }

            @Override
            public Object toDisplayString(Object receiver, boolean allowSideEffects) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaString) receiver)).toDisplayString(allowSideEffects);
            }

            @Override
            public int identityHashCode(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaString) receiver)).identityHashCode();
            }

            @Override
            protected TriState isIdenticalOrUndefined(Object receiver, Object other) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaString) receiver)).isIdenticalOrUndefined(other);
            }

        }
        @GeneratedBy(LuaString.class)
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
            public boolean isString(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaString) receiver) .isString();
            }

            @TruffleBoundary
            @Override
            public String asString(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaString) receiver) .asString();
            }

            @TruffleBoundary
            @Override
            public Object toDisplayString(Object receiver, boolean allowSideEffects) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaString) receiver) .toDisplayString(allowSideEffects);
            }

            @TruffleBoundary
            @Override
            public int identityHashCode(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaString) receiver) .identityHashCode();
            }

            @TruffleBoundary
            @Override
            protected TriState isIdenticalOrUndefined(Object receiver, Object other) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaString) receiver) .isIdenticalOrUndefined(other);
            }

        }
    }
}
