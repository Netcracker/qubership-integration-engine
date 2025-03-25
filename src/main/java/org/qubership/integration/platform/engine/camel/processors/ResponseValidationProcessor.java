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

package org.qubership.integration.platform.engine.camel.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.qubership.integration.platform.engine.camel.JsonMessageValidator;
import org.qubership.integration.platform.engine.errorhandling.ResponseValidationException;
import org.qubership.integration.platform.engine.errorhandling.ValidationException;
import org.qubership.integration.platform.engine.model.constants.CamelConstants;
import org.qubership.integration.platform.engine.service.debugger.util.MessageHelper;
import org.springframework.stereotype.Component;


/**
 * Processor perform JSON response body validation by scheme defined in system specification
 */
@Component
public class ResponseValidationProcessor implements Processor {

    private final JsonMessageValidator validator;

    public ResponseValidationProcessor(JsonMessageValidator validator) {
        this.validator = validator;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            validate(exchange);
        } catch (ValidationException e) {
            throw new ResponseValidationException(e.getMessage());
        }
    }

    private void validate(Exchange exchange) {
        validateJSON(exchange);
    }

    private void validateJSON(Exchange exchange) {
        String validationSchema = exchange.getProperty(CamelConstants.Properties.VALIDATION_SCHEMA, String.class);
        if (!StringUtils.isBlank(validationSchema)) {
            String inputJsonMessage = MessageHelper.extractBody(exchange);
            validator.validate(inputJsonMessage, validationSchema);
        }
    }
}
