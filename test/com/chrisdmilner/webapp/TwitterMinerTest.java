package com.chrisdmilner.webapp;

import org.junit.Test;

import static org.junit.Assert.*;

public class TwitterMinerTest {

    @Test
    public void mine() {
        assertTrue(TwitterMiner.mine("ChrisDMilner").noOfFacts() > 0);
    }
}