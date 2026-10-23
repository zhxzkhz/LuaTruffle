package com.zhhz.truffle.lua.builtins;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.nodes.RootNode;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.runtime.LuaFunction;
import com.zhhz.truffle.lua.runtime.LuaMultiValue;
import com.zhhz.truffle.lua.runtime.LuaNil;
@GenerateInline(false)

@NodeInfo(shortName = "string.gmatch")
public abstract class LuaStringGmatchBuiltinNode extends LuaBuiltinNode {

    @Specialization
    @TruffleBoundary
    protected LuaFunction doGmatch(Object[] arguments) {
        byte[] source = LuaStringSupport.ensureBytes(arguments, 0, "gmatch");
        byte[] pattern = LuaStringSupport.ensureBytes(arguments, 1, "gmatch");
        long init = arguments.length > 2 && !LuaStringSupport.isNil(arguments[2])
                ? LuaStringSupport.ensureInteger(arguments, 2, "gmatch") : 1L;
        int start;
        if (init > source.length) {
            start = source.length + 1;
        } else if (init > 0) {
            start = (int) (init - 1);
        } else if (init == 0 || init < -source.length) {
            start = 0;
        } else {
            start = (int) (source.length + init);
        }
        RootNode iterator = new GmatchIteratorRootNode(getContext().getLanguage(), source, pattern, start);
        return new LuaFunction(getContext().getFunctionShape(), iterator.getCallTarget());
    }

    private static final class GmatchIteratorRootNode extends RootNode {

        private final byte[] source;
        private final byte[] pattern;
        private int nextPosition;
        private int lastMatchEnd = -1;

        private GmatchIteratorRootNode(LuaLanguage language, byte[] source, byte[] pattern, int start) {
            super(language);
            this.source = source;
            this.pattern = pattern;
            this.nextPosition = start;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return nextMatch();
        }

        @TruffleBoundary
        private Object nextMatch() {
            while (nextPosition <= source.length) {
                LuaPatternMatcher.MatchResult match = new LuaPatternMatcher(source, pattern).matchAt(nextPosition);
                if (match != null && match.end() != lastMatchEnd) {
                    nextPosition = match.end();
                    lastMatchEnd = match.end();
                    return new LuaMultiValue(match.capturesOrWholeMatch());
                }
                nextPosition++;
            }
            return LuaNil.SINGLETON;
        }
    }
}
