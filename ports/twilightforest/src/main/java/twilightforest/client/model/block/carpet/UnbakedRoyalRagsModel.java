package twilightforest.client.model.block.carpet;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;
import org.joml.Vector3f;
import twilightforest.client.model.block.connected.ConnectionLogic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

//A carpet-shaped (1 pixel tall) connected texture model: the top and bottom faces use the "wool_ctm" sheet to draw a border
//around connected areas of carpet, the 4 side faces always use the plain "wool" texture.
public class UnbakedRoyalRagsModel implements IUnbakedGeometry<UnbakedRoyalRagsModel> {

	private final BlockElement[][] baseElements;
	private final BlockElement[][][] faceElements;

	public UnbakedRoyalRagsModel() {
		//base elements - the side faces without ctm. No Connected Textures on this bit.
		//the array is made of horizontal directions (Direction.get2DDataValue) and quads
		this.baseElements = new BlockElement[4][4];

		//face elements - the connected bit of the model.
		//the array is made of the directions, quads, and each logic value in the ConnectionLogic class
		//Topmost array indexes to up/down directions (Direction.get3DDataValue, down = 0, up = 1) then inside are quads
		this.faceElements = new BlockElement[2][4][5];
		Vec3i center = new Vec3i(8, 8, 8);

		for (Direction face : Direction.values()) {
			Direction[] planeDirections = ConnectionLogic.AXIS_PLANE_DIRECTIONS[face.getAxis().ordinal()];

			for (int quad = 0; quad < 4; quad++) {
				Vec3i corner = face.getNormal().offset(planeDirections[quad].getNormal()).offset(planeDirections[(quad + 1) % 4].getNormal()).offset(1, 1, 1).multiply(8);
				BlockElement element = new BlockElement(new Vector3f((float) Math.min(center.getX(), corner.getX()), (float) Math.min(center.getY(), corner.getY()) / 16f, (float) Math.min(center.getZ(), corner.getZ())), new Vector3f((float) Math.max(center.getX(), corner.getX()), (float) Math.max(center.getY(), corner.getY()) / 16f, (float) Math.max(center.getZ(), corner.getZ())), Map.of(), null, true);

				if (face.getAxis().isHorizontal()) {
					this.baseElements[face.get2DDataValue()][quad] = new BlockElement(element.from, element.to, Map.of(face, new BlockElementFace(face, -1, "", new BlockFaceUV(ConnectionLogic.NONE.remapUVs(element.uvsByFace(face)), 0))), null, true);
				} else {
					for (ConnectionLogic connectionType : ConnectionLogic.values()) {
						this.faceElements[face.get3DDataValue()][quad][connectionType.ordinal()] = new BlockElement(element.from, element.to, Map.of(face, new BlockElementFace(face, 0, "", new BlockFaceUV(connectionType.remapUVs(element.uvsByFace(face)), 0))), null, true);
					}
				}
			}
		}
	}

	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {
		Transformation transformation = context.getRootTransform();

		if (!transformation.isIdentity()) {
			modelState = new SimpleModelState(modelState.getRotation().compose(transformation), modelState.isUvLocked());
		}

		//making an array list like this is cursed, would not recommend
		@SuppressWarnings("unchecked") //this is fine, I hope
		List<BakedQuad>[] baseQuads = (List<BakedQuad>[]) Array.newInstance(List.class, 4);
		TextureAtlasSprite baseTexture = spriteGetter.apply(context.getMaterial("wool"));

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			baseQuads[direction.get2DDataValue()] = new ArrayList<>();

			for (BlockElement element : this.baseElements[direction.get2DDataValue()]) {
				baseQuads[direction.get2DDataValue()].add(UnbakedGeometryHelper.bakeElementFace(element, element.faces.values().iterator().next(), baseTexture, direction, modelState, modelLocation));
			}
		}

		//we'll use this to figure out which texture to use with the Connected Texture logic
		//NONE uses the first one, everything else uses the 2nd one
		TextureAtlasSprite[] sprites = new TextureAtlasSprite[]{spriteGetter.apply(context.getMaterial("wool")), spriteGetter.apply(context.getMaterial("wool_ctm"))};

		BakedQuad[][][] quads = new BakedQuad[2][4][5];

		for (int dir = 0; dir < 2; dir++) {
			for (int quad = 0; quad < 4; quad++) {
				for (int type = 0; type < 5; type++) {
					BlockElement element = this.faceElements[dir][quad][type];
					quads[dir][quad][type] = UnbakedGeometryHelper.bakeElementFace(element, element.faces.values().iterator().next(), ConnectionLogic.values()[type].chooseTexture(sprites), Direction.values()[dir], modelState, modelLocation);
				}
			}
		}

		ResourceLocation renderTypeHint = context.getRenderTypeHint();
		RenderTypeGroup renderTypes = renderTypeHint != null ? context.getRenderType(renderTypeHint) : RenderTypeGroup.EMPTY;
		return new RoyalRagsModel(baseQuads, quads, spriteGetter.apply(context.getMaterial("wool")), overrides, context.getTransforms(), renderTypes);
	}
}
