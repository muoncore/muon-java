/*
 * Copyright (c) 2011-2014 Pivotal Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.muoncore.transport.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.Environment;
import reactor.core.Dispatcher;
import reactor.core.dispatch.wait.AgileWaitingStrategy;
import reactor.core.dispatch.wait.WaitingMood;
import reactor.core.support.Assert;
import reactor.core.support.NamedDaemonThreadFactory;
import reactor.core.support.Recyclable;
import reactor.fn.Consumer;
import reactor.jarjar.com.lmax.disruptor.*;
import reactor.jarjar.com.lmax.disruptor.dsl.Disruptor;
import reactor.jarjar.com.lmax.disruptor.dsl.ProducerType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of a {@link reactor.core.Dispatcher} that uses a
 * {@link RingBuffer} to queue tasks to execute.
 *
 * @author Jon Brisbin
 * @author Stephane Maldini
 */
public final class RingBufferLocalDispatcher implements Dispatcher, WaitingMood {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final ExecutorService executor;
	private final Disruptor<RingBufferTask> disruptor;
	private final RingBuffer<RingBufferTask> ringBuffer;
	private final WaitingMood waitingMood;
	protected final List<Task> tailRecursionPile = new ArrayList<Task>();
	protected final int backlog;

	protected int tailRecurseSeq = -1;
	protected int tailRecursionPileSize = 0;

	/**
	 * Creates a new {@code RingBufferDispatcher} with the given {@code name}.
	 * It will use a RingBuffer with 1024 slots, configured with a producer type
	 * of {@link ProducerType#MULTI MULTI} and a {@link BlockingWaitStrategy
	 * blocking wait strategy}.
	 *
	 * @param name
	 *            The name of the dispatcher.
	 */
	public RingBufferLocalDispatcher(String name) {
		this(name, 32768, null, ProducerType.MULTI, new AgileWaitingStrategy());
		// this(name, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Creates a new {@code RingBufferDispatcher} with the given {@code name}
	 * and {@param bufferSize}, configured with a producer type of
	 * {@link ProducerType#MULTI MULTI} and a {@link BlockingWaitStrategy
	 * blocking wait strategy}.
	 *
	 * @param name
	 *            The name of the dispatcher
	 * @param bufferSize
	 *            The size to configure the ring buffer with
	 */
	public RingBufferLocalDispatcher(String name, int bufferSize) {
		this(name, bufferSize, null, ProducerType.MULTI, new AgileWaitingStrategy());
	}

	/**
	 * Creates a new {@literal RingBufferDispatcher} with the given {@code name}
	 * . It will use a {@link RingBuffer} with {@code bufferSize} slots,
	 * configured with a producer type of {@link ProducerType#MULTI MULTI} and a
	 * {@link BlockingWaitStrategy blocking wait. A given @param
	 * uncaughtExceptionHandler} will catch anything not handled e.g. by the
	 * owning {@code reactor.bus.EventBus} or {@code reactor.rx.Stream}.
	 *
	 * @param name
	 *            The name of the dispatcher
	 * @param bufferSize
	 *            The size to configure the ring buffer with
	 * @param uncaughtExceptionHandler
	 *            The last resort exception handler
	 */
	public RingBufferLocalDispatcher(String name, int bufferSize, final Consumer<Throwable> uncaughtExceptionHandler) {
		this(name, bufferSize, uncaughtExceptionHandler, ProducerType.MULTI, new BlockingWaitStrategy());

	}

	/**
	 * Creates a new {@literal RingBufferDispatcher} with the given {@code name}
	 * . It will use a {@link RingBuffer} with {@code bufferSize} slots,
	 * configured with the given {@code producerType},
	 * {@param uncaughtExceptionHandler} and {@code waitStrategy}. A null
	 * {@param uncaughtExceptionHandler} will make this dispatcher logging such
	 * exceptions.
	 *
	 * @param name
	 *            The name of the dispatcher
	 * @param bufferSize
	 *            The size to configure the ring buffer with
	 * @param producerType
	 *            The producer type to configure the ring buffer with
	 * @param waitStrategy
	 *            The wait strategy to configure the ring buffer with
	 * @param uncaughtExceptionHandler
	 *            The last resort exception handler
	 */
	@SuppressWarnings({ "unchecked" })
	public RingBufferLocalDispatcher(String name, int bufferSize, final Consumer<Throwable> uncaughtExceptionHandler,
			ProducerType producerType, WaitStrategy waitStrategy) {
		this.backlog = bufferSize;
		expandTailRecursionPile(backlog);

		if (WaitingMood.class.isAssignableFrom(waitStrategy.getClass())) {
			this.waitingMood = (WaitingMood) waitStrategy;
		} else {
			this.waitingMood = null;
		}

		this.executor = Executors.newSingleThreadExecutor(new NamedDaemonThreadFactory(name, getContext()));
		this.disruptor = new Disruptor<RingBufferTask>(new EventFactory<RingBufferTask>() {
			@Override
			public RingBufferTask newInstance() {
				return new RingBufferTask();
			}
		}, bufferSize, executor, producerType, waitStrategy);

		this.disruptor.handleExceptionsWith(new ExceptionHandler<Object>() {
			@Override
			public void handleEventException(Throwable ex, long sequence, Object event) {
				handleOnStartException(ex);
			}

			@Override
			public void handleOnStartException(Throwable ex) {
				if (null != uncaughtExceptionHandler) {
					uncaughtExceptionHandler.accept(ex);
				} else {
					log.error(ex.getMessage(), ex);
				}
			}

			@Override
			public void handleOnShutdownException(Throwable ex) {
				handleOnStartException(ex);
			}
		});
		this.disruptor.handleEventsWith(new EventHandler<RingBufferTask>() {
			@Override
			public void onEvent(RingBufferTask task, long sequence, boolean endOfBatch) throws Exception {
				task.run();
			}
		});

		this.ringBuffer = disruptor.start();
	}

	public boolean awaitAndShutdown(long timeout, TimeUnit timeUnit) {
		boolean alive = alive();
		shutdown();
		try {
			executor.awaitTermination(timeout, timeUnit);
			if (alive) {
				disruptor.shutdown();
			}
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}

	public void shutdown() {
		executor.shutdown();
		disruptor.shutdown();
		alive.compareAndSet(true, false);
	}

	public void forceShutdown() {
		executor.shutdownNow();
		disruptor.halt();
		alive.compareAndSet(true, false);
	}

	public long remainingSlots() {
		return ringBuffer.remainingCapacity();
	}

	@Override
	public void nervous() {
		if (waitingMood != null) {
			execute(new Runnable() {
				@Override
				public void run() {
					waitingMood.nervous();
				}
			});
		}
	}

	@Override
	public void calm() {
		if (waitingMood != null) {
			execute(new Runnable() {
				@Override
				public void run() {
					waitingMood.calm();
				}
			});

		}
	}

	protected Task tryAllocateTask() throws InsufficientCapacityException {
		try {
			long seqId = ringBuffer.tryNext();
			return ringBuffer.get(seqId).setSequenceId(seqId);
		} catch (reactor.jarjar.com.lmax.disruptor.InsufficientCapacityException e) {
			throw InsufficientCapacityException.INSTANCE;
		}
	}

	protected Task allocateTask() {
		long seqId = ringBuffer.next();
		return ringBuffer.get(seqId).setSequenceId(seqId);
	}

	protected void execute(Task task) {
		ringBuffer.publish(((RingBufferTask) task).getSequenceId());
	}

	private class RingBufferTask extends SingleThreadTask {
		private long sequenceId;

		public long getSequenceId() {
			return sequenceId;
		}

		public RingBufferTask setSequenceId(long sequenceId) {
			this.sequenceId = sequenceId;
			return this;
		}
	}

	public boolean supportsOrdering() {
		return true;
	}

	public long backlogSize() {
		return backlog;
	}

	public int getTailRecursionPileSize() {
		return tailRecursionPileSize;
	}

	protected void expandTailRecursionPile(int amount) {
		int toAdd = amount * 2;
		for (int i = 0; i < toAdd; i++) {
			tailRecursionPile.add(new SingleThreadTask());
		}
		this.tailRecursionPileSize += toAdd;
	}

	protected Task allocateRecursiveTask() {
		int next = ++tailRecurseSeq;
		if (next == tailRecursionPileSize) {
			expandTailRecursionPile(backlog);
		}
		Task elem = tailRecursionPile.get(next);
		return elem;
	}

	protected class SingleThreadTask extends Task {

		@Override
		public void run() {
			route(this);

			// Process any recursive tasks
			if (tailRecurseSeq < 0) {
				return;
			}
			int next = -1;
			while (next < tailRecurseSeq) {
				route(tailRecursionPile.get(++next));
			}

			// clean up extra tasks
			next = tailRecurseSeq;
			int max = backlog * 2;
			while (next >= max) {
				tailRecursionPile.remove(next--);
			}
			tailRecursionPileSize = max;
			tailRecurseSeq = -1;
		}
	}

	protected static final int DEFAULT_BUFFER_SIZE = 1024;

	private final AtomicBoolean alive = new AtomicBoolean(true);
	public final ClassLoader context = new ClassLoader(Thread.currentThread().getContextClassLoader()) {
	};

	public boolean alive() {
		return alive.get();
	}

	public boolean awaitAndShutdown() {
		return awaitAndShutdown(Integer.MAX_VALUE, TimeUnit.SECONDS);
	}

	/**
	 * Dispatchers can be traced through a {@code contextClassLoader} to let
	 * producers adapting their dispatching strategy
	 *
	 * @return boolean true if the programs is already run by this dispatcher
	 */
	public boolean inContext() {
		// FIX: Commenting this should fix the errors from muon-clojure
		// return context == Thread.currentThread().getContextClassLoader();
		return false;
	}

	protected final ClassLoader getContext() {
		return context;
	}

	public final <E> void tryDispatch(E event, Consumer<E> eventConsumer, Consumer<Throwable> errorConsumer) {
		Assert.isTrue(alive(), "This Dispatcher has been shut down.");
		boolean isInContext = inContext();
		Task task;
		if (isInContext) {
			task = allocateRecursiveTask();
		} else {
			try {
				task = tryAllocateTask();
			} catch (InsufficientCapacityException e) {
				throw new IllegalStateException("Error in task allocation");
			}
		}

		task.setData(event).setErrorConsumer(errorConsumer).setEventConsumer(eventConsumer);

		if (!isInContext) {
			execute(task);
		}
	}

	public final <E> void dispatch(E event, Consumer<E> eventConsumer, Consumer<Throwable> errorConsumer) {

		Assert.isTrue(alive(), "This Dispatcher has been shut down.");
		Assert.isTrue(eventConsumer != null, "The signal consumer has not been passed.");
		boolean isInContext = inContext();
		Task task;
		if (isInContext) {
			task = allocateRecursiveTask();
		} else {
			task = allocateTask();
		}

		task.setData(event).setErrorConsumer(errorConsumer).setEventConsumer(eventConsumer);

		if (!isInContext) {
			execute(task);
		}
	}

	public void execute(final Runnable command) {
		dispatch(null, new Consumer<Object>() {
			@Override
			public void accept(Object ev) {
				command.run();
			}
		}, null);
	}

	@SuppressWarnings("unchecked")
	protected static void route(Task task) {
		try {
			if (task.eventConsumer == null)
				return;

			task.eventConsumer.accept(task.data);

		} catch (Exception e) {
			if (task.errorConsumer != null) {

				task.errorConsumer.accept(e);

			} else if (Environment.alive()) {

				Environment.get().routeError(e);

			}
		} finally {
			task.recycle();
		}
	}

	public String toString() {
		return getClass().getSimpleName().replaceAll("Dispatcher", "");
	}

	public abstract class Task implements Runnable, Recyclable {
		protected volatile Object data;
		@SuppressWarnings("rawtypes")
		protected volatile Consumer eventConsumer;
		protected volatile Consumer<Throwable> errorConsumer;

		public Task setData(Object data) {
			this.data = data;
			return this;
		}

		public Task setEventConsumer(Consumer<?> eventConsumer) {
			this.eventConsumer = eventConsumer;
			return this;
		}

		public Task setErrorConsumer(Consumer<Throwable> errorConsumer) {
			this.errorConsumer = errorConsumer;
			return this;
		}

		@Override
		public void recycle() {
			data = null;
			errorConsumer = null;
			eventConsumer = null;
		}
	}
}
