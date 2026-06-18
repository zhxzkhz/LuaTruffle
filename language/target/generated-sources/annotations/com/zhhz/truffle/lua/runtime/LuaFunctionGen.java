// CheckStyle: start generated
package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.DSLSupport.SpecializationDataNode;
import com.oracle.truffle.api.dsl.InlineSupport.ReferenceField;
import com.oracle.truffle.api.dsl.InlineSupport.UnsafeAccessedField;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.LibraryExport;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.utilities.TriState;
import com.zhhz.truffle.lua.runtime.LuaFunction.Execute;
import com.zhhz.truffle.lua.runtime.LuaFunction.IsIdenticalOrUndefined;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

@GeneratedBy(LuaFunction.class)
@SuppressWarnings({"javadoc", "unused"})
final class LuaFunctionGen {

    static  {
        LibraryExport.register(LuaFunction.class, new InteropLibraryExports());
    }

    private LuaFunctionGen() {
    }

    @GeneratedBy(LuaFunction.class)
    private static final class InteropLibraryExports extends LibraryExport<InteropLibrary> {

        private InteropLibraryExports() {
            super(InteropLibrary.class, LuaFunction.class, false, false, 0);
        }

        @Override
        protected InteropLibrary createUncached(Object receiver) {
            assert receiver instanceof LuaFunction;
            InteropLibrary uncached = new Uncached(receiver);
            return uncached;
        }

        @Override
        protected InteropLibrary createCached(Object receiver) {
            assert receiver instanceof LuaFunction;
            return new Cached(receiver);
        }

        @GeneratedBy(LuaFunction.class)
        private static final class Cached extends LuaValueGen.InteropLibraryExports.Cached {

            static final ReferenceField<ExecuteDirectData> EXECUTE_DIRECT_CACHE_UPDATER = ReferenceField.create(MethodHandles.lookup(), "execute_direct_cache", ExecuteDirectData.class);

            /**
             * State Info: <pre>
             *   0: SpecializationActive {@link IsIdenticalOrUndefined#doLuaFunction}
             *   1: SpecializationActive {@link IsIdenticalOrUndefined#doOther}
             *   2: SpecializationActive {@link Execute#doDirect}
             *   3: SpecializationActive {@link Execute#doIndirect}
             * </pre> */
            @CompilationFinal private int state_0_;
            @UnsafeAccessedField @Child private ExecuteDirectData execute_direct_cache;
            /**
             * Source Info: <pre>
             *   Specialization: {@link Execute#doIndirect}
             *   Parameter: {@link IndirectCallNode} callNode</pre> */
            @Child private IndirectCallNode execute_indirect_callNode_;

            protected Cached(Object receiver) {
                super(receiver);
            }

            @SuppressWarnings("static-method")
            private boolean isIdenticalOrUndefinedFallbackGuard_(int state_0, LuaFunction arg0Value, Object arg1Value) {
                if (!((state_0 & 0b1) != 0 /* is SpecializationActive[LuaFunction.IsIdenticalOrUndefined.doLuaFunction(LuaFunction, LuaFunction)] */) && arg1Value instanceof LuaFunction) {
                    return false;
                }
                return true;
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link IsIdenticalOrUndefined#doLuaFunction}
             *     Activation probability: 0.32500
             *     With/without class size: 7/0 bytes
             *   Specialization {@link IsIdenticalOrUndefined#doOther}
             *     Activation probability: 0.17500
             *     With/without class size: 6/0 bytes
             * </pre> */
            @Override
            protected TriState isIdenticalOrUndefined(Object arg0Value_, Object arg1Value) {
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                LuaFunction arg0Value = ((LuaFunction) arg0Value_);
                int state_0 = this.state_0_;
                if ((state_0 & 0b11) != 0 /* is SpecializationActive[LuaFunction.IsIdenticalOrUndefined.doLuaFunction(LuaFunction, LuaFunction)] || SpecializationActive[LuaFunction.IsIdenticalOrUndefined.doOther(LuaFunction, Object)] */) {
                    if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaFunction.IsIdenticalOrUndefined.doLuaFunction(LuaFunction, LuaFunction)] */ && arg1Value instanceof LuaFunction) {
                        LuaFunction arg1Value_ = (LuaFunction) arg1Value;
                        return IsIdenticalOrUndefined.doLuaFunction(arg0Value, arg1Value_);
                    }
                    if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaFunction.IsIdenticalOrUndefined.doOther(LuaFunction, Object)] */) {
                        if (isIdenticalOrUndefinedFallbackGuard_(state_0, arg0Value, arg1Value)) {
                            return IsIdenticalOrUndefined.doOther(arg0Value, arg1Value);
                        }
                    }
                }
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return isIdenticalOrUndefinedAndSpecialize(arg0Value, arg1Value);
            }

            private TriState isIdenticalOrUndefinedAndSpecialize(LuaFunction arg0Value, Object arg1Value) {
                int state_0 = this.state_0_;
                if (arg1Value instanceof LuaFunction) {
                    LuaFunction arg1Value_ = (LuaFunction) arg1Value;
                    state_0 = state_0 | 0b1 /* add SpecializationActive[LuaFunction.IsIdenticalOrUndefined.doLuaFunction(LuaFunction, LuaFunction)] */;
                    this.state_0_ = state_0;
                    return IsIdenticalOrUndefined.doLuaFunction(arg0Value, arg1Value_);
                }
                state_0 = state_0 | 0b10 /* add SpecializationActive[LuaFunction.IsIdenticalOrUndefined.doOther(LuaFunction, Object)] */;
                this.state_0_ = state_0;
                return IsIdenticalOrUndefined.doOther(arg0Value, arg1Value);
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link Execute#doDirect}
             *     Activation probability: 0.32500
             *     With/without class size: 11/8 bytes
             *   Specialization {@link Execute#doIndirect}
             *     Activation probability: 0.17500
             *     With/without class size: 7/4 bytes
             * </pre> */
            @Override
            public Object execute(Object arg0Value_, Object... arg1Value) throws UnsupportedTypeException, ArityException, UnsupportedMessageException {
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                LuaFunction arg0Value = ((LuaFunction) arg0Value_);
                int state_0 = this.state_0_;
                if ((state_0 & 0b1100) != 0 /* is SpecializationActive[LuaFunction.Execute.doDirect(LuaFunction, Object[], CallTarget, DirectCallNode)] || SpecializationActive[LuaFunction.Execute.doIndirect(LuaFunction, Object[], IndirectCallNode)] */) {
                    if ((state_0 & 0b100) != 0 /* is SpecializationActive[LuaFunction.Execute.doDirect(LuaFunction, Object[], CallTarget, DirectCallNode)] */) {
                        ExecuteDirectData s0_ = this.execute_direct_cache;
                        if (s0_ != null) {
                            if ((arg0Value.getCallTarget() == s0_.cachedTarget_)) {
                                return Execute.doDirect(arg0Value, arg1Value, s0_.cachedTarget_, s0_.callNode_);
                            }
                        }
                    }
                    if ((state_0 & 0b1000) != 0 /* is SpecializationActive[LuaFunction.Execute.doIndirect(LuaFunction, Object[], IndirectCallNode)] */) {
                        {
                            IndirectCallNode callNode__ = this.execute_indirect_callNode_;
                            if (callNode__ != null) {
                                return Execute.doIndirect(arg0Value, arg1Value, callNode__);
                            }
                        }
                    }
                }
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return executeAndSpecialize(arg0Value, arg1Value);
            }

            private Object executeAndSpecialize(LuaFunction arg0Value, Object[] arg1Value) {
                int state_0 = this.state_0_;
                if (((state_0 & 0b1000)) == 0 /* is-not SpecializationActive[LuaFunction.Execute.doIndirect(LuaFunction, Object[], IndirectCallNode)] */) {
                    while (true) {
                        int count0_ = 0;
                        ExecuteDirectData s0_ = EXECUTE_DIRECT_CACHE_UPDATER.getVolatile(this);
                        ExecuteDirectData s0_original = s0_;
                        while (s0_ != null) {
                            if ((arg0Value.getCallTarget() == s0_.cachedTarget_)) {
                                break;
                            }
                            count0_++;
                            s0_ = null;
                            break;
                        }
                        if (s0_ == null && count0_ < 1) {
                            {
                                CallTarget cachedTarget__ = (arg0Value.getCallTarget());
                                if ((arg0Value.getCallTarget() == cachedTarget__)) {
                                    s0_ = this.insert(new ExecuteDirectData());
                                    s0_.cachedTarget_ = cachedTarget__;
                                    s0_.callNode_ = s0_.insert((DirectCallNode.create(cachedTarget__)));
                                    if (!EXECUTE_DIRECT_CACHE_UPDATER.compareAndSet(this, s0_original, s0_)) {
                                        continue;
                                    }
                                    state_0 = state_0 | 0b100 /* add SpecializationActive[LuaFunction.Execute.doDirect(LuaFunction, Object[], CallTarget, DirectCallNode)] */;
                                    this.state_0_ = state_0;
                                }
                            }
                        }
                        if (s0_ != null) {
                            return Execute.doDirect(arg0Value, arg1Value, s0_.cachedTarget_, s0_.callNode_);
                        }
                        break;
                    }
                }
                VarHandle.storeStoreFence();
                this.execute_indirect_callNode_ = this.insert((IndirectCallNode.create()));
                this.execute_direct_cache = null;
                state_0 = state_0 & 0xfffffffb /* remove SpecializationActive[LuaFunction.Execute.doDirect(LuaFunction, Object[], CallTarget, DirectCallNode)] */;
                state_0 = state_0 | 0b1000 /* add SpecializationActive[LuaFunction.Execute.doIndirect(LuaFunction, Object[], IndirectCallNode)] */;
                this.state_0_ = state_0;
                return Execute.doIndirect(arg0Value, arg1Value, this.execute_indirect_callNode_);
            }

            @Override
            public boolean hasLanguage(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaFunction) receiver)).hasLanguage();
            }

            @Override
            public Class<? extends TruffleLanguage<?>> getLanguage(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaFunction) receiver)).getLanguage();
            }

            @Override
            public boolean isExecutable(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaFunction) receiver)).isExecutable();
            }

            @Override
            public boolean hasMetaObject(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaFunction) receiver)).hasMetaObject();
            }

            @Override
            public Object getMetaObject(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaFunction) receiver)).getMetaObject();
            }

            @TruffleBoundary
            @Override
            public int identityHashCode(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return LuaFunction.identityHashCode((((LuaFunction) receiver)));
            }

            @GeneratedBy(LuaFunction.class)
            @DenyReplace
            private static final class ExecuteDirectData extends Node implements SpecializationDataNode {

                /**
                 * Source Info: <pre>
                 *   Specialization: {@link Execute#doDirect}
                 *   Parameter: {@link CallTarget} cachedTarget</pre> */
                @CompilationFinal CallTarget cachedTarget_;
                /**
                 * Source Info: <pre>
                 *   Specialization: {@link Execute#doDirect}
                 *   Parameter: {@link DirectCallNode} callNode</pre> */
                @Child DirectCallNode callNode_;

                ExecuteDirectData() {
                }

            }
        }
        @GeneratedBy(LuaFunction.class)
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
            public TriState isIdenticalOrUndefined(Object arg0Value_, Object arg1Value) {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                LuaFunction arg0Value = ((LuaFunction) arg0Value_);
                if (arg1Value instanceof LuaFunction) {
                    LuaFunction arg1Value_ = (LuaFunction) arg1Value;
                    return IsIdenticalOrUndefined.doLuaFunction(arg0Value, arg1Value_);
                }
                return IsIdenticalOrUndefined.doOther(arg0Value, arg1Value);
            }

            @TruffleBoundary
            @Override
            public Object execute(Object arg0Value_, Object... arg1Value) {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                LuaFunction arg0Value = ((LuaFunction) arg0Value_);
                return Execute.doIndirect(arg0Value, arg1Value, (IndirectCallNode.getUncached()));
            }

            @TruffleBoundary
            @Override
            public boolean hasLanguage(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaFunction) receiver) .hasLanguage();
            }

            @TruffleBoundary
            @Override
            public Class<? extends TruffleLanguage<?>> getLanguage(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaFunction) receiver) .getLanguage();
            }

            @TruffleBoundary
            @Override
            public boolean isExecutable(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaFunction) receiver) .isExecutable();
            }

            @TruffleBoundary
            @Override
            public boolean hasMetaObject(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaFunction) receiver) .hasMetaObject();
            }

            @TruffleBoundary
            @Override
            public Object getMetaObject(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaFunction) receiver) .getMetaObject();
            }

            @TruffleBoundary
            @Override
            public int identityHashCode(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return LuaFunction.identityHashCode(((LuaFunction) receiver) );
            }

        }
    }
}
