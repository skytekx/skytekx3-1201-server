package twilightforest.item;

public class GreaterFlaskItem extends BrittleFlaskItem {

	public GreaterFlaskItem(Properties properties) {
		super(properties);
	}

	//no breaking
	@Override
	public boolean canBreak() {
		return false;
	}
}
