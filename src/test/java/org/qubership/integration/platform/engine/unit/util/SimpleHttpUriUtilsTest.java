/*
 * Copyright 2024-2025 NetCracker Technology Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qubership.integration.platform.engine.unit.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.qubership.integration.platform.engine.util.SimpleHttpUriUtils;

import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SimpleHttpUriUtilsTest {

    @Test void should_return_same_uri_when_http_scheme_present() throws MalformedURLException {
        String in = "http://example.com/path";
        Assertions.assertEquals(in, SimpleHttpUriUtils.formatUri(in));
    }

    @Test void should_return_same_uri_when_https_and_port_present() throws MalformedURLException {
        String in = "https://example.com:8080/api";
        assertEquals(in, SimpleHttpUriUtils.formatUri(in));
    }

    @Test void should_add_https_scheme_when_host_without_scheme() throws MalformedURLException {
        assertEquals("https://example.com", SimpleHttpUriUtils.formatUri("example.com"));
    }

    @Test void should_add_https_scheme_when_host_and_path_without_scheme() throws MalformedURLException {
        assertEquals("https://example.com/path", SimpleHttpUriUtils.formatUri("example.com/path"));
    }

    @Test void should_add_https_scheme_when_localhost_with_port_and_path() throws MalformedURLException {
        assertEquals("https://localhost:8080/api", SimpleHttpUriUtils.formatUri("localhost:8080/api"));
    }

    @Test void should_add_https_scheme_when_ipv4_without_scheme() throws MalformedURLException {
        assertEquals("https://127.0.0.1", SimpleHttpUriUtils.formatUri("127.0.0.1"));
    }

    @Test void should_throw_when_input_is_empty() {
        assertThrows(MalformedURLException.class, () -> SimpleHttpUriUtils.formatUri(""));
    }

    @Test void should_throw_when_input_is_relative_path() {
        assertThrows(MalformedURLException.class, () -> SimpleHttpUriUtils.formatUri("/relative/path"));
    }

    @Test void should_return_null_when_input_is_null() throws MalformedURLException {
        assertNull(SimpleHttpUriUtils.formatUri(null));
    }

    @Test void should_be_idempotent_after_first_formatting() throws MalformedURLException {
        String once = SimpleHttpUriUtils.formatUri("example.com/path");
        String twice = SimpleHttpUriUtils.formatUri(once);
        assertEquals(once, twice);
    }

    //TODO Should we check for unsuitable protocol types ?
//    @Test void should_throw_when_scheme_is_unsupported_ftp() {
//        assertThrows(MalformedURLException.class, () -> SimpleHttpUriUtils.formatUri("ftp://example.com"));
//    }

    //TODO Looks like missing case
//    @Test void should_throw_when_http_scheme_has_no_host() {
//        assertThrows(MalformedURLException.class, () -> SimpleHttpUriUtils.formatUri("http://"));
//    }

    //TODO Should we check not only port numbers count but actual port values range 0-65535
//    @Test void should_accept_max_port_65535_when_scheme_present() throws MalformedURLException {
//        String in = "https://example.com:65535/ok";
//        assertEquals(in, SimpleHttpUriUtils.formatUri(in));
//    }

//    @Test void should_throw_when_port_exceeds_range_65536() {
//        assertThrows(MalformedURLException.class, () -> SimpleHttpUriUtils.formatUri("example.com:65536"));
//    }
}
