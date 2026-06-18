// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.interop;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.DynamicDispatchLibrary;
import com.oracle.truffle.api.library.LibraryExport;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.UnadoptableNode;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.ToJavaStringNode;
import com.zhhz.truffle.lua.nodes.interop.NodeObjectDescriptor.NameSymbol;
import java.lang.invoke.VarHandle;
import java.util.Objects;

@GeneratedBy(NameSymbol.class)
@SuppressWarnings("javadoc")
final class NameSymbolGen {

    private static final LibraryFactory<DynamicDispatchLibrary> DYNAMIC_DISPATCH_LIBRARY_ = LibraryFactory.resolve(DynamicDispatchLibrary.class);

    static  {
        LibraryExport.register(NameSymbol.class, new InteropLibraryExports());
    }

    private NameSymbolGen() {
    }

    @GeneratedBy(NameSymbol.class)
    private static final class InteropLibraryExports extends LibraryExport<InteropLibrary> {

        private InteropLibraryExports() {
            super(InteropLibrary.class, NameSymbol.class, false, false, 0);
        }

        @Override
        protected InteropLibrary createUncached(Object receiver) {
            assert receiver instanceof NameSymbol;
            InteropLibrary uncached = new Uncached();
            return uncached;
        }

        @Override
        protected InteropLibrary createCached(Object receiver) {
            assert receiver instanceof NameSymbol;
            return new Cached();
        }

        @GeneratedBy(NameSymbol.class)
        private static final class Cached extends InteropLibrary {

            /**
             * State Info: <pre>
             *   0: SpecializationActive {@link NameSymbol#asString(NameSymbol, ToJavaStringNode)}
             * </pre> */
            @CompilationFinal private int state_0_;
            /**
             * Source Info: <pre>
             *   Specialization: {@link NameSymbol#asString(NameSymbol, ToJavaStringNode)}
             *   Parameter: {@link ToJavaStringNode} toJavaStringNode</pre> */
            @Child private ToJavaStringNode toJavaStringNode_;

            protected Cached() {
            }

            @Override
            public boolean accepts(Object receiver) {
                assert !(receiver instanceof NameSymbol) || DYNAMIC_DISPATCH_LIBRARY_.getUncached().dispatch(receiver) == null : "Invalid library export. Exported receiver with dynamic dispatch found but not expected.";
                return receiver instanceof NameSymbol;
            }

            @Override
            public boolean isString(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((NameSymbol) receiver)).isString();
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link NameSymbol#asString(NameSymbol, ToJavaStringNode)}
             *     Activation probability: 1.00000
             *     With/without class size: 24/4 bytes
             * </pre> */
            @Override
            public String asString(Object arg0Value_) throws UnsupportedMessageException {
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                NameSymbol arg0Value = ((NameSymbol) arg0Value_);
                int state_0 = this.state_0_;
                if (state_0 != 0 /* is SpecializationActive[NameSymbol.asString(NameSymbol, ToJavaStringNode)] */) {
                    {
                        ToJavaStringNode toJavaStringNode__ = this.toJavaStringNode_;
                        if (toJavaStringNode__ != null) {
                            return arg0Value.asString(toJavaStringNode__);
                        }
                    }
                }
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return executeAndSpecialize(arg0Value);
            }

            private String executeAndSpecialize(NameSymbol arg0Value) {
                int state_0 = this.state_0_;
                ToJavaStringNode toJavaStringNode__ = this.insert((ToJavaStringNode.create()));
                Objects.requireNonNull(toJavaStringNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                VarHandle.storeStoreFence();
                this.toJavaStringNode_ = toJavaStringNode__;
                state_0 = state_0 | 0b1 /* add SpecializationActive[NameSymbol.asString(NameSymbol, ToJavaStringNode)] */;
                this.state_0_ = state_0;
                return arg0Value.asString(toJavaStringNode__);
            }

            @Override
            public TruffleString asTruffleString(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((NameSymbol) receiver)).asTruffleString();
            }

            @Override
            public boolean hasSourceLocation(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((NameSymbol) receiver)).hasSourceLocation();
            }

            @Override
            public SourceSection getSourceLocation(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((NameSymbol) receiver)).getSourceLocation();
            }

        }
        @GeneratedBy(NameSymbol.class)
        @DenyReplace
        private static final class Uncached extends InteropLibrary implements UnadoptableNode {

            protected Uncached() {
            }

            @Override
            @TruffleBoundary
            public boolean accepts(Object receiver) {
                assert !(receiver instanceof NameSymbol) || DYNAMIC_DISPATCH_LIBRARY_.getUncached().dispatch(receiver) == null : "Invalid library export. Exported receiver with dynamic dispatch found but not expected.";
                return receiver instanceof NameSymbol;
            }

            @TruffleBoundary
            @Override
            public boolean isString(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((NameSymbol) receiver) .isString();
            }

            @TruffleBoundary
            @Override
            public String asString(Object arg0Value_) {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                NameSymbol arg0Value = ((NameSymbol) arg0Value_);
                return arg0Value.asString((ToJavaStringNode.getUncached()));
            }

            @TruffleBoundary
            @Override
            public TruffleString asTruffleString(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((NameSymbol) receiver) .asTruffleString();
            }

            @TruffleBoundary
            @Override
            public boolean hasSourceLocation(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((NameSymbol) receiver) .hasSourceLocation();
            }

            @TruffleBoundary
            @Override
            public SourceSection getSourceLocation(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((NameSymbol) receiver) .getSourceLocation();
            }

        }
    }
}
