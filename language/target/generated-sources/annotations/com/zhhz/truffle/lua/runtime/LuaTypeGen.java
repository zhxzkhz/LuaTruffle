// CheckStyle: start generated
package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.dsl.DSLSupport.SpecializationDataNode;
import com.oracle.truffle.api.dsl.InlineSupport.ReferenceField;
import com.oracle.truffle.api.dsl.InlineSupport.UnsafeAccessedField;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.DynamicDispatchLibrary;
import com.oracle.truffle.api.library.LibraryExport;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.UnadoptableNode;
import com.zhhz.truffle.lua.runtime.LuaType.IsMetaInstance;
import java.lang.invoke.MethodHandles;
import java.util.Objects;

@GeneratedBy(LuaType.class)
@SuppressWarnings("javadoc")
final class LuaTypeGen {

    private static final LibraryFactory<DynamicDispatchLibrary> DYNAMIC_DISPATCH_LIBRARY_ = LibraryFactory.resolve(DynamicDispatchLibrary.class);
    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);

    static  {
        LibraryExport.register(LuaType.class, new InteropLibraryExports());
    }

    private LuaTypeGen() {
    }

    @GeneratedBy(LuaType.class)
    private static final class InteropLibraryExports extends LibraryExport<InteropLibrary> {

        private InteropLibraryExports() {
            super(InteropLibrary.class, LuaType.class, false, false, 0);
        }

        @Override
        protected InteropLibrary createUncached(Object receiver) {
            assert receiver instanceof LuaType;
            InteropLibrary uncached = new Uncached();
            return uncached;
        }

        @Override
        protected InteropLibrary createCached(Object receiver) {
            assert receiver instanceof LuaType;
            return new Cached();
        }

        @GeneratedBy(LuaType.class)
        private static final class Cached extends InteropLibrary {

            static final ReferenceField<CachedData> CACHED_CACHE_UPDATER = ReferenceField.create(MethodHandles.lookup(), "cached_cache", CachedData.class);

            /**
             * State Info: <pre>
             *   0: SpecializationActive {@link IsMetaInstance#doCached}
             *   1: SpecializationActive {@link IsMetaInstance#doGeneric}
             * </pre> */
            @CompilationFinal private int state_0_;
            @UnsafeAccessedField @Child private CachedData cached_cache;

            protected Cached() {
            }

            @Override
            public boolean accepts(Object receiver) {
                assert !(receiver instanceof LuaType) || DYNAMIC_DISPATCH_LIBRARY_.getUncached().dispatch(receiver) == null : "Invalid library export. Exported receiver with dynamic dispatch found but not expected.";
                return receiver instanceof LuaType;
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link IsMetaInstance#doCached}
             *     Activation probability: 0.65000
             *     With/without class size: 22/8 bytes
             *   Specialization {@link IsMetaInstance#doGeneric}
             *     Activation probability: 0.35000
             *     With/without class size: 8/0 bytes
             * </pre> */
            @ExplodeLoop
            @Override
            public boolean isMetaInstance(Object arg0Value_, Object arg1Value) throws UnsupportedMessageException {
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                LuaType arg0Value = ((LuaType) arg0Value_);
                int state_0 = this.state_0_;
                if (state_0 != 0 /* is SpecializationActive[LuaType.IsMetaInstance.doCached(LuaType, Object, LuaType, InteropLibrary)] || SpecializationActive[LuaType.IsMetaInstance.doGeneric(LuaType, Object)] */) {
                    if ((state_0 & 0b1) != 0 /* is SpecializationActive[LuaType.IsMetaInstance.doCached(LuaType, Object, LuaType, InteropLibrary)] */) {
                        CachedData s0_ = this.cached_cache;
                        while (s0_ != null) {
                            if ((s0_.valueLib_.accepts(arg1Value)) && (arg0Value == s0_.cachedType_)) {
                                return IsMetaInstance.doCached(arg0Value, arg1Value, s0_.cachedType_, s0_.valueLib_);
                            }
                            s0_ = s0_.next_;
                        }
                    }
                    if ((state_0 & 0b10) != 0 /* is SpecializationActive[LuaType.IsMetaInstance.doGeneric(LuaType, Object)] */) {
                        return IsMetaInstance.doGeneric(arg0Value, arg1Value);
                    }
                }
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return executeAndSpecialize(arg0Value, arg1Value);
            }

            private boolean executeAndSpecialize(LuaType arg0Value, Object arg1Value) {
                int state_0 = this.state_0_;
                if (((state_0 & 0b10)) == 0 /* is-not SpecializationActive[LuaType.IsMetaInstance.doGeneric(LuaType, Object)] */) {
                    while (true) {
                        int count0_ = 0;
                        CachedData s0_ = CACHED_CACHE_UPDATER.getVolatile(this);
                        CachedData s0_original = s0_;
                        while (s0_ != null) {
                            if ((s0_.valueLib_.accepts(arg1Value)) && (arg0Value == s0_.cachedType_)) {
                                break;
                            }
                            count0_++;
                            s0_ = s0_.next_;
                        }
                        if (s0_ == null) {
                            // assert (s0_.valueLib_.accepts(arg1Value));
                            // assert (arg0Value == s0_.cachedType_);
                            if (count0_ < (3)) {
                                s0_ = this.insert(new CachedData(s0_original));
                                s0_.cachedType_ = (arg0Value);
                                InteropLibrary valueLib__ = s0_.insert((INTEROP_LIBRARY_.create(arg1Value)));
                                Objects.requireNonNull(valueLib__, "A specialization cache returned a default value. The cache initializer must never return a default value for this cache. Use @Cached(neverDefault=false) to allow default values for this cached value or make sure the cache initializer never returns the default value.");
                                s0_.valueLib_ = valueLib__;
                                if (!CACHED_CACHE_UPDATER.compareAndSet(this, s0_original, s0_)) {
                                    continue;
                                }
                                state_0 = state_0 | 0b1 /* add SpecializationActive[LuaType.IsMetaInstance.doCached(LuaType, Object, LuaType, InteropLibrary)] */;
                                this.state_0_ = state_0;
                            }
                        }
                        if (s0_ != null) {
                            return IsMetaInstance.doCached(arg0Value, arg1Value, s0_.cachedType_, s0_.valueLib_);
                        }
                        break;
                    }
                }
                this.cached_cache = null;
                state_0 = state_0 & 0xfffffffe /* remove SpecializationActive[LuaType.IsMetaInstance.doCached(LuaType, Object, LuaType, InteropLibrary)] */;
                state_0 = state_0 | 0b10 /* add SpecializationActive[LuaType.IsMetaInstance.doGeneric(LuaType, Object)] */;
                this.state_0_ = state_0;
                return IsMetaInstance.doGeneric(arg0Value, arg1Value);
            }

            @Override
            public boolean hasLanguage(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaType) receiver)).hasLanguage();
            }

            @Override
            public Class<? extends TruffleLanguage<?>> getLanguage(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaType) receiver)).getLanguage();
            }

            @Override
            public boolean isMetaObject(Object receiver) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaType) receiver)).isMetaObject();
            }

            @Override
            public Object getMetaQualifiedName(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaType) receiver)).getName();
            }

            @Override
            public Object getMetaSimpleName(Object receiver) throws UnsupportedMessageException {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaType) receiver)).getName();
            }

            @Override
            public Object toDisplayString(Object receiver, boolean allowSideEffects) {
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                assert assertAdopted();
                return (((LuaType) receiver)).toDisplayString(allowSideEffects);
            }

            @GeneratedBy(LuaType.class)
            @DenyReplace
            private static final class CachedData extends Node implements SpecializationDataNode {

                @Child CachedData next_;
                /**
                 * Source Info: <pre>
                 *   Specialization: {@link IsMetaInstance#doCached}
                 *   Parameter: {@link LuaType} cachedType</pre> */
                @CompilationFinal LuaType cachedType_;
                /**
                 * Source Info: <pre>
                 *   Specialization: {@link IsMetaInstance#doCached}
                 *   Parameter: {@link InteropLibrary} valueLib</pre> */
                @Child InteropLibrary valueLib_;

                CachedData(CachedData next_) {
                    this.next_ = next_;
                }

            }
        }
        @GeneratedBy(LuaType.class)
        @DenyReplace
        private static final class Uncached extends InteropLibrary implements UnadoptableNode {

            protected Uncached() {
            }

            @Override
            @TruffleBoundary
            public boolean accepts(Object receiver) {
                assert !(receiver instanceof LuaType) || DYNAMIC_DISPATCH_LIBRARY_.getUncached().dispatch(receiver) == null : "Invalid library export. Exported receiver with dynamic dispatch found but not expected.";
                return receiver instanceof LuaType;
            }

            @TruffleBoundary
            @Override
            public boolean isMetaInstance(Object arg0Value_, Object arg1Value) {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                LuaType arg0Value = ((LuaType) arg0Value_);
                return IsMetaInstance.doGeneric(arg0Value, arg1Value);
            }

            @TruffleBoundary
            @Override
            public boolean hasLanguage(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaType) receiver) .hasLanguage();
            }

            @TruffleBoundary
            @Override
            public Class<? extends TruffleLanguage<?>> getLanguage(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaType) receiver) .getLanguage();
            }

            @TruffleBoundary
            @Override
            public boolean isMetaObject(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaType) receiver) .isMetaObject();
            }

            @TruffleBoundary
            @Override
            public Object getMetaQualifiedName(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaType) receiver) .getName();
            }

            @TruffleBoundary
            @Override
            public Object getMetaSimpleName(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaType) receiver) .getName();
            }

            @TruffleBoundary
            @Override
            public Object toDisplayString(Object receiver, boolean allowSideEffects) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaType) receiver) .toDisplayString(allowSideEffects);
            }

        }
    }
}
