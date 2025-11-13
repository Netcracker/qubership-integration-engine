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

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.qubership.integration.platform.engine.util.MapperUtils;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MapperUtilsTest {

    @Test void should_return_null_when_fromTimestamp_receives_null() {
        assertNull(MapperUtils.fromTimestamp(null));
    }

    @Test void should_return_null_when_toTimestamp_receives_null() {
        assertNull(MapperUtils.toTimestamp(null));
    }

    @Test void should_return_same_millis_when_fromTimestamp_has_zero_nanos() {
        long ms = 1_696_000_000_123L;
        Timestamp ts = new Timestamp(ms);
        assertEquals(ms, MapperUtils.fromTimestamp(ts));
    }

    @Test void should_round_trip_millis_only() {
        long ms = 42L;
        Long back = MapperUtils.fromTimestamp(MapperUtils.toTimestamp(ms));
        assertEquals(ms, back);
    }

    @Test void should_drop_submillisecond_nanos_when_mapping_to_long() {
        long baseMs = 1_000L;
        Timestamp ts = new Timestamp(baseMs);
        ts.setNanos(999_999);
        assertEquals(baseMs, MapperUtils.fromTimestamp(ts));
    }

    @Test void should_include_whole_milliseconds_from_nanos_in_getTime() {
        long baseMs = 1_650_000_000_000L;
        Timestamp ts = new Timestamp(baseMs);
        ts.setNanos(987_654_321);
        assertEquals(baseMs + 987, MapperUtils.fromTimestamp(ts));
    }
}
