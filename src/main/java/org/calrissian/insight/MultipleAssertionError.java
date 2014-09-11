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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An AssertionError that holds multiple errors.
 */
public class MultipleAssertionError extends AssertionError {

    /**
     * The assertion errors
     */
    private final List<AssertionError> errors;

    /**
     * The description of all the errors.
     */
    private final String name;

    /**
     * Constructor
     *
     * @param errors
     * @throws java.lang.NullPointerException     if the errors is null or there is a null in the errors list.
     * @throws java.lang.IllegalArgumentException if the errors is empty.
     */
    public MultipleAssertionError(final List<AssertionError> errors) {
        this(null, errors);
    }

    /**
     * Constructor
     *
     * @param name   to describe the errors.  If null then this is the same as not having a name.
     * @param errors
     * @throws java.lang.NullPointerException     if the errors is null or there is a null in the errors list.
     * @throws java.lang.IllegalArgumentException if the errors is empty.
     */
    public MultipleAssertionError(final String name, final List<AssertionError> errors) {
        if (errors == null) {
            throw new NullPointerException("Errors must not be null");
        }
        if (errors.isEmpty()) {
            throw new IllegalArgumentException("Cannot have a empty errors list.");
        }
        for (final AssertionError error : errors) {
            if (error == null) {
                throw new NullPointerException("An AssertionError in the errors list is null.");
            }
        }
        this.errors = Collections.unmodifiableList(new ArrayList(errors));
        this.name = name;
    }

    public List getErrors() {
        return errors;
    }

    @Override
    public String getMessage() {
        final StringBuilder builder = new StringBuilder(1024);
        if (name == null) {
            builder.append("Multiple assertion errors:");
        } else {
            builder.append(name).append(" had multiple failures:");
        }
        final String lineSeparator = System.getProperty("line.separator");
        builder.append(lineSeparator);
        for (final AssertionError error : errors) {
            // indent
            builder.append("  " + error.getMessage().replace(lineSeparator, lineSeparator + "  "))
                    .append(lineSeparator);
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
