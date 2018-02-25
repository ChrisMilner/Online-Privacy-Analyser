package com.chrisdmilner.webapp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        FacebookMinerTest.class,
        TwitterMinerTest.class,
        RedditMinerTest.class,
        AnalyserTest.class,
        MinerTest.class,
        KeywordTextAnalyserTest.class
})

public class FullTest {}
