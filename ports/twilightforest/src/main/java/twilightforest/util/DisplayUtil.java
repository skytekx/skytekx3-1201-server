package twilightforest.util;

import com.mojang.math.Transformation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.joml.Matrix4f;

import java.util.Optional;

/**
 * Spawns block and text Display entities for the structure debug commands. Every entity carries {@link #TAG} so
 * {@code /tf structure_util clear_display} can remove them again.
 */
public final class DisplayUtil {
	public static final String TAG = "twilightforest_debug_display";

	// Instead of making methods in Display and Display.BlockDisplay public, this is a lazy way of compiling the NBT data then using it to initialize the entity
	public static boolean spawnBlockDisplay(Level level, BoundingBox box, BlockState displayState, float padding) {
		Transformation transform = new Transformation(new Matrix4f().scale(box.getXSpan() + padding * 2, box.getYSpan() + padding * 2, box.getZSpan() + padding * 2));
		transform.getScale(); // Dummy call to ensure the matrix is factorized, or else there will be nulls on serialization

		// See Display.addAdditionalSaveData()
		Optional<Tag> serializedTransform = Transformation.EXTENDED_CODEC.encodeStart(NbtOps.INSTANCE, transform).result();

		if (serializedTransform.isEmpty()) return false;

		CompoundTag entityNBT = new CompoundTag();
		entityNBT.put("transformation", serializedTransform.get());

		// See Display.BlockDisplay.addAdditionalSaveData()
		entityNBT.put("block_state", NbtUtils.writeBlockState(displayState));

		entityNBT.put("Pos", newDoubleList(box.minX() - padding, box.minY() - padding, box.minZ() - padding));

		ListTag listtag = new ListTag();
		listtag.add(StringTag.valueOf(TAG));
		entityNBT.put("Tags", listtag);

		entityNBT.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.BLOCK_DISPLAY).toString());

		Optional<Entity> spawned = EntityType.create(entityNBT, level);

		if (spawned.isEmpty()) return false;
		Entity entity = spawned.get();

		return level.addFreshEntity(entity);
	}

	public static ListTag newDoubleList(double... numbers) {
		ListTag listtag = new ListTag();

		for (double d0 : numbers) {
			listtag.add(DoubleTag.valueOf(d0));
		}

		return listtag;
	}

	public static void setTextEntity(Level level, double x, double y, double z, Display.BillboardConstraints billboardConstraint, Component name) {
		CompoundTag entityNBT = new CompoundTag();

		entityNBT.put("Pos", newDoubleList(x, y, z));
		entityNBT.putString("text", Component.Serializer.toJson(name));

		Display.TextDisplay.Align.CODEC.encodeStart(NbtOps.INSTANCE, Display.TextDisplay.Align.CENTER).result()
			.ifPresent(alignment -> entityNBT.put("alignment", alignment));

		Display.BillboardConstraints.CODEC.encodeStart(NbtOps.INSTANCE, billboardConstraint).result()
			.ifPresent(billboard -> entityNBT.put("billboard", billboard));

		ListTag listtag = new ListTag();
		listtag.add(StringTag.valueOf(TAG));
		entityNBT.put("Tags", listtag);

		entityNBT.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.TEXT_DISPLAY).toString());

		Optional<Entity> spawned = EntityType.create(entityNBT, level);

		if (spawned.isEmpty()) return;
		Entity entity = spawned.get();

		level.addFreshEntity(entity);
	}

	private DisplayUtil() {
	}
}
