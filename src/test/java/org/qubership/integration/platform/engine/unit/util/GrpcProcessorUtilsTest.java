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

import io.grpc.stub.StreamObserver;
import org.apache.camel.Exchange;
import org.apache.camel.component.grpc.GrpcUtils;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.qubership.integration.platform.engine.model.constants.CamelConstants;
import org.qubership.integration.platform.engine.util.GrpcProcessorUtils;

import java.lang.NoSuchMethodException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тестируем GrpcProcessorUtils через мок Exchange и статик-моки GrpcUtils.
 * Проверяем извлечение типов request/response и ошибку, когда метод не найден.
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GrpcProcessorUtilsTest {

    // ---- фиктивные "прото"-типы и ImplBase, имитирующие код, сгенерированный gRPC ----
    public static final class FakeRequest {}
    public static final class FakeResponse {}

    /** Имитируем класс вида my.pkg.MyServiceGrpc$MyServiceImplBase */
    public static class FakeServiceGrpc {
        public abstract static class FakeServiceImplBase {
            // сигнатура как у gRPC: Request, StreamObserver<Response>
            public void doSomething(FakeRequest req, StreamObserver<FakeResponse> obs) {}
            // второй метод, чтобы убедиться что выбирается нужный по имени
            public void anotherCall(FakeRequest req, StreamObserver<FakeResponse> obs) {}
        }
    }

//    @Test
//    void should_return_request_class_when_method_exists() throws Exception {
//        Exchange ex = mock(Exchange.class);
//        when(ex.getProperty(CamelConstants.Properties.GRPC_SERVICE_NAME, String.class))
//                .thenReturn("test.grpc.FakeService");
//        when(ex.getProperty(CamelConstants.Properties.GRPC_METHOD_NAME, String.class))
//                .thenReturn("DoSomething");
//        when(ex.getContext()).thenReturn(null); // контекст не важен: GrpcUtils замокаем
//
//        try (MockedStatic<GrpcUtils> mocked = Mockito.mockStatic(GrpcUtils.class)) {
//            mocked.when(() -> GrpcUtils.extractServiceName("test.grpc.FakeService"))
//                    .thenReturn("FakeService");
//            mocked.when(() -> GrpcUtils.extractServicePackage("test.grpc.FakeService"))
//                    .thenReturn("test.grpc");
//            mocked.when(() -> GrpcUtils.convertMethod2CamelCase("DoSomething"))
//                    .thenReturn("doSomething");
//            mocked.when(() -> GrpcUtils.constructGrpcImplBaseClass("test.grpc", "FakeService", null))
//                    .thenReturn(FakeServiceGrpc.FakeServiceImplBase.class);
//
//            Class<?> reqClass = GrpcProcessorUtils.getRequestClass(ex);
//            assertEquals(FakeRequest.class, reqClass);
//        }
//    }
//
//    @Test
//    void should_throw_NoSuchMethodException_when_method_not_found() {
//        Exchange ex = mock(Exchange.class);
//        when(ex.getProperty(CamelConstants.Properties.GRPC_SERVICE_NAME, String.class))
//                .thenReturn("test.grpc.FakeService");
//        when(ex.getProperty(CamelConstants.Properties.GRPC_METHOD_NAME, String.class))
//                .thenReturn("DoSomething"); // исходное имя
//        when(ex.getContext()).thenReturn(null);
//
//        try (MockedStatic<GrpcUtils> mocked = Mockito.mockStatic(GrpcUtils.class)) {
//            mocked.when(() -> GrpcUtils.extractServiceName("test.grpc.FakeService"))
//                    .thenReturn("FakeService");
//            mocked.when(() -> GrpcUtils.extractServicePackage("test.grpc.FakeService"))
//                    .thenReturn("test.grpc");
//            // подменяем на "неизвестный" camelCase — чтобы поиск не нашёл метод
//            mocked.when(() -> GrpcUtils.convertMethod2CamelCase("DoSomething"))
//                    .thenReturn("unknownMethod");
//            mocked.when(() -> GrpcUtils.constructGrpcImplBaseClass("test.grpc", "FakeService", null))
//                    .thenReturn(FakeServiceGrpc.FakeServiceImplBase.class);
//
//            NoSuchMethodException exn = assertThrows(NoSuchMethodException.class,
//                    () -> GrpcProcessorUtils.getRequestClass(ex));
//            // Сообщение формируется через MethodDescriptor.generateFullMethodName(service, method)
//            assertTrue(exn.getMessage().contains("gRPC method not found"));
//            assertTrue(exn.getMessage().contains("test.grpc.FakeService/DoSomething"));
//        }
//    }

    //TODO uncomment if method really usefully
//    @Test
//    void should_return_response_class_when_method_exists() throws Exception {
//        Exchange ex = mock(Exchange.class);
//        when(ex.getProperty(CamelConstants.Properties.GRPC_SERVICE_NAME, String.class))
//                .thenReturn("test.grpc.FakeService");
//        when(ex.getProperty(CamelConstants.Properties.GRPC_METHOD_NAME, String.class))
//                .thenReturn("DoSomething");
//        when(ex.getContext()).thenReturn(null);
//
//        try (MockedStatic<GrpcUtils> mocked = Mockito.mockStatic(GrpcUtils.class)) {
//            mocked.when(() -> GrpcUtils.extractServiceName("test.grpc.FakeService"))
//                    .thenReturn("FakeService");
//            mocked.when(() -> GrpcUtils.extractServicePackage("test.grpc.FakeService"))
//                    .thenReturn("test.grpc");
//            mocked.when(() -> GrpcUtils.convertMethod2CamelCase("DoSomething"))
//                    .thenReturn("doSomething");
//            mocked.when(() -> GrpcUtils.constructGrpcImplBaseClass("test.grpc", "FakeService", null))
//                    .thenReturn(FakeServiceGrpc.FakeServiceImplBase.class);
//
//            Class<?> respClass = GrpcProcessorUtils.getResponseClass(ex);
//            assertEquals(FakeResponse.class, respClass);
//        }
//    }
}
