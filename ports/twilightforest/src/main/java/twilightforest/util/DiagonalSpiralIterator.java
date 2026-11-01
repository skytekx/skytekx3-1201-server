package twilightforest.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Walks a square grid outwards from a center in rings of equal Manhattan distance ("diamonds"), so positions are
 * visited in approximate order of proximity. Once a ring reaches past {@code radius} it is clipped to the bounding
 * square, so the whole (2 * radius / spacing + 1)^2 box is visited exactly once.
 * <p>
 * {@code radius} is given in the same units as the coordinates (it is divided by {@code spacing} internally),
 * unlike {@link XZQuadrantIterator}, which counts its radius in steps.
 * <p>
 * Backported from Twilight Forest 4.8 (upstream 7473f5cc8, 75c1395f3, 1f741d87a, 64824c3a9).
 */
public class DiagonalSpiralIterator<T> implements Iterator<T>, Iterable<T> {
	private final int xConstant;
	private final int zConstant;
	private final int radius;
	private final int spacing;
	private final XZQuadrantIterator.BinaryIntegerFunction<T> converter;

	private int offset = 0; // How far are the rows from the center
	private Direction side = Direction.NORTH; // Which of the 4 sides is being marched over
	private int advance = 0; // Where on the side?

	public static DiagonalSpiralIterator<BlockPos> atElevationZero(int xConstant, int zConstant, boolean skipCenter, int radius, int spacing) {
		return new DiagonalSpiralIterator<>(xConstant, zConstant, skipCenter, radius, spacing, (x, z) -> new BlockPos(x, 0, z));
	}

	public DiagonalSpiralIterator(int xConstant, int zConstant, boolean skipCenter, int radius, int spacing, XZQuadrantIterator.BinaryIntegerFunction<T> converter) {
		this.xConstant = xConstant;
		this.zConstant = zConstant;
		this.radius = radius / spacing;
		this.spacing = spacing;
		this.converter = converter;

		if (skipCenter) {
			this.offset = 1;
		}
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return this.offset <= this.radius * 2;
	}

	@Override
	public T next() {
		if (!this.hasNext()) throw new NoSuchElementException("DiagonalSpiralIterator exhausted");

		T newPos = this.nextBlockPos();
		this.advancePos();
		return newPos;
	}

	private void advancePos() {
		if (this.offset == 0) {
			this.offset = 1;
			this.advance = 0;
			return;
		}

		this.advance++; // Prepped for next iteration

		int advanceLimit = Math.min(this.offset, this.radius + 1);
		if (this.advance >= advanceLimit) {
			this.side = this.side.getClockWise();
			if (this.side == Direction.NORTH) {
				this.offset++;
			}
			this.advance = 0;
			if (this.offset > this.radius) {
				this.advance = this.offset - this.radius;
			}
		}
	}

	private T nextBlockPos() {
		Direction direction = this.side;
		Direction clockWise = direction.getClockWise();
		int u = this.offset - this.advance;
		int v = this.advance;
		return this.converter.apply(this.xConstant + (direction.getStepX() * u + clockWise.getStepX() * v) * this.spacing, this.zConstant + (direction.getStepZ() * u + clockWise.getStepZ() * v) * this.spacing);
	}
}
