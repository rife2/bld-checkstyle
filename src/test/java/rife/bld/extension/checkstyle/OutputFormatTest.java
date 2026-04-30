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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutputFormatTest {

    @Test
    void fromStringIsCaseInsensitive() {
        assertEquals(OutputFormat.XML, OutputFormat.fromString("XML"));
        assertEquals(OutputFormat.SARIF, OutputFormat.fromString("SaRiF"));
        assertEquals(OutputFormat.PLAIN, OutputFormat.fromString("PlAiN"));
    }

    @Test
    void fromStringParsesValidValues() {
        assertEquals(OutputFormat.XML, OutputFormat.fromString("xml"));
        assertEquals(OutputFormat.SARIF, OutputFormat.fromString("sarif"));
        assertEquals(OutputFormat.PLAIN, OutputFormat.fromString("plain"));
    }

    @Test
    void fromStringReturnsPlainForNull() {
        assertEquals(OutputFormat.PLAIN, OutputFormat.fromString(null));
    }

    @Test
    void fromStringThrowsForBlankValue() {
        assertThrows(IllegalArgumentException.class,
                () -> OutputFormat.fromString(""));
        assertThrows(IllegalArgumentException.class,
                () -> OutputFormat.fromString("   "));
    }

    @Test
    void fromStringThrowsForUnknownValue() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> OutputFormat.fromString("json"));
        assertEquals("Unknown output format: json", ex.getMessage());
    }

    @Test
    void toStringReturnsCliValue() {
        assertEquals("xml", OutputFormat.XML.toString());
        assertEquals("sarif", OutputFormat.SARIF.toString());
        assertEquals("plain", OutputFormat.PLAIN.toString());
    }

    @Test
    void valuesContainsAllFormats() {
        var values = OutputFormat.values();
        assertEquals(3, values.length);
        assertArrayEquals(
                new OutputFormat[]{OutputFormat.XML, OutputFormat.SARIF, OutputFormat.PLAIN},
                values
        );
    }
}