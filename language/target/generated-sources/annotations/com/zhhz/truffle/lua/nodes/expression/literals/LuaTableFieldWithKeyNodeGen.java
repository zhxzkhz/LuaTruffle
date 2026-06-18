// CheckStyle: start generated
package com.zhhz.truffle.lua.nodes.expression.literals;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.DSLSupport.SpecializationDataNode;
import com.oracle.truffle.api.dsl.InlineSupport.ReferenceField;
import com.oracle.truffle.api.dsl.InlineSupport.UnsafeAccessedField;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.runtime.LuaTable;
import java.lang.invoke.MethodHandles;
import java.util.Objects;

/**
 * Debug Info: <pre>
 *   Specialization {@link LuaTableFieldWithKeyNode#writeObjectProperty}
 *     Activation probability: 0.65000
 *     With/without class size: 19/4 bytes
 *   Specialization {@link LuaTableFieldWithKeyNode#writeObjectProperty}
 *     Activation probability: 0.35000
 *     With/without class size: 8/0 bytes
 * </pre> */
@GeneratedBy(LuaTableFieldWithKeyNode.class)
@SuppressWarnings({"javadoc", "unused"})
public final class LuaTableFieldWithKeyNodeGen extends LuaTableFieldWithKeyNode {

    static final ReferenceField<WriteObjectProperty0Data> WRITE_OBJECT_PROPERTY0_CACHE_UPDATER = ReferenceField.create(MethodHandles.lookup(), "writeObjectProperty0_cache", WriteObjectProperty0Data.class);
    private static final LibraryFactory<DynamicObjectLibrary> DYNAMIC_OBJECT_LIBRARY_ = LibraryFactory.resolve(DynamicObjectLibrary.class);

    /**
     * State Info: <pre>
     *   0: SpecializationActive {@link LuaTableFieldWithKeyNode#writeObjectProperty}
     *   1: SpecializationActive {@link LuaTableFieldWithKeyNode#writeObjectProperty}
     * </pre> */
    @CompilationFinal private int state_0_;
    @UnsafeAccessedField @Child private WriteObjectProperty0Data writeObjectProperty0_cache;

    private LuaTableFieldWithKeyNodeGen(LuaExpressionNode keyNode, LuaExpressionNode valueNode) {
        super(keyNode, valueNode);
    }

    @ExplodeLoop
    @Override
    protected void executeWithTable(VirtualFrame frameValue, LuaTable arg0Value, Object arg1Value, Object arg2Value) {
        int state_0 = this.state_0_;
        if (state_0 != 0 /* is SpecializationActive[LuaTableFieldWithKeyNode.writeObjectProperty(LuaTable, Object, Object, DynamicObjectLibrary)] || SpecializationActive[LuaTableFieldWithKeyNode.writeObjectProperty(LuaTable, Object, Object, DynamicObjectLibrary)] */) {
            if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaTableFieldWithKeyNode.writeObjectProperty(LuaTable, Object, Object, DynamicObjectLibrary)] */) {
                WriteObjectProperty0Data s0_ = this.writeObjectProperty0_cache;
                while (s0_ != null) {
                    if ((s0_.objLib_.accepts(arg0Value.getProperties()))) {
                        writeObjectProperty(arg0Value, arg1Value, arg2Value, s0_.objLib_);
                        return;
                    }
                    s0_ = s0_.next_;
                }
            }
            if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaTableFieldWithKeyNode.writeObjectProperty(LuaTable, Object, Object, DynamicObjectLibrary)] */) {
                this.writeObjectProperty1Boundary(state_0, arg0Value, arg1Value, arg2Value);
                return;
            }
        }
        CompilerDirectives.transferToInterpreterAndInvalidate();
        executeAndSpecialize(arg0Value, arg1Value, arg2Value);
        return;
    }

    @SuppressWarnings("static-method")
    @TruffleBoundary
    private void writeObjectProperty1Boundary(int state_0, LuaTable arg0Value, Object arg1Value, Object arg2Value) {
        {
            DynamicObjectLibrary objLib__ = (DYNAMIC_OBJECT_LIBRARY_.getUncached(arg0Value.getProperties()));
            writeObjectProperty(arg0Value, arg1Value, arg2Value, objLib__);
            return;
        }
    }

    private void executeAndSpecialize(LuaTable arg0Value, Object arg1Value, Object arg2Value) {
        int state_0 = this.state_0_;
        if (((state_0 & 0b10)) == 0 /* is-not SpecializationActive[LuaTableFieldWithKeyNode.writeObjectProperty(LuaTable, Object, Object, DynamicObjectLibrary)] */) {
            while (true) {
                int count0_ = 0;
                WriteObjectProperty0Data s0_ = WRITE_OBJECT_PROPERTY0_CACHE_UPDATER.getVolatile(this);
                WriteObjectProperty0Data s0_original = s0_;
                while (s0_ != null) {
                    if ((s0_.objLib_.accepts(arg0Value.getProperties()))) {
                        break;
                    }
                    count0_++;
                    s0_ = s0_.next_;
                }
                if (s0_ == null) {
                    // assert (s0_.objLib_.accepts(arg0Value.getProperties()));
                    if (count0_ < (LuaTableFieldWithKeyNode.CACHE_LIMIT)) {
                        s0_ = this.insert(new WriteObjectProperty0Data(s0_original));
                        DynamicObjectLibrary objLib__ = s0_.insert((DYNAMIC_OBJECT_LIBRARY_.create(arg0Value.getProperties())));
                        Objects.requireNonNull(objLib__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                        s0_.objLib_ = objLib__;
                        if (!WRITE_OBJECT_PROPERTY0_CACHE_UPDATER.compareAndSet(this, s0_original, s0_)) {
                            continue;
                        }
                        state_0 = state_0 | 0b1 /* add SpecializationActive[LuaTableFieldWithKeyNode.writeObjectProperty(LuaTable, Object, Object, DynamicObjectLibrary)] */;
                        this.state_0_ = state_0;
                    }
                }
                if (s0_ != null) {
                    writeObjectProperty(arg0Value, arg1Value, arg2Value, s0_.objLib_);
                    return;
                }
                break;
            }
        }
        {
            DynamicObjectLibrary objLib__ = null;
            objLib__ = (DYNAMIC_OBJECT_LIBRARY_.getUncached(arg0Value.getProperties()));
            this.writeObjectProperty0_cache = null;
            state_0 = state_0 & 0xfffffffe /* remove SpecializationActive[LuaTableFieldWithKeyNode.writeObjectProperty(LuaTable, Object, Object, DynamicObjectLibrary)] */;
            state_0 = state_0 | 0b10 /* add SpecializationActive[LuaTableFieldWithKeyNode.writeObjectProperty(LuaTable, Object, Object, DynamicObjectLibrary)] */;
            this.state_0_ = state_0;
            writeObjectProperty(arg0Value, arg1Value, arg2Value, objLib__);
            return;
        }
    }

    @NeverDefault
    public static LuaTableFieldWithKeyNode create(LuaExpressionNode keyNode, LuaExpressionNode valueNode) {
        return new LuaTableFieldWithKeyNodeGen(keyNode, valueNode);
    }

    @GeneratedBy(LuaTableFieldWithKeyNode.class)
    @DenyReplace
    private static final class WriteObjectProperty0Data extends Node implements SpecializationDataNode {

        @Child WriteObjectProperty0Data next_;
        /**
         * Source Info: <pre>
         *   Specialization: {@link LuaTableFieldWithKeyNode#writeObjectProperty}
         *   Parameter: {@link DynamicObjectLibrary} objLib</pre> */
        @Child DynamicObjectLibrary objLib_;

        WriteObjectProperty0Data(WriteObjectProperty0Data next_) {
            this.next_ = next_;
        }

    }
}
