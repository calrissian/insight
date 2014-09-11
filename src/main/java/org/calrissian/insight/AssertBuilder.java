/*
 * Copyright (C) 2014 The Calrissian Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.calrissian.insight;

/**
 * AssertBuilders are tailored collections of {@link org.hamcrest.Matcher}s that when ran will tell you of every failure
 * that occurred. The goal of AssertBuilders are to make testing easier and the cycle of fail-fix-run to be as short as
 * possible.
 */
public interface AssertBuilder {

    /**
     * Runs the {@link org.hamcrest.Matcher}s that have been added to the org.calrissian.insight.AssertBuilder.
     *
     * @throws java.lang.AssertionError if any of the {@link org.hamcrest.Matcher}s failed.
     */
    void runAssert();
}
