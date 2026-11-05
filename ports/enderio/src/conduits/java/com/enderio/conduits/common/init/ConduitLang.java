package com.enderio.conduits.common.init;

import com.enderio.EnderIO;
import com.tterrag.registrate.Registrate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ConduitLang {
    private static final Registrate REGISTRATE = EnderIO.registrate();

    public static final MutableComponent FLUID_RATE_TOOLTIP = REGISTRATE.addLang("tooltip", EnderIO.loc("conduit.fluid.rate"), "Transfer Rate %s mB/t");
    public static final Component MULTI_FLUID_TOOLTIP = REGISTRATE.addLang("tooltip", EnderIO.loc("conduit.fluid.multi"),
        "Allows multiple fluids to be transported on the same line");

    public static final Component CONDUIT_INSERT = REGISTRATE.addLang("gui", EnderIO.loc("conduit.insert"), "Insert");
    public static final Component CONDUIT_EXTRACT = REGISTRATE.addLang("gui", EnderIO.loc("conduit.extract"), "Extract");
    public static final Component CONDUIT_PRIORITY = REGISTRATE.addLang("gui", EnderIO.loc("conduit.priority"), "Priority");

    // Ender IO 8.0 names the toggle gui.enderio.conduit.redstone.strong_signal. The 6.x screen has no room for a label in the
    // output column, so the box carries its name as a tooltip, in the enabled and disabled style of the item conduit boxes.
    public static final Component STRONG_SIGNAL_ENABLED = REGISTRATE.addLang("gui", EnderIO.loc("conduit.redstone.strong_signal.enabled"), "Strong Signal Enabled");
    public static final Component STRONG_SIGNAL_DISABLED = REGISTRATE.addLang("gui", EnderIO.loc("conduit.redstone.strong_signal.disabled"), "Strong Signal Disabled");

    public static void register() {
    }
}
