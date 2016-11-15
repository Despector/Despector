/*
 * Copyright (c) 2015-2016 VoxelBox <http://engine.thevoxelbox.com>.
 * All Rights Reserved.
 */
package org.spongepowered.test.ast;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.IOException;

public class TernaryTest {

    public void mth_basicTernary(boolean a) {
        int r = a ? 6 : 3;
    }

    @Test
    public void testBasicTernary() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_basicTernary");
        String good = "int r = a ? 6 : 3;";
        assertEquals(good, insn);
    }

    public void mth_moreComplexTernary(boolean a, boolean b, int j) {
        int i = a || b ? 0 - j * 24 + Integer.MAX_VALUE : 1 + j;
    }

    @Test
    public void testMoreComplexTernary() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_moreComplexTernary");
        String good = "int i = a || b ? 0 - j * 24 + Integer.MAX_VALUE : 1 + j;";
        assertEquals(good, insn);
    }

    private int field;
    
    public void mth_basicTernaryToField(boolean a) {
        this.field = a ? 255 : 0;
    }

    @Test
    public void testBasicTernaryToField() throws IOException {
        String insn = TestHelper.getAsString(getClass(), "mth_basicTernaryToField");
        String good = "int r = a ? 6 : 3;";
        assertEquals(good, insn);
    }

}
