package com.chrisdmilner.webapp;

import org.junit.Test;

import static org.junit.Assert.*;

public class RedditMinerTest {

    @Test
    public void mine() {
        assertTrue(RedditMiner.mine("RadioactiveMonkey").noOfFacts() > 0);
    }
}