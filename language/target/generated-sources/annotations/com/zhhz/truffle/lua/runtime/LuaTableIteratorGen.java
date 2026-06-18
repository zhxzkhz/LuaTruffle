// CheckStyle: start generated
package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.StopIterationException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.DynamicDispatchLibrary;
import com.oracle.truffle.api.library.LibraryExport;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.UnadoptableNode;

@GeneratedBy(LuaTableIterator.class)
final class LuaTableIteratorGen {

    private static final LibraryFactory<DynamicDispatchLibrary> DYNAMIC_DISPATCH_LIBRARY_ = LibraryFactory.resolve(DynamicDispatchLibrary.class);

    static  {
        LibraryExport.register(LuaTableIterator.class, new InteropLibraryExports());
    }

    private LuaTableIteratorGen() {
    }

    @GeneratedBy(LuaTableIterator.class)
    private static final class InteropLibraryExports extends LibraryExport<InteropLibrary> {

        private static final Uncached UNCACHED = new Uncached();
        private static final Cached CACHE = new Cached();

        private InteropLibraryExports() {
            super(InteropLibrary.class, LuaTableIterator.class, false, false, 0);
        }

        @Override
        protected InteropLibrary createUncached(Object receiver) {
            assert receiver instanceof LuaTableIterator;
            InteropLibrary uncached = InteropLibraryExports.UNCACHED;
            return uncached;
        }

        @Override
        protected InteropLibrary createCached(Object receiver) {
            assert receiver instanceof LuaTableIterator;
            return InteropLibraryExports.CACHE;
        }

        @GeneratedBy(LuaTableIterator.class)
        private static final class Cached extends InteropLibrary implements UnadoptableNode {

            protected Cached() {
            }

            @Override
            public boolean accepts(Object receiver) {
                assert !(receiver instanceof LuaTableIterator) || DYNAMIC_DISPATCH_LIBRARY_.getUncached().dispatch(receiver) == null : "Invalid library export. Exported receiver with dynamic dispatch found but not expected.";
                return receiver instanceof LuaTableIterator;
            }

            @Override
            public boolean isIterator(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaTableIterator) receiver)).isIterator();
            }

            @Override
            public boolean hasIteratorNextElement(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaTableIterator) receiver)).hasIteratorNextElement();
            }

            @Override
            public Object getIteratorNextElement(Object receiver) throws UnsupportedMessageException, StopIterationException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaTableIterator) receiver)).getIteratorNextElement();
            }

        }
        @GeneratedBy(LuaTableIterator.class)
        @DenyReplace
        private static final class Uncached extends InteropLibrary implements UnadoptableNode {

            protected Uncached() {
            }

            @Override
            @TruffleBoundary
            public boolean accepts(Object receiver) {
                assert !(receiver instanceof LuaTableIterator) || DYNAMIC_DISPATCH_LIBRARY_.getUncached().dispatch(receiver) == null : "Invalid library export. Exported receiver with dynamic dispatch found but not expected.";
                return receiver instanceof LuaTableIterator;
            }

            @TruffleBoundary
            @Override
            public boolean isIterator(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaTableIterator) receiver) .isIterator();
            }

            @TruffleBoundary
            @Override
            public boolean hasIteratorNextElement(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaTableIterator) receiver) .hasIteratorNextElement();
            }

            @TruffleBoundary
            @Override
            public Object getIteratorNextElement(Object receiver) throws UnsupportedMessageException, StopIterationException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaTableIterator) receiver) .getIteratorNextElement();
            }

        }
    }
}
