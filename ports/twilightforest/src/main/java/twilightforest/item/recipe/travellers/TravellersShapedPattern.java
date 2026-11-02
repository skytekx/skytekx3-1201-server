package twilightforest.item.recipe.travellers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;

/**
 * The grid of a shaped Traveller's modifier recipe: 1.20.1 stand-in for 1.21's ShapedRecipePattern, which the shaped
 * modifier recipes nest under {@code "pattern": {"key": {...}, "pattern": [...]}}. Parsing and matching are
 * [VanillaCopy] of 1.20.1's ShapedRecipe, whose helpers are package-private. The key and the original rows are only
 * kept where the pattern was read from JSON or built by datagen, to write it back out.
 */
public record TravellersShapedPattern(int width, int height, NonNullList<Ingredient> ingredients, Optional<Data> data) {
	// ShapedRecipe's MAX_WIDTH and MAX_HEIGHT are package-private; the modifier recipes are for the 3x3 crafting grid
	private static final int MAX_SIZE = 3;

	public record Data(Map<Character, Ingredient> key, List<String> pattern) {
	}

	public static TravellersShapedPattern of(Map<Character, Ingredient> key, List<String> pattern) {
		Map<String, Ingredient> keys = new HashMap<>();
		key.forEach((symbol, ingredient) -> keys.put(String.valueOf(symbol), ingredient));
		keys.put(" ", Ingredient.EMPTY);
		String[] rows = shrink(pattern.toArray(String[]::new));
		int width = rows.length == 0 ? 0 : rows[0].length();
		int height = rows.length;
		return new TravellersShapedPattern(width, height, dissolvePattern(rows, keys, width, height), Optional.of(new Data(Collections.unmodifiableMap(new LinkedHashMap<>(key)), List.copyOf(pattern))));
	}

	public static TravellersShapedPattern fromJson(JsonObject json) {
		Map<Character, Ingredient> key = new LinkedHashMap<>();
		for (Map.Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(json, "key").entrySet()) {
			if (entry.getKey().length() != 1)
				throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
			if (" ".equals(entry.getKey()))
				throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
			key.put(entry.getKey().charAt(0), Ingredient.fromJson(entry.getValue(), false));
		}
		return of(key, patternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
	}

	public JsonObject toJson() {
		Data data = this.data().orElseThrow(() -> new IllegalStateException("This pattern was not built from a key and rows"));
		JsonObject json = new JsonObject();
		JsonObject key = new JsonObject();
		data.key().forEach((symbol, ingredient) -> key.add(String.valueOf(symbol), ingredient.toJson()));
		json.add("key", key);
		JsonArray pattern = new JsonArray();
		data.pattern().forEach(pattern::add);
		json.add("pattern", pattern);
		return json;
	}

	public void toNetwork(FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.width());
		buffer.writeVarInt(this.height());
		for (Ingredient ingredient : this.ingredients()) {
			ingredient.toNetwork(buffer);
		}
	}

	public static TravellersShapedPattern fromNetwork(FriendlyByteBuf buffer) {
		int width = buffer.readVarInt();
		int height = buffer.readVarInt();
		NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
		ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
		return new TravellersShapedPattern(width, height, ingredients, Optional.empty());
	}

	// [VanillaCopy] ShapedRecipe#matches
	public boolean matches(CraftingContainer container) {
		for (int i = 0; i <= container.getWidth() - this.width(); ++i) {
			for (int j = 0; j <= container.getHeight() - this.height(); ++j) {
				if (this.matches(container, i, j, true)) {
					return true;
				}

				if (this.matches(container, i, j, false)) {
					return true;
				}
			}
		}

		return false;
	}

	// [VanillaCopy] ShapedRecipe#matches
	private boolean matches(CraftingContainer container, int width, int height, boolean mirrored) {
		for (int i = 0; i < container.getWidth(); ++i) {
			for (int j = 0; j < container.getHeight(); ++j) {
				int k = i - width;
				int l = j - height;
				Ingredient ingredient = Ingredient.EMPTY;
				if (k >= 0 && l >= 0 && k < this.width() && l < this.height()) {
					if (mirrored) {
						ingredient = this.ingredients().get(this.width() - k - 1 + l * this.width());
					} else {
						ingredient = this.ingredients().get(k + l * this.width());
					}
				}

				if (!ingredient.test(container.getItem(i + j * container.getWidth()))) {
					return false;
				}
			}
		}

		return true;
	}

	// [VanillaCopy] ShapedRecipe#dissolvePattern
	private static NonNullList<Ingredient> dissolvePattern(String[] pattern, Map<String, Ingredient> keys, int patternWidth, int patternHeight) {
		NonNullList<Ingredient> ingredients = NonNullList.withSize(patternWidth * patternHeight, Ingredient.EMPTY);
		Set<String> set = new HashSet<>(keys.keySet());
		set.remove(" ");

		for (int i = 0; i < pattern.length; ++i) {
			for (int j = 0; j < pattern[i].length(); ++j) {
				String s = pattern[i].substring(j, j + 1);
				Ingredient ingredient = keys.get(s);
				if (ingredient == null) {
					throw new JsonSyntaxException("Pattern references symbol '" + s + "' but it's not defined in the key");
				}

				set.remove(s);
				ingredients.set(j + patternWidth * i, ingredient);
			}
		}

		if (!set.isEmpty()) {
			throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
		}
		return ingredients;
	}

	// [VanillaCopy] ShapedRecipe#shrink
	private static String[] shrink(String... toShrink) {
		int i = Integer.MAX_VALUE;
		int j = 0;
		int k = 0;
		int l = 0;

		for (int i1 = 0; i1 < toShrink.length; ++i1) {
			String s = toShrink[i1];
			i = Math.min(i, firstNonSpace(s));
			int j1 = lastNonSpace(s);
			j = Math.max(j, j1);
			if (j1 < 0) {
				if (k == i1) {
					++k;
				}

				++l;
			} else {
				l = 0;
			}
		}

		if (toShrink.length == l) {
			return new String[0];
		}

		String[] shrunk = new String[toShrink.length - l - k];
		for (int k1 = 0; k1 < shrunk.length; ++k1) {
			shrunk[k1] = toShrink[k1 + k].substring(i, j + 1);
		}
		return shrunk;
	}

	private static int firstNonSpace(String entry) {
		int i = 0;
		while (i < entry.length() && entry.charAt(i) == ' ') {
			++i;
		}
		return i;
	}

	private static int lastNonSpace(String entry) {
		int i = entry.length() - 1;
		while (i >= 0 && entry.charAt(i) == ' ') {
			--i;
		}
		return i;
	}

	// [VanillaCopy] ShapedRecipe#patternFromJson
	private static List<String> patternFromJson(JsonArray patternArray) {
		String[] rows = new String[patternArray.size()];
		if (rows.length > MAX_SIZE) {
			throw new JsonSyntaxException("Invalid pattern: too many rows, " + MAX_SIZE + " is maximum");
		} else if (rows.length == 0) {
			throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
		}

		for (int i = 0; i < rows.length; ++i) {
			String s = GsonHelper.convertToString(patternArray.get(i), "pattern[" + i + "]");
			if (s.length() > MAX_SIZE) {
				throw new JsonSyntaxException("Invalid pattern: too many columns, " + MAX_SIZE + " is maximum");
			}

			if (i > 0 && rows[0].length() != s.length()) {
				throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
			}

			rows[i] = s;
		}
		return List.of(rows);
	}
}
