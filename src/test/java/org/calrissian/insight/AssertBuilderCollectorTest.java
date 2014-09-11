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

import org.junit.Test;

/**
 * Test for the AssertBuilderCollector class.
 */
public class AssertBuilderCollectorTest {

    @Test(expected = NullPointerException.class)
    public void testAddNull() {
        new AssertBuilderCollector().add(null);
    }

    @Test
    public void testAddPass() {
        final AssertBuilderCollector assertBuilder = new AssertBuilderCollector();
        assertBuilder.add(pass());
        assertBuilder.runAssert();
    }

    @Test(expected = AssertionError.class)
    public void testAddFailure() {
        final AssertBuilderCollector assertBuilder = new AssertBuilderCollector();
        assertBuilder.add(fail());
        assertBuilder.runAssert();
    }

    @Test
    public void testReset() {
        final AssertBuilderCollector assertBuilder = new AssertBuilderCollector();
        assertBuilder.add(fail());
        assertBuilder.reset();
        assertBuilder.runAssert();
    }

    @Test
    public void testRunAndReset() {
        final AssertBuilderCollector assertBuilder = new AssertBuilderCollector();
        assertBuilder.add(fail());
        try {
            assertBuilder.runAndReset();
            throw new AssertionError("failed");
        } catch (final AssertionError e) {
            //pass
        }
        assertBuilder.runAssert();
    }

    /**
     * AssertBuilder that will always fail.
     *
     * @return an AssertBuilder that will fail.
     */
    private static AssertBuilder fail() {
        return new AssertBuilder() {
            @Override
            public void runAssert() {
                throw new AssertionError("failed");
            }
        };
    }

    /**
     * AssertBuilder that will always pass.
     *
     * @return an AssertBuilder that will pass.
     */
    private static AssertBuilder pass() {
        return new AssertBuilder() {
            @Override
            public void runAssert() {}
        };
    }


}