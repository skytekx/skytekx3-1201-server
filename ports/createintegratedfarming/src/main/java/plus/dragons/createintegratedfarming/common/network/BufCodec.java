/*
 * Copyright (C) 2026  SkyTekx
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createintegratedfarming.common.network;

import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;

/**
 * SkyTekx: the part of 1.21.1's {@code StreamCodec} that the roosting display records use, on the
 * {@link FriendlyByteBuf} of Forge 1.20.1.
 */
public interface BufCodec<T> {
    void encode(FriendlyByteBuf buffer, T value);

    T decode(FriendlyByteBuf buffer);

    static <T> BufCodec<T> ofMember(BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder) {
        return new BufCodec<>() {
            @Override
            public void encode(FriendlyByteBuf buffer, T value) {
                encoder.accept(value, buffer);
            }

            @Override
            public T decode(FriendlyByteBuf buffer) {
                return decoder.apply(buffer);
            }
        };
    }
}
