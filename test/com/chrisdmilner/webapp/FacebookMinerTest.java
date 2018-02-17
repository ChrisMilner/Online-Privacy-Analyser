package com.chrisdmilner.webapp;

import org.junit.Test;

import static org.junit.Assert.*;

public class FacebookMinerTest {

    @Test
    public void mine() {
        assertTrue(FacebookMiner.mine("100008177116719","EAACEdueP1ygBAD2dSZAPieZAmFIcWaB2qVT2C5RtGmKmvgRpMY8UEXODyFUFYGVMC0ZCZCuNVdmnZCYqGx0dwXVjXDsxpuRVA1PXJXkPiz17PMMLB4pNZAlmeRCnSz3wf4oW1OdMbEZBMMovdZATZCQW8f27cHZCBKYM0ZD").noOfFacts() > 0);
        assertTrue(FacebookMiner.mine("100008177116719","").noOfFacts() > 0);
    }
}