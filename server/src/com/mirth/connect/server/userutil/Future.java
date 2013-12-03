/*
 * Copyright (c) Mirth Corporation. All rights reserved. http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A {@code Future} represents the result of an asynchronous computation. Methods are provided to
 * check if the computation is complete, to wait for its completion, and to retrieve the result of
 * the computation. The result can only be retrieved using method {@code get} when the computation
 * has completed, blocking if necessary until it is ready. Cancellation is performed by the
 * {@code cancel} method. Additional methods are provided to determine if the task completed
 * normally or was cancelled. Once a computation has completed, the computation cannot be cancelled.
 * If you would like to use a {@code Future} for the sake of cancellability but not provide a usable
 * result, you can declare types of the form {@code Future<?>} and return {@code null} as a result
 * of the underlying task.
 */
public class Future<V> implements java.util.concurrent.Future<V> {

    private java.util.concurrent.Future<V> delegate;

    Future(java.util.concurrent.Future<V> delegate) {
        this.delegate = delegate;
    }

    /**
     * Attempts to cancel execution of this task. This attempt will fail if the task has already
     * completed, has already been cancelled, or could not be cancelled for some other reason. If
     * successful, and this task has not started when {@code cancel} is called, this task should
     * never run. If the task has already started, then the {@code mayInterruptIfRunning} parameter
     * determines whether the thread executing this task should be interrupted in an attempt to stop
     * the task.
     * 
     * After this method returns, subsequent calls to {@link #isDone} will always return
     * {@code true}. Subsequent calls to {@link #isCancelled} will always return {@code true} if
     * this method returned {@code true}.
     * 
     * @param mayInterruptIfRunning
     *            {@code true} if the thread executing this task should be interrupted; otherwise,
     *            in-progress tasks are allowed to complete
     * @return {@code false} if the task could not be cancelled, typically because it has already
     *         completed normally; {@code true} otherwise
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return delegate.cancel(mayInterruptIfRunning);
    }

    /**
     * Returns {@code true} if this task was cancelled before it completed normally.
     * 
     * @return {@code true} if this task was cancelled before it completed
     */
    @Override
    public boolean isCancelled() {
        return delegate.isCancelled();
    }

    /**
     * Returns {@code true} if this task completed.
     * 
     * Completion may be due to normal termination, an exception, or cancellation -- in all of these
     * cases, this method will return {@code true}.
     * 
     * @return {@code true} if this task completed
     */
    @Override
    public boolean isDone() {
        return delegate.isDone();
    }

    /**
     * Waits if necessary for the computation to complete, and then retrieves its result.
     * 
     * @return the computed result
     * @throws CancellationException
     *             if the computation was cancelled
     * @throws ExecutionException
     *             if the computation threw an exception
     * @throws InterruptedException
     *             if the current thread was interrupted while waiting
     */
    @Override
    public V get() throws InterruptedException, ExecutionException {
        return delegate.get();
    }

    /**
     * Waits if necessary for at most the given time for the computation to complete, and then
     * retrieves its result, if available.
     * 
     * @param timeoutInMillis
     *            the maximum time to wait, in milliseconds
     * @return the computed result
     * @throws CancellationException
     *             if the computation was cancelled
     * @throws ExecutionException
     *             if the computation threw an exception
     * @throws InterruptedException
     *             if the current thread was interrupted while waiting
     * @throws TimeoutException
     *             if the wait timed out
     * @see #get(long, TimeUnit)
     */
    public V get(long timeoutInMillis) throws InterruptedException, ExecutionException, TimeoutException {
        return get(timeoutInMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Waits if necessary for at most the given time for the computation to complete, and then
     * retrieves its result, if available.
     * 
     * @param timeout
     *            the maximum time to wait
     * @param unit
     *            the time unit of the timeout argument
     * @return the computed result
     * @throws CancellationException
     *             if the computation was cancelled
     * @throws ExecutionException
     *             if the computation threw an exception
     * @throws InterruptedException
     *             if the current thread was interrupted while waiting
     * @throws TimeoutException
     *             if the wait timed out
     */
    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.get(timeout, unit);
    }
}
