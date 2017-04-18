/*
 * The MIT License (MIT)
 *
 * Copyright (c) Despector <https://despector.voxelgenesis.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.despector.ast.stmt;

import org.spongepowered.despector.ast.AstVisitor;
import org.spongepowered.despector.ast.stmt.assign.ArrayAssignment;
import org.spongepowered.despector.ast.stmt.assign.InstanceFieldAssignment;
import org.spongepowered.despector.ast.stmt.assign.LocalAssignment;
import org.spongepowered.despector.ast.stmt.assign.StaticFieldAssignment;
import org.spongepowered.despector.ast.stmt.branch.Break;
import org.spongepowered.despector.ast.stmt.branch.DoWhile;
import org.spongepowered.despector.ast.stmt.branch.For;
import org.spongepowered.despector.ast.stmt.branch.ForEach;
import org.spongepowered.despector.ast.stmt.branch.If;
import org.spongepowered.despector.ast.stmt.branch.If.Elif;
import org.spongepowered.despector.ast.stmt.branch.If.Else;
import org.spongepowered.despector.ast.stmt.branch.Switch;
import org.spongepowered.despector.ast.stmt.branch.TryCatch;
import org.spongepowered.despector.ast.stmt.branch.TryCatch.CatchBlock;
import org.spongepowered.despector.ast.stmt.branch.While;
import org.spongepowered.despector.ast.stmt.invoke.InvokeStatement;
import org.spongepowered.despector.ast.stmt.misc.Comment;
import org.spongepowered.despector.ast.stmt.misc.Increment;
import org.spongepowered.despector.ast.stmt.misc.Return;
import org.spongepowered.despector.ast.stmt.misc.Throw;

public interface StatementVisitor extends AstVisitor {

    void visitArrayAssignment(ArrayAssignment arrayAssign);

    void visitBreak(Break breakStatement);

    void visitCatchBlock(CatchBlock catchBlock);

    void visitComment(Comment comment);

    void visitDoWhile(DoWhile doWhileLoop);

    void visitElif(Elif elseBlock);

    void visitElse(Else elseBlock);

    void visitFor(For forLoop);

    void visitForEach(ForEach forLoop);

    void visitIf(If ifBlock);

    void visitIncrement(Increment incrementStatement);

    void visitInstanceFieldAssignment(InstanceFieldAssignment stmt);

    void visitInvoke(InvokeStatement stmt);

    void visitLocalAssignment(LocalAssignment localAssign);

    void visitReturn(Return returnValue);

    void visitStaticFieldAssignment(StaticFieldAssignment staticFieldAssign);

    void visitSwitch(Switch tableSwitch);

    void visitSwitchCase(Switch.Case case1);

    void visitThrow(Throw throwException);

    void visitTryCatch(TryCatch tryBlock);

    void visitWhile(While whileLoop);

}
