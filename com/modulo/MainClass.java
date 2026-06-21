package com.modulo; //SPDX-License-Identifier: MIT    Copyright (c) 2026 Jason Burke

import com.modulo.exponentiation.ModuloExponentiator;
import com.modulo.multiplication.ModuloMultiplier;

public class MainClass {
    public static void main(String[] args) {
        long a = Long.parseLong(args[0]);
        long b = Long.parseLong(args[1]);
        long n = Long.parseLong(args[2]);
        if("power".equals(args[3])) {
            ModuloExponentiator mex = new ModuloExponentiator(n);
            System.out.println(mex.power(a, b));
        } else if("multiply".equals(args[3])){
            ModuloMultiplier mmult = new ModuloMultiplier(n);
            System.out.println(mmult.multiply(a, b));
        }
    }
}
