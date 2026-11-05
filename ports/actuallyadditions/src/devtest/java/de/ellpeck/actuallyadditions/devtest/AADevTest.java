package de.ellpeck.actuallyadditions.devtest;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

/**
 * SkyTekx: the dev only test mod of the Actually Additions backport (mod id aadevtest). It lives
 * in the source set devtest, which only the dev runs load, so the mod jar never contains it.
 * <p>
 * Its client part connects a dev client to a dedicated server and drives the server safety
 * tests from there, see AADevTestClient. A dedicated server needs nothing of it, since the
 * client gives its commands as an operator.
 */
@Mod(AADevTest.MODID)
public class AADevTest {
    public static final String MODID = "aadevtest";

    public AADevTest() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> AADevTestClient::init);
    }
}
