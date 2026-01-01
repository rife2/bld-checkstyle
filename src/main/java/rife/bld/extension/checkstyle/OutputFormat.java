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

    public final String label;

    /**
     * Sets the label of this output format.
     */
    OutputFormat(String label) {
        this.label = label;
    }
}