/*
 * MIT License
 *
 * Copyright (c) 2020 0utplay (Aldin Sijamhodzic)
 * Copyright (c) 2020 contributors
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

import com.velocitypowered.api.proxy.ProxyServer;
import de.tentact.languageapi.configuration.LanguageConfig;
import de.tentact.languageapi.console.ConsoleExecutor;
import de.tentact.languageapi.player.PlayerExecutor;
import de.tentact.languageapi.player.VelocityConsoleExecutor;
import de.tentact.languageapi.player.VelocityPlayerExecutor;
import org.jetbrains.annotations.NotNull;

public class VelocityLanguageAPI extends DefaultLanguageAPI {

    private final PlayerExecutor playerExecutor;
    private final ConsoleExecutor consoleExecutor;

    public VelocityLanguageAPI(ProxyServer proxyServer, LanguageConfig languageConfig) {
        super(languageConfig);
        this.playerExecutor = new VelocityPlayerExecutor(proxyServer, this, languageConfig);
        this.consoleExecutor = new VelocityConsoleExecutor(this, proxyServer.getConsoleCommandSource());
    }

    @Override
    public @NotNull PlayerExecutor getPlayerExecutor() {
        return this.playerExecutor;
    }

    @Override
    public @NotNull ConsoleExecutor getConsoleExecutor() {
        return this.consoleExecutor;
    }
}
