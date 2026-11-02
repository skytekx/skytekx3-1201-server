package twilightforest.compat.top;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import twilightforest.TwilightForestMod;
import twilightforest.entity.passive.quest.ram.QuestingRamContext;
import twilightforest.entity.passive.quest.ram.QuestingRamCurrentContext;
import twilightforest.util.ColorUtil;

import java.util.Map;

public class QuestRamWoolElement implements IElement {

    public static ResourceLocation ID = TwilightForestMod.prefix("quest_ram_wool");

    private final int colorData;

    public QuestRamWoolElement(int colorData) {
        this.colorData = colorData;
    }

    public QuestRamWoolElement(FriendlyByteBuf buf) {
        this.colorData = buf.readInt();
    }

    public boolean isColorPresent(DyeColor color) {
        return (this.colorData & (1 << color.getId())) > 0;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y) {
        // 4.8 b06ef824a: scale through the GUI pose stack instead of permanently shifting the model view stack
        PoseStack stack = graphics.pose();
        stack.pushPose();
        RenderSystem.enableDepthTest();
        stack.translate(3.0F, 10, 0);
        stack.scale(0.6f, 0.6f, 0.6f);

        // show what the (data pack driven) quest still asks for
        QuestingRamContext quest = QuestingRamCurrentContext.getClientContext();
        int getRenderedWools = 0;
        int column = 0;
        int row = 0;
        for (Map.Entry<DyeColor, Block> entry : ColorUtil.WOOL_TO_DYE_IN_RAM_ORDER.entrySet()) {
            if (!this.isColorPresent(entry.getKey())) {
                if (getRenderedWools % 8 == 0) {
                    row++;
                    column = 0;
                } else {
                    column++;
                }

                graphics.renderItem(quest.getDisplayStack(entry.getKey(), entry.getValue()), x + (column * 16), y + (row * 18));

                getRenderedWools++;
            }
        }
        RenderSystem.disableDepthTest();
        stack.popPose();
    }

    @Override
    public int getWidth() {
        return 4;
    }

    @Override
    public int getHeight() {
        int getRenderedWools = 0;
        int row = 0;
        for (Map.Entry<DyeColor, Block> entry : ColorUtil.WOOL_TO_DYE_IN_RAM_ORDER.entrySet()) {
            if (!this.isColorPresent(entry.getKey())) {
                if (getRenderedWools % 8 == 0) {
                    row++;
                }

                getRenderedWools++;
            }
        }

        return row * 11;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.colorData);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    public enum Factory implements IElementFactory {
        INSTANCE;

        @Override
        public IElement createElement(FriendlyByteBuf buf) {
            return new QuestRamWoolElement(buf);
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}
