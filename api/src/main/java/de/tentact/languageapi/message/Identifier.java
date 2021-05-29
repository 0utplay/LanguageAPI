package de.tentact.languageapi.message;

import java.util.Map;

public interface Identifier {

  /**
   * @param translationKey the translationKey that belongs to the identifier
   * @return a new identifier
   */
  static Identifier of(String translationKey) {
    return new DefaultIdentifier(translationKey);
  }

  /**
   * @param translationKey the translationKey that belongs to the identifier
   * @param parameters     the matching parameter explanations
   * @return a new identifier
   */
  static Identifier of(String translationKey, String... parameters) {
    return new DefaultIdentifier(translationKey, parameters);
  }

  /**
   * Returns the mapped parameters
   *
   * @return the mapped parameters
   */
  Map<Integer, String> getParameters();

  /**
   * The translationKey that belongs to the identifier
   *
   * @return the translationKey that belongs to the identifier
   */
  String getTranslationKey();

  /**
   * Sets an explanation for each parameter so it can be displayed to a user
   *
   * @param parameters the matching parameter explanations
   * @return this identifier
   */
  Identifier parameters(String... parameters);

  /**
   * Loads the parameters of an identifier from the cache or the database
   *
   * @return the loaded identifier
   */
  Identifier load();

  /**
   * Updates the parameters of the identifier in the cache & database
   *
   * @return the written identifier
   */
  Identifier write();
}
