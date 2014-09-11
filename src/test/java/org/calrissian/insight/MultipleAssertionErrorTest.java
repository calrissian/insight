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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the org.calrissian.insight.MultipleAssertionError class
 */
public class MultipleAssertionErrorTest {

    @Test(expected = NullPointerException.class)
    public void testConstructorNullError() {
        new MultipleAssertionError(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyErrors() {
        new MultipleAssertionError((List) Collections.emptyList());
    }

    @Test(expected = NullPointerException.class)
    public void testConsturctorNullInErrors() {
        new MultipleAssertionError(Arrays.asList(null, new AssertionError()));
    }

    @Test
    public void testGetMessage() {
        assertEquals("The message was wrong", "Multiple assertion errors:\n  test1",
                new MultipleAssertionError(Arrays.asList(new AssertionError("test1"))).getMessage());
    }

    @Test
    public void testGetMessageWithName() {
        assertEquals("The message was wrong", "Name had multiple failures:\n  test1",
                new MultipleAssertionError("Name", Arrays.asList(new AssertionError("test1"))).getMessage());
    }
}