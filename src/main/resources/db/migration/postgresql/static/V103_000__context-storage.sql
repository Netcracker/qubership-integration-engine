-- Copyright 2024-2025 NetCracker Technology Corporation
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.


create table IF NOT EXISTS context_system_records
(
    id                 TEXT not null
    constraint pk_context_system_recods
    primary key,
    value              JSONB,
    context_id         TEXT,
    context_service_id TEXT,
    created_at         TIMESTAMPTZ,
    updated_at         TIMESTAMPTZ,
    expires_at         TIMESTAMPTZ
    );
