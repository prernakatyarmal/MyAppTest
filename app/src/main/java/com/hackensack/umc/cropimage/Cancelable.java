

package com.hackensack.umc.cropimage;

import java.util.concurrent.ExecutionException;

/**
 * The interface for all the tasks that could be canceled.
 */
public interface Cancelable<T> {
	/*
	 * Requests this <code>Cancelable</code> to be canceled. This function will
	 * return <code>true</code> if and only if the task is originally running
	 * and now begin requested for cancel.
	 * 
	 * If subclass need to do more things to cancel the task. It can override
	 * the code like this: <pre>
	 * 
	 * @Override public boolean requestCancel() { if (super.requestCancel()) {
	 * // do necessary work to cancel the task return true; } return false; }
	 * </pre>
	 */
	public boolean requestCancel();

	public void await() throws InterruptedException;

	/**
	 * Gets the results of this <code>Cancelable</code> task.
	 * 
	 * @throws ExecutionException
	 *             if exception is thrown during the execution of the task
	 */
	public T get() throws InterruptedException, ExecutionException;
}