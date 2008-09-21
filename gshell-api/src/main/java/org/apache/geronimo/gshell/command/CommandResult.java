/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.gshell.command;

import org.apache.geronimo.gshell.notification.Notification;
import org.apache.geronimo.gshell.yarn.ReflectionToStringBuilder;
import org.apache.geronimo.gshell.yarn.ToStringStyle;

/**
 * Provides the result of a command execution.
 *
 * @version $Rev$ $Date$
 */
public class CommandResult
{
    private final Object value;

    private final Throwable failure;

    private final Notification notification;

    private CommandResult(final Object value, final Throwable failure, final Notification notification) {
        this.value = value;
        this.failure = failure;
        this.notification = notification;
    }

    /**
     * The result value of a command execution.
     *
     * @return  Command execution result value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * True if the command failed with an exception.
     *
     * @return  True if the command failed with an exception.
     *
     * @see #getFailure
     */
    public boolean hasFailed() {
        return failure != null;
    }

    /**
     * Returns the command failure cause.
     *
     * @return  The command failure cause; or null if there was no failure.
     */
    public Throwable getFailure() {
        return failure;
    }

    /**
     * True if the command exited with a notification.
     *
     * @return  True if the command exited with a notification.
     *
     * @see #getNotification
     */
    public boolean hasNotified() {
        return notification != null;
    }

    /**
     * Returns the command notification.
     *
     * @return  The command notification; or null if there was no notification.
     */
    public Notification getNotification() {
        return notification;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    /**
     * Command execution resulting in a normal value.
     */
    public static class ValueResult
        extends CommandResult
    {
        public ValueResult(final Object value) {
            super(value, null, null);
        }
    }

    /**
     * Command execution resulting in a failure.
     */
    public static class FailureResult
        extends CommandResult
    {
        public FailureResult(final Throwable failure) {
            super(null, failure, null);
        }
    }

    /**
     * Command execution resulting in a notification.
     */
    public static class NotificationResult
        extends CommandResult
    {
        public NotificationResult(final Notification notification) {
            super(null, null, notification);
        }
    }
}