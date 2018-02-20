package com.chrisdmilner.webapp;

import org.junit.Test;

import static org.junit.Assert.*;

public class RedditMinerTest {

    @Test
    public void mine() {
        FactBook f = RedditMiner.mine("RadioactiveMonkey123");
        assertTrue(f.noOfFacts() > 0);
        System.out.println(f.toString());
    }
}