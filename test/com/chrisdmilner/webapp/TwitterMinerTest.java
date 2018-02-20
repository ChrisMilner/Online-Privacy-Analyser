package com.chrisdmilner.webapp;

import org.junit.Test;

import static org.junit.Assert.*;

public class TwitterMinerTest {

    @Test
    public void mine() {
        FactBook f = TwitterMiner.mine("ChrisDMilner");
        assertTrue(f.noOfFacts() > 0);
        System.out.println(f.toString());
    }
}