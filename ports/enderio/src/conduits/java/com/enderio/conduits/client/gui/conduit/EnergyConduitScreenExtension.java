package com.enderio.conduits.client.gui.conduit;

import com.enderio.api.conduit.screen.ConduitScreenExtension;
import com.enderio.api.misc.Vector2i;
import com.enderio.conduits.common.conduit.type.energy.EnergyConduitData;
import com.enderio.core.client.gui.widgets.MultiIconButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.function.Supplier;

/**
 * The insert priority of an energy conduit side, from Ender IO 8.x (57fc40d61), with the plus and minus buttons of the item
 * conduit priority of 6.x (37890e67d). The conduit screen draws the value.
 */
public class EnergyConduitScreenExtension implements ConduitScreenExtension<EnergyConduitData> {

    @Override
    public List<AbstractWidget> createWidgets(Screen screen, Supplier<EnergyConduitData> conduitDataSupplier,
        UpdateExtendedData<EnergyConduitData> updateConduitData, Supplier<Direction> direction, Vector2i widgetsStart) {
        return List.of(
            MultiIconButton.createAddButton(
                widgetsStart.add(70, 4 + 4 + 8 + 16 + 8),
                () -> conduitDataSupplier.get().getPriority(direction.get()),
                priority -> updateConduitData.update(data -> {
                    data.compute(direction.get()).setPriority(priority);
                    return data;
                })),
            MultiIconButton.createMinusButton(
                widgetsStart.add(70, 4 + 4 + 8 + 16 + 16),
                () -> conduitDataSupplier.get().getPriority(direction.get()),
                priority -> updateConduitData.update(data -> {
                    data.compute(direction.get()).setPriority(priority);
                    return data;
                })));
    }
}
