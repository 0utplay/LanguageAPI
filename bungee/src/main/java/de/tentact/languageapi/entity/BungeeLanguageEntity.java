/*
 * MIT License
 *
 * Copyright (c) 2021 0utplay (Aldin Sijamhodzic)
 * Copyright (c) 2021 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.tentact.languageapi.entity;

import de.tentact.languageapi.message.Message;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Locale;
import java.util.UUID;

public class BungeeLanguageEntity extends DefaultLanguageOfflineEntity implements LanguageEntity {

  private final ProxiedPlayer bungeePlayer;

  public BungeeLanguageEntity(LanguageOfflineEntity languageOfflineEntity, ProxiedPlayer player) {
    this(languageOfflineEntity.getEntityId(), languageOfflineEntity.getLocale(), player);
  }

  public BungeeLanguageEntity(UUID entityId, Locale locale, ProxiedPlayer player) {
    super(entityId, locale);
    this.bungeePlayer = player;
  }

  @Override
  public void sendMessage(Message translation, Object... parameters) {
    translation.buildAsync(super.locale, parameters).thenAccept(message ->
        this.bungeePlayer.sendMessage(TextComponent.fromLegacyText(message)));
  }
}