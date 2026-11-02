package twilightforest.item.travellers_gear.modifiers;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import twilightforest.client.TFKeyBinds;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves the {@code ${tfkeybinds/<key mapping name>}} placeholders of the modifier names into the bound key. Backport
 * of 4.8's TooltipStringInterpolator. 4.8 reads TFKeyBinds directly, which a dedicated server does not have (the
 * command messages also render modifier names there); the key mappings are only read on the client here, the server
 * falls back to the translated name of the key mapping.
 */
public final class TooltipStringInterpolator {
	// pattern to find ${var}
	private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
	private static final Map<String, Function<String, String>> TWO_PART = Map.of(
		"tfkeybinds", TooltipStringInterpolator::resolveTFKeybind);

	private TooltipStringInterpolator() {
	}

	/**
	 * Renders all ${…} in the translation of {@code translatableKey}.
	 */
	public static MutableComponent render(String translatableKey) {
		Matcher m = VAR_PATTERN.matcher(Component.translatable(translatableKey).getString());
		StringBuilder sb = new StringBuilder();
		while (m.find()) {
			String var = m.group(1);
			String replacement = lookupVariable(var);
			// quote in case the replacement contains $ or \
			m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		m.appendTail(sb);
		return Component.literal(sb.toString());
	}

	private static String lookupVariable(String var) {
		String[] parts = var.split("/");
		if (parts.length != 2)
			throw new IllegalArgumentException("The number of / in " + var + " is not 1");

		Function<String, String> resolver = TWO_PART.get(parts[0]);
		if (resolver == null) {
			throw new IllegalArgumentException("Unknown namespace: " + parts[0]);
		}
		return resolver.apply(parts[1]);
	}

	private static String resolveTFKeybind(String keyString) {
		String keyName = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> TFKeyBinds.getBoundKeyName(keyString));
		return keyName != null ? keyName : Component.translatable(keyString).getString();
	}
}
