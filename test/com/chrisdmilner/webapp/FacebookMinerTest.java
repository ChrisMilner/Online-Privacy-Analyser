package com.chrisdmilner.webapp;

import org.junit.Test;

import static org.junit.Assert.*;

public class FacebookMinerTest {

    @Test
    public void mine() {
        FactBook f = FacebookMiner.mine("100008177116719","EAACEdueP1ygBAD2dSZAPieZAmFIcWaB2qVT2C5RtGmKmvgRpMY8UEXODyFUFYGVMC0ZCZCuNVdmnZCYqGx0dwXVjXDsxpuRVA1PXJXkPiz17PMMLB4pNZAlmeRCnSz3wf4oW1OdMbEZBMMovdZATZCQW8f27cHZCBKYM0ZD");
        assertTrue(f.noOfFacts() > 0);
        System.out.println(f.toString());

        f = FacebookMiner.mine("100008177116719","");
        assertTrue(f.noOfFacts() > 0);
        System.out.println(f.toString());
    }
}