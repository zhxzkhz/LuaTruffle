// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.access;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.DSLSupport.SpecializationDataNode;
import com.oracle.truffle.api.dsl.InlineSupport.InlineTarget;
import com.oracle.truffle.api.dsl.InlineSupport.ReferenceField;
import com.oracle.truffle.api.dsl.InlineSupport.StateField;
import com.oracle.truffle.api.dsl.InlineSupport.UnsafeAccessedField;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.EncapsulatingNodeReference;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.util.LuaToMemberNode;
import com.zhhz.truffle.lua.nodes.util.LuaToMemberNodeGen;
import com.zhhz.truffle.lua.runtime.LuaTable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaReadTableNode#readArray}
 *     Activation probability: 0.27381
 *     With/without class size: 7/0 bytes
 *   Specialization {@link LuaReadTableNode#readStringProperty}
 *     Activation probability: 0.23095
 *     With/without class size: 9/4 bytes
 *   Specialization {@link LuaReadTableNode#readObjectProperty}
 *     Activation probability: 0.18810
 *     With/without class size: 8/4 bytes
 *   Specialization {@link LuaReadTableNode#doHostObject}
 *     Activation probability: 0.14524
 *     With/without class size: 9/13 bytes
 *   Specialization {@link LuaReadTableNode#doHostObject}
 *     Activation probability: 0.10238
 *     With/without class size: 6/9 bytes
 *   Specialization {@link LuaReadTableNode#doNonTable}
 *     Activation probability: 0.05952
 *     With/without class size: 4/0 bytes
 * </pre> */
@GeneratedBy(LuaReadTableNode.class)
@SuppressWarnings({"javadoc", "unused"})
public final class LuaReadTableNodeGen extends LuaReadTableNode {

    private static final StateField HOST_OBJECT0_LUA_READ_TABLE_NODE_HOST_OBJECT0_STATE_0_UPDATER = StateField.create(HostObject0Data.lookup_(), "hostObject0_state_0_");
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaReadTableNode#doHostObject}
     *   Parameter: {@link LuaToMemberNode} asMember
     *   Inline method: {@link LuaToMemberNodeGen#inline}</pre> */
    private static final LuaToMemberNode INLINED_HOST_OBJECT0_AS_MEMBER_ = LuaToMemberNodeGen.inline(InlineTarget.create(LuaToMemberNode.class, HOST_OBJECT0_LUA_READ_TABLE_NODE_HOST_OBJECT0_STATE_0_UPDATER.subUpdater(0, 6), ReferenceField.create(HostObject0Data.lookup_(), "hostObject0_asMember__field1_", Node.class), ReferenceField.create(HostObject0Data.lookup_(), "hostObject0_asMember__field2_", Node.class)));
    private static final StateField HOST_OBJECT1_LUA_READ_TABLE_NODE_HOST_OBJECT1_STATE_0_UPDATER = StateField.create(HostObject1Data.lookup_(), "hostObject1_state_0_");
    /**
     * Source Info: <pre>
     *   Specialization: {@link LuaReadTableNode#doHostObject}
     *   Parameter: {@link LuaToMemberNode} asMember
     *   Inline method: {@link LuaToMemberNodeGen#inline}</pre> */
    private static final LuaToMemberNode INLINED_HOST_OBJECT1_AS_MEMBER_ = LuaToMemberNodeGen.inline(InlineTarget.create(LuaToMemberNode.class, HOST_OBJECT1_LUA_READ_TABLE_NODE_HOST_OBJECT1_STATE_0_UPDATER.subUpdater(0, 6), ReferenceField.create(HostObject1Data.lookup_(), "hostObject1_asMember__field1_", Node.class), ReferenceField.create(HostObject1Data.lookup_(), "hostObject1_asMember__field2_", Node.class)));
    static final ReferenceField<ReadStringPropertyData> READ_STRING_PROPERTY_CACHE_UPDATER = ReferenceField.create(MethodHandles.lookup(), "readStringProperty_cache", ReadStringPropertyData.class);
    static final ReferenceField<ReadObjectPropertyData> READ_OBJECT_PROPERTY_CACHE_UPDATER = ReferenceField.create(MethodHandles.lookup(), "readObjectProperty_cache", ReadObjectPropertyData.class);
    static final ReferenceField<HostObject0Data> HOST_OBJECT0_CACHE_UPDATER = ReferenceField.create(MethodHandles.lookup(), "hostObject0_cache", HostObject0Data.class);
    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);
    private static final LibraryFactory<DynamicObjectLibrary> DYNAMIC_OBJECT_LIBRARY_ = LibraryFactory.resolve(DynamicObjectLibrary.class);

    @Child private LuaExpressionNode tableNode_;
    @Child private LuaExpressionNode keyNode_;
    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaReadTableNode#readArray}
     *   1: SpecializationActive {@link LuaReadTableNode#readStringProperty}
     *   2: SpecializationActive {@link LuaReadTableNode#readObjectProperty}
     *   3: SpecializationActive {@link LuaReadTableNode#doHostObject}
     *   4: SpecializationActive {@link LuaReadTableNode#doHostObject}
     *   5: SpecializationActive {@link LuaReadTableNode#doNonTable}
     * </pre> */
    @CompilationFinal private int state_0_;
    @UnsafeAccessedField @Child private ReadStringPropertyData readStringProperty_cache;
    @UnsafeAccessedField @Child private ReadObjectPropertyData readObjectProperty_cache;
    @UnsafeAccessedField @Child private HostObject0Data hostObject0_cache;
    @Child private HostObject1Data hostObject1_cache;

    private LuaReadTableNodeGen(LuaExpressionNode tableNode, LuaExpressionNode keyNode) {
        this.tableNode_ = tableNode;
        this.keyNode_ = keyNode;
    }

    @Override
    protected LuaExpressionNode getTableNode() {
        return this.tableNode_;
    }

    @Override
    protected LuaExpressionNode getKeyNode() {
        return this.keyNode_;
    }

    @SuppressWarnings("static-method")
    private boolean fallbackGuard_(int state_0, Object tableNodeValue, Object keyNodeValue) {
        if (!((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] */) && tableNodeValue instanceof TruffleObject) {
            return false;
        }
        return true;
    }

    @Override
    public Object executeGeneric(VirtualFrame frameValue) {
        int state_0 = this.state_0_;
        if ((state_0 & 0b111110) == 0 /* only-active SpecializationActive[LuaReadTableNode.readArray(LuaTable, long)] */ && (state_0 != 0  /* is-not SpecializationActive[LuaReadTableNode.readArray(LuaTable, long)] && SpecializationActive[LuaReadTableNode.readStringProperty(LuaTable, String, DynamicObjectLibrary)] && SpecializationActive[LuaReadTableNode.readObjectProperty(LuaTable, Object, DynamicObjectLibrary)] && SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] && SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] && SpecializationActive[LuaReadTableNode.doNonTable(Object, Object)] */)) {
            return executeGeneric_long0(state_0, frameValue);
        } else {
            return executeGeneric_generic1(state_0, frameValue);
        }
    }

    private Object executeGeneric_long0(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object tableNodeValue_ = this.tableNode_.executeGeneric(frameValue);
        long keyNodeValue_;
        try {
            keyNodeValue_ = this.keyNode_.executeLong(frameValue);
        } catch (UnexpectedResultException ex) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return executeAndSpecialize(tableNodeValue_, ex.getResult());
        }
        assert (state_0 & 0b1) != 0 /* is SpecializationActive[LuaReadTableNode.readArray(LuaTable, long)] */;
        if (tableNodeValue_ instanceof LuaTable) {
            LuaTable tableNodeValue__ = (LuaTable) tableNodeValue_;
            if ((LuaReadTableNode.isArrayIndex(keyNodeValue_))) {
                return readArray(tableNodeValue__, keyNodeValue_);
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(tableNodeValue_, keyNodeValue_);
    }

    @SuppressWarnings("static-method")
    @TruffleBoundary
    private Object hostObject1Boundary(int state_0, HostObject1Data s4_, TruffleObject tableNodeValue__, Object keyNodeValue_) {
        EncapsulatingNodeReference encapsulating_ = EncapsulatingNodeReference.getCurrent();
        Node prev_ = encapsulating_.set(this);
        try {
            {
                Node node__ = (s4_);
                InteropLibrary objects__ = (INTEROP_LIBRARY_.getUncached(tableNodeValue__));
                return LuaReadTableNode.doHostObject(tableNodeValue__, keyNodeValue_, node__, objects__, INLINED_HOST_OBJECT1_AS_MEMBER_);
            }
        } finally {
            encapsulating_.set(prev_);
        }
    }

    @ExplodeLoop
    private Object executeGeneric_generic1(int state_0__, VirtualFrame frameValue) {
        int state_0 = state_0__;
        Object tableNodeValue_ = this.tableNode_.executeGeneric(frameValue);
        Object keyNodeValue_ = this.keyNode_.executeGeneric(frameValue);
        if (state_0 != 0 /* is SpecializationActive[LuaReadTableNode.readArray(LuaTable, long)] || SpecializationActive[LuaReadTableNode.readStringProperty(LuaTable, String, DynamicObjectLibrary)] || SpecializationActive[LuaReadTableNode.readObjectProperty(LuaTable, Object, DynamicObjectLibrary)] || SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] || SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] || SpecializationActive[LuaReadTableNode.doNonTable(Object, Object)] */) {
            if ((state_0 & 0b111) != 0 /* is SpecializationActive[LuaReadTableNode.readArray(LuaTable, long)] || SpecializationActive[LuaReadTableNode.readStringProperty(LuaTable, String, DynamicObjectLibrary)] || SpecializationActive[LuaReadTableNode.readObjectProperty(LuaTable, Object, DynamicObjectLibrary)] */ && tableNodeValue_ instanceof LuaTable) {
                LuaTable tableNodeValue__ = (LuaTable) tableNodeValue_;
                if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaReadTableNode.readArray(LuaTable, long)] */ && keyNodeValue_ instanceof Long) {
                    long keyNodeValue__ = (long) keyNodeValue_;
                    if ((LuaReadTableNode.isArrayIndex(keyNodeValue__))) {
                        return readArray(tableNodeValue__, keyNodeValue__);
                    }
                }
                if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaReadTableNode.readStringProperty(LuaTable, String, DynamicObjectLibrary)] */ && keyNodeValue_ instanceof String) {
                    String keyNodeValue__ = (String) keyNodeValue_;
                    ReadStringPropertyData s1_ = this.readStringProperty_cache;
                    while (s1_ != null) {
                        if ((s1_.objLib_.accepts(tableNodeValue__))) {
                            return readStringProperty(tableNodeValue__, keyNodeValue__, s1_.objLib_);
                        }
                        s1_ = s1_.next_;
                    }
                }
                if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaReadTableNode.readObjectProperty(LuaTable, Object, DynamicObjectLibrary)] */) {
                    ReadObjectPropertyData s2_ = this.readObjectProperty_cache;
                    while (s2_ != null) {
                        if ((s2_.objLib_.accepts(tableNodeValue__))) {
                            return readObjectProperty(tableNodeValue__, keyNodeValue_, s2_.objLib_);
                        }
                        s2_ = s2_.next_;
                    }
                }
            }
            if ((state_0 & 0b111000) != 0 /* is SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] || SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] || SpecializationActive[LuaReadTableNode.doNonTable(Object, Object)] */) {
                if ((state_0 & 0b11000) != 0 /* is SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] || SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] */ && tableNodeValue_ instanceof TruffleObject) {
                    TruffleObject tableNodeValue__ = (TruffleObject) tableNodeValue_;
                    if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] */) {
                        HostObject0Data s3_ = this.hostObject0_cache;
                        while (s3_ != null) {
                            if ((s3_.objects_.accepts(tableNodeValue__))) {
                                Node node__ = (s3_);
                                return LuaReadTableNode.doHostObject(tableNodeValue__, keyNodeValue_, node__, s3_.objects_, INLINED_HOST_OBJECT0_AS_MEMBER_);
                            }
                            s3_ = s3_.next_;
                        }
                    }
                    if ((state_0 & 0b10000) != 0 /* is SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] */) {
                        HostObject1Data s4_ = this.hostObject1_cache;
                        if (s4_ != null) {
                            return this.hostObject1Boundary(state_0, s4_, tableNodeValue__, keyNodeValue_);
                        }
                    }
                }
                if ((state_0 & 0b100000) != 0 /* is SpecializationActive[LuaReadTableNode.doNonTable(Object, Object)] */) {
                    if (fallbackGuard_(state_0, tableNodeValue_, keyNodeValue_)) {
                        return doNonTable(tableNodeValue_, keyNodeValue_);
                    }
                }
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        return executeAndSpecialize(tableNodeValue_, keyNodeValue_);
    }

    @Override
    public void executeVoid(VirtualFrame frameValue) {
        this.executeGeneric(frameValue);
        return;
    }

    private Object executeAndSpecialize(Object tableNodeValue, Object keyNodeValue) {
        int state_0 = this.state_0_;
        if (tableNodeValue instanceof LuaTable) {
            LuaTable tableNodeValue_ = (LuaTable) tableNodeValue;
            if (((state_0 & 0b11110)) == 0 /* is-not SpecializationActive[LuaReadTableNode.readStringProperty(LuaTable, String, DynamicObjectLibrary)] && SpecializationActive[LuaReadTableNode.readObjectProperty(LuaTable, Object, DynamicObjectLibrary)] && SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] && SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] */ && keyNodeValue instanceof Long) {
                long keyNodeValue_ = (long) keyNodeValue;
                if ((LuaReadTableNode.isArrayIndex(keyNodeValue_))) {
                    state_0 = state_0 | 0b1 /* add SpecializationActive[LuaReadTableNode.readArray(LuaTable, long)] */;
                    this.state_0_ = state_0;
                    return readArray(tableNodeValue_, keyNodeValue_);
                }
            }
            if (((state_0 & 0b11100)) == 0 /* is-not SpecializationActive[LuaReadTableNode.readObjectProperty(LuaTable, Object, DynamicObjectLibrary)] && SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] && SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] */ && keyNodeValue instanceof String) {
                String keyNodeValue_ = (String) keyNodeValue;
                while (true) {
                    int count1_ = 0;
                    ReadStringPropertyData s1_ = READ_STRING_PROPERTY_CACHE_UPDATER.getVolatile(this);
                    ReadStringPropertyData s1_original = s1_;
                    while (s1_ != null) {
                        if ((s1_.objLib_.accepts(tableNodeValue_))) {
                            break;
                        }
                        count1_++;
                        s1_ = s1_.next_;
                    }
                    if (s1_ == null) {
                        // assert (s1_.objLib_.accepts(tableNodeValue_));
                        if (count1_ < (LuaReadTableNode.CACHE_LIMIT)) {
                            s1_ = this.insert(new ReadStringPropertyData(s1_original));
                            DynamicObjectLibrary objLib__ = s1_.insert((DYNAMIC_OBJECT_LIBRARY_.create(tableNodeValue_)));
                            Objects.requireNonNull(objLib__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                            s1_.objLib_ = objLib__;
                            if (!READ_STRING_PROPERTY_CACHE_UPDATER.compareAndSet(this, s1_original, s1_)) {
                                continue;
                            }
                            state_0 = state_0 & 0xfffffffe /* remove SpecializationActive[LuaReadTableNode.readArray(LuaTable, long)] */;
                            state_0 = state_0 | 0b10 /* add SpecializationActive[LuaReadTableNode.readStringProperty(LuaTable, String, DynamicObjectLibrary)] */;
                            this.state_0_ = state_0;
                        }
                    }
                    if (s1_ != null) {
                        return readStringProperty(tableNodeValue_, keyNodeValue_, s1_.objLib_);
                    }
                    break;
                }
            }
            if (((state_0 & 0b11000)) == 0 /* is-not SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] && SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] */) {
                while (true) {
                    int count2_ = 0;
                    ReadObjectPropertyData s2_ = READ_OBJECT_PROPERTY_CACHE_UPDATER.getVolatile(this);
                    ReadObjectPropertyData s2_original = s2_;
                    while (s2_ != null) {
                        if ((s2_.objLib_.accepts(tableNodeValue_))) {
                            break;
                        }
                        count2_++;
                        s2_ = s2_.next_;
                    }
                    if (s2_ == null) {
                        // assert (s2_.objLib_.accepts(tableNodeValue_));
                        if (count2_ < (LuaReadTableNode.CACHE_LIMIT)) {
                            s2_ = this.insert(new ReadObjectPropertyData(s2_original));
                            DynamicObjectLibrary objLib__1 = s2_.insert((DYNAMIC_OBJECT_LIBRARY_.create(tableNodeValue_)));
                            Objects.requireNonNull(objLib__1, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                            s2_.objLib_ = objLib__1;
                            if (!READ_OBJECT_PROPERTY_CACHE_UPDATER.compareAndSet(this, s2_original, s2_)) {
                                continue;
                            }
                            this.readStringProperty_cache = null;
                            state_0 = state_0 & 0xfffffffc /* remove SpecializationActive[LuaReadTableNode.readArray(LuaTable, long)], SpecializationActive[LuaReadTableNode.readStringProperty(LuaTable, String, DynamicObjectLibrary)] */;
                            state_0 = state_0 | 0b100 /* add SpecializationActive[LuaReadTableNode.readObjectProperty(LuaTable, Object, DynamicObjectLibrary)] */;
                            this.state_0_ = state_0;
                        }
                    }
                    if (s2_ != null) {
                        return readObjectProperty(tableNodeValue_, keyNodeValue, s2_.objLib_);
                    }
                    break;
                }
            }
        }
        if (tableNodeValue instanceof TruffleObject) {
            TruffleObject tableNodeValue_ = (TruffleObject) tableNodeValue;
            {
                Node node__ = null;
                if (((state_0 & 0b10000)) == 0 /* is-not SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] */) {
                    while (true) {
                        int count3_ = 0;
                        HostObject0Data s3_ = HOST_OBJECT0_CACHE_UPDATER.getVolatile(this);
                        HostObject0Data s3_original = s3_;
                        while (s3_ != null) {
                            if ((s3_.objects_.accepts(tableNodeValue_))) {
                                node__ = (s3_);
                                break;
                            }
                            count3_++;
                            s3_ = s3_.next_;
                        }
                        if (s3_ == null) {
                            // assert (s3_.objects_.accepts(tableNodeValue_));
                            if (count3_ < (LuaReadTableNode.CACHE_LIMIT)) {
                                s3_ = this.insert(new HostObject0Data(s3_original));
                                node__ = (s3_);
                                InteropLibrary objects__ = s3_.insert((INTEROP_LIBRARY_.create(tableNodeValue_)));
                                Objects.requireNonNull(objects__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                                s3_.objects_ = objects__;
                                if (!HOST_OBJECT0_CACHE_UPDATER.compareAndSet(this, s3_original, s3_)) {
                                    continue;
                                }
                                this.readStringProperty_cache = null;
                                this.readObjectProperty_cache = null;
                                state_0 = state_0 & 0xfffffff8 /* remove SpecializationActive[LuaReadTableNode.readArray(LuaTable, long)], SpecializationActive[LuaReadTableNode.readStringProperty(LuaTable, String, DynamicObjectLibrary)], SpecializationActive[LuaReadTableNode.readObjectProperty(LuaTable, Object, DynamicObjectLibrary)] */;
                                state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] */;
                                this.state_0_ = state_0;
                            }
                        }
                        if (s3_ != null) {
                            return LuaReadTableNode.doHostObject(tableNodeValue_, keyNodeValue, node__, s3_.objects_, INLINED_HOST_OBJECT0_AS_MEMBER_);
                        }
                        break;
                    }
                }
            }
            {
                InteropLibrary objects__ = null;
                Node node__ = null;
                {
                    EncapsulatingNodeReference encapsulating_ = EncapsulatingNodeReference.getCurrent();
                    Node prev_ = encapsulating_.set(this);
                    try {
                        HostObject1Data s4_ = this.insert(new HostObject1Data());
                        node__ = (s4_);
                        objects__ = (INTEROP_LIBRARY_.getUncached(tableNodeValue_));
                        VarHandle.storeStoreFence();
                        this.hostObject1_cache = s4_;
                        this.readStringProperty_cache = null;
                        this.readObjectProperty_cache = null;
                        this.hostObject0_cache = null;
                        state_0 = state_0 & 0xfffffff0 /* remove SpecializationActive[LuaReadTableNode.readArray(LuaTable, long)], SpecializationActive[LuaReadTableNode.readStringProperty(LuaTable, String, DynamicObjectLibrary)], SpecializationActive[LuaReadTableNode.readObjectProperty(LuaTable, Object, DynamicObjectLibrary)], SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] */;
                        state_0 = state_0 | 0b10000 /* add SpecializationActive[LuaReadTableNode.doHostObject(TruffleObject, Object, Node, InteropLibrary, LuaToMemberNode)] */;
                        this.state_0_ = state_0;
                        return LuaReadTableNode.doHostObject(tableNodeValue_, keyNodeValue, node__, objects__, INLINED_HOST_OBJECT1_AS_MEMBER_);
                    } finally {
                        encapsulating_.set(prev_);
                    }
                }
            }
        }
        state_0 = state_0 | 0b100000 /* add SpecializationActive[LuaReadTableNode.doNonTable(Object, Object)] */;
        this.state_0_ = state_0;
        return doNonTable(tableNodeValue, keyNodeValue);
    }

    @NeverDefault
    public static LuaReadTableNode create(LuaExpressionNode tableNode, LuaExpressionNode keyNode) {
        return new LuaReadTableNodeGen(tableNode, keyNode);
    }

    @GeneratedBy(LuaReadTableNode.class)
    @DenyReplace
    private static final class ReadStringPropertyData extends Node implements SpecializationDataNode {

        @Child ReadStringPropertyData next_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaReadTableNode#readStringProperty}
         *   Parameter: {@link DynamicObjectLibrary} objLib</pre> */
        @Child DynamicObjectLibrary objLib_;

        ReadStringPropertyData(ReadStringPropertyData next_) {
            this.next_ = next_;
        }

    }
    @GeneratedBy(LuaReadTableNode.class)
    @DenyReplace
    private static final class ReadObjectPropertyData extends Node implements SpecializationDataNode {

        @Child ReadObjectPropertyData next_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaReadTableNode#readObjectProperty}
         *   Parameter: {@link DynamicObjectLibrary} objLib</pre> */
        @Child DynamicObjectLibrary objLib_;

        ReadObjectPropertyData(ReadObjectPropertyData next_) {
            this.next_ = next_;
        }

    }
    @GeneratedBy(LuaReadTableNode.class)
    @DenyReplace
    private static final class HostObject0Data extends Node implements SpecializationDataNode {

        @Child HostObject0Data next_;
        /**
         * State Info: <pre>
         *   0-5: InlinedCache
         *        Specialization: {@link LuaReadTableNode#doHostObject}
         *        Parameter: {@link LuaToMemberNode} asMember
         *        Inline method: {@link LuaToMemberNodeGen#inline}
         * </pre> */
        @CompilationFinal @UnsafeAccessedField private int hostObject0_state_0_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaReadTableNode#doHostObject}
         *   Parameter: {@link InteropLibrary} objects</pre> */
        @Child InteropLibrary objects_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaReadTableNode#doHostObject}
         *   Parameter: {@link LuaToMemberNode} asMember
         *   Inline method: {@link LuaToMemberNodeGen#inline}
         *   Inline field: {@link Node} field1</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node hostObject0_asMember__field1_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaReadTableNode#doHostObject}
         *   Parameter: {@link LuaToMemberNode} asMember
         *   Inline method: {@link LuaToMemberNodeGen#inline}
         *   Inline field: {@link Node} field2</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node hostObject0_asMember__field2_;

        HostObject0Data(HostObject0Data next_) {
            this.next_ = next_;
        }

        private static Lookup lookup_() {
            return MethodHandles.lookup();
        }

    }
    @GeneratedBy(LuaReadTableNode.class)
    @DenyReplace
    private static final class HostObject1Data extends Node implements SpecializationDataNode {

        /**
         * State Info: <pre>
         *   0-5: InlinedCache
         *        Specialization: {@link LuaReadTableNode#doHostObject}
         *        Parameter: {@link LuaToMemberNode} asMember
         *        Inline method: {@link LuaToMemberNodeGen#inline}
         * </pre> */
        @CompilationFinal @UnsafeAccessedField private int hostObject1_state_0_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaReadTableNode#doHostObject}
         *   Parameter: {@link LuaToMemberNode} asMember
         *   Inline method: {@link LuaToMemberNodeGen#inline}
         *   Inline field: {@link Node} field1</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node hostObject1_asMember__field1_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaReadTableNode#doHostObject}
         *   Parameter: {@link LuaToMemberNode} asMember
         *   Inline method: {@link LuaToMemberNodeGen#inline}
         *   Inline field: {@link Node} field2</pre> */
        @Child @UnsafeAccessedField @SuppressWarnings("unused") private Node hostObject1_asMember__field2_;

        HostObject1Data() {
        }

        private static Lookup lookup_() {
            return MethodHandles.lookup();
        }

    }
}
