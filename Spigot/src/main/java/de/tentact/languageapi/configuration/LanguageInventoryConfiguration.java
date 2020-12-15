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

import org.bukkit.Material;

import java.util.Collection;

public class LanguageInventoryConfiguration {

    private final String name;
    private final String fillItemMaterial;
    private final byte subId;
    private final Collection<LanguageItem> languages;
    private final boolean useInventory;

    public LanguageInventoryConfiguration(boolean useInventory, String name, String fillItemMaterial, byte subId, Collection<LanguageItem> languages) {
        this.useInventory = useInventory;
        this.name = name;
        this.fillItemMaterial = fillItemMaterial;
        this.subId = subId;
        this.languages = languages;
    }

    public boolean isUseInventory() {
        return this.useInventory;
    }

    public String getName() {
        return this.name;
    }

    public Material getFillItemMaterial() {
        return Material.getMaterial(this.fillItemMaterial);
    }

    public byte getSubId() {
        return this.subId;
    }

    public Collection<LanguageItem> getLanguages() {
        return this.languages;
    }

    private int calculateRows() {
        return (int) Math.ceil(languages.size() / 9.0);
    }

    public int getInventorySize() {
        return this.calculateRows() * 9;
    }
}
