// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.interop;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.InlineSupport.InlineTarget;
import com.oracle.truffle.api.dsl.InlineSupport.StateField;
import com.oracle.truffle.api.dsl.InlineSupport.UnsafeAccessedField;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.DynamicDispatchLibrary;
import com.oracle.truffle.api.library.LibraryExport;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnadoptableNode;
import com.oracle.truffle.api.profiles.InlinedBranchProfile;
import com.zhhz.truffle.lua.nodes.interop.NodeObjectDescriptor.WriteDescriptor;
import java.lang.invoke.MethodHandles;

@GeneratedBy(WriteDescriptor.class)
@SuppressWarnings("javadoc")
final class WriteDescriptorGen {

    private static final LibraryFactory<DynamicDispatchLibrary> DYNAMIC_DISPATCH_LIBRARY_ = LibraryFactory.resolve(DynamicDispatchLibrary.class);

    static  {
        LibraryExport.register(WriteDescriptor.class, new InteropLibraryExports());
    }

    private WriteDescriptorGen() {
    }

    @GeneratedBy(WriteDescriptor.class)
    private static final class InteropLibraryExports extends LibraryExport<InteropLibrary> {

        private InteropLibraryExports() {
            super(InteropLibrary.class, WriteDescriptor.class, false, false, 0);
        }

        @Override
        protected InteropLibrary createUncached(Object receiver) {
            assert receiver instanceof WriteDescriptor;
            InteropLibrary uncached = new Uncached();
            return uncached;
        }

        @Override
        protected InteropLibrary createCached(Object receiver) {
            assert receiver instanceof WriteDescriptor;
            return new Cached();
        }

        @GeneratedBy(WriteDescriptor.class)
        private static final class Cached extends InteropLibrary {

            private static final StateField STATE_0_ReadMemberNode__UPDATER = StateField.create(MethodHandles.lookup(), "state_0_");
            /**
             * Source Info: <pre>
             *   Specialization: {@link WriteDescriptor#readMember(WriteDescriptor, String, Node, InlinedBranchProfile)}
             *   Parameter: {@link InlinedBranchProfile} error
             *   Inline method: {@link InlinedBranchProfile#inline}</pre> */
            private static final InlinedBranchProfile INLINED_ERROR_ = InlinedBranchProfile.inline(InlineTarget.create(InlinedBranchProfile.class, STATE_0_ReadMemberNode__UPDATER.subUpdater(0, 1)));

            /**
             * State Info: <pre>
             *   0: InlinedCache
             *        Specialization: {@link WriteDescriptor#readMember}
             *        Parameter: {@link InlinedBranchProfile} error
             *        Inline method: {@link InlinedBranchProfile#inline}
             * </pre> */
            @CompilationFinal @UnsafeAccessedField private int state_0_;

            protected Cached() {
            }

            @Override
            public boolean accepts(Object receiver) {
                assert !(receiver instanceof WriteDescriptor) || DYNAMIC_DISPATCH_LIBRARY_.getUncached().dispatch(receiver) == null : "Invalid library export. Exported receiver with dynamic dispatch found but not expected.";
                return receiver instanceof WriteDescriptor;
            }

            @Override
            public boolean hasMembers(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((WriteDescriptor) receiver)).hasMembers();
            }

            @Override
            public boolean isMemberReadable(Object receiver, String member) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((WriteDescriptor) receiver)).isMemberReadable(member);
            }

            @Override
            public Object getMembers(Object receiver, boolean includeInternal) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((WriteDescriptor) receiver)).getMembers(includeInternal);
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link WriteDescriptor#readMember(WriteDescriptor, String, Node, InlinedBranchProfile)}
             *     Activation probability: 1.00000
             *     With/without class size: 24/1 bytes
             * </pre> */
            @Override
            public Object readMember(Object arg0Value_, String arg1Value) throws UnsupportedMessageException, UnknownIdentifierException {
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                WriteDescriptor arg0Value = ((WriteDescriptor) arg0Value_);
                {
                    Node node__ = (this);
                    return arg0Value.readMember(arg1Value, node__, INLINED_ERROR_);
                }
            }

        }
        @GeneratedBy(WriteDescriptor.class)
        @DenyReplace
        private static final class Uncached extends InteropLibrary implements UnadoptableNode {

            protected Uncached() {
            }

            @Override
            @TruffleBoundary
            public boolean accepts(Object receiver) {
                assert !(receiver instanceof WriteDescriptor) || DYNAMIC_DISPATCH_LIBRARY_.getUncached().dispatch(receiver) == null : "Invalid library export. Exported receiver with dynamic dispatch found but not expected.";
                return receiver instanceof WriteDescriptor;
            }

            @TruffleBoundary
            @Override
            public boolean hasMembers(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((WriteDescriptor) receiver) .hasMembers();
            }

            @TruffleBoundary
            @Override
            public boolean isMemberReadable(Object receiver, String member) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((WriteDescriptor) receiver) .isMemberReadable(member);
            }

            @TruffleBoundary
            @Override
            public Object getMembers(Object receiver, boolean includeInternal) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((WriteDescriptor) receiver) .getMembers(includeInternal);
            }

            @TruffleBoundary
            @Override
            public Object readMember(Object arg0Value_, String arg1Value) throws UnknownIdentifierException {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                WriteDescriptor arg0Value = ((WriteDescriptor) arg0Value_);
                return arg0Value.readMember(arg1Value, (this), (InlinedBranchProfile.getUncached()));
            }

        }
    }
}
