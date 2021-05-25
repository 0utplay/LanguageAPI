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

package de.tentact.languageapi.cache;

import de.tentact.languageapi.database.RedisDatabaseProvider;
import io.lettuce.core.SetArgs;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;

public class RedisCache<K, V> implements LanguageCache<K, V> {

  private static final SetArgs EXPIRATION = SetArgs.Builder.ex(Duration.ofHours(1));
  protected final RedisDatabaseProvider redisDatabaseProvider;

  public RedisCache(RedisDatabaseProvider redisDatabaseProvider) {
    this.redisDatabaseProvider = redisDatabaseProvider;
  }

  @Override
  public void put(K key, V value) {
    this.redisDatabaseProvider.getConnection().sync().set(key, value, EXPIRATION);
  }

  @Override
  public V getIfPresent(K key) {
    return (V) this.redisDatabaseProvider.getConnection().sync().get(key);
  }

  @Override
  public void invalidate(K key) {
    this.redisDatabaseProvider.getConnection().sync().del(key);
  }

  @Override
  public Collection<V> getValues() {
    return this.redisDatabaseProvider.getConnection().sync().get;
  }

  @Override
  public Map<K, V> asMap() {

  }

  static class PersistenceRedisCache<K, V> extends RedisCache<K, V> implements LanguageCache<K, V> {

    public PersistenceRedisCache(RedisDatabaseProvider redisDatabaseProvider) {
      super(redisDatabaseProvider);
    }

    @Override
    public void put(K key, V value) {
      super.redisDatabaseProvider.getConnection().sync().set(key, value);
    }
  }
}
