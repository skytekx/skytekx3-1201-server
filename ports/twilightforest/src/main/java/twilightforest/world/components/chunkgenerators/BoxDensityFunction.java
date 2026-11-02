package twilightforest.world.components.chunkgenerators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.List;

public class BoxDensityFunction implements DensityFunction.SimpleFunction {
	public static final MapCodec<BoxDensityFunction> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
		Codec.INT.fieldOf("minX").forGetter(f -> f.minX),
		Codec.INT.fieldOf("minY").forGetter(f -> f.minY),
		Codec.INT.fieldOf("minZ").forGetter(f -> f.minZ),
		Codec.INT.fieldOf("maxX").forGetter(f -> f.maxX),
		Codec.INT.fieldOf("maxY").forGetter(f -> f.maxY),
		Codec.INT.fieldOf("maxZ").forGetter(f -> f.maxZ),
		Codec.DOUBLE.fieldOf("minValue").forGetter(f -> f.minValue),
		Codec.DOUBLE.fieldOf("maxValue").forGetter(f -> f.maxValue),
		TerrainAdjustment.CODEC.fieldOf("beardifier").forGetter(f -> f.terrainAdjustment)
	).apply(inst, BoxDensityFunction::new));
	public static final KeyDispatchDataCodec<BoxDensityFunction> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

	// Copy of Beardifier.BEARD_KERNEL. Beardifier's contribution helpers are private in vanilla 1.20.1 (only Forge's access transformer
	// opens them) and take ints, while TF 4.8 calls the 1.21 double overload for the halved vertical distance of BURY.
	private static final int BEARD_KERNEL_RADIUS = 12;
	private static final int BEARD_KERNEL_SIZE = 24;
	private static final float[] BEARD_KERNEL = Util.make(new float[BEARD_KERNEL_SIZE * BEARD_KERNEL_SIZE * BEARD_KERNEL_SIZE], kernel -> {
		for (int z = 0; z < BEARD_KERNEL_SIZE; z++) {
			for (int x = 0; x < BEARD_KERNEL_SIZE; x++) {
				for (int y = 0; y < BEARD_KERNEL_SIZE; y++) {
					kernel[z * BEARD_KERNEL_SIZE * BEARD_KERNEL_SIZE + x * BEARD_KERNEL_SIZE + y] = (float) Math.pow(Math.E, -Mth.lengthSquared(x - BEARD_KERNEL_RADIUS, y - BEARD_KERNEL_RADIUS + 0.5, z - BEARD_KERNEL_RADIUS) / 16.0);
				}
			}
		}
	});

	private final int minX, minY, minZ, maxX, maxY, maxZ;
	private final double minValue, maxValue;
	private final TerrainAdjustment terrainAdjustment;

	public static DensityFunction combine(List<BoundingBox> boxes, int dYMin, int dYMax, TerrainAdjustment terrainAdjustment) {
		if (boxes.isEmpty()) return DensityFunctions.constant(0);

		DensityFunction densityFunction = make(boxes.get(0), dYMin, dYMax, terrainAdjustment);

		for (int idx = 1; idx < boxes.size(); idx++) {
			densityFunction = DensityFunctions.add(make(boxes.get(idx), dYMin, dYMax, terrainAdjustment), densityFunction);
		}

		return densityFunction;
	}

	public static BoxDensityFunction make(BoundingBox box, int dYMin, int dYMax, TerrainAdjustment terrainAdjustment) {
		return new BoxDensityFunction(box.minX(), box.minY() + dYMin, box.minZ(), box.maxX(), box.maxY() + dYMax, box.maxZ(), -4.0, 4.0, terrainAdjustment);
	}

	public BoxDensityFunction(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, double minValue, double maxValue, TerrainAdjustment terrainAdjustment) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.terrainAdjustment = terrainAdjustment;
	}

	@Override // Parallels vanilla Beardifier behavior
	public double compute(FunctionContext context) {
		int blockX = context.blockX();
		int blockY = context.blockY();
		int blockZ = context.blockZ();

		// Dist is zero if inside the box
		int xDist = Math.max(0, Math.max(this.minX - blockX, blockX - this.maxX));
		int zDist = Math.max(0, Math.max(this.minZ - blockZ, blockZ - this.maxZ));

		int distAboveBottom = blockY - this.minY;
		// 1.20.1 has no TerrainAdjustment.ENCAPSULATE (added in 1.20.5)
		int yDist = switch (this.terrainAdjustment) {
			case BURY, BEARD_THIN -> distAboveBottom;
			case BEARD_BOX -> Math.max(0, Math.max(this.minY - blockY, blockY - this.maxY));
			default -> 0;
		};

		double densityValue = switch (this.terrainAdjustment) {
			case BURY -> getBuryContribution(xDist, yDist * 0.5, zDist);
			case BEARD_THIN, BEARD_BOX -> getBeardContribution(xDist, yDist, zDist, distAboveBottom) * 0.8;
			default -> 0;
		};

		return Mth.clamp(densityValue, this.minValue, this.maxValue);
	}

	// Beardifier.getBuryContribution(double, double, double) of 1.21
	private static double getBuryContribution(double x, double y, double z) {
		double distance = Mth.length(x, y, z);
		return Mth.clampedMap(distance, 0.0, 6.0, 1.0, 0.0);
	}

	// Beardifier.getBeardContribution(int, int, int, int)
	private static double getBeardContribution(int x, int y, int z, int yToGround) {
		int kernelX = x + BEARD_KERNEL_RADIUS;
		int kernelY = y + BEARD_KERNEL_RADIUS;
		int kernelZ = z + BEARD_KERNEL_RADIUS;
		if (isInKernelRange(kernelX) && isInKernelRange(kernelY) && isInKernelRange(kernelZ)) {
			double groundOffset = yToGround + 0.5;
			double distanceSqr = Mth.lengthSquared(x, groundOffset, z);
			double contribution = -groundOffset * Mth.fastInvSqrt(distanceSqr / 2.0) / 2.0;
			return contribution * BEARD_KERNEL[kernelZ * BEARD_KERNEL_SIZE * BEARD_KERNEL_SIZE + kernelX * BEARD_KERNEL_SIZE + kernelY];
		} else {
			return 0.0;
		}
	}

	private static boolean isInKernelRange(int index) {
		return index >= 0 && index < BEARD_KERNEL_SIZE;
	}

	@Override
	public double minValue() {
		return this.minValue;
	}

	@Override
	public double maxValue() {
		return this.maxValue;
	}

	@Override
	public KeyDispatchDataCodec<? extends DensityFunction> codec() {
		return KEY_CODEC;
	}
}
