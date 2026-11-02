package twilightforest.world.components.structures.fallentrunk;

import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import twilightforest.world.components.chunkgenerators.TanhHillFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The terraforming under a Fallen Trunk: flattens the terrain around and inside the trunk and raises up to one mound per side.
 * {@link twilightforest.world.components.chunkgenerators.TFBeardifier} adds it to the terrain density of the Twilight Forest
 * dimension through {@link twilightforest.world.components.structures.CustomDensitySource}.
 */
public class TrunkUnderDensityFunction extends Beardifier {
	private final boolean isBigTree;
	private final boolean isXOriented;
	private final RandomSource random;  // used to create dirt mounds
	private final BoundingBox boundingBox;
	private final List<TanhHillFunction> tanhHillFunctions;
	protected final BoundingBox moundBase;
	protected static final float MOUND_RADIUS = 3F;
	protected static final int MAX_RANDOM_RADIUS_INCREASE_BIG_TREE = 4;
	protected static final int MAX_RANDOM_RADIUS_INCREASE_NON_BIG_TREE = 2;
	protected static final float BIG_TREE_MOUND_HEIGHT = 3F;
	protected static final float NON_BIG_TREE_MOUND_HEIGHT = 1.5F;

	public TrunkUnderDensityFunction(ObjectListIterator<Rigid> pieceIterator, FallenTrunkPiece piece, boolean isBigTree, int minMounds, int maxMounds) {
		super(pieceIterator, ObjectLists.<JigsawJunction>emptyList().iterator());
		this.isBigTree = isBigTree;
		this.boundingBox = this.getFallenTrunkPiece().box();
		// Seeded by the trunk position, so every chunk the trunk overlaps builds the same mounds
		this.random = RandomSource.create(this.boundingBox.minX() * 14413411L + this.boundingBox.minZ() * 43387781L);
		this.isXOriented = this.boundingBox.maxX() - this.boundingBox.minX() > this.boundingBox.maxZ() - this.boundingBox.minZ();
		int length = this.isXOriented ? this.boundingBox.getXSpan() : this.boundingBox.getZSpan();
		Vec3i moundBaseCorner = new Vec3i(
			this.isXOriented ? length / 2 : 0,
			1,
			!this.isXOriented ? length / 2 : 0);
		this.moundBase = BoundingBox.fromCorners(moundBaseCorner, moundBaseCorner);
		this.tanhHillFunctions = new ArrayList<>();
		int attemptedMoundsNumber = this.random.nextInt(minMounds, maxMounds + 1);
		for (int i = 0; i < attemptedMoundsNumber; i++) {
			Optional<TanhHillFunction> function = this.getTanhHillFunctionWithoutCoveringHole(piece, i % 2 == 0);
			function.ifPresent(this.tanhHillFunctions::add);
		}
	}

	@Override
	public double compute(FunctionContext context) {
		int x = context.blockX();
		int y = context.blockY();
		int z = context.blockZ();

		int groundLevelDelta = this.getFallenTrunkPiece().groundLevelDelta();
		int horizontalDistanceX = Math.max(0, Math.max(this.boundingBox.minX() - x, x - this.boundingBox.maxX()));
		int horizontalDistanceZ = Math.max(0, Math.max(this.boundingBox.minZ() - z, z - this.boundingBox.maxZ()));
		int adjustedGroundLevel = this.boundingBox.minY() + groundLevelDelta + (this.isBigTree ? 1 : 0);
		int verticalDistance = y - adjustedGroundLevel;

		double moundContribution = this.computeMoundsContribution(context);
		if (moundContribution > 0)
			return moundContribution;
		return flatSurroundingTerrain(horizontalDistanceX, verticalDistance, horizontalDistanceZ);
	}

	public static double flatSurroundingTerrain(double x, double y, double z) {
		if (y > 20 || y < -10)
			return 0;
		if (x == 0 && z == 0 && y > 0)
			return -1;  // flat everything inside the trunk
		final double a = FallenTrunkPiece.TERRAFORM_PIECE_SIZE, c = FallenTrunkPiece.TERRAFORM_PIECE_SIZE;
		final double verticalScale = 0.7;
		double normX = Math.abs(x / a);
		double normZ = Math.abs(z / c);
		double normalizedSquaredDistance = normX * normX + normZ * normZ;

		if (normalizedSquaredDistance >= 1.0)
			return 0.0;

		double normalizedDistance = Math.sqrt(normalizedSquaredDistance);

		double factor = 1.0 - normalizedDistance;
		if (y < 0)  // make less steep to avoid vertical walls around the trunk
			y = normalizedDistance * 0.5 + y / 5;  // add normalizedDistance to avoid axis aligned "stairs"
		// multiply by exponent to make the beardifier weaker with distance
		return -y * factor / verticalScale * (Math.exp(-normalizedDistance / (Math.max(1 - normalizedDistance, 1e-8))));
	}

	protected double computeMoundsContribution(FunctionContext context) {
		int x = context.blockX() - this.boundingBox.minX();
		int y = context.blockY() - this.boundingBox.minY();
		int z = context.blockZ() - this.boundingBox.minZ();
		int radius = getRadius(this.boundingBox);
		double ax = Math.abs((this.isXOriented ? z : x) - radius + 1);
		double az = Math.abs(y - radius + 1);
		if (radius == 2D) {  // This case is generated differently
			if (Math.abs((this.isXOriented ? z : x) - 1.5) + Math.abs(y - 1.5) <= 2)
				return -1;
		} else {
			if ((int) (Math.max(ax, az) + (Math.min(ax, az) * 0.5)) < radius)
				return -1;
		}

		double max = -1D;
		for (TanhHillFunction h : this.tanhHillFunctions) {
			double value = h.compute(context);
			if (value > max)
				max = value;
		}
		return max;
	}

	protected Optional<TanhHillFunction> getTanhHillFunctionWithoutCoveringHole(FallenTrunkPiece piece, boolean isOnRightSide) {
		if (this.isBigTree)
			return Optional.ofNullable(this.getTanhHillFunction(Util.getRandom(piece.getAllPotentialBaseMoundBlockPos(isOnRightSide).toArray(new BlockPos[0]), this.random), isOnRightSide));  // Big trees don't have holes

		Set<BlockPos> blockPosSet = piece.getAllowedBaseMoundBlockPos(isOnRightSide);
		if (blockPosSet.isEmpty())
			return Optional.empty();
		BlockPos moundBlockPos = Util.getRandom(blockPosSet.toArray(new BlockPos[0]), this.random);
		return Optional.ofNullable(this.getTanhHillFunction(moundBlockPos, isOnRightSide));
	}

	protected TanhHillFunction getTanhHillFunction(BlockPos baseBlockPos, boolean isOnRightSide) {
		int maxRandomRadiusIncrease = this.isBigTree ? MAX_RANDOM_RADIUS_INCREASE_BIG_TREE : MAX_RANDOM_RADIUS_INCREASE_NON_BIG_TREE;
		float moundRadius = MOUND_RADIUS + this.random.nextInt(0, maxRandomRadiusIncrease);
		float moundHeight = this.isBigTree ? BIG_TREE_MOUND_HEIGHT : NON_BIG_TREE_MOUND_HEIGHT;

		float moundBiasAngle = Mth.TWO_PI * this.random.nextFloat();
		return new TanhHillFunction(baseBlockPos.getX(), baseBlockPos.getY(), baseBlockPos.getZ(), moundRadius, moundHeight, moundBiasAngle, this.isXOriented, isOnRightSide);
	}

	protected Beardifier.Rigid getFallenTrunkPiece() {
		Beardifier.Rigid piece = this.pieceIterator.next();
		this.pieceIterator.back(Integer.MAX_VALUE);
		return piece;
	}

	private static int getRadius(BoundingBox box) {
		return getRadius(box.getYSpan());
	}

	private static int getRadius(int diameter) {
		return (int) Math.ceil(diameter / 2D);
	}
}
