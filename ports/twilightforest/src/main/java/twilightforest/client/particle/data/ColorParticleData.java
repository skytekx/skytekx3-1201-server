package twilightforest.client.particle.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.util.Locale;

/**
 * A particle option carrying an ARGB colour, the 1.20.1 stand-in for 1.20.5's ColorParticleOption that TF 4.8 uses for
 * twilightforest:magic_effect. Works on both sides; only the particle that reads it is client only.
 */
public record ColorParticleData(ParticleType<ColorParticleData> type, int color) implements ParticleOptions {

	public static final Deserializer<ColorParticleData> DESERIALIZER = new Deserializer<>() {
		@Override
		public ColorParticleData fromCommand(ParticleType<ColorParticleData> type, StringReader reader) throws CommandSyntaxException {
			reader.expect(' ');
			float red = reader.readFloat();
			reader.expect(' ');
			float green = reader.readFloat();
			reader.expect(' ');
			float blue = reader.readFloat();
			float alpha = 1.0F;
			if (reader.canRead()) {
				reader.expect(' ');
				alpha = reader.readFloat();
			}
			return create(type, alpha, red, green, blue);
		}

		@Override
		public ColorParticleData fromNetwork(ParticleType<ColorParticleData> type, FriendlyByteBuf buffer) {
			return new ColorParticleData(type, buffer.readInt());
		}
	};

	public static Codec<ColorParticleData> codec(ParticleType<ColorParticleData> type) {
		return RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("color").forGetter(ColorParticleData::color)
		).apply(instance, color -> new ColorParticleData(type, color)));
	}

	public static ColorParticleData create(ParticleType<ColorParticleData> type, float red, float green, float blue) {
		return create(type, 1.0F, red, green, blue);
	}

	public static ColorParticleData create(ParticleType<ColorParticleData> type, float alpha, float red, float green, float blue) {
		return new ColorParticleData(type, FastColor.ARGB32.color(as8BitChannel(alpha), as8BitChannel(red), as8BitChannel(green), as8BitChannel(blue)));
	}

	private static int as8BitChannel(float value) {
		return Mth.floor(Mth.clamp(value, 0.0F, 1.0F) * 255.0F);
	}

	public float getRed() {
		return FastColor.ARGB32.red(this.color) / 255.0F;
	}

	public float getGreen() {
		return FastColor.ARGB32.green(this.color) / 255.0F;
	}

	public float getBlue() {
		return FastColor.ARGB32.blue(this.color) / 255.0F;
	}

	public float getAlpha() {
		return FastColor.ARGB32.alpha(this.color) / 255.0F;
	}

	@Override
	public ParticleType<?> getType() {
		return this.type;
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf buffer) {
		buffer.writeInt(this.color);
	}

	@Override
	public String writeToString() {
		return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %.2f", BuiltInRegistries.PARTICLE_TYPE.getKey(this.type), this.getRed(), this.getGreen(), this.getBlue(), this.getAlpha());
	}
}
