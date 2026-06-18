// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.interop;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.InlineSupport.InlineTarget;
import com.oracle.truffle.api.dsl.InlineSupport.StateField;
import com.oracle.truffle.api.dsl.InlineSupport.UnsafeAccessedField;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.DynamicDispatchLibrary;
import com.oracle.truffle.api.library.LibraryExport;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnadoptableNode;
import com.oracle.truffle.api.profiles.InlinedBranchProfile;
import java.lang.invoke.MethodHandles;

@GeneratedBy(NodeObjectDescriptorKeys.class)
@SuppressWarnings("javadoc")
final class NodeObjectDescriptorKeysGen {

    private static final LibraryFactory<DynamicDispatchLibrary> DYNAMIC_DISPATCH_LIBRARY_ = LibraryFactory.resolve(DynamicDispatchLibrary.class);

    static  {
        LibraryExport.register(NodeObjectDescriptorKeys.class, new InteropLibraryExports());
    }

    private NodeObjectDescriptorKeysGen() {
    }

    @GeneratedBy(NodeObjectDescriptorKeys.class)
    private static final class InteropLibraryExports extends LibraryExport<InteropLibrary> {

        private InteropLibraryExports() {
            super(InteropLibrary.class, NodeObjectDescriptorKeys.class, false, false, 0);
        }

        @Override
        protected InteropLibrary createUncached(Object receiver) {
            assert receiver instanceof NodeObjectDescriptorKeys;
            InteropLibrary uncached = new Uncached();
            return uncached;
        }

        @Override
        protected InteropLibrary createCached(Object receiver) {
            assert receiver instanceof NodeObjectDescriptorKeys;
            return new Cached();
        }

        @GeneratedBy(NodeObjectDescriptorKeys.class)
        private static final class Cached extends InteropLibrary {

            private static final StateField STATE_0_ReadArrayElementNode__UPDATER = StateField.create(MethodHandles.lookup(), "state_0_");
            /**
             * Source Info: <pre>
             *   Specialization: {@link NodeObjectDescriptorKeys#readArrayElement(NodeObjectDescriptorKeys, long, Node, InlinedBranchProfile)}
             *   Parameter: {@link InlinedBranchProfile} exception
             *   Inline method: {@link InlinedBranchProfile#inline}</pre> */
            private static final InlinedBranchProfile INLINED_EXCEPTION_ = InlinedBranchProfile.inline(InlineTarget.create(InlinedBranchProfile.class, STATE_0_ReadArrayElementNode__UPDATER.subUpdater(0, 1)));

            /**
             * State Info: <pre>
             *   0: InlinedCache
             *        Specialization: {@link NodeObjectDescriptorKeys#readArrayElement}
             *        Parameter: {@link InlinedBranchProfile} exception
             *        Inline method: {@link InlinedBranchProfile#inline}
             * </pre> */
            @CompilationFinal @UnsafeAccessedField private int state_0_;

            protected Cached() {
            }

            @Override
            public boolean accepts(Object receiver) {
                assert !(receiver instanceof NodeObjectDescriptorKeys) || DYNAMIC_DISPATCH_LIBRARY_.getUncached().dispatch(receiver) == null : "Invalid library export. Exported receiver with dynamic dispatch found but not expected.";
                return receiver instanceof NodeObjectDescriptorKeys;
            }

            @Override
            public boolean hasArrayElements(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((NodeObjectDescriptorKeys) receiver)).hasArrayElements();
            }

            @Override
            public boolean isArrayElementReadable(Object receiver, long index) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((NodeObjectDescriptorKeys) receiver)).isArrayElementReadable(index);
            }

            @Override
            public long getArraySize(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((NodeObjectDescriptorKeys) receiver)).getArraySize();
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link NodeObjectDescriptorKeys#readArrayElement(NodeObjectDescriptorKeys, long, Node, InlinedBranchProfile)}
             *     Activation probability: 1.00000
             *     With/without class size: 24/1 bytes
             * </pre> */
            @Override
            public Object readArrayElement(Object arg0Value_, long arg1Value) throws UnsupportedMessageException, InvalidArrayIndexException {
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                NodeObjectDescriptorKeys arg0Value = ((NodeObjectDescriptorKeys) arg0Value_);
                {
                    Node node__ = (this);
                    return arg0Value.readArrayElement(arg1Value, node__, INLINED_EXCEPTION_);
                }
            }

        }
        @GeneratedBy(NodeObjectDescriptorKeys.class)
        @DenyReplace
        private static final class Uncached extends InteropLibrary implements UnadoptableNode {

            protected Uncached() {
            }

            @Override
            @TruffleBoundary
            public boolean accepts(Object receiver) {
                assert !(receiver instanceof NodeObjectDescriptorKeys) || DYNAMIC_DISPATCH_LIBRARY_.getUncached().dispatch(receiver) == null : "Invalid library export. Exported receiver with dynamic dispatch found but not expected.";
                return receiver instanceof NodeObjectDescriptorKeys;
            }

            @TruffleBoundary
            @Override
            public boolean hasArrayElements(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((NodeObjectDescriptorKeys) receiver) .hasArrayElements();
            }

            @TruffleBoundary
            @Override
            public boolean isArrayElementReadable(Object receiver, long index) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((NodeObjectDescriptorKeys) receiver) .isArrayElementReadable(index);
            }

            @TruffleBoundary
            @Override
            public long getArraySize(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((NodeObjectDescriptorKeys) receiver) .getArraySize();
            }

            @TruffleBoundary
            @Override
            public Object readArrayElement(Object arg0Value_, long arg1Value) throws InvalidArrayIndexException {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                NodeObjectDescriptorKeys arg0Value = ((NodeObjectDescriptorKeys) arg0Value_);
                return arg0Value.readArrayElement(arg1Value, (this), (InlinedBranchProfile.getUncached()));
            }

        }
    }
}
