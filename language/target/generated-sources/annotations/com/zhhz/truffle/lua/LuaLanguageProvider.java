// CheckStyle: start generated
package com.zhhz.truffle.lua;

import com.oracle.truffle.api.TruffleLanguage.ContextPolicy;
import com.oracle.truffle.api.TruffleLanguage.Registration;
import com.oracle.truffle.api.debug.DebuggerTags.AlwaysHalt;
import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags.CallTag;
import com.oracle.truffle.api.instrumentation.StandardTags.ExpressionTag;
import com.oracle.truffle.api.instrumentation.StandardTags.ReadVariableTag;
import com.oracle.truffle.api.instrumentation.StandardTags.RootBodyTag;
import com.oracle.truffle.api.instrumentation.StandardTags.RootTag;
import com.oracle.truffle.api.instrumentation.StandardTags.StatementTag;
import com.oracle.truffle.api.instrumentation.StandardTags.WriteVariableTag;
import com.oracle.truffle.api.provider.TruffleLanguageProvider;
import java.util.Collection;
import java.util.List;

@GeneratedBy(LuaLanguage.class)
@Registration(characterMimeTypes = "application/x-lua", contextPolicy = ContextPolicy.SHARED, defaultMimeType = "application/x-lua", id = "lua", name = "Lua")
@ProvidedTags({CallTag.class, StatementTag.class, RootTag.class, RootBodyTag.class, ExpressionTag.class, AlwaysHalt.class, ReadVariableTag.class, WriteVariableTag.class})
public final class LuaLanguageProvider extends TruffleLanguageProvider {

    @Override
    protected String getLanguageClassName() {
        return "com.zhhz.truffle.lua.LuaLanguage";
    }

    @Override
    protected Object create() {
        return new LuaLanguage();
    }

    @Override
    protected Collection<String> getServicesClassNames() {
        return List.of();
    }

    @Override
    protected List<?> createFileTypeDetectors() {
        return List.of(new LuaFileDetector());
    }

    @Override
    protected List<String> getInternalResourceIds() {
        return List.of();
    }

    @Override
    protected Object createInternalResource(String resourceId) {
        throw new IllegalArgumentException(String.format("Unsupported internal resource id %s, supported ids are ", resourceId));
    }

}
