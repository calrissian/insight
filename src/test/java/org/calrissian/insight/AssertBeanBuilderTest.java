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

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class AssertBeanBuilderTest {

    /**
     * Mock instance uses in the test instance
     */
    private Callable callable = null;

    /**
     * Instance being tested
     */
    private AssertBeanBuilder builder = null;

    /**
     * Setup for the tests
     */
    @Before
    public void setUp() {
        callable = mock(Callable.class);
        builder = new AssertBeanBuilder(callable);
    }

    /**
     * Tear down for the eash test.
     */
    @After
    public void tearDown() {
        builder = null;
        callable = null;
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullCallable() {
        new AssertBeanBuilder("test", null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullName() {
        new AssertBeanBuilder(null, callable);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullCallableNoName() {
        new AssertBeanBuilder(null);
    }

    @Test(expected = NullPointerException.class)
    public void testThatNullProperty() {
        builder.that(null, Matchers.any(Object.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThatEmptyStringForProperty() {
        builder.that("", Matchers.any(Object.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThatInvalidCharacter() {
        builder.that("*", Matchers.any(Object.class));
    }

    @Test(expected = NullPointerException.class)
    public void testThat() {
        builder.that(null);
    }

    @Test
    public void testIsNull() throws Exception {
        when(callable.call()).thenReturn(null);
        builder.isNull();
        builder.runAssert();
    }

    @Test
    public void testNotNull() throws Exception {
        when(callable.call()).thenReturn("");
        builder.notNull();
        builder.runAssert();
    }

    @Test
    public void testIsNullFailure() throws Exception {
        when(callable.call()).thenReturn("");
        builder.isNull();
        try {
            builder.runAssert();
            fail();
        } catch (final AssertionError e) {
            // success
            Assert.assertEquals("java.lang.AssertionError: \n" +
                    "Expected: null\n" +
                    "     but: was \"\""
                    , e.toString());
        }
    }

    @Test(expected = AssertionError.class)
    public void testNotNullFailure() throws Exception {
        when(callable.call()).thenReturn(null);
        builder.notNull();
        builder.runAssert();
        fail();
    }

    @Test
    public void testAccessPropertyGetMethod() throws Exception {
        runAssertionTest(new Object() {
            public String getText() {
                return "worked";
            }
        }, "text");
    }

    @Test
    public void testAccessChildProperty() throws Exception {
        runAssertionTest(new Object() {
            public Object getThing() {
                return new Object() {
                    public String getText() {
                        return "worked";
                    }
                };
            }
        }, "thing.text");
    }

    @Test
    public void testAccessList() throws Exception {
        runAssertionTest(Arrays.asList("worked"), "[0]");
    }

    @Test
    public void testAccessChildList() throws Exception {
        runAssertionTest(new Object() {
            public Collection getText() {
                return Arrays.asList("test", "worked");
            }
        }, "text[1]");
    }

    @Test
    public void testAccessMap() throws Exception {
        runAssertionTest(Collections.singletonMap("test", "worked"), "[test]");
    }

    @Test
    public void testAccessMapWithANumber() throws Exception {
        runAssertionTest(Collections.singletonMap("1", "worked"), "[1]");
    }

    @Test
    public void testAccessArray() throws Exception {
        runAssertionTest(new String[]{"worked"}, "[0]");
    }

    @Test
    public void testAccessChildMap() throws Exception {
        runAssertionTest(new Object() {
                             public Map getText() {
                                 final Map<String, String> map = new HashMap<String, String>();
                                 map.put("test", "worked");
                                 return map;
                             }
                         },
                "text[test]"
        );
    }

    @Test
    public void testMultipleAssertFailures() throws Exception {
        runAssertionErrorTest(new Object() {
            public String getText() {
                return "notWork";
            }

            public String getOther() {
                return "notWork";
            }
        }, "Multiple assertion errors:\n  \n  Expected: text is \"worked\"\n       but: text was " +
                "\"notWork\"\n  \n  Expected: other is \"worked\"\n       but: other was \"notWork\"", "text", "other");
    }

    @Test
    public void testAccessPropertyGetMethodNotPresent() throws Exception {
        runAssertionErrorTest(new Object(), "Property (text) does not exist on root bean.", "text");
    }

    @Test
    public void testAccessPropertyGetMethodThrowsException() throws Exception {
        runAssertionErrorTest(new Object() {
                                  public String getText() {
                                      throw new RuntimeException("I threw an exception");
                                  }
                              },
                "Error accessing bean (text) reason: java.lang.RuntimeException: I threw an exception",
                "text"
        );
    }

    @Test
    public void testAccessChildPropertyDoesNotExist() throws Exception {
        runAssertionErrorTest(
                new Object() {
                    public Object getThing() {
                        return new Object();
                    }
                }, "Property (text) does not exist on bean thing.",
                "thing.text"
        );
    }

    @Test
    public void testAccessListWithString() throws Exception {
        runAssertionErrorTest(new Object() {
                                  public List<String> getTest() {
                                      return Arrays.asList("test");
                                  }
                              }, "what cannot index into bean (test[what]).  The index must be a number when " +
                        "accessing Lists or Arrays.",
                "test[what]"
        );
    }

    @Test
    public void testListButBeanNotList() throws Exception {
        runAssertionErrorTest(new Object() {
                                  public String getTest() {
                                      return "test";
                                  }
                              }, "test[what] is not a Map, List or Array but a java.lang.String",
                "test[what]"
        );
    }

    @Test
    public void testListButNull() throws Exception {
        runAssertionErrorTest(new Object() {
                                  public String getTest() {
                                      return null;
                                  }
                              }, "Cannot access into Map, List, or Array of test[what] because the bean is null.",
                "test[what]"
        );
    }

    /**
     * The test for a successful assertion test.  The beanString should access a bean that is a String "worked",
     * otherwise a failure will be thrown.
     *
     * @param returnObject the object that will be accessed for the beanString.
     * @param beanString   the bean strings that will access the bean that is being compared.
     * @throws Exception because of the callable.
     */
    private void runAssertionTest(final Object returnObject, final String... beanString) throws Exception {
        when(callable.call()).thenReturn(returnObject);
        for (final String bean : beanString) {
            builder.that(bean, Matchers.is("worked"));
        }
        builder.runAssert();
    }

    /**
     * The test for a failed assertion test.  The beanString should not access a bean that is a String "worked",
     * otherwise a failure will be thrown.
     *
     * @param returnObject         the object that will be accessed for the beanString.
     * @param expectedErrorMessage the expected message in the AssertionError.
     * @param beanString           the bean strings that will access the bean that is being compared.
     * @throws Exception because of the callable.
     */
    private void runAssertionErrorTest(final Object returnObject,
                                       final String expectedErrorMessage,
                                       final String... beanString) throws Exception {
        try {
            runAssertionTest(returnObject, beanString);
            fail("No assertion error");
        } catch (final AssertionError e) {
            e.printStackTrace();
            assertThat("Assertion Message was wrong", e.getMessage(), Matchers.is(expectedErrorMessage));
        }
    }


}