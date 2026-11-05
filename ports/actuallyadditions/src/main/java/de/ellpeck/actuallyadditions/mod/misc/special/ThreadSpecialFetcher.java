/*
 * This file ("ThreadSpecialFetcher.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.misc.special;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

public class ThreadSpecialFetcher extends Thread {

    public ThreadSpecialFetcher() {
        this.setName(ActuallyAdditions.NAME + " Special Fetcher");
        this.setDaemon(true);
        this.start();
    }

    @Override
    public void run() {
        ActuallyAdditions.LOGGER.info("Fetching Special People Stuff...");
        try {
            // SkyTekx: upstream d9b0a714. The list on the 1.16 branch last changed in 2021, upstream
            // keeps the current one on main as specialPeopleStuffNew.properties
            URL url = new URL("https://raw.githubusercontent.com/Ellpeck/ActuallyAdditions/main/specialPeopleStuffNew.properties");
            Properties specialProperties = new Properties();
            specialProperties.load(new InputStreamReader(url.openStream()));
            SpecialRenderInit.parse(specialProperties);

            ActuallyAdditions.LOGGER.info("Fetching Special People Stuff done!");
        } catch (Exception e) {
            ActuallyAdditions.LOGGER.error("Fetching Special People Stuff failed! (You can ignore this error technically.)", e);
        }
    }
}
