// CheckStyle: start generated
package com.zhhz.truffle.lua.runtime;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.DynamicDispatchLibrary;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryExport;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.UnadoptableNode;
import com.oracle.truffle.api.utilities.FinalBitSet;

@GeneratedBy(LuaLanguageView.class)
@SuppressWarnings("javadoc")
final class LuaLanguageViewGen {

    private static final LibraryFactory<InteropLibrary> INTEROP_LIBRARY_ = LibraryFactory.resolve(InteropLibrary.class);
    private static final LibraryFactory<DynamicDispatchLibrary> DYNAMIC_DISPATCH_LIBRARY_ = LibraryFactory.resolve(DynamicDispatchLibrary.class);

    static  {
        LibraryExport.register(LuaLanguageView.class, new InteropLibraryExports());
    }

    private LuaLanguageViewGen() {
    }

    @GeneratedBy(LuaLanguageView.class)
    private static final class InteropLibraryExports extends LibraryExport<InteropLibrary> {

        static final FinalBitSet ENABLED_MESSAGES = createMessageBitSet(INTEROP_LIBRARY_, "hasLanguage", "getLanguage", "hasMetaObject", "getMetaObject", "toDisplayString");
        private static final Uncached UNCACHED = new Uncached();

        private InteropLibraryExports() {
            super(InteropLibrary.class, LuaLanguageView.class, false, false, 0);
        }

        @Override
        protected InteropLibrary createUncached(Object receiver) {
            assert receiver instanceof LuaLanguageView;
            InteropLibrary uncached = createDelegate(INTEROP_LIBRARY_, InteropLibraryExports.UNCACHED);
            return uncached;
        }

        @Override
        protected InteropLibrary createCached(Object receiver) {
            assert receiver instanceof LuaLanguageView;
            return createDelegate(INTEROP_LIBRARY_, new Cached(receiver));
        }

        @GeneratedBy(LuaLanguageView.class)
        private static final class Cached extends InteropLibrary implements DelegateExport {

            @Child private InteropLibrary receiverDelegateInteropLibrary_;

            protected Cached(Object receiver) {
                LuaLanguageView castReceiver = ((LuaLanguageView) receiver) ;
                this.receiverDelegateInteropLibrary_ = INTEROP_LIBRARY_.create((castReceiver.delegate));
            }

            @Override
            public FinalBitSet getDelegateExportMessages() {
                return ENABLED_MESSAGES;
            }

            @Override
            public Object readDelegateExport(Object receiver_) {
                return ((LuaLanguageView) receiver_).delegate;
            }

            @Override
            public Library getDelegateExportLibrary(Object delegate) {
                return this.receiverDelegateInteropLibrary_;
            }

            @Override
            public boolean accepts(Object receiver) {
                assert !(receiver instanceof LuaLanguageView) || DYNAMIC_DISPATCH_LIBRARY_.getUncached().dispatch(receiver) == null : "Invalid library export. Exported receiver with dynamic dispatch found but not expected.";
                if (!(receiver instanceof LuaLanguageView)) {
                    return false;
                } else if (!this.receiverDelegateInteropLibrary_.accepts((((LuaLanguageView) receiver).delegate))) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public boolean hasLanguage(Object receiver) {
                assert receiver instanceof LuaLanguageView : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaLanguageView) receiver)).hasLanguage();
            }

            @Override
            public Class<? extends TruffleLanguage<?>> getLanguage(Object receiver) throws UnsupportedMessageException {
                assert receiver instanceof LuaLanguageView : "Invalid library usage. Library does not accept given receiver.";
                return (((LuaLanguageView) receiver)).getLanguage();
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link LuaLanguageView#hasMetaObject(LuaLanguageView, InteropLibrary)}
             *     Activation probability: 0.25000
             *     With/without class size: 7/0 bytes
             * </pre> */
            @Override
            public boolean hasMetaObject(Object arg0Value_) {
                assert arg0Value_ instanceof LuaLanguageView : "Invalid library usage. Library does not accept given receiver.";
                LuaLanguageView arg0Value = ((LuaLanguageView) arg0Value_);
                {
                    InteropLibrary interop__ = this.receiverDelegateInteropLibrary_;
                    return arg0Value.hasMetaObject(interop__);
                }
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link LuaLanguageView#getMetaObject(LuaLanguageView, InteropLibrary)}
             *     Activation probability: 0.25000
             *     With/without class size: 7/0 bytes
             * </pre> */
            @Override
            public Object getMetaObject(Object arg0Value_) throws UnsupportedMessageException {
                assert arg0Value_ instanceof LuaLanguageView : "Invalid library usage. Library does not accept given receiver.";
                LuaLanguageView arg0Value = ((LuaLanguageView) arg0Value_);
                {
                    InteropLibrary interop__ = this.receiverDelegateInteropLibrary_;
                    return arg0Value.getMetaObject(interop__);
                }
            }

            /**
             * Debug Info: <pre>
             *   Specialization {@link LuaLanguageView#toDisplayString(LuaLanguageView, boolean, InteropLibrary)}
             *     Activation probability: 0.25000
             *     With/without class size: 7/0 bytes
             * </pre> */
            @Override
            public Object toDisplayString(Object arg0Value_, boolean arg1Value) {
                assert arg0Value_ instanceof LuaLanguageView : "Invalid library usage. Library does not accept given receiver.";
                LuaLanguageView arg0Value = ((LuaLanguageView) arg0Value_);
                {
                    InteropLibrary interop__ = this.receiverDelegateInteropLibrary_;
                    return arg0Value.toDisplayString(arg1Value, interop__);
                }
            }

        }
        @GeneratedBy(LuaLanguageView.class)
        @DenyReplace
        private static final class Uncached extends InteropLibrary implements DelegateExport, UnadoptableNode {

            protected Uncached() {
            }

            @Override
            public FinalBitSet getDelegateExportMessages() {
                return ENABLED_MESSAGES;
            }

            @Override
            public Object readDelegateExport(Object receiver_) {
                return (((LuaLanguageView) receiver_)).delegate;
            }

            @Override
            public Library getDelegateExportLibrary(Object delegate_) {
                return INTEROP_LIBRARY_.getUncached(delegate_);
            }

            @Override
            @TruffleBoundary
            public boolean accepts(Object receiver) {
                assert !(receiver instanceof LuaLanguageView) || DYNAMIC_DISPATCH_LIBRARY_.getUncached().dispatch(receiver) == null : "Invalid library export. Exported receiver with dynamic dispatch found but not expected.";
                return receiver instanceof LuaLanguageView;
            }

            @TruffleBoundary
            @Override
            public boolean hasLanguage(Object receiver) {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaLanguageView) receiver) .hasLanguage();
            }

            @TruffleBoundary
            @Override
            public Class<? extends TruffleLanguage<?>> getLanguage(Object receiver) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(receiver) : "Invalid library usage. Library does not accept given receiver.";
                return ((LuaLanguageView) receiver) .getLanguage();
            }

            @TruffleBoundary
            @Override
            public boolean hasMetaObject(Object arg0Value_) {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                LuaLanguageView arg0Value = ((LuaLanguageView) arg0Value_);
                return arg0Value.hasMetaObject(INTEROP_LIBRARY_.getUncached((arg0Value.delegate)));
            }

            @TruffleBoundary
            @Override
            public Object getMetaObject(Object arg0Value_) throws UnsupportedMessageException {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                LuaLanguageView arg0Value = ((LuaLanguageView) arg0Value_);
                return arg0Value.getMetaObject(INTEROP_LIBRARY_.getUncached((arg0Value.delegate)));
            }

            @TruffleBoundary
            @Override
            public Object toDisplayString(Object arg0Value_, boolean arg1Value) {
                // declared: true
                assert this.accepts(arg0Value_) : "Invalid library usage. Library does not accept given receiver.";
                LuaLanguageView arg0Value = ((LuaLanguageView) arg0Value_);
                return arg0Value.toDisplayString(arg1Value, INTEROP_LIBRARY_.getUncached((arg0Value.delegate)));
            }

        }
    }
}
