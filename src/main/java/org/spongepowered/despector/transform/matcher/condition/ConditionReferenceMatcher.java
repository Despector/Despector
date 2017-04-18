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
package org.spongepowered.despector.transform.matcher.condition;

import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.insn.condition.Condition;
import org.spongepowered.despector.transform.matcher.ConditionMatcher;
import org.spongepowered.despector.transform.matcher.MatchContext;
import org.spongepowered.despector.util.AstUtil;

/**
 * A condition matcher that matches any condition references a given local
 * instance.
 */
public class ConditionReferenceMatcher implements ConditionMatcher<Condition> {

    private final LocalInstance local;
    private final String ctx_local;

    public ConditionReferenceMatcher(LocalInstance local) {
        this.local = local;
        this.ctx_local = null;
    }

    public ConditionReferenceMatcher(String ctx) {
        this.ctx_local = ctx;
        this.local = null;
    }

    @Override
    public Condition match(MatchContext ctx, Condition cond) {
        LocalInstance loc = this.local;
        if (loc == null) {
            loc = ctx.getLocal(this.ctx_local);
            if (loc == null) {
                return null;
            }
        }
        if (AstUtil.references(cond, loc)) {
            return cond;
        }
        return null;
    }

}
