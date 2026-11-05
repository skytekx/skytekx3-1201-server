package com.enderio.conduits.client.gui.conduit;

import com.enderio.api.conduit.screen.ConduitScreenExtension;
import com.enderio.api.misc.Vector2i;
import com.enderio.conduits.common.conduit.type.redstone.RedstoneConduitData;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.core.client.gui.widgets.CheckBox;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.function.Supplier;

/**
 * The strong signal toggle of a redstone conduit output, from Ender IO 8.0 (isStrongOutputSignal). It sits in the insert
 * (output) column under the channel, and its tooltip names it like the round robin and self feed boxes of the item conduit.
 */
public class RedstoneConduitScreenExtension implements ConduitScreenExtension<RedstoneConduitData> {

    @Override
    public List<AbstractWidget> createWidgets(Screen screen, Supplier<RedstoneConduitData> conduitDataSupplier,
        UpdateExtendedData<RedstoneConduitData> updateConduitData, Supplier<Direction> direction, Vector2i widgetsStart) {
        return List.of(new CheckBox(
            widgetsStart.add(0, 40),
            () -> conduitDataSupplier.get().isStrong(direction.get()),
            isStrong -> updateConduitData.update(data -> {
                data.setStrong(direction.get(), isStrong);
                return data;
            }),
            () -> ConduitLang.STRONG_SIGNAL_ENABLED,
            () -> ConduitLang.STRONG_SIGNAL_DISABLED));
    }
}
