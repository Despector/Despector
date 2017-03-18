/*
 * The MIT License (MIT)
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
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
package org.spongepowered.despector.util;

import org.spongepowered.despector.ast.members.insn.branch.condition.AndCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.BooleanCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.CompareCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.Condition;
import org.spongepowered.despector.ast.members.insn.branch.condition.InverseCondition;
import org.spongepowered.despector.ast.members.insn.branch.condition.OrCondition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ConditionUtil {

    /**
     * Creates the inverse of the given condition. Where it can this is
     * performed without resorting to wrapping in a {@link InverseCondition}.
     */
    public static Condition inverse(Condition condition) {
        if (condition instanceof BooleanCondition) {
            BooleanCondition b = (BooleanCondition) condition;
            return new BooleanCondition(b.getConditionValue(), !b.isInverse());
        } else if (condition instanceof AndCondition) {
            AndCondition and = (AndCondition) condition;
            List<Condition> args = new ArrayList<>();
            for (Condition arg : and.getOperands()) {
                args.add(inverse(arg));
            }
            return new OrCondition(args);
        } else if (condition instanceof OrCondition) {
            OrCondition and = (OrCondition) condition;
            List<Condition> args = new ArrayList<>();
            for (Condition arg : and.getOperands()) {
                args.add(inverse(arg));
            }
            return new AndCondition(args);
        } else if (condition instanceof CompareCondition) {
            CompareCondition cmp = (CompareCondition) condition;
            return new CompareCondition(cmp.getLeft(), cmp.getRight(), cmp.getOperator().inverse());
        }
        return new InverseCondition(condition);
    }

    /**
     * Gets if two conditions are inverses of each other.
     */
    public static boolean isInverse(Condition a, Condition other) {
        if (other instanceof BooleanCondition && a instanceof BooleanCondition) {
            BooleanCondition ab = (BooleanCondition) a;
            BooleanCondition ob = (BooleanCondition) other;
            return ab.getConditionValue().equals(ob.getConditionValue()) && ab.isInverse() != ob.isInverse();
        }
        if (other instanceof CompareCondition && a instanceof CompareCondition) {
            CompareCondition ab = (CompareCondition) a;
            CompareCondition ob = (CompareCondition) other;
            return ab.getLeft().equals(ob.getLeft()) && ab.getRight().equals(ob.getRight()) && ab.getOperator() == ob.getOperator().inverse();
        }
        return false;
    }

    private static int getMapping(Map<Condition, Integer> mapping, Condition condition) {
        if (mapping.containsKey(condition)) {
            return mapping.get(condition);
        }
        int highest = 1;
        for (Condition key : mapping.keySet()) {
            int kvalue = mapping.get(key);
            if (isInverse(condition, key)) {
                mapping.put(condition, -kvalue);
                return -kvalue;
            }
            if (condition.equals(key)) {
                mapping.put(condition, kvalue);
                return kvalue;
            }
            if (kvalue >= highest) {
                highest = kvalue + 1;
            }
        }
        mapping.put(condition, highest);
        return highest;
    }

    private static int[] encode(AndCondition and, Map<Condition, Integer> mapping) {
        int[] encoding = new int[and.getOperands().size()];
        int i = 0;
        for (Condition c : and.getOperands()) {
            int m = getMapping(mapping, c);
            encoding[i++] = m;
        }
        return encoding;
    }

    /**
     * Gets is the first param contains the inverse of the second param.
     */
    private static boolean containsInverse(int[] a, int[] b) {
        outer: for (int i = 0; i < b.length; i++) {
            int next = b[i];
            for (int o = 0; o < a.length; o++) {
                if (a[o] == -next) {
                    continue outer;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Gets is the first param contains the second param.
     */
    private static boolean contains(int[] a, int[] b) {
        outer: for (int i = 0; i < b.length; i++) {
            int next = b[i];
            for (int o = 0; o < a.length; o++) {
                if (a[o] == next) {
                    continue outer;
                }
            }
            return false;
        }
        return true;
    }

    private static boolean contains(int[] a, int b) {
        for (int o = 0; o < a.length; o++) {
            if (a[o] == b) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a sub part from a larger term. Be sure to check that the term
     * contains the subpart <strong>before</strong> calling this.
     */
    private static int[] remove(int[] next, int[] common) {
        int[] remaining = new int[next.length - common.length];
        int remaining_index = 0;
        int common_index = 0;
        for (int o = 0; o < next.length; o++) {
            if (common_index < common.length && next[o] == common[common_index]) {
                common_index++;
            } else {
                remaining[remaining_index++] = next[o];
            }
        }
        if (common_index != common.length) {
            return null;
        }
        return remaining;
    }

    private static int[] findCommonSubpart(int[] a, int[] b) {
        int[] common = new int[Math.max(a.length, b.length)];
        int common_length = 0;

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                if (a[i] == b[j]) {
                    common[common_length++] = a[i];
                    break;
                }
            }
        }
        if (common_length == 0) {
            return null;
        }
        return Arrays.copyOf(common, common_length);
    }

    private static Pair<int[], int[]> simplifyCommonSubparts(int[] a, int[] b, List<int[]> encodings) {
        if (a.length < b.length) {
            return null;
        }

        int[] common = findCommonSubpart(a, b);
        if (common == null) {
            return null;
        }
        int common_length = common.length;

        int[] remaining_a = remove(a, common);
        int[] remaining_b = remove(b, common);

        if (containsInverse(remaining_a, remaining_b)) {
            if (remaining_a.length == remaining_b.length) {
                return new Pair<>(common, null);
            }
            int[] new_m = new int[a.length - remaining_b.length];
            int d = 0;
            outer: for (int j = 0; j < a.length; j++) {
                for (int v = 0; v < remaining_b.length; v++) {
                    if (a[j] == -remaining_b[v]) {
                        continue outer;
                    }
                }
                new_m[d++] = a[j];
            }
            return new Pair<>(new_m, null);
        } else if (remaining_a.length == remaining_b.length && remaining_a.length == 1) {
            int[] new_m = new int[common_length];
            for (int i = 0; i < common_length; i++) {
                new_m[i] = common[i];
            }

            int[] dm = new int[] {-remaining_a[0], -remaining_b[0]};

            for (int i = 0; i < encodings.size(); i++) {
                int[] n = encodings.get(i);
                if (n == a || n == b) {
                    continue;
                }
                if (n.length == 2 && contains(n, dm)) {
                    return new Pair<>(null, new_m);
                }
            }

        }
        return null;
    }

    private static Condition reverse(Map<Condition, Integer> mapping, int val) {
        for (Map.Entry<Condition, Integer> e : mapping.entrySet()) {
            if (e.getValue() == val) {
                return e.getKey();
            }
        }
        return null;
    }

    private static Condition decode(int[] next, Map<Condition, Integer> mapping) {
        List<Condition> partial = new ArrayList<>();
        for (int o = 0; o < next.length; o++) {
            int val = next[o];
            Condition p = reverse(mapping, val);
            if (p == null) {
                p = inverse(reverse(mapping, -val));
                if (p == null) {
                    throw new IllegalStateException();
                }
            }
            partial.add(p);
        }
        if (partial.size() == 1) {
            return partial.get(0);
        }
        return new AndCondition(partial);
    }

    private static List<Condition> decode(List<int[]> encodings, Map<Condition, Integer> mapping) {
        List<Condition> reverse = new ArrayList<>();
        for (int i = 0; i < encodings.size(); i++) {
            int[] next = encodings.get(i);
            reverse.add(decode(next, mapping));
        }
        return reverse;
    }

    private static int countOccurances(List<int[]> encodings, int value) {
        int count = 0;
        for (int[] next : encodings) {
            if (contains(next, value)) {
                count++;
            }
        }
        return count;
    }

    private static BisectionResult findBiscection(List<int[]> encodings) {

        // This attempts to find a bisection of the terms. A bisection here
        // being a pair of values such that every term in the encoding contains
        // exactly one of the values. In other words the terms can be split into
        // two groups such that all members of one group contain the first value
        // and all members of the second group contain the second term.

        // this can probably be extended to find the n-section of the set but I
        // think this is sufficient for now.

        if (encodings.size() < 4) {
            if (encodings.size() >= 2) {
                int[] c = findCommonSubpart(encodings.get(0), encodings.get(1));
                int[] p = c;
                if (c != null && encodings.size() == 3) {
                    c = findCommonSubpart(c, encodings.get(2));
                }
                if (c == null) {
                    if (p != null) {
                        List<int[]> r = new ArrayList<>();
                        int[] r2 = null;
                        for (int[] e : encodings) {
                            if (contains(e, p)) {
                                r.add(remove(e, p));
                            } else {
                                r2 = e;
                            }
                        }
                        return new BisectionResult(p, r2, r, null);
                    }
                    return null;
                }
                List<int[]> r = new ArrayList<>();
                for (int[] e : encodings) {
                    if (contains(e, c)) {
                        r.add(remove(e, c));
                    }
                }
                return new BisectionResult(c, null, r, null);
            }
            return null;
        }

        // if there is a bisection then there must be at least one common value
        // in the first three terms
        int[] a = findCommonSubpart(encodings.get(0), encodings.get(1));
        if (a == null) {
            a = findCommonSubpart(encodings.get(0), encodings.get(2));
            if (a == null) {
                return null;
            }
        }

        // We now want to find which value of the term is the most common if
        // there are more than one to give us the largest initial section. This
        // is not 100% reliable but we just have to take those punches or redo
        // the entire subsequent calculation attempting to find a bisection for
        // each possible term. Possible, but perhaps an exercise for later.
        int max = 0;
        int[] min_a = new int[1];
        if (a.length > 1) {
            for (int j = 0; j < a.length; j++) {
                int c = countOccurances(encodings, a[j]);
                if (c > max) {
                    max = c;
                    min_a[0] = a[j];
                }
            }
        } else {
            min_a[0] = a[0];
        }

        List<int[]> remaining = new ArrayList<>();

        for (int j = 0; j < encodings.size(); j++) {
            int[] next = encodings.get(j);
            int[] r = null;
            if (contains(next, min_a)) {
                r = remove(next, min_a);
            }
            remaining.add(r);
        }

        outer: for (int k = 1; k < a.length; k++) {
            int[] t = new int[] {a[k]};
            for (int j = 0; j < encodings.size(); j++) {
                int[] next = remaining.get(j);
                if (next == null || !contains(next, t)) {
                    continue outer;
                }
            }
            min_a = Arrays.copyOf(min_a, min_a.length + 1);
            min_a[min_a.length - 1] = a[k];
        }

        List<int[]> untouched = new ArrayList<>();
        for (int i = 0; i < encodings.size(); i++) {
            if (remaining.get(i) == null) {
                untouched.add(encodings.get(i));
            }
        }

        List<int[]> group1 = new ArrayList<>();
        for (int i = 0; i < remaining.size(); i++) {
            int[] n = remaining.get(i);
            if (n != null) {
                group1.add(n);
            }
        }

        if (untouched.size() == 0) {
            return new BisectionResult(min_a, null, group1, null);
        } else if (untouched.size() == 1) {
            return new BisectionResult(min_a, untouched.get(0), group1, null);
        }

        int[] b = findCommonSubpart(untouched.get(0), untouched.get(1));
        for (int i = 2; i < untouched.size(); i++) {
            if (b == null) {
                break;
            }
            b = findCommonSubpart(b, encodings.get(i));
        }
        if (b == null) {
            return new BisectionResult(min_a, null, group1, untouched);
        }

        List<int[]> remaining2 = new ArrayList<>();

        for (int j = 0; j < untouched.size(); j++) {
            int[] next = untouched.get(j);
            int[] r = null;
            if (contains(next, b)) {
                r = remove(next, b);
            }
            remaining2.add(r);
        }
        return new BisectionResult(min_a, b, group1, remaining2);
    }

    private static Condition postsimplify(List<int[]> encodings, Map<Condition, Integer> mapping) {
        BisectionResult bisection = findBiscection(encodings);
        if (bisection != null) {
            if (bisection.second == null) {
                Condition common_condition = decode(bisection.first, mapping);
                List<Condition> operands = decode(bisection.first_remaining, mapping);
                if (bisection.second_remaining != null) {
                    List<Condition> operands2 = decode(bisection.second_remaining, mapping);
                    return new OrCondition(new AndCondition(common_condition, new OrCondition(operands)), new OrCondition(operands2));
                }
                return new AndCondition(common_condition, new OrCondition(operands));
            }
            List<int[]> a = bisection.first_remaining;
            List<int[]> b = bisection.second_remaining;
            Condition first = decode(bisection.first, mapping);
            Condition second = decode(bisection.second, mapping);

            if (b == null) {
                List<Condition> operands = decode(a, mapping);
                int[] t = null;
                for (int i = 0; i < encodings.size(); i++) {
                    t = encodings.get(i);
                    if (contains(t, bisection.first)) {
                        break;
                    }
                    t = null;
                }
                if (t != null) {
                    int i = 0;
                    for (; i < t.length; i++) {
                        if (t[i] == bisection.first[0]) {
                            break;
                        }
                    }
                    if (i < t.length / 2) {
                        return new OrCondition(new AndCondition(first, new OrCondition(operands)), second);
                    }
                    return new OrCondition(new AndCondition(new OrCondition(operands), first), second);
                }
                return new OrCondition(new AndCondition(first, new OrCondition(operands)), second);
            }

            if (a.size() == b.size()) {
                boolean equal = true;
                // check if our groups are equal to each other, if they are then
                // we can make things even simpler
                outer: for (int i = 0; i < a.size(); i++) {
                    int[] next = a.get(i);
                    for (int j = 0; j < b.size(); j++) {
                        int[] n = b.get(j);
                        if (Arrays.equals(next, n)) {
                            continue outer;
                        }
                    }
                    equal = false;
                    break;
                }
                if (equal) {
                    // we have a proper bisection where both groups are also the
                    // same and thus we have an equation of the form

                    // (a + b)(c + d + ... + e)
                    List<Condition> group = decode(a, mapping);
                    return new AndCondition(new OrCondition(first, second), new OrCondition(group));
                }
            }
            List<Condition> group = decode(bisection.first_remaining, mapping);
            List<Condition> group2 = decode(bisection.second_remaining, mapping);
            return new OrCondition(new AndCondition(first, new OrCondition(group)), new AndCondition(second, new OrCondition(group2)));
        }
        return null;
    }

    private static final boolean DEBUG_SIMPLIFICATION = Boolean.getBoolean("despect.debug.simplification");

    /**
     * Attempts to simplify the given condition.
     */
    public static Condition simplifyCondition(Condition condition) {
        // A brute force simplification of sum-of-products expressions
        if (condition instanceof OrCondition) {
            OrCondition or = (OrCondition) condition;
            List<int[]> encodings = new ArrayList<>(or.getOperands().size());
            Map<Condition, Integer> mapping = new HashMap<>();
            // Each of the conditions is encoded into an integer array, every
            // condition is inserted into a map to track an integer value for
            // each condition. Conditions that are equivalent are given the same
            // number and conditions that are inverses of each other are given
            // numbers which are the negative of each other.

            // This encoding allows very quick and easy comparisons of
            // whether conditions are equal or inverses of each other.

            // This simplification method is a massive fucking hack in an
            // attempt to more easily handle more than a small number of
            // conditions. The problem of minimizing boolean functions is
            // NP-hard and traditional solutions such as the Quine–McCluskey
            // algorithm start to require a prohibative amount of memory and
            // time with even a seemingly small number of conditions.

            // TODO: The Espresso heuristic logic minimizer may be worth looking
            // into
            for (int i = 0; i < or.getOperands().size(); i++) {
                Condition c = or.getOperands().get(i);
                if (c instanceof AndCondition) {
                    encodings.add(encode((AndCondition) c, mapping));
                } else {
                    encodings.add(new int[] {getMapping(mapping, c)});
                }
            }
            if (DEBUG_SIMPLIFICATION) {
                for (Map.Entry<Condition, Integer> e : mapping.entrySet()) {
                    System.out.println(e.getKey() + " : " + e.getValue());
                }
                System.out.print("Exp: ");
                for (int[] e : encodings) {
                    for (int i = 0; i < e.length; i++) {
                        System.out.print(e[i]);
                    }
                    System.out.print(" | ");
                }
                System.out.println();
            }
            for (int j = 0; j < encodings.size(); j++) {
                for (int k = 0; k < encodings.size(); k++) {
                    int[] n = encodings.get(k);
                    for (Iterator<int[]> it = encodings.iterator(); it.hasNext();) {
                        int[] m = it.next();
                        if (m == n || m.length < n.length) {
                            continue;
                        }
                        // if m contains n either in whole or part then it can
                        // be removed as any time n is true, m will also be true
                        if (contains(m, n)) {
                            it.remove();
                            if (DEBUG_SIMPLIFICATION) {
                                System.out.println("Removed expression containing other expression");
                                System.out.print("Exp: ");
                                for (int[] e : encodings) {
                                    for (int i = 0; i < e.length; i++) {
                                        System.out.print(e[i]);
                                    }
                                    System.out.print(" | ");
                                }
                                System.out.println();
                            }
                        }
                    }
                    for (int l = 0; l < encodings.size(); l++) {
                        if (l == k) {
                            continue;
                        }
                        int[] m = encodings.get(l);
                        if (m.length < n.length) {
                            continue;
                        }
                        // if m contains the inverse of n then those parts
                        // corresponding to n can be removed from m
                        if (containsInverse(m, n)) {
                            int[] new_m = new int[m.length - n.length];
                            int d = 0;
                            outer: for (int u = 0; u < m.length; u++) {
                                for (int v = 0; v < n.length; v++) {
                                    if (m[u] == -n[v]) {
                                        continue outer;
                                    }
                                }
                                new_m[d++] = m[u];
                            }
                            encodings.set(l, new_m);
                            if (DEBUG_SIMPLIFICATION) {
                                System.out.println("Removed inverse of other expression from expression");
                                System.out.print("Exp: ");
                                for (int[] e : encodings) {
                                    for (int i = 0; i < e.length; i++) {
                                        System.out.print(e[i]);
                                    }
                                    System.out.print(" | ");
                                }
                                System.out.println();
                            }
                        } else {
                            // this extracts common subparts from m and n and
                            // then performs a few simplifications ont he
                            // remaining pieces.
                            Pair<int[], int[]> s = simplifyCommonSubparts(m, n, encodings);
                            if (s != null) {
                                if (s.getFirst() != null) {
                                    encodings.set(l, s.getFirst());
                                    if (DEBUG_SIMPLIFICATION) {
                                        System.out.println("Applied inverse removal to common sub part");
                                        System.out.print("Exp: ");
                                        for (int[] e : encodings) {
                                            for (int i = 0; i < e.length; i++) {
                                                System.out.print(e[i]);
                                            }
                                            System.out.print(" | ");
                                        }
                                        System.out.println();
                                    }
                                } else {
                                    encodings.set(k, s.getSecond());
                                    encodings.remove(l);
                                    if (DEBUG_SIMPLIFICATION) {
                                        System.out.println("Applied De Morgans law to common sub part");
                                        System.out.print("Exp: ");
                                        for (int[] e : encodings) {
                                            for (int i = 0; i < e.length; i++) {
                                                System.out.print(e[i]);
                                            }
                                            System.out.print(" | ");
                                        }
                                        System.out.println();
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            // postsimplify looks for common patterns and breaks them out
            Condition ps = postsimplify(encodings, mapping);
            if (ps != null) {
                return ps;
            }
            List<Condition> reverse = decode(encodings, mapping);
            if (encodings.size() == 1) {
                return reverse.get(0);
            }
            return new OrCondition(reverse);
        }
        return condition;
    }

    private static class BisectionResult {

        public int[] first;
        public int[] second;
        public List<int[]> first_remaining;
        public List<int[]> second_remaining;

        public BisectionResult(int[] f, int[] s, List<int[]> fr, List<int[]> sr) {
            this.first = f;
            this.second = s;
            this.first_remaining = fr;
            this.second_remaining = sr;
        }

    }

    private ConditionUtil() {
    }
}
