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

package de.tentact.languageapi.configuration;

import com.github.derrop.documents.DefaultDocument;
import com.github.derrop.documents.Document;
import com.github.derrop.documents.Documents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

public class Configuration {

    Document settingsDocument = new DefaultDocument();
    final File settingsFile = new File("plugins/LanguageAPI", "config.json");
    private LanguageConfig languageConfig;

    public Configuration(Logger logger) {
        if (settingsFile.exists()) {
            settingsDocument = Documents.jsonStorage().read(settingsFile);
        } else {
            try {
                Files.createFile(settingsFile.toPath());
                settingsDocument.append("config",
                        new LanguageConfig(
                                new MySQL(
                                        "hostname",
                                        "languagapi",
                                        "languagapi",
                                        "password",
                                        3306
                                ),
                                new LanguageSetting(
                                        "de_de",
                                        5,
                                        true
                                )
                        )
                ).json().write(settingsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.getLanguageConfig().setLogger(logger);
        this.getLanguageConfig().getMySQL().setLogger(logger);
    }

    public LanguageConfig getLanguageConfig() {
        if (this.languageConfig != null) {
            return this.languageConfig;
        }
        this.languageConfig = this.settingsDocument.get("config", LanguageConfig.class);
        return this.languageConfig;
    }

}

