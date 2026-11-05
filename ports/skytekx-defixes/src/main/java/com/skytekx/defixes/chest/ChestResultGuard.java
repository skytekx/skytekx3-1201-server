package com.skytekx.defixes.chest;

/**
 * Added to {@code DraconiumChestMenu} by {@code DraconiumChestMenuMixin}. Every open menu of a Draconium
 * Chest computes its own crafting result from the one grid that all menus share, and it only computes it
 * again when the grid changes through that same menu. So when one player crafts, the other menus keep a
 * result whose ingredients are gone, and stock 1.20.1 lets the second player take it, which is the
 * crafting dupe of the chest. The menu therefore remembers the grid that its result was made from, and
 * {@code DETileMenuMixin} calls {@link #skytekx_defixes$refreshStaleResult()} before the server handles any
 * click in the menu. If the grid is no longer the remembered one, the menu computes its result again
 * first, so a click can only take a result that the current grid makes.
 * <p>
 * Fix 1. Upstream closed the same dupe on its 1.21 branch in e405445b ("Fix chest dupe issue") in another
 * way, by moving the result into the chest. This mechanism is this mod's own and copies none of that code.
 */
public interface ChestResultGuard {
    void skytekx_defixes$refreshStaleResult();
}
