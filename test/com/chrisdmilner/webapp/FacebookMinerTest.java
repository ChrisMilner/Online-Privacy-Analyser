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

        f = FacebookMiner.mine("100024732261125", "EAACEdueP1ygBAOErBNu31L5ZCTYI6xdgoL2huNo6psiylCRIZB6IKm16YCau9WDQJUSXsKgF6MfTsv4CvLH8JZBE4Vu4WGv538X2ko6pacVw3do0mZCvzHi4ZAfcItxvQZC4OAuyrW9ixSGIecE0SbAt1gN4VHj8ZBZCl2SZAZBmzeH1R33xY5hUPimRZBKiSs1juCibMKAAL3iFuvQwZCwpUgeulk1WPmNPIVYZD");
        assertTrue(f.noOfFacts() > 0);
        System.out.println(f.toString());
    }
}