package com.zhhz.truffle.lua.parser;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import com.zhhz.truffle.lua.LuaLanguage;
import com.zhhz.truffle.lua.nodes.LuaAstRootNode;
import com.zhhz.truffle.lua.nodes.LuaExpressionNode;
import com.zhhz.truffle.lua.nodes.LuaRootNode;
import com.zhhz.truffle.lua.nodes.LuaStatementNode;
import com.zhhz.truffle.lua.nodes.access.*;
import com.zhhz.truffle.lua.nodes.controlflow.*;
import com.zhhz.truffle.lua.nodes.expression.*;
import com.zhhz.truffle.lua.nodes.expression.binary.*;
import com.zhhz.truffle.lua.nodes.expression.literals.*;
import com.zhhz.truffle.lua.nodes.expression.logical.*;
import com.zhhz.truffle.lua.nodes.util.LuaUnboxNodeGen;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

import static com.zhhz.truffle.lua.runtime.LuaContext.toTruffleString;

public class LuaNodeParser extends LuaBaseParser {

    public static RootCallTarget parseRootLua(LuaLanguage language, Source source) {
        LuaNodeParser visitor = new LuaNodeParser(language, source);
        parseLuaImpl(source, visitor);

        final int functionStartPos = 0;
        final int functionLength = Math.max(source.getLength(), 0);
        final SourceSection functionSrc = source.createSection(functionStartPos, functionLength);

        LuaStatementNode rootBlock = visitor.rootBlock;

        var frameDescriptorBuilder = visitor.exitMain();

        final LuaRootNode rootNode = new LuaAstRootNode(language, frameDescriptorBuilder, rootBlock, functionSrc ,toTruffleString(source.getName()));
        rootNode.insert(rootBlock);

        return rootNode.getCallTarget();
    }


    private final LuaStatementVisitor statementVisitor = new LuaStatementVisitor();
    private final LuaExpressionVisitor expressionVisitor = new LuaExpressionVisitor();

    private LuaStatementNode rootBlock = null;

    private int currentFixedParamCount;
    private boolean currentIsVariadic;
    private final Deque<ParamState> paramStateStack = new ArrayDeque<>();
    private int loopDepth;
    private final Deque<FunctionControlInfo> controlInfoStack = new ArrayDeque<>();
    private final Deque<BlockControlInfo> blockInfoStack = new ArrayDeque<>();
    private static final LuaExpressionNode[] EMPTY_EXPRESSIONS = new LuaExpressionNode[0];
    private static final LuaStatementNode[] EMPTY_STATEMENTS = new LuaStatementNode[0];
    private static final LuaCloseSlotNode[] EMPTY_CLOSE_NODES = new LuaCloseSlotNode[0];
    private int nextBlockId;
    private boolean suppressLocalTracking;

    protected LuaNodeParser(LuaLanguage language, Source source) {
        super(language, source);
        this.currentFixedParamCount = 0;
        this.currentIsVariadic = true;
        this.loopDepth = 0;
        this.nextBlockId = 0;
        this.suppressLocalTracking = false;
    }

    @Override
    public LuaStatementNode visitChunk(LuaParser.ChunkContext ctx) {
        enterMain();
        enterControlInfo();
        enterBlockInfo();
        try {
            var value = visit(ctx.block());
            validateControlInfo();
            rootBlock = value;
            return value;
        } finally {
            exitBlockInfo();
            exitControlInfo();
        }
    }

    @Override
    protected void onLocalVariableDeclared(TruffleString name, LocalAttribute attribute, int slotIndex, int declarationId) {
        if (suppressLocalTracking || blockInfoStack.isEmpty()) {
            return;
        }
        BlockControlInfo block = blockInfoStack.peek();
        block.localDeclarationIds.add(declarationId);
        if (attribute == LocalAttribute.CLOSE) {
            CloseSlot closeSlot = new CloseSlot(declarationId, slotIndex, name);
            block.closeSlots.add(closeSlot);
            controlInfoStack.peek().closeSlots.put(declarationId, closeSlot);
        }
    }

    @Override
    public LuaStatementNode visitBlock(LuaParser.BlockContext ctx) {
        return statementVisitor.visitBlock(ctx);
    }

    @Override
    public LuaStatementNode visitFuncbody(LuaParser.FuncbodyContext ctx) {
        // 这个 FunctionBodyNode 是我们自己定义的 Truffle AST 节点。
        return new LuaFunctionBodyNode(visitBlock(ctx.block()));

    }

    @Override
    public LuaStatementNode visitStatFunctionDef(LuaParser.StatFunctionDefContext ctx) {
        return statementVisitor.visitStatFunctionDef(ctx);
    }

    @Override
    public LuaStatementNode visitStatFunctionCall(LuaParser.StatFunctionCallContext ctx) {
        return statementVisitor.visitStatFunctionCall(ctx);
    }


    @Override
    public LuaStatementNode visitStatAssign(LuaParser.StatAssignContext ctx) {
        return super.visitStatAssign(ctx);
    }



    private class LuaStatementVisitor extends LuaParserBaseVisitor<LuaStatementNode> {

        @Override
        public LuaStatementNode visitBlock(LuaParser.BlockContext ctx) {
            BlockControlInfo blockInfo = currentBlockInfo();

            List<LuaStatementNode> bodyNodes = new ArrayList<>();
            for (LuaParser.StatContext child : ctx.stat()) {
                var node = visit(child);
                //过滤方法定义返回的空值
                if (Objects.nonNull(node)) {
                    bodyNodes.add(node);
                }
            }

            if (Objects.nonNull(ctx.retstat())) {
                bodyNodes.add(visitRetstat(ctx.retstat()));
            }


            for (LuaStatementNode statement : bodyNodes) {
                if (statement.hasSource() && !isHaltInCondition(statement)) {
                    statement.addStatementTag();
                }
            }
            LuaBlockNode blockNode = new LuaBlockNode(
                    bodyNodes.toArray(EMPTY_STATEMENTS),
                    createCloseNodes(blockInfo),
                    blockInfo.path()
            );
            setSourceSectionFromContext(blockNode,ctx);
            return blockNode;
        }

        @Override
        public LuaStatementNode visitStatBreak(LuaParser.StatBreakContext ctx) {
            if (loopDepth == 0) {
                throw new LuaParseError(source, ctx.start.getLine(), ctx.start.getCharPositionInLine(), ctx.start.getText().length(), "break outside loop");
            }
            LuaStatementNode luaBreakNode = new LuaBreakNode();
            srcFromToken(luaBreakNode,ctx.start);
            return luaBreakNode;
        }

        @Override
        public LuaStatementNode visitStatLabel(LuaParser.StatLabelContext ctx) {
            TruffleString label = asTruffleString(ctx.label().NAME().getSymbol(), false);
            addLabel(label, ctx.label().NAME().getSymbol());
            LuaLabelNode node = new LuaLabelNode(label);
            setSourceSectionFromContext(node, ctx);
            return node;
        }

        @Override
        public LuaStatementNode visitStatGoto(LuaParser.StatGotoContext ctx) {
            TruffleString label = asTruffleString(ctx.NAME().getSymbol(), false);
            LuaGotoNode node = new LuaGotoNode(label, null);
            addGoto(label, ctx.NAME().getSymbol(), node);
            setSourceSectionFromContext(node, ctx);
            return node;
        }

        /**
         * 处理 `do...end` 语句块。
         * <p>
         * 这个方法的职责不是创建一个特殊的 `LuaDoNode`，而是：
         * <p>
         * 1. 正确地管理作用域栈，为 `block` 创建一个新的词法作用域。
         * <p>
         * 2. 委托给 `visitBlock` 来创建代表这个语句块的 `LuaBlockNode`。
         */
        @Override
        public LuaStatementNode visitStatDo(LuaParser.StatDoContext ctx) {
            // 1. 【进入新作用域】在解析 `block` 之前，调用 enterBlockScope。
            //    这会在你的 `scopeStack` 上压入一个新的 `Map<String, Integer>`，
            //    之后所有在这个块内的 `local` 声明都会被放入这个新的作用域。
            enterBlockScope();

            enterBlockInfo();
            try {
                // 2. 【委托解析】直接 visit `block` 子节点。
                //    `visitBlock` 会在这个新的、最顶层的作用域环境中，
                //    解析所有的子语句，并返回一个 `LuaBlockNode`。
                return visit(ctx.block());
            } finally {
                exitBlockInfo();
                // 3. 【退出作用域】在 `block` 解析完成后，调用 exitBlockScope。
                //    这会从 `scopeStack` 中弹出刚刚为 `do...end` 块创建的作用域，
                //    从而确保其中的局部变量不会泄露到外部。
                exitBlockScope();
            }
        }

        @Override
        public LuaStatementNode visitStatWhile(LuaParser.StatWhileContext ctx) {
            // 1. visit 条件表达式 `exp`
            LuaExpressionNode conditionNode = expressionVisitor.visit(ctx.exp());

            // 2. visit 循环体 `block`
            enterBlockScope();
            enterLoop();
            enterBlockInfo();
            LuaStatementNode bodyNode;
            try {
                bodyNode = visit(ctx.block());
            } finally {
                exitBlockInfo();
                exitLoop();
                exitBlockScope();
            }

            LuaWhileNode node = new LuaWhileNode(conditionNode, bodyNode);
            setSourceSectionFromContext(node, ctx);
            return node;
        }

        @Override
        public LuaStatementNode visitStatRepeat(LuaParser.StatRepeatContext ctx) {
            // 1. 【进入新作用域】
            //    `repeat...until` 块本身形成了一个作用域，其内部的 local 变量
            //    在 `until` 条件中也可见。
            enterBlockScope();
            enterLoop();
            enterBlockInfo();

            // 2. 【解析循环体】
            //    首先 visit `block`。这将在我们刚刚创建的新作用域中进行。
            LuaStatementNode bodyNode;
            LuaExpressionNode conditionNode;
            try {
                bodyNode = visit(ctx.block());

                // 3. 【解析条件表达式】
                //    【关键】在【退出作用域之前】visit `exp`。
                //    这确保了 `exp` 的解析（包括其中的变量查找）
                //    能够看到 `block` 中定义的局部变量。
                conditionNode = expressionVisitor.visit(ctx.exp());
            } finally {
                // 4. 【退出作用域】
                //    在循环体和条件都解析完毕后，我们才退出这个作用域。
                exitBlockInfo();
                exitLoop();
                exitBlockScope();
            }

            // 5. 【创建循环节点】
            //    创建并返回我们的 LuaRepeatNode。
            LuaStatementNode repeatNode = new LuaRepeatNode(bodyNode, conditionNode);
            setSourceSectionFromContext(repeatNode, ctx);
            return repeatNode;
        }

        @Override
        public LuaStatementNode visitStatIf(LuaParser.StatIfContext ctx) {

            // 1. 确定是否有 else 块
            LuaStatementNode elseBranch = null;
            int blockCount = ctx.block().size();
            int expCount = ctx.exp().size();
            if (blockCount > expCount) {
                // 如果块的数量比条件表达式的数量多，说明最后一个块是 else 块
                enterBlockScope();
                enterBlockInfo();
                try {
                    elseBranch = visit(ctx.block(blockCount - 1));
                } finally {
                    exitBlockInfo();
                    exitBlockScope();
                }
            }

            // 2. 从后往前处理 elseif 链
            // elseif 的条件表达式在 exp 列表中的索引是从 1 开始的
            for (int i = expCount - 1; i >= 1; i--) {
                LuaExpressionNode elseifCondition = expressionVisitor.visit(ctx.exp(i));
                // 对应的 block 也在相同的索引 i 处
                enterBlockScope();
                enterBlockInfo();
                LuaStatementNode elseifThen;
                try {
                    elseifThen = visit(ctx.block(i));
                } finally {
                    exitBlockInfo();
                    exitBlockScope();
                }
                elseBranch = new LuaIfNode(elseifCondition, elseifThen, elseBranch);
            }


            // 3. 处理最外层的 if-then
            LuaExpressionNode mainCondition = expressionVisitor.visit(ctx.exp(0));
            enterBlockScope();
            enterBlockInfo();
            LuaStatementNode mainThen;
            try {
                mainThen = visit(ctx.block(0));
            } finally {
                exitBlockInfo();
                exitBlockScope();
            }
            var node = new LuaIfNode(mainCondition, mainThen, elseBranch);
            setSourceSectionFromContext(node, ctx);
            return node;
        }

        @Override
        public LuaStatementNode visitStatForNumeric(LuaParser.StatForNumericContext ctx) {
            LuaExpressionNode startNode = expressionVisitor.visit(ctx.exp(0));
            LuaExpressionNode limitNode = expressionVisitor.visit(ctx.exp(1));
            LuaExpressionNode stepNode = ctx.exp().size() > 2 ? expressionVisitor.visit(ctx.exp(2)) : null;

            enterBlockScope();

            TruffleString loopVarName = asTruffleString(ctx.NAME().getSymbol(), false);
            int loopVariableIndex = declareLocalVariable(loopVarName);
            int limitIndex = declareHiddenSlot(hiddenSlotName("for.limit", ctx.start));
            int stepIndex = declareHiddenSlot(hiddenSlotName("for.step", ctx.start));

            enterLoop();
            enterBlockInfo();
            LuaStatementNode bodyNode;
            try {
                bodyNode = visit(ctx.block());
            } finally {
                exitBlockInfo();
                exitLoop();
                exitBlockScope();
            }

            // 創建我們新的、方案 A 的節點
            LuaNumericForNode node = new LuaNumericForNode(loopVariableIndex, limitIndex, stepIndex, startNode, limitNode, stepNode, bodyNode);
            setSourceSectionFromContext(node, ctx);
            return node;
        }

        @Override
        public LuaStatementNode visitStatForGeneric(LuaParser.StatForGenericContext ctx) {
            // 1. 【解析 in explist】
            // `in` 后面的表达式列表在【外部】作用域中解析。
            LuaExpressionNode[] expList = visitExpListAsArray(ctx.explist());
            LuaMultiValueNode expListNode = new LuaMultiValueNode(expList);
            int iteratorFuncIndex = declareHiddenSlot(hiddenSlotName("for.iterator", ctx.start));
            int stateIndex = declareHiddenSlot(hiddenSlotName("for.state", ctx.start));
            int controlVarIndex = declareHiddenSlot(hiddenSlotName("for.control", ctx.start));

            // 2. 【进入新作用域】
            //    通用 for 循环也创建了一个新的作用域块。
            enterBlockScope();

            // 3. 【为循环变量声明 FrameSlot 和写入节点】
            //    循环变量 (namelist) 是这个新作用域的局部变量。
            List<TruffleString> loopVarNames = parseNameList(ctx.namelist());
            LuaWriteLocalVariableNode[] writeNodes = new LuaWriteLocalVariableNode[loopVarNames.size()];
            for (int i = 0; i < loopVarNames.size(); i++) {
                TruffleString varName = loopVarNames.get(i);
                // 声明槽位
                int slotIndex = declareLocalVariable(varName);
                // 创建写入节点
                writeNodes[i] = LuaWriteLocalVariableNodeGen.create(slotIndex);
            }

            // 4. 【解析循环体】
            //    循环体 `block` 在包含了循环变量的新作用域中解析。
            enterLoop();
            enterBlockInfo();
            LuaStatementNode bodyNode;
            try {
                bodyNode = visit(ctx.block());
            } finally {
                exitBlockInfo();
                exitLoop();
                // 5. 【退出作用域】
                exitBlockScope();
            }
            //setSourceSectionFromContext(bodyNode, ctx);

            // 6. 【创建 For 节点】
            LuaGenericForNode node = new LuaGenericForNode(expListNode, iteratorFuncIndex, stateIndex, controlVarIndex, writeNodes, bodyNode);
            setSourceSectionFromContext(node, ctx);
            return node;
        }

        @Override
        public LuaStatementNode visitStatAssign(LuaParser.StatAssignContext ctx) {
            //var value = super.visitStatAssign(ctx);
            // 1. 【创建 varNodes】
            //    调用一个辅助方法来处理 varlist，得到所有写入目标的节点数组。

            List<LuaParser.VarContext> vars = ctx.varlist().var();
            LuaWriteVariableNode[] varNodes = new LuaWriteVariableNode[vars.size()];
            for (int i = 0; i < vars.size(); i++) {
                // 对于 varlist 中的每一个 var，我们都调用另一个更具体的辅助方法来创建写入节点。
                varNodes[i] = createWriteNodeForVar(vars.get(i));
            }

            // 2. 【创建 valuesNode】
            //    调用另一个辅助方法来处理 explist，得到值的提供者。
            LuaExpressionNode[] valueExpList = visitExpListAsArray(ctx.explist());

            // 3. 【创建最终的赋值节点】
            //    将“目标”和“来源”组合成一个多重赋值语句节点。
            //    Truffle DSL 会为我们生成 LuaMultiAssignmentNodeGen 类。
            var l = new LuaMultiAssignmentNode(varNodes, valueExpList);
            l.addStatementTag();
            setSourceSectionFromContext(l,ctx);
            return l;
        }

        @Override
        public LuaStatementNode visitStatLocalVarAssign(LuaParser.StatLocalVarAssignContext ctx) {

            LuaExpressionNode[] valueExpList = visitExpListAsArray(ctx.explist());

            // --- 阶段 1: 处理左侧 (LHS) - 变量声明 ---
            // 我们的目标是创建一个 `LuaWriteVariableNode[]` 数组，
            // 其中的每个节点都指向一个【新创建】的 FrameSlot。

            List<TerminalNode> names = ctx.attnamelist().NAME();
            List<LuaParser.AttribContext> attributes = ctx.attnamelist().attrib();
            LuaWriteVariableNode[] writeNodes = new LuaWriteVariableNode[names.size()];
            boolean hasClose = false;
            for (int i = 0; i < names.size(); i++) {
                TerminalNode nameNode = names.get(i);
                TruffleString varName = asTruffleString(nameNode.getSymbol(), false);
                LocalAttribute attribute = parseLocalAttribute(attributes.get(i));
                if (attribute == LocalAttribute.CLOSE) {
                    if (hasClose) {
                        throw new LuaParseError(source, nameNode.getSymbol().getLine(), nameNode.getSymbol().getCharPositionInLine(), nameNode.getText().length(), "multiple to-be-closed variables in local declaration");
                    }
                    hasClose = true;
                }
                int slotIndex = declareLocalVariable(varName, attribute);
                LuaWriteLocalVariableNode writeNode = LuaWriteLocalVariableNodeGen.create(slotIndex);
                srcFromToken(writeNode, nameNode.getSymbol());

                writeNodes[i] = writeNode;
            }

            // 3. 【创建最终的赋值节点】
            //    将“目标”和“来源”组合成一个多重赋值语句节点。
            //    Truffle DSL 会为我们生成 LuaMultiAssignmentNodeGen 类。
            LuaMultiAssignmentNode node = new LuaMultiAssignmentNode(writeNodes, valueExpList);
            setSourceSectionFromContext(node, ctx);
            return node;
        }


        @Override
        public LuaStatementNode visitStatFunctionDef(LuaParser.StatFunctionDefContext ctx) {

            Token nameToken = ctx.funcname().NAME(0).getSymbol();

            var funcName = asTruffleString(nameToken, false);

            var functionLiteralNode = parseFunction(ctx.funcname(),ctx.funcbody(), funcName);


            // 调用一个新的辅助方法来处理复杂的 funcname 语法
            LuaWriteVariableNode writeNode = createWriteNodeForFuncName(ctx.funcname());

            LuaWriteVariableNode[] destination = new LuaWriteVariableNode[]{writeNode};

            LuaMultiAssignmentNode node = new LuaMultiAssignmentNode(destination, new LuaExpressionNode[]{functionLiteralNode});
            setSourceSectionFromContext(node, ctx);
            return node;
        }

        @Override
        public LuaStatementNode visitStatLocalFunctionDef(LuaParser.StatLocalFunctionDefContext ctx) {

            TruffleString funcName = asTruffleString(ctx.NAME().getSymbol(), false);

            // --- 阶段 1: 声明局部变量 & 创建写入目标 ---
            // 为函数名声明一个新的局部变量槽位
            int slotIndex = declareLocalVariable(funcName);

            // 创建一个写入节点，指向这个新槽位
            LuaWriteLocalVariableNode writeNode = LuaWriteLocalVariableNodeGen.create(slotIndex);
            srcFromToken(writeNode, ctx.NAME().getSymbol());

            // 因为这是单赋值，所以写入目标的数组只有一个元素
            LuaWriteVariableNode[] destinationNodes = new LuaWriteVariableNode[]{writeNode};

            LuaExpressionNode valueNode = parseFunction(ctx.funcbody(), funcName);

            LuaMultiAssignmentNode node = new LuaMultiAssignmentNode(destinationNodes, new LuaExpressionNode[]{valueNode});
            setSourceSectionFromContext(node, ctx);
            return node;
        }


        @Override
        public LuaStatementNode visitStatFunctionCall(LuaParser.StatFunctionCallContext ctx) {
            return expressionVisitor.visitStatFunctionCall(ctx);
        }

        @Override
        public LuaStatementNode visitExplist(LuaParser.ExplistContext ctx) {
            throw new UnsupportedOperationException("expList should be handled by its parent rule directly via visitExpListAsArray");
        }

        @Override
        public LuaStatementNode visitRetstat(LuaParser.RetstatContext ctx) {
            final LuaExpressionNode[] valueNodes = visitExpListAsArray(ctx.explist());

            // 创建 LuaReturnNode
            final LuaReturnNode returnNode = new LuaReturnNode(valueNodes);

            //内容范围问题后续修复
            setSourceSectionFromContext(returnNode,ctx);

            return returnNode;
        }

        @Override
        public LuaStatementNode visitStatEmpty(LuaParser.StatEmptyContext ctx) {
            return super.visitStatEmpty(ctx);
        }

    }

    private class LuaExpressionVisitor extends LuaParserBaseVisitor<LuaExpressionNode> {

        @Override
        public LuaExpressionNode visitStatFunctionCall(LuaParser.StatFunctionCallContext ctx) {
            return visit(ctx.functioncall());
        }

        private LuaExpressionNode createBinary(ParserRuleContext ctx, Token opToken, LuaParser.ExpContext leftCtx, LuaParser.ExpContext rightCtx) {
            if (leftCtx == null) {
                throw new LuaParseError(source, ctx.start.getLine(), ctx.start.getCharPositionInLine(), ctx.getText().length(), "missing left expression operand");
            }
            if (opToken == null) {
                return visit(leftCtx);
            }
            if (rightCtx == null) {
                throw new LuaParseError(source, opToken.getLine(), opToken.getCharPositionInLine(), opToken.getText().length(), "missing right expression operand");
            }
            return createBinary(ctx, opToken, visit(leftCtx), visit(rightCtx));
        }

        private LuaExpressionNode createBinary(ParserRuleContext ctx, Token opToken, LuaExpressionNode leftNode, LuaExpressionNode rightNode) {

            final LuaExpressionNode leftUnboxed = LuaUnboxNodeGen.create(leftNode);
            final LuaExpressionNode rightUnboxed = LuaUnboxNodeGen.create(rightNode);
            final LuaExpressionNode result = switch (opToken.getText()) {
                case "+" -> LuaAddNodeGen.create(leftUnboxed, rightUnboxed);
                case "-" -> LuaSubNodeGen.create(leftUnboxed, rightUnboxed);
                case "*" -> LuaMulNodeGen.create(leftUnboxed, rightUnboxed);
                case "/" -> LuaDivNodeGen.create(leftUnboxed, rightUnboxed);
                case "//" -> LuaFloorDivNodeGen.create(leftUnboxed, rightUnboxed);
                case "%" -> LuaModNodeGen.create(leftUnboxed, rightUnboxed);
                case "^" -> LuaPowerNodeGen.create(leftUnboxed, rightUnboxed);
                case ".." -> LuaConcatNodeGen.create(leftUnboxed, rightUnboxed);
                case "<" -> LuaLessThanNodeGen.create(leftUnboxed, rightUnboxed);
                case "<=" -> LuaLessOrEqualNodeGen.create(leftUnboxed, rightUnboxed);
                case ">" -> LuaGreaterThanNodeGen.create(leftUnboxed, rightUnboxed);
                case ">=" -> LuaGreaterOrEqualNodeGen.create(leftUnboxed, rightUnboxed);
                case "==" -> LuaEqualNodeGen.create(leftUnboxed, rightUnboxed);
                case "~=" -> LuaLogicalNotNodeGen.create(LuaEqualNodeGen.create(leftUnboxed, rightUnboxed));
                case "|" -> LuaBitwiseOrNodeGen.create(leftUnboxed, rightUnboxed);
                case "~" -> LuaBitwiseXorNodeGen.create(leftUnboxed, rightUnboxed);
                case "&" -> LuaBitwiseAndNodeGen.create(leftUnboxed, rightUnboxed);
                case ">>" -> LuaBitwiseShiftNodeGen.create(false, leftUnboxed, rightUnboxed);
                case "<<" -> LuaBitwiseShiftNodeGen.create(true, leftUnboxed, rightUnboxed);
                case "and" -> new LuaLogicalAndNode(leftUnboxed, rightUnboxed);
                case "or" -> new LuaLogicalOrNode(leftUnboxed, rightUnboxed);
                default -> throw new RuntimeException("unexpected operation: " + opToken.getText());
            };

            if (leftNode.hasSource() && rightNode.hasSource()) {
                int start = leftNode.getSourceCharIndex();
                int length = rightNode.getSourceEndIndex() - start;
                result.setSourceSection(start, length);
            } else {
                setSourceSectionFromContext(result, ctx);
            }
            result.addExpressionTag();

            return result;
        }

        @Override
        public LuaExpressionNode visitExpLogicalOr(LuaParser.ExpLogicalOrContext ctx) {
            return createBinary(ctx, ctx.op, ctx.exp(0), ctx.exp(1));
        }

        @Override
        public LuaExpressionNode visitExpLogicalAnd(LuaParser.ExpLogicalAndContext ctx) {
            return createBinary(ctx, ctx.op, ctx.exp(0), ctx.exp(1));
        }

        @Override
        public LuaExpressionNode visitExpComparison(LuaParser.ExpComparisonContext ctx) {
            return createBinary(ctx, ctx.op, ctx.exp(0), ctx.exp(1));
        }


        @Override
        public LuaExpressionNode visitExpBitwiseOr(LuaParser.ExpBitwiseOrContext ctx) {
            return createBinary(ctx, ctx.op, ctx.exp(0), ctx.exp(1));
        }


        @Override
        public LuaExpressionNode visitExpBitwiseXor(LuaParser.ExpBitwiseXorContext ctx) {
            return createBinary(ctx, ctx.op, ctx.exp(0), ctx.exp(1));
        }

        @Override
        public LuaExpressionNode visitExpBitwiseAnd(LuaParser.ExpBitwiseAndContext ctx) {
            return createBinary(ctx, ctx.op, ctx.exp(0), ctx.exp(1));
        }

        @Override
        public LuaExpressionNode visitExpBitwiseShift(LuaParser.ExpBitwiseShiftContext ctx) {
            return createBinary(ctx, ctx.op, ctx.exp(0), ctx.exp(1));
        }

        @Override
        public LuaExpressionNode visitExpConcat(LuaParser.ExpConcatContext ctx) {
            return createBinary(ctx, ctx.DD() == null ? null : ctx.DD().getSymbol(), ctx.exp(0), ctx.exp(1));
        }

        @Override
        public LuaExpressionNode visitExpAddSub(LuaParser.ExpAddSubContext ctx) {
            return createBinary(ctx, ctx.op, ctx.exp(0), ctx.exp(1));
        }

        @Override
        public LuaExpressionNode visitExpMulDivMod(LuaParser.ExpMulDivModContext ctx) {
            return createBinary(ctx, ctx.op, ctx.exp(0), ctx.exp(1));
        }


        @Override
        public LuaExpressionNode visitExpUnop(LuaParser.ExpUnopContext ctx) {
            LuaExpressionNode operandNode = visit(ctx.exp());
            String op = ctx.op.getText();

            final LuaExpressionNode resultNode = switch (op) {
                case "-" -> LuaUnaryMinusNodeGen.create(operandNode);
                case "not" -> LuaLogicalNotNodeGen.create(operandNode);
                case "#" -> LuaLengthNodeGen.create(operandNode);
                case "~" -> LuaBitwiseNotNodeGen.create(operandNode);
                default ->
                        throw new LuaParseError(source, ctx.op.getLine(), ctx.op.getCharPositionInLine(), ctx.op.getText().length(), "unexpected unary operator: " + op);
            };

            setSourceSectionFromContext(resultNode, ctx);
            return resultNode;
        }

        @Override
        public LuaExpressionNode visitExpPower(LuaParser.ExpPowerContext ctx) {
            return createBinary(ctx, ctx.CARET() == null ? null : ctx.CARET().getSymbol(), ctx.exp(0), ctx.exp(1));
        }

        @Override
        public LuaExpressionNode visitExpNil(LuaParser.ExpNilContext ctx) {
            Token literalToken = ctx.start;
            LuaExpressionNode result = new LuaNilLiteralNode();
            srcFromToken(result, literalToken);
            result.addExpressionTag();
            return result;
        }

        @Override
        public LuaExpressionNode visitExpTrue(LuaParser.ExpTrueContext ctx) {
            Token literalToken = ctx.start;
            LuaExpressionNode result = new LuaBooleanLiteralNode(true);
            srcFromToken(result, literalToken);
            result.addExpressionTag();
            return result;
        }

        @Override
        public LuaExpressionNode visitExpFalse(LuaParser.ExpFalseContext ctx) {
            Token literalToken = ctx.start;
            LuaExpressionNode result = new LuaBooleanLiteralNode(false);
            srcFromToken(result, literalToken);
            result.addExpressionTag();
            return result;
        }

        @Override
        public LuaExpressionNode visitExpString(LuaParser.ExpStringContext ctx) {
            return visit(ctx.string());
        }

        @Override
        public LuaExpressionNode visitStringChar(LuaParser.StringCharContext ctx) {
            var name = ctx.start;
            LuaStringLiteralNode node = new LuaStringLiteralNode(unescapeLuaString(asTruffleString(name,true).toJavaStringUncached(), name));
            node.addExpressionTag();
            setSourceSectionFromContext(node,ctx);
            return node;
        }

        @Override
        public LuaExpressionNode visitStringNormal(LuaParser.StringNormalContext ctx) {
            var name = ctx.start;
            LuaStringLiteralNode node = new LuaStringLiteralNode(unescapeLuaString(asTruffleString(name,true).toJavaStringUncached(), name));
            node.addExpressionTag();
            setSourceSectionFromContext(node,ctx);
            return node;
        }

        @Override
        public LuaExpressionNode visitStringLong(LuaParser.StringLongContext ctx) {
            LuaStringLiteralNode node = new LuaStringLiteralNode(parseLuaLongString(ctx.start.getText()));
            node.addExpressionTag();
            setSourceSectionFromContext(node, ctx);
            return node;
        }

        @Override
        public LuaExpressionNode visitExpVararg(LuaParser.ExpVarargContext ctx) {
            // 1. 语义检查：如果当前函数不是可变参数函数，使用 `...` 是语法错误
            if (!LuaNodeParser.this.currentIsVariadic) {
                throw new LuaParseError(source, ctx.start.getLine(), ctx.start.getCharPositionInLine(), 3, "cannot use '...' outside a vararg function");
            }

            LuaExpressionNode node = new LuaReadVarargsNode(LuaNodeParser.this.currentFixedParamCount);
            node.addExpressionTag();
            setSourceSectionFromContext(node, ctx);
            return node;
        }

        @Override
        public LuaExpressionNode visitExpNumber(LuaParser.ExpNumberContext ctx) {
            Token literalToken = ctx.number().start;
            String text = literalToken.getText();
            LuaExpressionNode result;

            boolean isHex = text.length() > 2 && text.charAt(0) == '0' && (text.charAt(1) == 'x' || text.charAt(1) == 'X');
            boolean hasFractionOrExponent;

            if (isHex) {
                hasFractionOrExponent = text.indexOf('.', 2) >= 0
                        || text.indexOf('p', 2) >= 0 || text.indexOf('P', 2) >= 0;
            } else {
                hasFractionOrExponent = text.indexOf('.') >= 0
                        || text.indexOf('e') >= 0 || text.indexOf('E') >= 0;
            }

            if (hasFractionOrExponent) {
                double value;
                if (isHex) {
                    String doubleText = text;
                    if (doubleText.indexOf('p') < 0 && doubleText.indexOf('P') < 0) {
                        doubleText = doubleText + "p0";
                    }
                    try {
                        value = Double.parseDouble(doubleText);
                    } catch (NumberFormatException ex) {
                        throw new LuaParseError(source, literalToken.getLine(),
                                literalToken.getCharPositionInLine(), text.length(),
                                "invalid hex float number: " + text);
                    }
                } else {
                    value = Double.parseDouble(text);
                }
                result = new LuaDoubleLiteralNode(value);
            } else {
                try {
                    long value;
                    if (isHex) {
                        value = Long.parseUnsignedLong(text.substring(2), 16);
                    } else {
                        value = Long.parseLong(text);
                    }
                    result = new LuaLongLiteralNode(value);
                } catch (NumberFormatException ex) {
                    result = new LuaDoubleLiteralNode(Double.parseDouble(text));
                }
            }

            srcFromToken(result, literalToken);
            result.addExpressionTag();
            return result;
        }

        @Override
        public LuaExpressionNode visitExpPrefixExpAtom(LuaParser.ExpPrefixExpAtomContext ctx) {

            LuaExpressionNode expressionNode = visit(ctx.prefixexp());

            if (expressionNode == null) {
                throw new LuaParseError(source, ctx.start.getLine(), ctx.start.getCharPositionInLine(), 1, "unexpected null expression in prefix expression");
            }

            return expressionNode;
        }

        @Override
        public LuaExpressionNode visitExpFunctionDef(LuaParser.ExpFunctionDefContext ctx) {
            // 对于匿名函数，我们没有一个明确的名字，所以可以传递一个
            // 像 "<anonymous>" 或基于源代码位置的调试名。
            TruffleString debugName;
            if (ctx.getParent().getParent().getParent() instanceof LuaParser.StatAssignContext statAssignContext){
                //静态查找函数名字，如果是表达式命名是不准的，用于开发阶段调试
                int index = statAssignContext.explist().exp().indexOf(ctx.getParent());
                if (index > -1 && index < statAssignContext.varlist().var().size()){
                    debugName = toTruffleString( "<function(" + statAssignContext.varlist().var(index).prefixexp().getText() + "):" + ctx.start.getLine() + ">" );
                } else {
                    debugName = toTruffleString( "<function:" + ctx.start.getLine() + ">" );
                }
            } else {
                debugName = toTruffleString( "<function anonymous:" + ctx.start.getLine() + ">" );
            }

            return parseFunction(ctx.functiondef().funcbody(), debugName);
        }

        @Override
        public LuaExpressionNode visitTableconstructor(LuaParser.TableconstructorContext ctx) {
            if (ctx.fieldlist() == null) {
                var table = new LuaTableConstructorNode(new LuaTableFieldInitializerNode[0]);
                table.addExpressionTag();
                setSourceSectionFromContext(table,ctx);
                return table;
            }

            List<LuaParser.FieldContext> fields = ctx.fieldlist().field();
            List<LuaTableFieldInitializerNode> initializers = new ArrayList<>(fields.size());

            // 【关键】我们需要一个计数器来追踪列表式字段的索引
            int nextArrayIndex = 1; // Lua 数组索引从 1 开始

            for (LuaParser.FieldContext fieldCtx : fields) {
                if (fieldCtx instanceof LuaParser.FieldKeyExprContext keyExprCtx) {
                    // --- 情况 A: [exp1] = exp2 ---
                    LuaExpressionNode keyNode = expressionVisitor.visit(keyExprCtx.exp(0));
                    LuaExpressionNode valueNode = expressionVisitor.visit(keyExprCtx.exp(1));
                    initializers.add(LuaTableFieldWithKeyNodeGen.create(keyNode, valueNode));

                } else if (fieldCtx instanceof LuaParser.FieldKeyNameContext nameKeyCtx) {
                    // --- 情况 B: NAME = exp ---
                    // 这是 `[NAME_string] = exp` 的语法糖
                    TruffleString keyName = asTruffleString(nameKeyCtx.NAME().getSymbol());
                    LuaExpressionNode keyNode = new LuaStringLiteralNode(keyName);
                    LuaExpressionNode valueNode = expressionVisitor.visit(nameKeyCtx.exp());
                    initializers.add(LuaTableFieldWithKeyNodeGen.create(keyNode, valueNode));

                } else if (fieldCtx instanceof LuaParser.FieldValueContext valueCtx) {
                    // --- 情况 C: exp (列表式) ---
                    LuaExpressionNode valueNode = expressionVisitor.visit(valueCtx.exp());

                    LuaTableFieldWithValueNode valueFieldNode = LuaTableFieldWithValueNodeGen.create(valueNode,nextArrayIndex);

                    initializers.add(valueFieldNode);

                    // 【递增】下一个可用的数组索引
                    nextArrayIndex++;
                }
            }

            LuaTableConstructorNode node = new LuaTableConstructorNode(initializers.toArray(new LuaTableFieldInitializerNode[0]));
            node.addExpressionTag();
            setSourceSectionFromContext(node, ctx);
            return node;
        }

        @Override
        public LuaExpressionNode visitFuncCall(LuaParser.FuncCallContext ctx) {
            LuaExpressionNode functionNode = visit(ctx.prefixexp());
            LuaExpressionNode[] argNodes = visitArgsAsArray(ctx.args());
            for (LuaExpressionNode node : argNodes) {
                if (node == null){
                    throw new LuaParseError(source, ctx.start.getLine(), ctx.start.getCharPositionInLine(), 1, "unexpected null argument in function call");
                }
            }
            var invoke = new LuaInvokeNode(functionNode, argNodes);
            invoke.addExpressionTag();
            setSourceSectionFromContext(invoke,ctx);
            return invoke;
        }

        @Override
        public LuaExpressionNode visitMethodCall(LuaParser.MethodCallContext ctx) {
            LuaExpressionNode receiverNode = visit(ctx.prefixexp());
            TruffleString methodName = asTruffleString(ctx.NAME().getSymbol());
            LuaExpressionNode keyNode = new LuaStringLiteralNode(methodName);
            LuaExpressionNode[] argNodes = visitArgsAsArray(ctx.args());

            LuaMethodCallNode node = new LuaMethodCallNode(receiverNode, keyNode, argNodes);
            node.addExpressionTag();
            setSourceSectionFromContext(node, ctx);
            return node;
        }

        /**
         * 1. 处理链条的起点：一个简单的名字。
         *    对应 g4 规则: `var: NAME # VarName`
         */
        @Override
        public LuaExpressionNode visitVarName(LuaParser.VarNameContext ctx) {
            // 这里的逻辑很简单：根据这个名字，创建一个读取节点。
            // createReadNodeForName 是一个辅助方法，它会进行作用域查找，
            // 并返回 LuaReadLocal/Upvalue/GlobalVariableNode 之一。

            return createReadNodeForName(ctx.NAME().getSymbol());
        }

        @Override
        public LuaExpressionNode visitPrefixParen(LuaParser.PrefixParenContext ctx) {
            LuaExpressionNode expression = visit(ctx.exp());
            LuaParenExpressionNode node = new LuaParenExpressionNode(expression);
            node.addExpressionTag();
            setSourceSectionFromContext(node, ctx);
            return node;
        }

        /**
         * 3. 【递归核心】处理索引访问：`... [exp]`
         *    对应 g4 规则: `var: var '[' exp ']' # VarIndexed`
         */
        @Override
        public LuaExpressionNode visitVarIndexed(LuaParser.VarIndexedContext ctx) {

            // a. 递归地 visit 左边的 `var` 部分，得到代表 table 的表达式节点。
            //    这个调用会自动处理 `a.b` 这种更复杂的左侧部分。
            LuaExpressionNode tableNode = visit(ctx.prefixexp());

            // b. 递归地 visit 括号内的 `exp` 部分，得到代表 key 的表达式节点。
            LuaExpressionNode keyNode = visit(ctx.exp());

            // c. 用它们创建一个 LuaReadTableNode。
            //    这个新节点“包裹”了 tableNode，形成了 AST 树的嵌套结构。
            LuaExpressionNode node = LuaReadTableNodeGen.create(tableNode, keyNode);
            node.addExpressionTag();
            setSourceSectionFromContext(node, ctx);
            return node;
        }

        /**
         * 4. 【递归核心】处理成员访问：`... .NAME`
         *    对应 g4 规则: `var: var '.' NAME # VarMemberAccess`
         */
        @Override
        public LuaExpressionNode visitVarMemberAccess(LuaParser.VarMemberAccessContext ctx) {
            // 【关键】`ctx` 现在有两个清晰的子节点：`ctx.prefixexp()` 和 `ctx.NAME()`

            // a. 递归地 visit 左边的 `var` 部分，得到 table 表达式节点。
            LuaExpressionNode tableNode = visit(ctx.prefixexp());

            // b. 将右边的 `NAME` 转换为一个字符串字面量节点，作为 key。
            TruffleString keyName = asTruffleString(ctx.NAME().getSymbol());
            LuaExpressionNode keyNode = new LuaStringLiteralNode(keyName);

            srcFromToken(keyNode,ctx.NAME().getSymbol());

            // c. 用它们创建一个 LuaReadTableNode。
            LuaExpressionNode node = LuaReadTableNodeGen.create(tableNode, keyNode);
            node.addExpressionTag();
            setSourceSectionFromContext(node, ctx);
            return node;
        }

    }


    /**
     * 根据变量名 Token 创建对应的变量读取 AST 节点。
     * <p>
     * 该方法负责处理标识符（Identifier），将其转换为具体的变量访问节点（如 LocalRead, GlobalRead 等），
     * 并绑定源码位置信息以便调试。
     *
     * @param nameToken ANTLR 解析生成的变量名 Token
     * @return LuaExpressionNode 具体的变量读取节点
     */
    private LuaExpressionNode createReadNodeForName(Token nameToken) {
        // 1. 字符串转换：将 ANTLR 的 Token 文本转换为 Truffle 框架专用的 TruffleString
        // TruffleString 是 GraalVM 用于跨语言互操作和性能优化的字符串表示，比 Java String 更高效
        final TruffleString name = asTruffleString(nameToken, false);

        // 2. 节点实例化：调用内部工厂方法创建具体的读取节点
        // 这里隐含了核心的作用域解析逻辑：编译器需要判断 'name' 是局部变量、闭包外变量(Upvalue)还是全局变量
        final LuaExpressionNode readNode = _createNode(name);

        // 4. 工具标记：为该节点添加 "Expression" 标签
        // Truffle 的 Instrument API 依赖这些标签来实现调试器步进、代码覆盖率统计等功能
        // 标记为 Expression 意味着调试器可以在此处暂停并查看其求值结果
        readNode.addExpressionTag();

        // 这对于调试器（Debugger）正确高亮代码、异常堆栈定位非常重要。
        //readNode.setSourceSection(nameToken.getStartIndex(), nameToken.getStopIndex() - nameToken.getStartIndex() + 1);
        srcFromToken(readNode,nameToken);

        return readNode;
    }
    private LuaExpressionNode _createNode(TruffleString name) {
        // 调用基类中封装好的 `findVariable` 方法进行符号解析
        VariableResolutionResult resolution = findVariable(name);
        final LuaExpressionNode readNode;

        //  根据查找结果，创建不同类型的读取节点
            if (resolution != null) {
            // 在词法作用域中找到了变量 (局部变量或 upvalue)
                if (!resolution.isUpvalue()) {
                // 深度为0，意味着在当前最内层作用域找到了，这是一个*局部变量*。
                // 我们使用之前为它分配的 `slotIndex` 来创建节点。
                readNode = LuaReadLocalVariableNodeGen.create(resolution.slotIndex());

            } else {
                // 深度大于0，意味着它是一个*Upvalue*。
                // 我们需要将槽位索引和嵌套深度都传递给节点。
                readNode = LuaReadUpvalueNodeGen.create(resolution.slotIndex(), resolution.depth());
            }
        } else {
            // `findVariable` 返回 null，意味着在所有词法作用域中都找不到。
            // 根据Lua规则，这被视为一个*全局变量*的读取。
            // 我们将变量名传递给节点，它将在运行时从全局表中查找。
            readNode = LuaReadGlobalVariableNodeGen.create(name);
        }

        return readNode;
    }

    /**
     * 根据一个 'var' 语法节点，创建合适的“写入”节点。
     * 这个方法是赋值语句左侧变量处理的核心。
     *
     * @param varCtx ANTLR 的 var 上下文。
     * @return 一个 LuaWriteVariableNode 的具体实例。
     */
    private LuaWriteVariableNode createWriteNodeForVar(LuaParser.VarContext varCtx) {
        var prefixExpCtx = varCtx.prefixexp();
        switch (prefixExpCtx) {
            case LuaParser.VarNameContext varNameContext -> {
                // --- 情况 A: 变量是一个简单的名字 (e.g., `x = 10`) ---
                TruffleString name = asTruffleString(varNameContext.NAME().getSymbol(), false);

                // --- 对于非声明的普通赋值 `x = 10` ---
                // 1. 【进行查找】调用已经实现的作用域查找方法
                return getLuaWriteVariableNode(name);
            }
            case LuaParser.VarIndexedContext indexedCtx -> {
                LuaExpressionNode tableNode = expressionVisitor.visit(indexedCtx.prefixexp());
                LuaExpressionNode keyNode = expressionVisitor.visit(indexedCtx.exp());
                return LuaWriteTableNodeGen.create(tableNode, keyNode);
            }
            case LuaParser.VarMemberAccessContext memberCtx -> {
                LuaExpressionNode tableNode = expressionVisitor.visit(memberCtx.prefixexp());
                TruffleString keyName = asTruffleString(memberCtx.NAME().getSymbol(), false);
                LuaExpressionNode keyNode = new LuaStringLiteralNode(keyName);
                return LuaWriteTableNodeGen.create(tableNode, keyNode);
            }
            case LuaParser.FuncCallContext ignored -> throw new LuaParseError(
                    source,
                    prefixExpCtx.start.getLine(),
                    prefixExpCtx.start.getCharPositionInLine(),
                    prefixExpCtx.getText().length(),
                    "syntax error near '='"
            );
            default -> throw new LuaParseError(
                    source,
                    prefixExpCtx.start.getLine(),
                    prefixExpCtx.start.getCharPositionInLine(),
                    prefixExpCtx.getText().length(),
                    "syntax error: unexpected symbol near '=' (cannot assign to " + prefixExpCtx.getClass().getSimpleName() + ")"
            );
        }
    }


    /**
     * 为 expList 创建一个辅助方法，它不直接重写 visitExpList
     * 将 ANTLR 解析器生成的表达式列表上下文 (ExpListContext) 转换为 Truffle AST 表达式节点数组。
     * <p>
     * 该方法通常用于处理如函数调用参数 `func(a, b, c)` 或多重赋值 `x, y = 1, 2` 中的值列表。
     *
     * @param ctx ANTLR 解析生成的表达式列表上下文对象
     * @return LuaExpressionNode[] 包含所有转换后 AST 节点的数组；如果输入上下文为 null，则返回空数组。
     */
    public LuaExpressionNode[] visitExpListAsArray(LuaParser.ExplistContext ctx) {
        // 1. 防御性检查：处理语法中允许为空的情况
        // 如果上下文为 null（例如没有参数的函数调用），直接返回一个长度为 0 的数组
        if (ctx == null) {
            return EMPTY_EXPRESSIONS;
        }

        List<LuaParser.ExpContext> expContexts = ctx.exp();
        LuaExpressionNode[] expressions = new LuaExpressionNode[expContexts.size()];

        // 3. 遍历解析树：ctx.exp() 是 ANTLR 生成的方法，返回该规则下所有匹配的表达式(exp)上下文
        for (int i = 0; i < expressions.length; i++) {
            // 4. 递归访问/转换：委托 expressionVisitor 将单个 ANTLR 表达式上下文转换为具体的 Truffle AST 节点
            // 这里的 expressionVisitor 通常是一个继承自 LuaParserBaseVisitor<LuaExpressionNode> 的实例
            expressions[i] = expressionVisitor.visit(expContexts.get(i));
        }

        return expressions;
    }

    /**
     * 【核心辅助方法】
     * 处理函数调用的参数部分 (args)，并将其转换为一个表达式节点数组。
     *
     * @param argsCtx ANTLR 的 args 上下文，可能为 null。
     * @return 代表所有参数的表达式节点数组。
     */
    private LuaExpressionNode[] visitArgsAsArray(LuaParser.ArgsContext argsCtx) {
        switch (argsCtx) {
            case null -> {
                // 在某些语法中可能没有 args
                return EMPTY_EXPRESSIONS;
            }

            // --- 【关键】使用 instanceof 进行分派 ---
            case LuaParser.ArgsParenContext parenCtx -> {
                // --- 情况 1: f(...) - 带括号的参数列表 ---
                // 我们之前已经有了 visitExplistAsArray，这里完美复用！
                return visitExpListAsArray(parenCtx.explist());
            }
            case LuaParser.ArgsTableContext tableCtx -> {
                LuaExpressionNode tableNode = expressionVisitor.visit(tableCtx.tableconstructor());
                return new LuaExpressionNode[]{tableNode};
            }
            case LuaParser.ArgsStringContext stringCtx -> {
                // --- 情况 3: f"..." - 字符串字面量作为唯一参数 ---
                // visit string 会返回一个 LuaStringLiteralNode
                LuaExpressionNode stringNode = expressionVisitor.visit(stringCtx.string());

                // 将这个单一的字符串节点包装成一个元素的数组
                return new LuaExpressionNode[]{stringNode};

            }
            default ->
                // 健壮性代码
                    throw new IllegalStateException("Unsupported args context type: " + argsCtx.getClass().getSimpleName());
        }

    }


    /**
     * 辅助方法：解析一个 nameList 语法节点，并将其中的所有标识符
     * 提取为一个 TruffleString 的列表。
     *
     * @param nameListCtx ANTLR 的 nameList 上下文对象。
     * @return 一个包含所有变量名的 List<TruffleString>。
     */
    private List<TruffleString> parseNameList(LuaParser.NamelistContext nameListCtx) {
        if (nameListCtx == null) {
            // 在某些语法中 namelist 可能是可选的，做个健壮性检查
            return Collections.emptyList();
        }

        List<TruffleString> names = new ArrayList<>();

        // ANTLR 会将所有匹配 `NAME` 规则的 TerminalNode 收集到一个列表中。
        // 我们可以通过 `nameListCtx.NAME()` 来访问这个列表。
        for (TerminalNode nameNode : nameListCtx.NAME()) {
            // 1. 获取标识符的文本内容 (e.g., "k", "v")
            // 2. 将 TerminalNode 转换为 TruffleString
            // 3. 添加到结果列表中
            names.add(asTruffleString(nameNode.getSymbol(), false));
        }

        return names;
    }

    /**
     * 为函数创建一个“序言”节点。
     * 这个节点负责在函数开始时，将传入的参数写入局部变量槽位。
     * @param parameterNames 参数名列表
     * @return 一个包含所有参数赋值语句的 LuaBlockNode
     */
    private LuaStatementNode createParameterPrologue(List<TruffleString> parameterNames) {
        if (parameterNames.isEmpty()) {
            return LuaNopNode.INSTANCE;
        }


        LuaStatementNode[] prologueStatements = new LuaStatementNode[parameterNames.size()];
        for (int i = 0; i < parameterNames.size(); i++) {
            TruffleString paramName = parameterNames.get(i);
            // 查找槽位索引
            VariableResolutionResult result = findVariable(paramName);
            int slotIndex = result.slotIndex();

            // 为每个参数创建一个 LuaWriteArgumentNode
            // 参数 i 对应 argument 数组中的索引 i
            prologueStatements[i] = LuaWriteArgumentNodeGen.create(i, slotIndex);
        }

        return new LuaBlockNode(prologueStatements);
    }

    /**
     * 辅助方法：解析一个 funcName，并为其创建一个写入节点。
     * @param funcNameCtx ANTLR 的 funcName 上下文。
     * @return 一个 LuaWriteGlobalVariableNode 或 LuaWriteTableNode。
     */
    private LuaWriteVariableNode createWriteNodeForFuncName(LuaParser.FuncnameContext funcNameCtx) {
        List<TerminalNode> names = funcNameCtx.NAME();

        if (names.size() == 1 && funcNameCtx.COL() == null) {
            TruffleString varName = toTruffleString(names.getFirst().getText());
            return getLuaWriteVariableNode(varName);
        }

        // --- 情况 B: 带路径的函数名 `function a.b.c() ...` ---
        // 这个路径 `a.b.c` 等价于 `(a.b).c`

        // 1. 链条的起点 `a`，是一个读取表达式
        LuaExpressionNode currentReceiver = createReadNodeForName(names.getFirst().getSymbol());

        // 2. 迭代中间部分 `... .b ...`
        for (int i = 1; i < names.size() - 1; i++) {
            TruffleString keyName = toTruffleString(names.get(i).getText());
            LuaExpressionNode keyNode = new LuaStringLiteralNode(keyName);
            // 不断地用 LuaReadTableNode 包装
            currentReceiver = LuaReadTableNodeGen.create(currentReceiver, keyNode);
        }

        // 3. 链条的终点 `... .c`，是我们要写入的【键】
        // 最后一个名字（或冒号后的名字）是 key
        TerminalNode finalNameNode = (funcNameCtx.COL() != null) ?
                funcNameCtx.NAME(names.size() - 1) :
                names.getLast();

        TruffleString finalKeyName = toTruffleString(finalNameNode.getText());
        LuaExpressionNode finalKeyNode = new LuaStringLiteralNode(finalKeyName);

        // 最终，我们得到了一个指向 `a.b` 的表达式 (currentReceiver)
        // 和一个代表键 `"c"` 的表达式 (finalKeyNode)。
        // 用它们来创建一个 LuaWriteTableNode。
        return LuaWriteTableNodeGen.create(currentReceiver, finalKeyNode);
    }

    private LuaWriteVariableNode getLuaWriteVariableNode(TruffleString varName) {
        VariableResolutionResult result = findVariable(varName);
        if (result == null) {
            return LuaWriteGlobalVariableNodeGen.create(varName);
        } else {
            LuaWriteVariableNode writeNode;
            if (result.depth() == 0) {
                writeNode = LuaWriteLocalVariableNodeGen.create(result.slotIndex());
            } else {
                writeNode = LuaWriteUpvalueNodeGen.create(result.depth(), result.slotIndex());
            }
            if (result.attribute() == LocalAttribute.CONST) {
                return new LuaWriteConstVariableNode(writeNode);
            }
            return writeNode;
        }
    }


    private LuaExpressionNode parseFunction(LuaParser.FuncbodyContext funcBody, TruffleString functionName){
        return parseFunction(null,funcBody,functionName);
    }

    private void enterLoop() {
        loopDepth++;
    }

    private void exitLoop() {
        loopDepth--;
    }

    private static String hiddenSlotName(String prefix, Token token) {
        return "$" + prefix + "@" + token.getStartIndex();
    }

    private void enterControlInfo() {
        controlInfoStack.push(new FunctionControlInfo());
    }

    private void exitControlInfo() {
        controlInfoStack.pop();
    }

    private void enterBlockInfo() {
        BlockControlInfo parent = blockInfoStack.peek();
        int[] parentPath = parent == null ? new int[0] : parent.path();
        int[] path = Arrays.copyOf(parentPath, parentPath.length + 1);
        path[path.length - 1] = nextBlockId++;
        blockInfoStack.push(new BlockControlInfo(path));
    }

    private void exitBlockInfo() {
        blockInfoStack.pop();
    }

    private BlockControlInfo currentBlockInfo() {
        BlockControlInfo block = blockInfoStack.peek();
        if (block == null) {
            throw new IllegalStateException("no active parser block");
        }
        return block;
    }

    private void addLabel(TruffleString label, Token token) {
        FunctionControlInfo info = controlInfoStack.peek();
        LabelInfo labelInfo = new LabelInfo(label, token, currentBlockInfo().path(), visibleLocalIds());
        if (hasVisibleLabel(info, labelInfo)) {
            throw new LuaParseError(source, token.getLine(), token.getCharPositionInLine(), token.getText().length(), "label '" + token.getText() + "' already defined");
        }
        info.labels.computeIfAbsent(label, ignored -> new ArrayList<>()).add(labelInfo);
    }

    private void addGoto(TruffleString label, Token token, LuaGotoNode node) {
        controlInfoStack.peek().gotos.add(new PendingGoto(label, token, node, currentBlockInfo().path(), visibleLocalIds()));
    }

    private void validateControlInfo() {
        FunctionControlInfo info = controlInfoStack.peek();
        for (PendingGoto pendingGoto : info.gotos) {
            LabelInfo label = findLabel(info, pendingGoto);
            if (label == null) {
                Token token = pendingGoto.token();
                throw new LuaParseError(source, token.getLine(), token.getCharPositionInLine(), token.getText().length(), "no visible label '" + token.getText() + "' for goto");
            }
            validateGotoTarget(pendingGoto, label);
            pendingGoto.node().setTargetBlockPath(label.blockPath());
            pendingGoto.node().setCloseOnJump(createCloseOnJumpNodes(info, pendingGoto, label));
        }
    }

    private static boolean hasVisibleLabel(FunctionControlInfo info, LabelInfo candidate) {
        List<LabelInfo> sameNameLabels = info.labels.get(candidate.label());
        if (sameNameLabels == null) {
            return false;
        }
        for (LabelInfo existing : sameNameLabels) {
            if (sharesVisibleScope(existing.blockPath(), candidate.blockPath())) {
                return true;
            }
        }
        return false;
    }

    private static LabelInfo findLabel(FunctionControlInfo info, PendingGoto pendingGoto) {
        List<LabelInfo> sameNameLabels = info.labels.get(pendingGoto.label());
        if (sameNameLabels == null) {
            return null;
        }
        LabelInfo result = null;
        for (LabelInfo label : sameNameLabels) {
            if (isPrefix(label.blockPath(), pendingGoto.blockPath())) {
                if (result != null) {
                    return null;
                }
                result = label;
            }
        }
        if (result != null) {
            return result;
        }
        return sameNameLabels.size() == 1 ? sameNameLabels.getFirst() : null;
    }

    private void validateGotoTarget(PendingGoto pendingGoto, LabelInfo label) {
        Token token = pendingGoto.token();
        if (!isPrefix(label.blockPath(), pendingGoto.blockPath())) {
            throw new LuaParseError(source, token.getLine(), token.getCharPositionInLine(), token.getText().length(), "goto '" + token.getText() + "' jumps into a block");
        }
        for (Integer labelLocalId : label.visibleLocalIds()) {
            if (!pendingGoto.visibleLocalIds().contains(labelLocalId)) {
                throw new LuaParseError(source, token.getLine(), token.getCharPositionInLine(), token.getText().length(), "goto '" + token.getText() + "' jumps into the scope of a local variable");
            }
        }
    }

    private List<Integer> visibleLocalIds() {
        List<Integer> result = new ArrayList<>();
        Iterator<BlockControlInfo> iterator = blockInfoStack.descendingIterator();
        while (iterator.hasNext()) {
            result.addAll(iterator.next().localDeclarationIds);
        }
        return result;
    }

    private static boolean isPrefix(int[] prefix, int[] path) {
        if (prefix.length > path.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (prefix[i] != path[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean sharesVisibleScope(int[] first, int[] second) {
        return isPrefix(first, second) || isPrefix(second, first);
    }

    private static LuaCloseSlotNode[] createCloseNodes(BlockControlInfo blockInfo) {
        if (blockInfo.closeSlots.isEmpty()) {
            return EMPTY_CLOSE_NODES;
        }
        LuaCloseSlotNode[] result = new LuaCloseSlotNode[blockInfo.closeSlots.size()];
        for (int i = 0; i < result.length; i++) {
            CloseSlot slot = blockInfo.closeSlots.get(i);
            result[i] = new LuaCloseSlotNode(slot.slotIndex(), slot.name());
        }
        return result;
    }

    private static LuaCloseSlotNode[] createCloseOnJumpNodes(FunctionControlInfo info, PendingGoto pendingGoto, LabelInfo label) {
        if (info.closeSlots.isEmpty()) {
            return EMPTY_CLOSE_NODES;
        }
        List<LuaCloseSlotNode> result = new ArrayList<>();
        for (Integer localId : pendingGoto.visibleLocalIds()) {
            if (label.visibleLocalIds().contains(localId)) {
                continue;
            }
            CloseSlot closeSlot = info.closeSlots.get(localId);
            if (closeSlot != null) {
                result.add(new LuaCloseSlotNode(closeSlot.slotIndex(), closeSlot.name()));
            }
        }
        return result.isEmpty() ? EMPTY_CLOSE_NODES : result.toArray(EMPTY_CLOSE_NODES);
    }

    private LocalAttribute parseLocalAttribute(LuaParser.AttribContext ctx) {
        if (ctx == null || ctx.NAME() == null) {
            return LocalAttribute.NONE;
        }
        String attributeName = ctx.NAME().getText();
        return switch (attributeName) {
            case "const" -> LocalAttribute.CONST;
            case "close" -> LocalAttribute.CLOSE;
            default -> throw new LuaParseError(source, ctx.NAME().getSymbol().getLine(), ctx.NAME().getSymbol().getCharPositionInLine(), attributeName.length(), "unknown local attribute '" + attributeName + "'");
        };
    }

    private static final class FunctionControlInfo {
        private final Map<TruffleString, List<LabelInfo>> labels = new HashMap<>();
        private final List<PendingGoto> gotos = new ArrayList<>();
        private final Map<Integer, CloseSlot> closeSlots = new HashMap<>();
    }

    private static final class BlockControlInfo {
        private final int[] path;
        private final List<Integer> localDeclarationIds = new ArrayList<>();
        private final List<CloseSlot> closeSlots = new ArrayList<>();

        private BlockControlInfo(int[] path) {
            this.path = path;
        }

        private int[] path() {
            return path;
        }
    }

    private record LabelInfo(TruffleString label, Token token, int[] blockPath, List<Integer> visibleLocalIds) {}

    private record PendingGoto(TruffleString label, Token token, LuaGotoNode node, int[] blockPath, List<Integer> visibleLocalIds) {}

    private record CloseSlot(int declarationId, int slotIndex, TruffleString name) {}

    private record ParamState(int fixedParamCount, boolean variadic) {}


    /**
     * 解析一个函数定义，并将其转换为一个 LuaFunctionLiteralNode 表达式节点。
     * 在 Lua 中，函数定义本身是一个表达式，其执行结果是一个函数对象（闭包）。
     *
     * @param funcNameCtx  来自 ANTLR 的函数名上下文，可能为 null（例如匿名函数）。
     * @param funcBody     来自 ANTLR 的函数体上下文（参数列表和代码块）。
     * @param functionName 用于调试的函数名。
     * @return 一个 LuaFunctionLiteralNode，当它被执行时，会创建一个新的 Lua 函数实例。
     */
    private LuaExpressionNode parseFunction(LuaParser.FuncnameContext funcNameCtx, LuaParser.FuncbodyContext funcBody, TruffleString functionName) {
        // --- 1. 参数解析 ---
        // 初始化一个列表，用于存储解析出的函数参数的名称。
        List<TruffleString> parameterNames = new ArrayList<>();
        var parList = funcBody.parlist();
        boolean isVariadic = false;


        // 检查参数列表是否存在且不为空。
        if (parList instanceof LuaParser.ParlistNamelistContext parCtx) {
            // 遍历所有在源代码中显式声明的参数名。
            for (TerminalNode node : parCtx.namelist().NAME()) {
                Token paramToken = node.getSymbol();
                // 将 ANTLR 的 Token 转换为 TruffleString，这是 Truffle 语言的标准字符串类型。
                TruffleString paramName = asTruffleString(paramToken, false);
                parameterNames.add(paramName);
            }
        }

        // 处理 '...' (可变参数标记)
        // 检查 parlist 中是否包含 "..." 的 token。
        if (parList instanceof LuaParser.ParlistVarargContext
                || parList instanceof LuaParser.ParlistNamelistContext parCtx && parCtx.DDD() != null) {
            isVariadic = true;
        }


        // --- 2. 处理方法定义的语法糖 (冒号 ':') ---
        // 【关键】检查函数定义时是否使用了冒号，例如 `function table:myMethod()`
        if (funcNameCtx != null && funcNameCtx.COL() != null) {
            // 如果是方法定义 (a:b)，Lua 语法会自动在参数列表的最前面插入一个名为 "self" 的隐式参数。
            // 我们在这里模拟这个行为，将 "self" 作为第一个参数添加到列表中。
            // 这使得在方法体内可以直接使用 `self` 访问对象实例。
            parameterNames.addFirst(toTruffleString("self"));
        }

        int fixedParamCount = parameterNames.size();

        boolean functionScopeEntered = false;
        boolean paramStatePushed = false;
        FrameDescriptor frameDescriptor;
        LuaBlockNode fullBodyNode;
        try {
            // --- 3. 作用域管理与函数体解析 ---
            // 【进入作用域】为当前函数创建一个新的词法作用域。
            // 此操作会将所有参数名注册为这个新作用域内的局部变量。
            // 这是构建 FrameDescriptor 的前置步骤。
            suppressLocalTracking = true;
            try {
                enterFunctionScope(parameterNames);
                functionScopeEntered = true;
            } finally {
                suppressLocalTracking = false;
            }

            paramStateStack.push(new ParamState(currentFixedParamCount, currentIsVariadic));
            paramStatePushed = true;
            currentFixedParamCount = fixedParamCount;
            currentIsVariadic = isVariadic;

            // 【解析函数体】递归地调用 statementVisitor 来解析函数体内的代码块。
            // 这会将函数体内的所有语句转换成一个 LuaBlockNode。
            enterControlInfo();
            enterBlockInfo();
            LuaBlockNode bodyNode;
            try {
                bodyNode = (LuaBlockNode) visit(funcBody.block());
                validateControlInfo();
            } finally {
                exitBlockInfo();
                exitControlInfo();
            }

            // --- 4.【关键修改】构建函数序言 (Prologue) ---
            // 序言是一段在函数体执行前自动运行的代码，它的核心任务是处理参数。
            // 它负责将调用时传入的实际参数值，赋值给我们刚刚解析出的、代表参数的局部变量。
            // 例如，对于 `function(a, b)`，序言会生成类似 `local a = arguments[0]; local b = arguments[1];` 的逻辑。
            LuaStatementNode prologueNode = createParameterPrologue(parameterNames);

            // 【组合函数体】将序言节点和原始的用户代码块节点组合成一个新的、完整的函数体。
            // 这样，当函数被调用时，会先执行参数赋值（序言），然后再执行用户的代码。
            fullBodyNode = new LuaBlockNode(new LuaStatementNode[]{ prologueNode, bodyNode });

            // --- 5. 构建 Truffle AST 节点 ---
            // 【退出作用域】关闭当前函数的作用域。
            // 这个操作会完成 FrameDescriptor 的构建，该描述符定义了此函数栈帧的布局（即包含了多少个局部变量槽位）。
            frameDescriptor = exitFunctionScope();
            functionScopeEntered = false;
        } finally {
            if (functionScopeEntered) {
                exitFunctionScope();
            }
            if (paramStatePushed) {
                ParamState prev = paramStateStack.pop();
                currentFixedParamCount = prev.fixedParamCount();
                currentIsVariadic = prev.variadic();
            }
        }

        int start = getStart(funcNameCtx, funcBody);
        final SourceSection functionSrc = source.createSection(start, funcBody.stop.getStopIndex() - start + 1);

        // 【创建根节点】创建函数的 RootNode。这是 Truffle 中任何可执行代码单元的顶层容器。
        // 它封装了执行函数所需的所有信息：语言实例、栈帧布局(frameDescriptor)、
        // 完整的可执行代码(fullBodyNode)以及用于调试的函数名。
        final LuaRootNode rootNode = new LuaAstRootNode(language, frameDescriptor, fullBodyNode,functionSrc,functionName);

        // 【创建函数字面量节点】
        // 这个节点是一个表达式节点。当它在运行时被执行时，它的作用就是创建一个真正的 LuaFunction 对象（闭包）。
        // 这个闭包对象将编译好的代码（通过 rootNode.getCallTarget() 获取）与当前的词法环境打包在一起。
        var func = new LuaFunctionLiteralNode(rootNode.getCallTarget(),funcNameCtx != null ? toTruffleString(funcNameCtx.NAME().getLast().getText()) : null);
        // 为节点附加源代码位置信息，用于调试和错误报告。
        func.addExpressionTag();

        // 返回这个函数字面量节点，它将被插入到父节点的 AST 中。
        return func;
    }

    private static int getStart(LuaParser.FuncnameContext funcNameCtx, LuaParser.FuncbodyContext funcBody) {
        int start;
        if (funcNameCtx != null && funcNameCtx.start != null) {
            // Function definition like: function foo()
            start = funcNameCtx.start.getStartIndex();
        } else if (funcBody.getParent() != null && funcBody.getParent().start != null && funcBody.getParent().start.getText().equals("function")) {
            // Anonymous function definition like: function()
            start = funcBody.getParent().start.getStartIndex();
        } else {
            // Fallback
            start = funcBody.start.getStartIndex();
        }
        return start;
    }

    private static boolean isHaltInCondition(LuaStatementNode statement) {
        return (statement instanceof LuaIfNode) || (statement instanceof LuaWhileNode);
    }

    // 辅助方法，用于从 ANTLR Context 设置 SourceSection
    private static void setSourceSectionFromContext(LuaStatementNode node, ParserRuleContext ctx) {
            int start = ctx.getStart().getStartIndex();
            int length = ctx.getStop().getStopIndex() - start + 1;
            node.setSourceSection(start, length);
    }


    private static void srcFromToken(LuaStatementNode node, Token token) {
        node.setSourceSection(token.getStartIndex(), token.getStopIndex() - token.getStartIndex() + 1);
    }

    @TruffleBoundary
    private static TruffleString parseLuaLongString(String rawString) {
        int equalsCount = 0;
        while (1 + equalsCount < rawString.length() && rawString.charAt(1 + equalsCount) == '=') {
            equalsCount++;
        }

        int contentStart = equalsCount + 2;
        int contentEnd = rawString.length() - equalsCount - 2;
        String content = rawString.substring(contentStart, contentEnd);
        if (content.startsWith("\r\n")) {
            content = content.substring(2);
        } else if (content.startsWith("\n") || content.startsWith("\r")) {
            content = content.substring(1);
        }
        return toTruffleString(content);
    }

    @TruffleBoundary
    private TruffleString unescapeLuaString(String rawString, Token token) {
        StringBuilder sb = new StringBuilder(rawString.length());
        int i = 0;
        while (i < rawString.length()) {
            char c = rawString.charAt(i);
            if (c == '\\') {
                i++;
                if (i >= rawString.length()) {
                    sb.append('\\');
                    break;
                }
                char next = rawString.charAt(i);
                switch (next) {
                    case 'a': sb.append('\u0007'); i++; break;
                    case 'b': sb.append('\b'); i++; break;
                    case 'f': sb.append('\f'); i++; break;
                    case 'n', '\n': sb.append('\n'); i++; break;
                    case 'r': sb.append('\r'); i++; break;
                    case 't': sb.append('\t'); i++; break;
                    case 'v': sb.append('\u000B'); i++; break;
                    case '\\': sb.append('\\'); i++; break;
                    case '"': sb.append('"'); i++; break;
                    case '\'': sb.append('\''); i++; break;
                    case '\r': sb.append('\n'); i++; if (i < rawString.length() && rawString.charAt(i) == '\n') i++; break;
                    case 'z':
                        do {
                            i++;
                        } while (i < rawString.length() && isLuaWhitespace(rawString.charAt(i)));
                        break;
                    case 'x': {
                        i++;
                        int hexVal = 0;
                        int digits = 0;
                        while (i < rawString.length() && digits < 2) {
                            int d = hexDigit(rawString.charAt(i));
                            if (d < 0) break;
                            hexVal = hexVal * 16 + d;
                            digits++;
                            i++;
                        }
                        if (digits == 0) {
                            throwInvalidEscape(token, "invalid escape sequence near '\\x'");
                        }
                        sb.append((char) hexVal);
                        break;
                    }
                    case 'u': {
                        i++;
                        if (i >= rawString.length() || rawString.charAt(i) != '{') {
                            throwInvalidEscape(token, "invalid escape sequence near '\\u'");
                        }
                        i++;
                        int uniVal = 0;
                        int uniDigits = 0;
                        while (i < rawString.length() && rawString.charAt(i) != '}') {
                            int d = hexDigit(rawString.charAt(i));
                            if (d < 0) {
                                throwInvalidEscape(token, "invalid escape sequence near '\\u{'");
                            }
                            uniVal = uniVal * 16 + d;
                            uniDigits++;
                            i++;
                        }
                        if (i >= rawString.length() || uniDigits == 0) {
                            throwInvalidEscape(token, "invalid escape sequence near '\\u{'");
                        }
                        i++; // skip '}'
                        if (!Character.isValidCodePoint(uniVal)) {
                            throwInvalidEscape(token, "UTF-8 value too large near '\\u{'");
                        }
                        sb.appendCodePoint(uniVal);
                        break;
                    }
                    default:
                        if (next >= '0' && next <= '9') {
                            int decVal = 0;
                            int decDigits = 0;
                            while (i < rawString.length() && decDigits < 3) {
                                char dc = rawString.charAt(i);
                                if (dc < '0' || dc > '9') break;
                                decVal = decVal * 10 + (dc - '0');
                                decDigits++;
                                i++;
                            }
                            if (decVal > 255) {
                                throwInvalidEscape(token, "decimal escape too large near '\\" + decVal + "'");
                            }
                            sb.append((char) decVal);
                        } else {
                            sb.append(next);
                            i++;
                        }
                        break;
                }
            } else {
                sb.append(c);
                i++;
            }
        }
        return toTruffleString(sb.toString());
    }

    private void throwInvalidEscape(Token token, String message) {
        throw new LuaParseError(source, token.getLine(), token.getCharPositionInLine(), token.getText().length(), message);
    }

    private static boolean isLuaWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f' || c == '\u000B';
    }

    private static int hexDigit(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        return -1;
    }

}
