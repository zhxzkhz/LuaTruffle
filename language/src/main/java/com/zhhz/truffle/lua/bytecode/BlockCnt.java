package com.zhhz.truffle.lua.bytecode;

/**
 * 活跃块 (lparser.c: BlockCnt)，用于跟踪局部变量作用域与 goto/label。
 */
public final class BlockCnt {

    public BlockCnt previous;
    public int firstlabel;
    public int firstgoto;
    public int nactvar;
    public boolean upval;
    public boolean isloop;
}
