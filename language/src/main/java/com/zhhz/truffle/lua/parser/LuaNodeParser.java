package com.zhhz.truffle.lua.parser;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.nodes.Node;
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
        final int functionEndPos = source.getLength() - 1;

        final SourceSection functionSrc = source.createSection(functionStartPos, functionEndPos);

        LuaStatementNode rootBlock = visitor.rootBlock;

        var frameDescriptorBuilder = visitor.exitMain();

        final LuaRootNode rootNode = new LuaAstRootNode(language, frameDescriptorBuilder, rootBlock, functionSrc ,toTruffleString(source.getName()));
        rootNode.insert(rootBlock);

        //whileNode(rootBlock,0);

        return rootNode.getCallTarget();
    }

    private static final String depthString = "\t".repeat(1000);

    private static void whileNode(Node _node,int depth){
        _node.getChildren().forEach(node -> {
            System.out.println(depthString.substring(0,depth) + node + " > " + node.getClass());
            whileNode(node,depth+1);
        });
    }


    private final LuaStatementVisitor statementVisitor = new LuaStatementVisitor();
    private final LuaExpressionVisitor expressionVisitor = new LuaExpressionVisitor();

    private LuaStatementNode rootBlock = null;

    // 【新增】当前正在解析的函数的参数信息
    private final int fixedParamCount;
    private final boolean isVariadic;

    protected LuaNodeParser(LuaLanguage language, Source source) {
        super(language, source);
        // 顶层脚本默认看作是可变参数函数 (可以接收命令行参数)，固定参数为 0
        this.fixedParamCount = 0;
        this.isVariadic = true;
    }

    private LuaNodeParser(LuaLanguage language, Source source,
                          Stack<Scope> scopes,
                          Stack<FrameDescriptor.Builder> stack,
                          int fixedParamCount,   // 【新增】
                          boolean isVariadic     // 【新增】
                          ) {
        super(language, source, scopes, stack);
        // 顶层脚本默认看作是可变参数函数 (可以接收命令行参数)，固定参数为 0
        this.fixedParamCount = fixedParamCount;
        this.isVariadic = isVariadic;
    }

    @Override
    public LuaStatementNode visitChunk(LuaParser.ChunkContext ctx) {
        enterMain();
        var value = visit(ctx.block());
        rootBlock = value;
        return value;
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
        // 直接委托给 visitFunctionCall
        return visit(ctx.functioncall());
    }


    @Override
    public LuaStatementNode visitStatAssign(LuaParser.StatAssignContext ctx) {
        return super.visitStatAssign(ctx);
    }



    private class LuaStatementVisitor extends LuaParserBaseVisitor<LuaStatementNode> {

        @Override
        public LuaStatementNode visitBlock(LuaParser.BlockContext ctx) {

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


            List<LuaStatementNode> flattenedNodes = new ArrayList<>(bodyNodes.size());
            flattenBlocks(bodyNodes, flattenedNodes);

            for (LuaStatementNode statement : flattenedNodes) {
                if (statement.hasSource() && !isHaltInCondition(statement)) {
                    statement.addStatementTag();
                }
            }
            LuaBlockNode blockNode = new LuaBlockNode(flattenedNodes.toArray(new LuaStatementNode[0]));
            setSourceSectionFromContext(blockNode,ctx);
            return blockNode;
        }

        @Override
        public LuaStatementNode visitStatBreak(LuaParser.StatBreakContext ctx) {
            LuaStatementNode luaBreakNode = new LuaBreakNode();
            srcFromToken(luaBreakNode,ctx.start);
            return luaBreakNode;
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

            // 2. 【委托解析】直接 visit `block` 子节点。
            //    `visitBlock` 会在这个新的、最顶层的作用域环境中，
            //    解析所有的子语句，并返回一个 `LuaBlockNode`。
            LuaStatementNode blockNode = visit(ctx.block());

            // 3. 【退出作用域】在 `block` 解析完成后，调用 exitBlockScope。
            //    这会从 `scopeStack` 中弹出刚刚为 `do...end` 块创建的作用域，
            //    从而确保其中的局部变量不会泄露到外部。
            exitBlockScope();

            // 4. 【返回结果】直接返回由 `visitBlock` 创建的 `LuaBlockNode`。
            //    从执行的角度看，`do...end` 块就是一个普通的语句块。
            return blockNode;
        }

        @Override
        public LuaStatementNode visitStatWhile(LuaParser.StatWhileContext ctx) {
            // 1. visit 条件表达式 `exp`
            LuaExpressionNode conditionNode = expressionVisitor.visit(ctx.exp());

            // 2. visit 循环体 `block`
            LuaStatementNode bodyNode = visit(ctx.block());

            // 3. 创建并返回我们的 LuaWhileNode
            return new LuaWhileNode(conditionNode, bodyNode);
        }

        @Override
        public LuaStatementNode visitStatRepeat(LuaParser.StatRepeatContext ctx) {
            // 1. 【进入新作用域】
            //    `repeat...until` 块本身形成了一个作用域，其内部的 local 变量
            //    在 `until` 条件中也可见。
            enterBlockScope();

            // 2. 【解析循环体】
            //    首先 visit `block`。这将在我们刚刚创建的新作用域中进行。
            LuaStatementNode bodyNode = visit(ctx.block());

            // 3. 【解析条件表达式】
            //    【关键】在【退出作用域之前】visit `exp`。
            //    这确保了 `exp` 的解析（包括其中的变量查找）
            //    能够看到 `block` 中定义的局部变量。
            LuaExpressionNode conditionNode = expressionVisitor.visit(ctx.exp());

            // 4. 【退出作用域】
            //    在循环体和条件都解析完毕后，我们才退出这个作用域。
            exitBlockScope();

            // 5. 【创建循环节点】
            //    创建并返回我们的 LuaRepeatNode。
            return new LuaRepeatNode(bodyNode, conditionNode);
        }

        @Override
        public LuaStatementNode visitStatIf(LuaParser.StatIfContext ctx) {

            // 1. 确定是否有 else 块
            LuaStatementNode elseBranch = null;
            int blockCount = ctx.block().size();
            int expCount = ctx.exp().size();
            if (blockCount > expCount) {
                // 如果块的数量比条件表达式的数量多，说明最后一个块是 else 块
                elseBranch = visit(ctx.block(blockCount - 1));
            }

            // 2. 从后往前处理 elseif 链
            // elseif 的条件表达式在 exp 列表中的索引是从 1 开始的
            for (int i = expCount - 1; i >= 1; i--) {
                LuaExpressionNode elseifCondition = expressionVisitor.visit(ctx.exp(i));
                // 对应的 block 也在相同的索引 i 处
                LuaStatementNode elseifThen = visit(ctx.block(i));
                elseBranch = new LuaIfNode(elseifCondition, elseifThen, elseBranch);
            }


            // 3. 处理最外层的 if-then
            LuaExpressionNode mainCondition = expressionVisitor.visit(ctx.exp(0));
            LuaStatementNode mainThen = visit(ctx.block(0));
            return new LuaIfNode(mainCondition, mainThen, elseBranch);
        }

        @Override
        public LuaStatementNode visitStatForNumeric(LuaParser.StatForNumericContext ctx) {
            enterBlockScope();

            TruffleString loopVarName = asTruffleString(ctx.NAME().getSymbol(), false);
            int loopVariableIndex = declareLocalVariable(loopVarName);

            LuaExpressionNode startNode = expressionVisitor.visit(ctx.exp(0));
            LuaExpressionNode limitNode = expressionVisitor.visit(ctx.exp(1));
            LuaExpressionNode stepNode = ctx.exp().size() > 2 ? expressionVisitor.visit(ctx.exp(2)) : null;

            LuaStatementNode bodyNode = visit(ctx.block());

            exitBlockScope();

            // 創建我們新的、方案 A 的節點
            return new LuaNumericForNode(loopVariableIndex, startNode, limitNode, stepNode, bodyNode);
        }

        @Override
        public LuaStatementNode visitStatForGeneric(LuaParser.StatForGenericContext ctx) {
            // 1. 【解析 in explist】
            // `in` 后面的表达式列表在【外部】作用域中解析。
            LuaExpressionNode[] expList = visitExpListAsArray(ctx.explist());
            LuaMultiValueNode expListNode = new LuaMultiValueNode(expList);

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
            LuaStatementNode bodyNode = visit(ctx.block());
            //setSourceSectionFromContext(bodyNode, ctx);

            // 5. 【退出作用域】
            exitBlockScope();

            // 6. 【创建 For 节点】
            return new LuaGenericForNode(expListNode, writeNodes, bodyNode);
        }

        @Override
        public LuaStatementNode visitStatAssign(LuaParser.StatAssignContext ctx) {
            //var value = super.visitStatAssign(ctx);
            // 1. 【创建 varNodes】
            //    调用一个辅助方法来处理 varlist，得到所有写入目标的节点数组。

            List<LuaWriteVariableNode> writeNodes = new ArrayList<>();
            for (LuaParser.VarContext varCtx : ctx.varlist().var()) {
                // 对于 varlist 中的每一个 var，我们都调用另一个更具体的辅助方法来创建写入节点。
                writeNodes.add(createWriteNodeForVar(varCtx));
            }

            LuaWriteVariableNode[] varNodes = writeNodes.toArray(new LuaWriteVariableNode[0]);


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

            // --- 阶段 1: 处理左侧 (LHS) - 变量声明 ---
            // 我们的目标是创建一个 `LuaWriteVariableNode[]` 数组，
            // 其中的每个节点都指向一个【新创建】的 FrameSlot。

            List<LuaWriteVariableNode> writeNodesList = new ArrayList<>();

            for (TerminalNode nameNode : ctx.attnamelist().NAME()) {
                TruffleString varName = asTruffleString(nameNode.getSymbol(), false);
                int slotIndex = declareLocalVariable(varName);
                LuaWriteLocalVariableNode writeNode = LuaWriteLocalVariableNodeGen.create(slotIndex);
                srcFromToken(writeNode, nameNode.getSymbol());

                writeNodesList.add(writeNode);
            }

            LuaExpressionNode[] valueExpList = visitExpListAsArray(ctx.explist());

            // 3. 【创建最终的赋值节点】
            //    将“目标”和“来源”组合成一个多重赋值语句节点。
            //    Truffle DSL 会为我们生成 LuaMultiAssignmentNodeGen 类。
            return new LuaMultiAssignmentNode(writeNodesList.toArray(new LuaWriteVariableNode[0]), valueExpList);
        }


        @Override
        public LuaStatementNode visitStatFunctionDef(LuaParser.StatFunctionDefContext ctx) {

            Token nameToken = ctx.funcname().NAME(0).getSymbol();

            var funcName = asTruffleString(nameToken, false);

            var functionLiteralNode = parseFunction(ctx.funcname(),ctx.funcbody(), funcName);


            // 调用一个新的辅助方法来处理复杂的 funcname 语法
            LuaWriteVariableNode writeNode = createWriteNodeForFuncName(ctx.funcname());

            LuaWriteVariableNode[] destination = new LuaWriteVariableNode[]{writeNode};

            return new LuaMultiAssignmentNode(destination, new LuaExpressionNode[]{functionLiteralNode});
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

            return new LuaMultiAssignmentNode(destinationNodes, new LuaExpressionNode[]{valueNode});
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
            final LuaExpressionNode[] valueNodes;
            // 检查是否存在 explist (即是否有返回值)
            if (ctx.explist() != null) {
                // 如果有返回值，遍历 explist 中的所有 exp
                int numReturns = ctx.explist().exp().size();
                valueNodes = new LuaExpressionNode[numReturns];

                for (int i = 0; i < numReturns; i++) {
                    // 递归访问每个表达式，并确保它是一个表达式节点
                    valueNodes[i] = expressionVisitor.visit(ctx.explist().exp(i));
                }
            } else {
                // 如果没有返回值 (例如 return;)，创建一个空数组
                valueNodes = new LuaExpressionNode[0];
            }

            // 创建 LuaReturnNode
            final LuaReturnNode returnNode = new LuaReturnNode(valueNodes);

            final int start = ctx.getStart().getStartIndex();
            //内容范围问题后续修复
            setSourceSectionFromContext(returnNode,ctx);

            return returnNode;
        }

        @Override
        public LuaStatementNode visitStatEmpty(LuaParser.StatEmptyContext ctx) {
            return super.visitStatEmpty(ctx);
        }

        private void flattenBlocks(Iterable<? extends LuaStatementNode> bodyNodes, List<LuaStatementNode> flattenedNodes) {
            for (LuaStatementNode n : bodyNodes) {
                if (n instanceof LuaBlockNode) {
                    flattenBlocks(((LuaBlockNode) n).getBlock(), flattenedNodes);
                } else {
                    flattenedNodes.add(n);
                }
            }
        }
    }

    private class LuaExpressionVisitor extends LuaParserBaseVisitor<LuaExpressionNode> {

        @Override
        public LuaExpressionNode visitStatFunctionCall(LuaParser.StatFunctionCallContext ctx) {
            return visit(ctx.functioncall());
        }

        private LuaExpressionNode createBinary(List<? extends ParserRuleContext> children,  TerminalNode op) {
            if (op == null) {
                assert children.size() == 1;
                return visit(children.getFirst());
            } else {
                assert children.size() == 2;
                return createBinary(op.getSymbol(), visit(children.get(0)), visit(children.get(1)));
            }
        }

        private LuaExpressionNode createBinary(Token opToken, LuaExpressionNode leftNode, LuaExpressionNode rightNode) {

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
                case ">" -> LuaLogicalNotNodeGen.create(LuaLessOrEqualNodeGen.create(leftUnboxed, rightUnboxed));
                case ">=" -> LuaLogicalNotNodeGen.create(LuaLessThanNodeGen.create(leftUnboxed, rightUnboxed));
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

            int start = leftNode.getSourceCharIndex();
            int length = rightNode.getSourceEndIndex() - start;
            if (length + start > source.getLength()){
                System.out.println("source = " + source.getCharacters());
            }
            result.setSourceSection(start, length);
            result.addExpressionTag();

            return result;
        }

        @Override
        public LuaExpressionNode visitExpLogicalOr(LuaParser.ExpLogicalOrContext ctx) {
            return createBinary(ctx.getRuleContexts(ParserRuleContext.class), ctx.OR());
        }

        @Override
        public LuaExpressionNode visitExpLogicalAnd(LuaParser.ExpLogicalAndContext ctx) {
            return createBinary(ctx.getRuleContexts(ParserRuleContext.class), ctx.AND());
        }

        @Override
        public LuaExpressionNode visitExpComparison(LuaParser.ExpComparisonContext ctx) {
            return createBinary(ctx.getRuleContexts(ParserRuleContext.class), ctx.getToken(ctx.op.getType(), 0));
        }


        @Override
        public LuaExpressionNode visitExpBitwiseOr(LuaParser.ExpBitwiseOrContext ctx) {
            return createBinary(ctx.getRuleContexts(ParserRuleContext.class), ctx.getToken(ctx.op.getType(), 0));
        }


        @Override
        public LuaExpressionNode visitExpBitwiseXor(LuaParser.ExpBitwiseXorContext ctx) {
            return createBinary(ctx.getRuleContexts(ParserRuleContext.class), ctx.SQUIG());
        }

        @Override
        public LuaExpressionNode visitExpBitwiseAnd(LuaParser.ExpBitwiseAndContext ctx) {
            return createBinary(ctx.getRuleContexts(ParserRuleContext.class), ctx.AMP());
        }

        @Override
        public LuaExpressionNode visitExpBitwiseShift(LuaParser.ExpBitwiseShiftContext ctx) {
            return createBinary(ctx.getRuleContexts(ParserRuleContext.class), ctx.getToken(ctx.op.getType(), 0));
        }

        @Override
        public LuaExpressionNode visitExpConcat(LuaParser.ExpConcatContext ctx) {
            return createBinary(ctx.getRuleContexts(ParserRuleContext.class), ctx.DD());
        }

        @Override
        public LuaExpressionNode visitExpAddSub(LuaParser.ExpAddSubContext ctx) {
            return createBinary(ctx.getRuleContexts(ParserRuleContext.class), ctx.getToken(ctx.op.getType(), 0));
        }

        @Override
        public LuaExpressionNode visitExpMulDivMod(LuaParser.ExpMulDivModContext ctx) {
            return createBinary(ctx.getRuleContexts(ParserRuleContext.class), ctx.getToken(ctx.op.getType(), 0));
        }


        @Override
        public LuaExpressionNode visitExpUnop(LuaParser.ExpUnopContext ctx) {
            LuaExpressionNode operandNode = visit(ctx.exp());
            String op = ctx.op.getText();

            final LuaExpressionNode resultNode;

            switch (op) {
                case "-":
                    resultNode = LuaUnaryMinusNodeGen.create(operandNode);
                    break;
                case "not":
                    resultNode = LuaLogicalNotNodeGen.create(operandNode);
                    break;
                case "#":
                    resultNode = LuaLengthNodeGen.create(operandNode);
                    break;
                case "~":
                    resultNode = LuaBitwiseNotNodeGen.create(operandNode);
                    break;
                default:
                    resultNode = null;
                    semErr(ctx.op,"未知错误");
            }

            setSourceSectionFromContext(resultNode, ctx);
            return resultNode;
        }

        @Override
        public LuaExpressionNode visitExpPower(LuaParser.ExpPowerContext ctx) {
            return createBinary(ctx.getRuleContexts(ParserRuleContext.class), ctx.CARET());
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
            var name = ctx.start;
            LuaStringLiteralNode node = new LuaStringLiteralNode(unescapeLuaString(asTruffleString(name,true).toJavaStringUncached()));
            node.addExpressionTag();
            setSourceSectionFromContext(node,ctx);
            return node;
        }

        @Override
        public LuaExpressionNode visitStringChar(LuaParser.StringCharContext ctx) {
            var name = ctx.start;
            LuaStringLiteralNode node = new LuaStringLiteralNode(unescapeLuaString(asTruffleString(name,true).toJavaStringUncached()));
            node.addExpressionTag();
            setSourceSectionFromContext(node,ctx);
            return node;
        }

        @Override
        public LuaExpressionNode visitExpVararg(LuaParser.ExpVarargContext ctx) {
            // 1. 语义检查：如果当前函数不是可变参数函数，使用 `...` 是语法错误
            if (!LuaNodeParser.this.isVariadic) {
                // 你应该抛出一个解析异常，或者语法错误
                throw new RuntimeException("cannot use '...' outside a vararg function");
            }

            // 2. 创建节点，传入当前函数的固定参数个数
            return new LuaReadVarargsNode(LuaNodeParser.this.fixedParamCount);
        }

        @Override
        public LuaExpressionNode visitExpNumber(LuaParser.ExpNumberContext ctx) {
            Token literalToken = ctx.number().start;
            String text = literalToken.getText();
            LuaExpressionNode result;

            // 检查是否是十六进制 (以 0x 或 0X 开头)
            if (text.startsWith("0x") || text.startsWith("0X")) {
                try {
                    // 1. 尝试解析为十六进制整数
                    // Long.decode 可以自动识别 "0x" 前缀并按16进制解析
                    // 使用 decode 而不是 parseLong 是因为它能处理负号（虽然 lexer 通常把负号分开）和 0x
                    result = new LuaLongLiteralNode(Long.decode(text));
                } catch (NumberFormatException ex) {
                    // 2. 如果 Long 解析失败（溢出或者包含小数点/指数），尝试解析为 Double
                    try {
                        result = new LuaDoubleLiteralNode(Double.parseDouble(text));
                    } catch (NumberFormatException ex2) {
                        // Java 的 Double.parseDouble 对十六进制浮点数要求比较严格 (必须有 p 指数)
                        // 如果 Lua 写了 "0xFF.8" (没有 p)，Java 会报错。
                        // 为了兼容性，我们可以尝试补全 "p0"
                        try {
                            result = new LuaDoubleLiteralNode(Double.parseDouble(text + "p0"));
                        } catch (NumberFormatException ex3) {
                            // 极其罕见的情况，抛出运行时错误
                            throw new RuntimeException("Invalid number format: " + text);
                        }
                    }
                }
            } else {
                // 十进制逻辑 (保持原样)
                try {
                    /* 如果文字足够小，可以放入长值中，请尝试。 */
                    result = new LuaLongLiteralNode(Long.parseLong(text));
                } catch (NumberFormatException ex) {
                    /* 长值溢出，或者包含小数点，因此退回到Double。 */
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
                throw new RuntimeException("异常");
            }

            int start = ctx.start.getStartIndex();
            int length = ctx.stop.getStartIndex() - start + 1;
            final LuaParenExpressionNode result = new LuaParenExpressionNode(expressionNode);
            result.addExpressionTag();
            setSourceSectionFromContext(result,ctx);
            return result;
        }

        @Override
        public LuaExpressionNode visitExpFunctionDef(LuaParser.ExpFunctionDefContext ctx) {
            // 对于匿名函数，我们没有一个明确的名字，所以可以传递一个
            // 像 "<anonymous>" 或基于源代码位置的调试名。
            TruffleString debugName;
            if (ctx.getParent().getParent().getParent() instanceof LuaParser.StatAssignContext statAssignContext){
                //静态查找函数名字，如果是表达式命名是不准的，用于开发阶段调试
                int index = statAssignContext.explist().exp().indexOf(ctx.getParent());
                if (index > -1){
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

            List<LuaTableFieldInitializerNode> initializers = new ArrayList<>();

            // 【关键】我们需要一个计数器来追踪列表式字段的索引
            int nextArrayIndex = 1; // Lua 数组索引从 1 开始

            for (LuaParser.FieldContext fieldCtx : ctx.fieldlist().field()) {
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

            return new LuaTableConstructorNode(initializers.toArray(new LuaTableFieldInitializerNode[0]));
        }

        @Override
        public LuaExpressionNode visitFuncCall(LuaParser.FuncCallContext ctx) {
            LuaExpressionNode functionNode = visit(ctx.prefixexp());

            LuaExpressionNode[] argNodes = visitArgsAsArray(ctx.args());
            for (LuaExpressionNode node : argNodes) {
                if (node == null){
                    throw new RuntimeException();
                }
            }
            var invoke = new LuaInvokeNode(functionNode, argNodes);
            invoke.addExpressionTag();
            setSourceSectionFromContext(invoke,ctx);
            return invoke;
        }

        @Override
        public LuaExpressionNode visitMethodCall(LuaParser.MethodCallContext ctx) {
            // 【冒号调用解糖的地方】

            // 1. 获取 "self" (冒号左边的对象)
            LuaExpressionNode receiverNode = visit(ctx.prefixexp());

            // 2. 构建要调用的函数表达式 (receiver.NAME)
            TruffleString methodName = asTruffleString(ctx.NAME().getSymbol());
            LuaExpressionNode keyNode = new LuaStringLiteralNode(methodName);
            LuaExpressionNode functionNode = LuaReadTableNodeGen.create(receiverNode, keyNode);

            // 3. 构建参数列表，将 "self" 作为第一个参数
            List<LuaExpressionNode> argNodesList = new ArrayList<>();
            argNodesList.add(receiverNode); // 隐式地插入 self

            LuaExpressionNode[] otherArgNodes = visitArgsAsArray(ctx.args());
            argNodesList.addAll(Arrays.asList(otherArgNodes));

            // 4. 创建调用节点
            var invoke =  new LuaInvokeNode(functionNode, argNodesList.toArray(new LuaExpressionNode[0]));
            invoke.addExpressionTag();
            setSourceSectionFromContext(invoke,ctx);

            return invoke;
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
            // 【核心】这个方法只有一个任务：
            // 忽略括号，直接继续向下 visit 括号内部的 `exp` 子节点。
            // `visit(ctx.exp())` 会根据 `exp` 的具体内容 (是一个加法、一个变量名、
            // 还是另一个函数调用等)，自动地、递归地调用正确的 visit 方法，
            // 并最终返回一个代表该表达式的、正确的 LuaExpressionNode。
            return visit(ctx.exp());
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
            return LuaReadTableNodeGen.create(tableNode, keyNode);
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
            return LuaReadTableNodeGen.create(tableNode, keyNode);
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
                VariableResolutionResult result = findVariable(name);

                // 2. 【进行调度】根据查找结果创建不同的写入节点
                if (result == null) {
                    // 情况 A.1: 在所有局部/外层作用域都找不到
                    // -> 这是一个对全局变量的写入
                    return LuaWriteGlobalVariableNodeGen.create(name); // 假设它也接收 SourceSection
                } else {
                    if (result.depth() == 0) {
                        // 情况 A.2: 深度为 0，变量在当前函数的 Frame 中
                        // -> 这是一个对本地变量的写入
                        return LuaWriteLocalVariableNodeGen.create(result.slotIndex());
                    } else {
                        // 情况 A.3: 深度 > 0，变量在外层函数的 Frame 中
                        // -> 这是一个对上值 (Upvalue) 的写入
                        return LuaWriteUpvalueNodeGen.create(result.depth(), result.slotIndex());
                    }
                }

                // 2. 【进行调度】根据查找结果创建不同的写入节点
            }
            case LuaParser.VarIndexedContext indexedCtx -> {
                // --- 情况 B1: 处理 `...[exp]` 形式的写入目标 ---

                // a. 【递归】visit 左边的 `var` 部分，得到 table 表达式
                LuaExpressionNode tableNode = expressionVisitor.visit(indexedCtx.prefixexp());

                // b. visit 括号内的 `exp` 部分，得到 key 表达式
                LuaExpressionNode keyNode = expressionVisitor.visit(indexedCtx.exp());

                // c. 用它们创建一个 LuaWriteTableNode
                return LuaWriteTableNodeGen.create(tableNode, keyNode);

            }
            case LuaParser.VarMemberAccessContext memberCtx -> {
                // --- 情况 B2: 处理 `... .NAME` 形式的写入目标 ---
                // a. 【递归】visit 左边的 `var` 部分，得到 table 表达式
                LuaExpressionNode tableNode = expressionVisitor.visit(memberCtx.prefixexp());

                // b. 将 `NAME` 转换为字符串字面量作为 key
                TruffleString keyName = asTruffleString(memberCtx.NAME().getSymbol(), false);
                LuaExpressionNode keyNode = new LuaStringLiteralNode(keyName);

                // c. 用它们创建一个 LuaWriteTableNode
                return LuaWriteTableNodeGen.create(tableNode, keyNode);

            }
            case LuaParser.FuncCallContext ignored -> throw  new LuaParseError(
                    source,
                    prefixExpCtx.start.getLine(),
                    prefixExpCtx.start.getCharPositionInLine() + 1,
                    prefixExpCtx.getText().length(),
                    "syntax error near '='"
            );
            default -> throw new LuaParseError(
                    source,
                    prefixExpCtx.start.getLine(),
                    prefixExpCtx.start.getCharPositionInLine() + 1,
                    prefixExpCtx.getText().length(),
                    "syntax error: unexpected symbol near '=' (cannot assign to " + prefixExpCtx.getClass().getSimpleName() + ")"
            );
        }

        // 情况 F: 其他非法情况 (例如 (a+b) = 1)
        // 括号表达式、二元运算结果等都不能作为左值


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
            return new LuaExpressionNode[0];
        }

        // 2. 准备容器：创建一个列表用于暂存转换后的 Truffle 节点
        List<LuaExpressionNode> expressions = new ArrayList<>();

        // 3. 遍历解析树：ctx.exp() 是 ANTLR 生成的方法，返回该规则下所有匹配的表达式(exp)上下文
        for (LuaParser.ExpContext expCtx : ctx.exp()) {
            // 4. 递归访问/转换：委托 expressionVisitor 将单个 ANTLR 表达式上下文转换为具体的 Truffle AST 节点
            // 这里的 expressionVisitor 通常是一个继承自 LuaParserBaseVisitor<LuaExpressionNode> 的实例
            expressions.add(expressionVisitor.visit(expCtx));
        }

        // 5. 格式转换与返回：将 List 转换为强类型的 LuaExpressionNode 数组
        // 在 Truffle 节点开发中，通常首选数组而非 List 来存储 @Children 子节点，以优化内存占用和遍历性能
        return expressions.toArray(new LuaExpressionNode[0]);
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
                return new LuaExpressionNode[0];
            }

            // --- 【关键】使用 instanceof 进行分派 ---
            case LuaParser.ArgsParenContext parenCtx -> {
                // --- 情况 1: f(...) - 带括号的参数列表 ---
                // 我们之前已经有了 visitExplistAsArray，这里完美复用！
                return visitExpListAsArray(parenCtx.explist());
                // --- 情况 1: f(...) - 带括号的参数列表 ---

                // 我们之前已经有了 visitExplistAsArray，这里完美复用！
            }
            case LuaParser.ArgsTableContext tableCtx -> {
                // --- 情况 2: f{...} - table 构造器作为唯一参数 ---

                // visit tableconstructor 会返回一个 LuaTableConstructorNode
                LuaExpressionNode tableNode = expressionVisitor.visit(tableCtx.tableconstructor());

                // 将这个单一的 table 节点包装成一个元素的数组
                return new LuaExpressionNode[]{tableNode};

                // 将这个单一的 table 节点包装成一个元素的数组
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


        List<LuaStatementNode> prologueStatements = new ArrayList<>();
        for (int i = 0; i < parameterNames.size(); i++) {
            TruffleString paramName = parameterNames.get(i);
            // 查找槽位索引
            VariableResolutionResult result = findVariable(paramName);
            int slotIndex = result.slotIndex();

            // 为每个参数创建一个 LuaWriteArgumentNode
            // 参数 i 对应 argument 数组中的索引 i
            prologueStatements.add(LuaWriteArgumentNodeGen.create(i, slotIndex));
        }

        return new LuaBlockNode(prologueStatements.toArray(new LuaStatementNode[0]));
    }

    /**
     * 辅助方法：解析一个 funcName，并为其创建一个写入节点。
     * @param funcNameCtx ANTLR 的 funcName 上下文。
     * @return 一个 LuaWriteGlobalVariableNode 或 LuaWriteTableNode。
     */
    private LuaWriteVariableNode createWriteNodeForFuncName(LuaParser.FuncnameContext funcNameCtx) {
        List<TerminalNode> names = funcNameCtx.NAME();

        if (names.size() == 1 && funcNameCtx.COL() == null) {
            // --- 情况 A: 简单函数名 `function f() ...` ---
            TruffleString varName = toTruffleString(names.getFirst().getText());
            // 在这里，我们需要进行作用域查找来决定是局部还是全局。
            // 但 StatFunctionDef 总是定义全局函数（除非被 `local` 修饰，那是另一个规则）。
            // 所以我们创建一个全局写入节点。
            return LuaWriteGlobalVariableNodeGen.create(varName);
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
                funcNameCtx.NAME(names.size()) : // a:b, names.size() == 1
                names.getLast();

        TruffleString finalKeyName = toTruffleString(finalNameNode.getText());
        LuaExpressionNode finalKeyNode = new LuaStringLiteralNode(finalKeyName);

        // 最终，我们得到了一个指向 `a.b` 的表达式 (currentReceiver)
        // 和一个代表键 `"c"` 的表达式 (finalKeyNode)。
        // 用它们来创建一个 LuaWriteTableNode。
        return LuaWriteTableNodeGen.create(currentReceiver, finalKeyNode);
    }


    private LuaExpressionNode parseFunction(LuaParser.FuncbodyContext funcBody, TruffleString functionName){
        return parseFunction(null,funcBody,functionName);
    }


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
        // 检查 parlist 中是否包含 "..." 的 token
        // 或者是 ParlistVarargContext
        if (parList instanceof LuaParser.ParlistVarargContext || parList.getText().endsWith("...")) {
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

        // --- 3. 作用域管理与函数体解析 ---
        // 【进入作用域】为当前函数创建一个新的词法作用域。
        // 此操作会将所有参数名注册为这个新作用域内的局部变量。
        // 这是构建 FrameDescriptor 的前置步骤。
        enterFunctionScope(parameterNames);

        LuaNodeParser visitor = new LuaNodeParser(language, source,scopeStack,fdBuilderStack, fixedParamCount, isVariadic);

        // 【解析函数体】递归地调用 statementVisitor 来解析函数体内的代码块。
        // 这会将函数体内的所有语句转换成一个 LuaBlockNode。
        LuaBlockNode bodyNode = (LuaBlockNode) visitor.visit(funcBody.block());

        // --- 4.【关键修改】构建函数序言 (Prologue) ---
        // 序言是一段在函数体执行前自动运行的代码，它的核心任务是处理参数。
        // 它负责将调用时传入的实际参数值，赋值给我们刚刚解析出的、代表参数的局部变量。
        // 例如，对于 `function(a, b)`，序言会生成类似 `local a = arguments[0]; local b = arguments[1];` 的逻辑。
        LuaStatementNode prologueNode = createParameterPrologue(parameterNames);

        // 【组合函数体】将序言节点和原始的用户代码块节点组合成一个新的、完整的函数体。
        // 这样，当函数被调用时，会先执行参数赋值（序言），然后再执行用户的代码。
        LuaBlockNode fullBodyNode = new LuaBlockNode(new LuaStatementNode[]{ prologueNode, bodyNode });

        // --- 5. 构建 Truffle AST 节点 ---
        // 【退出作用域】关闭当前函数的作用域。
        // 这个操作会完成 FrameDescriptor 的构建，该描述符定义了此函数栈帧的布局（即包含了多少个局部变量槽位）。
        FrameDescriptor frameDescriptor = exitFunctionScope();

        // TODO: 暂时从函数名开始标记位置,后续需要优化为整个函数位置
        int start = funcNameCtx != null ? funcNameCtx.start.getStartIndex() : funcBody.start.getStartIndex();
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

    private static boolean isHaltInCondition(LuaStatementNode statement) {
        return (statement instanceof LuaIfNode) || (statement instanceof LuaWhileNode);
    }

    // 辅助方法，用于从 ANTLR Context 设置 SourceSection
    private static void setSourceSectionFromContext(LuaStatementNode node, ParserRuleContext ctx) {
        int start = ctx.getStart().getStartIndex();
        int length = ctx.getStart().getStopIndex() - start + 1;
        node.setSourceSection(start, length);
    }


    private static void srcFromToken(LuaStatementNode node, Token token) {
        node.setSourceSection(token.getStartIndex(), token.getStopIndex() - token.getStartIndex() + 1);
    }

    @TruffleBoundary
    private static TruffleString unescapeLuaString(String rawString) {

        StringBuilder sb = new StringBuilder(rawString.length());
        boolean inEscape = false;
        for (int i = 0; i < rawString.length(); i++) {
            char c = rawString.charAt(i);
            if (inEscape) {
                // 上一个字符是 '\'
                switch (c) {
                    case 'a': sb.append('\u0007'); break; // bell
                    case 'b': sb.append('\b'); break;   // backspace
                    case 'f': sb.append('\f'); break;   // form feed
                    case 'n': sb.append('\n'); break;   // newline
                    case 'r': sb.append('\r'); break;   // carriage return
                    case 't': sb.append('\t'); break;   // tab
                    case 'v': sb.append('\u000B'); break; // vertical tab
                    case '\\': sb.append('\\'); break; // backslash
                    case '"': sb.append('"'); break;  // double quote
                    case '\'': sb.append('\''); break; // single quote
                    // 你还需要处理数字转义，如 \ddd 和 \xHH，这更复杂
                    default:
                        // 如果是无法识别的转义，Lua 的行为是直接保留，例如 \c -> c
                        sb.append(c);
                        break;
                }
                inEscape = false;
            } else {
                if (c == '\\') {
                    // 遇到了转义字符的开头
                    inEscape = true;
                } else {
                    // 普通字符，直接添加
                    sb.append(c);
                }
            }
        }
        return toTruffleString(sb.toString());
    }

}
