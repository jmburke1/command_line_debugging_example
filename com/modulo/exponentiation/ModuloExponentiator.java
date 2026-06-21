package com.modulo.exponentiation; //SPDX-License-Identifier: MIT    Copyright (c) 2026 Jason Burke

import com.modulo.multiplication.ModuloMultiplier;

public class ModuloExponentiator {
    private final long l;
    private final ModuloMultiplier pick;

    public ModuloExponentiator(long q) {
        l = q;
        pick = new ModuloMultiplier(q);
    }

    public long power(long a, long b) {
        a %= l;
        if(a < 0) {
            a += l;
        }
        return recursivePower(a, b);
    }
    private long recursivePower(long a, long b) {
        if(b == 0) {
            return 1L;
        }
        long x;
        if(b % 2 == 1) {
            x = recursivePower(a, b - 1);
            return pick.multiply(a, x);
        } else {
            x = recursivePower(a, b / 2);
            return pick.multiply(x, x);
        }
    }
}
