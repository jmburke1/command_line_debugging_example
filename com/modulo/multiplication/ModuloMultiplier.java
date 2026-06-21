/*
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Jason Burke
 */
package com.modulo.multiplication;

public class ModuloMultiplier {
    private final long l;

    public ModuloMultiplier(long q) {
        l = q;
    }

    public long multiply(long a, long b) {
        a %= l;
        if(a < 0L) {
            a += l;
        }
        b %= l;
        if(b < 0L) {
            b += l;
        }
        return recursiveMultiply(a, b);
    }
    private long recursiveMultiply(long a, long b) {
        if(b == 0L) {
            return 0L;
        }
        long x;
        if(b % 2L == 1L) {
            x = recursiveMultiply(a, b - 1L);
        } else {
            x = recursiveMultiply(a, b / 2L);
            a = x;
        }
        if(a < l - x) {
            return a + x;
        }
        return a - (l - x);
    }
}
