/*
 * Copyright (c) 2016 Nova Ordis LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.novaordis.release.model.maven;

/**
 * A project versioning model describes how the versions of various modules for a multi-module project evolve in
 * relationship with each other. For more details see
 * https://kb.novaordis.com/index.php/Multi-Module_Maven_Projects#Modules_and_Versions
 *
 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 11/27/16
 */
public enum ProjectVersioningModel {

    SINGLE_MODULE,
    MULTIPLE_MODULE_LOCKSTEP,
    MULTIPLE_MODULE_INDEPENDENT
}
