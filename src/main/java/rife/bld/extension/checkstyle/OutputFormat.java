/*
 * Copyright 2023-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rife.bld.extension.checkstyle;

/**
 * The Checkstyle output format for XML, SARIF and default (plain) logger.
 *
 * @author <a href="https://erik.thauvin.net">Erik C. Thauvin</a>
 * @since 1.0
 */
public enum OutputFormat {
    /**
     * Extensible Markup Language (XML) output format.
     */
    XML("xml"),
    /**
     * Static Analysis Results Interchange Format (SARIF) output format.
     */
    SARIF("sarif"),
    /**
     * Default (plain) output format.
     */
    PLAIN("plain");

    private final String value;

    OutputFormat(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Parses a string into an {@code OutputFormat}.
     *
     * @param text the format name, case-insensitive
     * @return the matching format, or {@link #PLAIN} if null
     * @throws IllegalArgumentException if the format is unknown
     */
    public static OutputFormat fromString(String text) {
        if (text == null) {
            return PLAIN;
        }
        for (var format : values()) {
            if (format.value.equalsIgnoreCase(text)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown output format: " + text);
    }
}