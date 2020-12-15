/*
 * MIT License
 *
 * Copyright (c) 2020 0utplay
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

package de.tentact.languageapi.api;

import de.tentact.languageapi.configuration.LanguageConfig;
import de.tentact.languageapi.file.FileHandler;
import de.tentact.languageapi.player.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BungeeCordLanguageAPI extends DefaultLanguageAPI {

    private final PlayerManager playerManager;
    private final FileHandler fileHandler;
    private final PlayerExecutor playerExecutor;

    public BungeeCordLanguageAPI(LanguageConfig languageConfig) {
        super(languageConfig);
        this.playerManager = new BungeePlayerManager();
        this.fileHandler = new BungeeFileHandler(this);
        this.playerExecutor = new BungeePlayerExecutor(this, languageConfig);
    }

    @Override
    public @NotNull PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    @Override
    public @NotNull PlayerExecutor getPlayerExecutor() {
        return this.playerExecutor;
    }

    @Override
    public @NotNull SpecificPlayerExecutor getSpecificPlayerExecutor(@NotNull UUID playerId) {
        return new BungeeSpecificPlayerExecutor(playerId);
    }

    @Override
    public FileHandler getFileHandler() {
        return this.fileHandler;
    }


}
