package com.log4ic.utils.thread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-8-22 上午10:27
 */
public class ThreadPool extends ThreadGroup {

    private static Log LOGGER = LogFactory.getLog(ThreadPool.class);

    protected boolean _isShutdown = false;  //线程池是否关闭

    protected LinkedList<IWorker> workQueue = new LinkedList<IWorker>();      //工作队列

    protected LinkedList<IWorker> workingQueue = new LinkedList<IWorker>();      //工作队列

    protected static int threadPoolId = 0;  //线程池的id

    protected LinkedList<WorkThread> runningThread = new LinkedList<WorkThread>();

    protected int maxCount = 5;

    public ThreadPool(int maxCount) {  //poolSize 表示线程池中的工作线程的数量
        super((threadPoolId++) + "");      //指定ThreadGroup的名称
        this.setDaemon(true);               //继承到的方法，设置是否守护线程池
        this.maxCount = maxCount;
    }

    /**
     * 向工作队列中加入一个新任务,由工作线程去执行该任务
     */
    public synchronized void addWorker(IWorker worker) {
        if (this._isShutdown) {
            throw new IllegalStateException();
        }
        if (worker != null) {
            this.workQueue.add(worker);//向队列中加入一个任务
            if (this.runningThread.size() < this.maxCount) {
                if (this.runningThread.size() == 1 && runningThread.get(0).getState().equals(Thread.State.WAITING)) {
                    WorkThread workThread = runningThread.get(0);
                    synchronized (workThread) {
                        workThread.notify();
                    }
                } else {
                    this.createRunningWorkThread();
                }
            }
        }
    }

    public synchronized boolean isRunning(IWorker worker) {
        return workingQueue.indexOf(worker) != -1;
    }

    public synchronized boolean isWaiting(IWorker worker) {
        return workQueue.indexOf(worker) != -1;
    }


    public synchronized boolean isRunning(int id) {
        for (IWorker worker : this.workingQueue) {
            if (worker.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isWaiting(int id) {
        for (IWorker worker : this.workQueue) {
            if (worker.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public synchronized IWorker getRunningWorker(int id) {
        for (IWorker worker : this.workingQueue) {
            if (worker.getId() == id) {
                return worker;
            }
        }
        return null;
    }

    public synchronized IWorker getWaitingWorker(int id) {
        for (IWorker worker : this.workQueue) {
            if (worker.getId() == id) {
                return worker;
            }
        }
        return null;
    }

    /**
     * 获取IWorker对象
     *
     * @param id
     * @return
     */
    public synchronized IWorker getWorker(int id) {
        for (IWorker worker : this.workQueue) {
            if (worker.getId() == id) {
                return worker;
            }
        }
        for (IWorker worker : this.workingQueue) {
            if (worker.getId() == id) {
                return worker;
            }
        }
        return null;
    }

    public synchronized int indexOfWaitingQueue(IWorker worker) {
        return this.workQueue.indexOf(worker);
    }

    public synchronized int indexOfRunningQueue(IWorker worker) {
        return this.workingQueue.indexOf(worker);
    }

    public synchronized IWorker removeWaitingWorker(int index) {
        return this.workQueue.remove(index);
    }

    public synchronized boolean removeWaitingWorker(IWorker worker) {
        return this.workQueue.remove(worker);
    }


    public synchronized int getMaxThreadCount() {
        return this.maxCount;
    }

    public synchronized void setMaxThreadCount(int maxCount) {
        this.maxCount = maxCount;
        int waitCount = this.getWaitWorkerCount();
        int runningThreadCount = this.runningThread.size();
        if (runningThreadCount < this.maxCount && waitCount != 0) {
            int newCount = this.maxCount - runningThreadCount;
            if (newCount > waitCount) {
                newCount = waitCount;
            }
            for (int i = 0; i < newCount; i++) {
                this.createRunningWorkThread();
                waitCount++;
            }
        }
    }

    private synchronized WorkThread createRunningWorkThread() {
        WorkThread workThread = new WorkThread(this.getRunningThreadCount());
        this.runningThread.add(workThread);
        workThread.start();//创建并启动工作线程
        return workThread;
    }

    /**
     * 从工作队列中取出一个任务,工作线程会调用此方法
     */
    protected synchronized IWorker nextWorker() {
        if (this.getWaitWorkerCount() == 0) {
            return null;
        }
        IWorker worker = workQueue.removeFirst(); //反回队列中第一个元素,并从队列中删除
        workingQueue.add(worker); //添加至正在运行的列队
        return worker;
    }

    public synchronized boolean isShutdown() {
        return this._isShutdown && workQueue.size() == 0;
    }

    public synchronized boolean isShuttingDown() {
        return this._isShutdown;
    }

    public synchronized int getWaitWorkerCount() {
        return this.workQueue.size();
    }

    public synchronized int getWorkerCount() {
        return this.workQueue.size() + this.workingQueue.size();
    }

    public synchronized int getRunningThreadCount() {
        return this.runningThread.size();
    }

    public synchronized void cleanup()
            throws InterruptedException {
        this.workQueue.clear();

        if (!isShutdown()) {
            safeShutdown();
            waitFinish();
        }
        this._isShutdown = false;
    }

    /**
     * 强制关闭池
     */
    public synchronized void shutdown() {
        this._isShutdown = true;
        this.interrupt();
        this.workQueue.clear();
    }

    /**
     * 安全关闭池
     */
    public synchronized void safeShutdown() {
        this._isShutdown = true;
    }

    /**
     * 等待工作线程把所有任务执行完毕
     */
    public synchronized void waitFinish() throws InterruptedException {
        if (!this._isShutdown) {
            throw new IllegalStateException();
        }
        while (this.getWorkerCount() > 0) {
            this.wait();
        }
        this.workingQueue.clear();
    }


    /**
     * @author: 张立鑫
     * @version: 1
     * @date: 11-8-22 上午10:27
     */
    protected class WorkThread extends Thread {
        private int id;

        public WorkThread(int id) {
            //父类构造方法,将线程加入到当前ThreadPool线程组中
            super(ThreadPool.this, id + "");
            this.id = id;
        }

        public void run() {
            while (!isInterrupted()) {  //isInterrupted()方法继承自Thread类，判断线程是否被中断
                IWorker worker = nextWorker();     //取出任务
                //如果getTask()返回null或者线程执行getTask()时被中断，则结束此线程
                if (worker == null) {
                    if (runningThread.size() > 1) {
                        LOGGER.debug("Not have worker,tread will interrupt!");
                        this.interrupt();
                        runningThread.remove(this);
                        LOGGER.debug("Tread interrupt!");
                        return;
                    } else {
                        try {
                            synchronized (this) {
                                LOGGER.debug("last tread , waiting...");
                                this.wait();
                                LOGGER.debug("last tread , starting...");
                                continue;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    worker.run();  //运行任务
                    workingQueue.remove(worker);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }
}
