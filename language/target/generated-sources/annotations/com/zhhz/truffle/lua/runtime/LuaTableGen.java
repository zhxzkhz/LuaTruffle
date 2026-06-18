// CheckStyle: start generated
package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.DSLSupport.SpecializationDataNode;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnknownKeyException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.LibraryExport;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.TruffleString.FromJavaStringNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToIntegerNode;
import com.zhhz.truffle.lua.nodes.util.LuaCoerceToIntegerNodeGen;
import java.lang.invoke.VarHandle;
import java.util.Objects;

@GeneratedBy(LuaTable.class)
@SuppressWarnings("javadoc")
final class LuaTableGen {

    private static final LibraryFactory<DynamicObjectLibrary> DYNAMIC_OBJECT_LIBRARY_ = LibraryFactory.resolve(DynamicObjectLibrary.class);

    static  {
        LibraryExport.register(LuaTable.class, new InteropLibraryExports());
    }

    private LuaTableGen() {
    }

    @GeneratedBy(LuaTable.class)
    private static final class InteropLibraryExports extends LibraryExport<InteropLibrary> {

        private InteropLibraryExports() {
            super(InteropLibrary.class, LuaTable.class, false, false, 0);
        }

        @Override
        protected InteropLibrary createUncached(Object receiver) {
            assert receiver instanceof LuaTable;
            InteropLibrary uncached = new Uncached(receiver);
            return uncached;
        }

        @Override
        protected InteropLibrary createCached(Object receiver) {
            assert receiver instanceof LuaTable;
            return new Cached(receiver);
        }

        @GeneratedBy(LuaTable.class)
        private static final class Cached extends LuaValueGen.InteropLibraryExports.Cached {

            @Child private DynamicObjectLibrary receiverDynamicObjectLibrary_;
            /**
             * State Info: <pre>
             *   0: SpecializationActive {@link LuaTable#isMemberReadable(LuaTable, String, FromJavaStringNode, DynamicObjectLibrary)}
             *   1: SpecializationActive {@link LuaTable#readMember(LuaTable, String, LuaCoerceToIntegerNode, FromJavaStringNode, DynamicObjectLibrary)}
             *   2: SpecializationActive {@link LuaTable#writeMember(LuaTable, String, Object, FromJavaStringNode, DynamicObjectLibrary)}
             * </pre> */
            @CompilationFinal private int state_0_;
            /**
             * Source Info: <pre>
             *   Specialization: {@link LuaTable#isMemberReadable(LuaTable, String, FromJavaStringNode, DynamicObjectLibrary)}
             *   Parameter: {@link FromJavaStringNode} fromJavaStringNode</pre> */
            @Child private FromJavaStringNode isMemberReadableNode__isMemberReadable_fromJavaStringNode_;
            @Child private ReadMemberNode_ReadMemberData readMemberNode__readMember_cache;
            /**
             * Source Info: <pre>
             *   Specialization: {@link LuaTable#writeMember(LuaTable, String, Object, FromJavaStringNode, DynamicObjectLibrary)}
             *   Parameter: {@link FromJavaStringNode} fromJavaStringNode</pre> */
            @Child private FromJavaStringNode writeMemberNode__writeMember_fromJavaStringNode_;

            protected Cached(Object receiver) {
                super(receiver);
                LuaTable castReceiver = ((LuaTable) receiver) ;
                this.receiverDynamicObjectLibrary_ = DYNAMIC_OBJECT_LIBRARY_.create((castReceiver));
            }

            @Override
            public boolean accepts(Object receiver) {
                if (!(super.accepts(receiver))) {
                    return false;
                } else if (!this.receiverDynamicObjectLibrary_.accepts((receiver))) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public boolean hasMembers(Object receiver) {
                assert super.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaTable) receiver)).hasMembers();
            }

            @Override
            public boolean isMemberModifiable(Object receiver, String member) {
                assert super.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaTable) receiver)).isMemberModifiable(member);
            }

            @Override
            public boolean isMemberInsertable(Object receiver, String member) {
                assert super.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaTable) receiver)).isMemberInsertable(member);
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link LuaTable#getMembers(LuaTable, boolean, DynamicObjectLibrary)}
             *     Activation probability: 0.12500
             *     With/without class size: 5/0 bytes
             * </pre> */
            @Override
            public Object getMembers(Object arg0Value_, boolean arg1Value) throws UnsupportedMessageException {
                assert super.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                {
                    DynamicObjectLibrary objLib__ = this.receiverDynamicObjectLibrary_;
                    return arg0Value.getMembers(arg1Value, objLib__);
                }
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link LuaTable#isMemberReadable(LuaTable, String, FromJavaStringNode, DynamicObjectLibrary)}
             *     Activation probability: 0.12500
             *     With/without class size: 6/4 bytes
             * </pre> */
            @Override
            public boolean isMemberReadable(Object arg0Value_, String arg1Value) {
                assert super.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                int state_0 = this.state_0_;
                if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaTable.isMemberReadable(LuaTable, String, FromJavaStringNode, DynamicObjectLibrary)] */) {
                    {
                        FromJavaStringNode fromJavaStringNode__ = this.isMemberReadableNode__isMemberReadable_fromJavaStringNode_;
                        if (fromJavaStringNode__ != null) {
                            DynamicObjectLibrary objLib__ = this.receiverDynamicObjectLibrary_;
                            return arg0Value.isMemberReadable(arg1Value, fromJavaStringNode__, objLib__);
                        }
                    }
                }
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return isMemberReadableNode_AndSpecialize(arg0Value, arg1Value);
            }

            private boolean isMemberReadableNode_AndSpecialize(LuaTable arg0Value, String arg1Value) {
                int state_0 = this.state_0_;
                {
                    DynamicObjectLibrary objLib__ = null;
                    FromJavaStringNode fromJavaStringNode__ = this.insert((FromJavaStringNode.create()));
                    Objects.requireNonNull(fromJavaStringNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                    VarHandle.storeStoreFence();
                    this.isMemberReadableNode__isMemberReadable_fromJavaStringNode_ = fromJavaStringNode__;
                    objLib__ = this.receiverDynamicObjectLibrary_;
                    state_0 = state_0 | 0b1 /* add SpecializationActive[LuaTable.isMemberReadable(LuaTable, String, FromJavaStringNode, DynamicObjectLibrary)] */;
                    this.state_0_ = state_0;
                    return arg0Value.isMemberReadable(arg1Value, fromJavaStringNode__, objLib__);
                }
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link LuaTable#readMember(LuaTable, String, LuaCoerceToIntegerNode, FromJavaStringNode, DynamicObjectLibrary)}
             *     Activation probability: 0.12500
             *     With/without class size: 7/8 bytes
             * </pre> */
            @Override
            public Object readMember(Object arg0Value_, String arg1Value) throws UnsupportedMessageException, UnknownIdentifierException {
                assert super.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                int state_0 = this.state_0_;
                if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaTable.readMember(LuaTable, String, LuaCoerceToIntegerNode, FromJavaStringNode, DynamicObjectLibrary)] */) {
                    ReadMemberNode_ReadMemberData s0_ = this.readMemberNode__readMember_cache;
                    if (s0_ != null) {
                        {
                            DynamicObjectLibrary objLib__ = this.receiverDynamicObjectLibrary_;
                            return arg0Value.readMember(arg1Value, s0_.coerceNode_, s0_.fromJavaStringNode_, objLib__);
                        }
                    }
                }
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return readMemberNode_AndSpecialize(arg0Value, arg1Value);
            }

            private Object readMemberNode_AndSpecialize(LuaTable arg0Value, String arg1Value) throws UnknownIdentifierException {
                int state_0 = this.state_0_;
                {
                    DynamicObjectLibrary objLib__ = null;
                    ReadMemberNode_ReadMemberData s0_ = this.insert(new ReadMemberNode_ReadMemberData());
                    s0_.coerceNode_ = s0_.insert((LuaCoerceToIntegerNode.create()));
                    FromJavaStringNode fromJavaStringNode__ = s0_.insert((FromJavaStringNode.create()));
                    Objects.requireNonNull(fromJavaStringNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                    s0_.fromJavaStringNode_ = fromJavaStringNode__;
                    objLib__ = this.receiverDynamicObjectLibrary_;
                    VarHandle.storeStoreFence();
                    this.readMemberNode__readMember_cache = s0_;
                    state_0 = state_0 | 0b10 /* add SpecializationActive[LuaTable.readMember(LuaTable, String, LuaCoerceToIntegerNode, FromJavaStringNode, DynamicObjectLibrary)] */;
                    this.state_0_ = state_0;
                    return arg0Value.readMember(arg1Value, s0_.coerceNode_, fromJavaStringNode__, objLib__);
                }
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link LuaTable#writeMember(LuaTable, String, Object, FromJavaStringNode, DynamicObjectLibrary)}
             *     Activation probability: 0.12500
             *     With/without class size: 6/4 bytes
             * </pre> */
            @Override
            public void writeMember(Object arg0Value_, String arg1Value, Object arg2Value) throws UnsupportedMessageException, UnknownIdentifierException, UnsupportedTypeException {
                assert super.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                int state_0 = this.state_0_;
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaTable.writeMember(LuaTable, String, Object, FromJavaStringNode, DynamicObjectLibrary)] */) {
                    {
                        FromJavaStringNode fromJavaStringNode__ = this.writeMemberNode__writeMember_fromJavaStringNode_;
                        if (fromJavaStringNode__ != null) {
                            DynamicObjectLibrary objLib__ = this.receiverDynamicObjectLibrary_;
                            arg0Value.writeMember(arg1Value, arg2Value, fromJavaStringNode__, objLib__);
                            return;
                        }
                    }
                }
                CompilerDirectives.transferToInterpreterAndInvalidate();
                writeMemberNode_AndSpecialize(arg0Value, arg1Value, arg2Value);
                return;
            }

            private void writeMemberNode_AndSpecialize(LuaTable arg0Value, String arg1Value, Object arg2Value) {
                int state_0 = this.state_0_;
                {
                    DynamicObjectLibrary objLib__ = null;
                    FromJavaStringNode fromJavaStringNode__ = this.insert((FromJavaStringNode.create()));
                    Objects.requireNonNull(fromJavaStringNode__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                    VarHandle.storeStoreFence();
                    this.writeMemberNode__writeMember_fromJavaStringNode_ = fromJavaStringNode__;
                    objLib__ = this.receiverDynamicObjectLibrary_;
                    state_0 = state_0 | 0b100 /* add SpecializationActive[LuaTable.writeMember(LuaTable, String, Object, FromJavaStringNode, DynamicObjectLibrary)] */;
                    this.state_0_ = state_0;
                    arg0Value.writeMember(arg1Value, arg2Value, fromJavaStringNode__, objLib__);
                    return;
                }
            }

            @Override
            public boolean hasArrayElements(Object receiver) {
                assert super.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaTable) receiver)).hasArrayElements();
            }

            @Override
            public long getArraySize(Object receiver) throws UnsupportedMessageException {
                assert super.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaTable) receiver)).getArraySize();
            }

            @Override
            public boolean isArrayElementReadable(Object receiver, long index) {
                assert super.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaTable) receiver)).isArrayElementReadable(index);
            }

            @Override
            public Object readArrayElement(Object receiver, long index) throws UnsupportedMessageException, InvalidArrayIndexException {
                assert super.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaTable) receiver)).readArrayElement(index);
            }

            @Override
            public boolean hasHashEntries(Object receiver) {
                assert super.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaTable) receiver)).hasHashEntries();
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link LuaTable#getHashSize(LuaTable, DynamicObjectLibrary)}
             *     Activation probability: 0.12500
             *     With/without class size: 5/0 bytes
             * </pre> */
            @Override
            public long getHashSize(Object arg0Value_) throws UnsupportedMessageException {
                assert super.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                {
                    DynamicObjectLibrary objLib__ = this.receiverDynamicObjectLibrary_;
                    return arg0Value.getHashSize(objLib__);
                }
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link LuaTable#isHashEntryReadable(LuaTable, Object, DynamicObjectLibrary)}
             *     Activation probability: 0.12500
             *     With/without class size: 5/0 bytes
             * </pre> */
            @Override
            public boolean isHashEntryReadable(Object arg0Value_, Object arg1Value) {
                assert super.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                {
                    DynamicObjectLibrary objLib__ = this.receiverDynamicObjectLibrary_;
                    return arg0Value.isHashEntryReadable(arg1Value, objLib__);
                }
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link LuaTable#readHashValue(LuaTable, Object, DynamicObjectLibrary)}
             *     Activation probability: 0.12500
             *     With/without class size: 5/0 bytes
             * </pre> */
            @Override
            public Object readHashValue(Object arg0Value_, Object arg1Value) throws UnsupportedMessageException, UnknownKeyException {
                assert super.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                {
                    DynamicObjectLibrary objLib__ = this.receiverDynamicObjectLibrary_;
                    return arg0Value.readHashValue(arg1Value, objLib__);
                }
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link LuaTable#getHashEntriesIterator(LuaTable, DynamicObjectLibrary)}
             *     Activation probability: 0.12500
             *     With/without class size: 5/0 bytes
             * </pre> */
            @Override
            public Object getHashEntriesIterator(Object arg0Value_) throws UnsupportedMessageException {
                assert super.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                {
                    DynamicObjectLibrary objLib__ = this.receiverDynamicObjectLibrary_;
                    return arg0Value.getHashEntriesIterator(objLib__);
                }
            }

            @GeneratedBy(LuaTable.class)
            @DenyReplace
            private static final class ReadMemberNode_ReadMemberData extends Node implements SpecializationDataNode {

                /**
                 * Source Info: <pre>
                 *   Specialization: {@link LuaTable#readMember(LuaTable, String, LuaCoerceToIntegerNode, FromJavaStringNode, DynamicObjectLibrary)}
                 *   Parameter: {@link LuaCoerceToIntegerNode} coerceNode</pre> */
                @Child LuaCoerceToIntegerNode coerceNode_;
                /**
                 * Source Info: <pre>
                 *   Specialization: {@link LuaTable#readMember(LuaTable, String, LuaCoerceToIntegerNode, FromJavaStringNode, DynamicObjectLibrary)}
                 *   Parameter: {@link FromJavaStringNode} fromJavaStringNode</pre> */
                @Child FromJavaStringNode fromJavaStringNode_;

                ReadMemberNode_ReadMemberData() {
                }

            }
        }
        @GeneratedBy(LuaTable.class)
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
            public boolean hasMembers(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaTable) receiver) .hasMembers();
            }

            @TruffleBoundary
            @Override
            public boolean isMemberModifiable(Object receiver, String member) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaTable) receiver) .isMemberModifiable(member);
            }

            @TruffleBoundary
            @Override
            public boolean isMemberInsertable(Object receiver, String member) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaTable) receiver) .isMemberInsertable(member);
            }

            @TruffleBoundary
            @Override
            public Object getMembers(Object arg0Value_, boolean arg1Value) {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                return arg0Value.getMembers(arg1Value, DYNAMIC_OBJECT_LIBRARY_.getUncached((arg0Value)));
            }

            @TruffleBoundary
            @Override
            public boolean isMemberReadable(Object arg0Value_, String arg1Value) {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                return arg0Value.isMemberReadable(arg1Value, (FromJavaStringNode.getUncached()), DYNAMIC_OBJECT_LIBRARY_.getUncached((arg0Value)));
            }

            @TruffleBoundary
            @Override
            public Object readMember(Object arg0Value_, String arg1Value) throws UnknownIdentifierException {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                return arg0Value.readMember(arg1Value, (LuaCoerceToIntegerNodeGen.getUncached()), (FromJavaStringNode.getUncached()), DYNAMIC_OBJECT_LIBRARY_.getUncached((arg0Value)));
            }

            @TruffleBoundary
            @Override
            public void writeMember(Object arg0Value_, String arg1Value, Object arg2Value) {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                arg0Value.writeMember(arg1Value, arg2Value, (FromJavaStringNode.getUncached()), DYNAMIC_OBJECT_LIBRARY_.getUncached((arg0Value)));
                return;
            }

            @TruffleBoundary
            @Override
            public boolean hasArrayElements(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaTable) receiver) .hasArrayElements();
            }

            @TruffleBoundary
            @Override
            public long getArraySize(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaTable) receiver) .getArraySize();
            }

            @TruffleBoundary
            @Override
            public boolean isArrayElementReadable(Object receiver, long index) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaTable) receiver) .isArrayElementReadable(index);
            }

            @TruffleBoundary
            @Override
            public Object readArrayElement(Object receiver, long index) throws UnsupportedMessageException, InvalidArrayIndexException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaTable) receiver) .readArrayElement(index);
            }

            @TruffleBoundary
            @Override
            public boolean hasHashEntries(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaTable) receiver) .hasHashEntries();
            }

            @TruffleBoundary
            @Override
            public long getHashSize(Object arg0Value_) {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                return arg0Value.getHashSize(DYNAMIC_OBJECT_LIBRARY_.getUncached((arg0Value)));
            }

            @TruffleBoundary
            @Override
            public boolean isHashEntryReadable(Object arg0Value_, Object arg1Value) {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                return arg0Value.isHashEntryReadable(arg1Value, DYNAMIC_OBJECT_LIBRARY_.getUncached((arg0Value)));
            }

            @TruffleBoundary
            @Override
            public Object readHashValue(Object arg0Value_, Object arg1Value) {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                return arg0Value.readHashValue(arg1Value, DYNAMIC_OBJECT_LIBRARY_.getUncached((arg0Value)));
            }

            @TruffleBoundary
            @Override
            public Object getHashEntriesIterator(Object arg0Value_) {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                LuaTable arg0Value = ((LuaTable) arg0Value_);
                return arg0Value.getHashEntriesIterator(DYNAMIC_OBJECT_LIBRARY_.getUncached((arg0Value)));
            }

        }
    }
}
