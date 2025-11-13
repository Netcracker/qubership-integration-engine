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

package org.qubership.integration.platform.engine.unit.util.log;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.qubership.integration.platform.engine.errorhandling.errorcode.ErrorCode;
import org.qubership.integration.platform.engine.util.log.ExtendedErrorLogger;
import org.qubership.integration.platform.engine.util.log.ExtendedErrorLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ExtendedErrorLoggerTest {

    @Test
    void should_prefix_plain_message_when_errorCode_present() {
        Logger delegate = mock(Logger.class);
        ErrorCode code = mock(ErrorCode.class);
        when(code.getFormattedCode()).thenReturn("E-123");

        try (MockedStatic<LoggerFactory> mocked = Mockito.mockStatic(LoggerFactory.class)) {
            mocked.when(() -> LoggerFactory.getLogger("my.logger")).thenReturn(delegate);

            ExtendedErrorLogger log = ExtendedErrorLoggerFactory.getLogger("my.logger");
            log.error(code, "Something bad happened");

            verify(delegate).error("[error_code=E-123] Something bad happened");
        }
    }

    @Test
    void should_not_prefix_plain_message_when_errorCode_is_null() {
        Logger delegate = mock(Logger.class);

        try (MockedStatic<LoggerFactory> mocked = Mockito.mockStatic(LoggerFactory.class)) {
            mocked.when(() -> LoggerFactory.getLogger("my.logger")).thenReturn(delegate);

            ExtendedErrorLogger log = ExtendedErrorLoggerFactory.getLogger("my.logger");
            log.error((ErrorCode) null, "Just a message");

            verify(delegate).error("Just a message");
        }
    }

    @Test
    void should_prefix_and_forward_single_arg_when_format_with_one_arg() {
        Logger delegate = mock(Logger.class);
        ErrorCode code = mock(ErrorCode.class);
        when(code.getFormattedCode()).thenReturn("E-42");

        try (MockedStatic<LoggerFactory> mocked = Mockito.mockStatic(LoggerFactory.class)) {
            mocked.when(() -> LoggerFactory.getLogger("fmt")).thenReturn(delegate);

            ExtendedErrorLogger log = ExtendedErrorLoggerFactory.getLogger("fmt");
            log.error(code, "Hello {}", "world");

            verify(delegate).error("[error_code=E-42] Hello {}", "world");
        }
    }

    @Test
    void should_prefix_and_forward_two_args_when_format_with_two_args() {
        Logger delegate = mock(Logger.class);
        ErrorCode code = mock(ErrorCode.class);
        when(code.getFormattedCode()).thenReturn("E-7");

        try (MockedStatic<LoggerFactory> mocked = Mockito.mockStatic(LoggerFactory.class)) {
            mocked.when(() -> LoggerFactory.getLogger("fmt2")).thenReturn(delegate);

            ExtendedErrorLogger log = ExtendedErrorLoggerFactory.getLogger("fmt2");
            log.error(code, "X={}, Y={}", 10, 20);

            verify(delegate).error("[error_code=E-7] X={}, Y={}", 10, 20);
        }
    }

    @Test
    void should_prefix_and_forward_varargs_when_format_with_varargs() {
        Logger delegate = mock(Logger.class);
        ErrorCode code = mock(ErrorCode.class);
        when(code.getFormattedCode()).thenReturn("E-VA");

        try (MockedStatic<LoggerFactory> mocked = Mockito.mockStatic(LoggerFactory.class)) {
            mocked.when(() -> LoggerFactory.getLogger("fmtN")).thenReturn(delegate);

            ExtendedErrorLogger log = ExtendedErrorLoggerFactory.getLogger("fmtN");
            log.error(code, "vals: {} {} {}", 1, "two", 3L);

            verify(delegate).error("[error_code=E-VA] vals: {} {} {}", 1, "two", 3L);
        }
    }

    @Test
    void should_prefix_and_forward_throwable_when_error_with_throwable() {
        Logger delegate = mock(Logger.class);
        ErrorCode code = mock(ErrorCode.class);
        when(code.getFormattedCode()).thenReturn("E-T");
        RuntimeException ex = new RuntimeException("boom");

        try (MockedStatic<LoggerFactory> mocked = Mockito.mockStatic(LoggerFactory.class)) {
            mocked.when(() -> LoggerFactory.getLogger("withEx")).thenReturn(delegate);

            ExtendedErrorLogger log = ExtendedErrorLoggerFactory.getLogger("withEx");
            log.error(code, "Failed op", ex);

            verify(delegate).error("[error_code=E-T] Failed op", ex);
        }
    }

    @Test
    void should_use_class_name_when_constructed_via_factory_with_class() {
        Logger delegate = mock(Logger.class);

        try (MockedStatic<LoggerFactory> mocked = Mockito.mockStatic(LoggerFactory.class)) {
            mocked.when(() -> LoggerFactory.getLogger(String.class.getName())).thenReturn(delegate);

            ExtendedErrorLogger log = ExtendedErrorLoggerFactory.getLogger(String.class);
            log.error("hi");

            verify(delegate).error("hi");
        }
    }

    @Test
    void should_leave_plain_error_overloads_untouched() {
        Logger delegate = mock(Logger.class);

        try (MockedStatic<LoggerFactory> mocked = Mockito.mockStatic(LoggerFactory.class)) {
            mocked.when(() -> LoggerFactory.getLogger("plain")).thenReturn(delegate);

            ExtendedErrorLogger log = ExtendedErrorLoggerFactory.getLogger("plain");
            log.error("msg");
            log.error("fmt {}", 123);
            log.error("fmt {} {}", "a", "b");

            verify(delegate).error("msg");
            verify(delegate).error("fmt {}", 123);
            verify(delegate).error("fmt {} {}", "a", "b");
        }
    }

    @Test
    void should_return_delegate_name_from_getName() {
        Logger delegate = mock(Logger.class);
        when(delegate.getName()).thenReturn("delegate.name");

        try (MockedStatic<LoggerFactory> mocked = Mockito.mockStatic(LoggerFactory.class)) {
            mocked.when(() -> LoggerFactory.getLogger("name")).thenReturn(delegate);

            ExtendedErrorLogger log = ExtendedErrorLoggerFactory.getLogger("name");
            log.info("noop");

            org.junit.jupiter.api.Assertions.assertEquals("delegate.name", log.getName());
        }
    }
}
