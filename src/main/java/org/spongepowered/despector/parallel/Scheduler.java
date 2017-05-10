/*
 * The MIT License (MIT)
 *
 * Copyright (c) Despector <https://despector.voxelgenesis.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.despector.parallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Scheduler<T extends Runnable> {

    private final Worker<T>[] workers;
    private final List<T> tasks = new ArrayList<>();

    private ReentrantLock lock = new ReentrantLock();
    private Condition finished = this.lock.newCondition();
    private int finished_count;

    @SuppressWarnings("unchecked")
    public Scheduler(int workers) {
        if (workers <= 0) {
            workers = 1;
        }
        this.workers = new Worker[workers];
        for (int i = 0; i < workers; i++) {
            this.workers[i] = new Worker<>();
        }
    }

    public void add(T task) {
        this.tasks.add(task);
    }

    public List<T> getTasks() {
        return this.tasks;
    }

    public void execute() {
        int i = 0;
        for (T task : this.tasks) {
            this.workers[i % this.workers.length].add(task);
            i++;
        }
        this.finished_count = 0;
        for (Worker<T> worker : this.workers) {
            worker.start();
        }
        try {
            this.lock.lock();
            this.finished.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.lock.unlock();
        }
    }

    public void reset() {
        this.tasks.clear();
        for (Worker<T> worker : this.workers) {
            worker.reset();
        }
    }

    void markWorkerDone() {
        try {
            this.lock.lock();
            this.finished_count++;
            if (this.finished_count == this.workers.length) {
                this.finished.signalAll();
            }
        } finally {
            this.lock.unlock();
        }
    }

    private class Worker<R> extends Thread {

        private final List<T> tasks = new ArrayList<>();

        public void reset() {
            this.tasks.clear();
        }

        public void add(T task) {
            this.tasks.add(task);
        }

        @Override
        public void run() {
            for (T task : this.tasks) {
                task.run();
            }
            Scheduler.this.markWorkerDone();
        }

    }

}
