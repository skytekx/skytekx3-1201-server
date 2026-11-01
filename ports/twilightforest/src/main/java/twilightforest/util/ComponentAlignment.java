package twilightforest.util;

/**
 * Horizontal text alignment inside a column of the Ore Meter HUD table (4.8 97f03a8a2).
 */
public enum ComponentAlignment {
	LEFT {
		@Override
		public int getTextOffset(int textWidth, int maxColumnWidth) {
			return 0;
		}
	},
	CENTER {
		@Override
		public int getTextOffset(int textWidth, int maxColumnWidth) {
			return (maxColumnWidth - textWidth) >> 1;
		}
	},
	RIGHT {
		@Override
		public int getTextOffset(int textWidth, int maxColumnWidth) {
			return maxColumnWidth - textWidth;
		}
	};

	public abstract int getTextOffset(int textWidth, int maxColumnWidth);
}
