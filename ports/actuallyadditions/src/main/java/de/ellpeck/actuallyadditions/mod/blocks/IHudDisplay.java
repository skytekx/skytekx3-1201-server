/*
 * This file ("IHudDisplay.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.blocks;

/**
 * Marks a block or item that draws an overlay while the player looks at it.
 * <p>
 * SkyTekx: upstream 668f7ffe replaced displayHud(GuiGraphics, Minecraft, ...) by getHud(), which
 * returns a client only IBlockHud. This interface holds neither, so common classes that implement
 * it never name a client class and a dedicated server verifies them without loading one. The
 * overlays live in de.ellpeck.actuallyadditions.mod.blocks.blockhuds, and ClientEvents registers
 * them in BlockHuds on the physical client.
 */
public interface IHudDisplay {
}
