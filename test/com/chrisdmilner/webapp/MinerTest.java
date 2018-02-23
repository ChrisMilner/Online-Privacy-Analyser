package com.chrisdmilner.webapp;

import org.junit.Test;

import static org.junit.Assert.*;

public class MinerTest {

    @Test
    public void mine() {
        String fb = "https://www.facebook.com/profile.php?id=100008177116719";
        String tw = "https://twitter.com/ChrisDMilner";
        String rd = "https://www.reddit.com/user/RadioactiveMonkey123/";
        String at = "EAACEdueP1ygBAD2dSZAPieZAmFIcWaB2qVT2C5RtGmKmvgRpMY8UEXODyFUFYGVMC0ZCZCuNVdmnZCYqGx0dwXVjXDsxpuRVA1PXJXkPiz17PMMLB4pNZAlmeRCnSz3wf4oW1OdMbEZBMMovdZATZCQW8f27cHZCBKYM0ZD";

        String json = Miner.mine(fb, tw, rd, at);

        System.out.println(json);
    }
}