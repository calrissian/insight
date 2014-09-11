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

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * An abstract class that is the template for AssertBuilders.
 */
public abstract class AbstractMatcherAssertBuilder<T> implements AssertBuilder {

    /**
     * The instance that will retrieve the object on demand when the time is ready to do the assertions.
     */
    private final Callable<?> callable;

    /**
     * An optional name to the object that is retrieved to identify the assertion errors over other AssertBuilders.
     */
    private final String name;

    /**
     * Holds all the matchers to apply to the Object.  This collection has order and it is important to keep the order
     * how the user has added the matchers.
     */
    private final List<Matcher<?>> matchers = new LinkedList<Matcher<?>>();

    /**
     * Constructor without a name to identify the org.calrissian.insight.AssertBuilder
     *
     * @param callable to get the object to run the matchers.
     */
    protected AbstractMatcherAssertBuilder(final Callable<T> callable) {
        if (callable == null) {
            throw new NullPointerException("Callable must not be null.");
        }
        this.callable = callable;
        name = null;
    }

    /**
     * Constructor
     *
     * @param callable to get the object to run the matchers.
     * @param name     of the org.calrissian.insight.AssertBuilder to identify this org.calrissian.insight
     *                 .AssertBuilder from others.
     */
    protected AbstractMatcherAssertBuilder(final String name, final Callable<T> callable) {
        if (callable == null) {
            throw new NullPointerException("Callable must not be null.");
        }
        if (name == null) {
            throw new NullPointerException("Name cannot be null.");
        }
        this.callable = callable;
        this.name = name;
    }

    /**
     * Adds a matcher to the list of matchers that will be ran on the <code>org.calrissian.insight
     * .AssertBuilder#runAssert</code>
     *
     * @param matcher to add
     */
    protected void addMatcher(final Matcher<?> matcher) {
        if (matcher == null) {
            throw new NullPointerException("Matcher must not be null.");
        }
        matchers.add(matcher);
    }

    /**
     * Adds a check that the object during assertion is null.
     */
    protected void addNullCheck() {
        addMatcher(Matchers.nullValue());
    }

    /**
     * Adds a check that the object during assertion is not null.
     */
    protected void addNotNullCheck() {
        addMatcher(Matchers.notNullValue());
    }

    public void runAssert() {
        // need to get the object we will run the assertions on.
        final Object object;
        try {
            object = callable.call();
        } catch (final Exception e) {
            final String message = (name == null) ? "Could not retrieve object." :
                    "Could not retrieve object (" + name + ").";
            final AssertionError a = new AssertionError(message);
            a.initCause(e);
            throw a;
        }

        // run through all the assertions.
        final List<AssertionError> failures = new LinkedList<AssertionError>();
        for (final Matcher entry : matchers) {
            try {
                MatcherAssert.assertThat(object, entry);
            } catch (final AssertionError e) {
                // collect all the failures
                failures.add(e);
            }
        }

        // if we have no failures we have succeeded in our assertions
        if (failures.isEmpty()) {
            return;
        }
        if (failures.size() == 1) {
            final AssertionError assertionError = failures.get(0);
            if (name == null) {
                throw assertionError;
            }

            final AssertionError a = new AssertionError(name + " failed because: " + assertionError.getMessage());
            a.initCause(assertionError);
            throw a;
        }
        // throw the combined error
        throw new MultipleAssertionError(name, failures);
    }
}
