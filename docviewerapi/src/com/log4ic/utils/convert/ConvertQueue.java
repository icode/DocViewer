package com.log4ic.utils.convert;

import com.log4ic.utils.thread.IWorker;
import com.log4ic.utils.thread.ThreadPool;

import java.io.*;
import java.util.LinkedList;

/**
 * @author: 张立鑫
 * @version: 1
 * @date: 11-8-22 上午11:11
 */
public class ConvertQueue extends ThreadPool {
    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
        fillQueue();
    }

    private String poolName;

    private void fillQueue() {
        LinkedList<IWorker> list = deserializePool(this.poolName);
        if (list != null && list.size() > 0) {
            for (IWorker worker : list) {
                this.addWorker(worker);
            }
        }
    }

    public ConvertQueue(int maxCount) {
        super(maxCount);
    }

    public ConvertQueue(int maxCount, String poolName) {
        super(maxCount);
        this.poolName = poolName;
        fillQueue();
    }

    @Override
    protected IWorker nextWorker() {
        IWorker worker = super.nextWorker();
        serializePool(this.poolName, this.workQueue);
        return worker;
    }

    private synchronized static final LinkedList<IWorker> deserializePool(String name) {
        FileInputStream fis = null;
        ObjectInputStream oin = null;
        try {
            fis = new FileInputStream(name);

            oin = new ObjectInputStream(fis);

            return (LinkedList<IWorker>) oin.readObject();
        } catch (Exception e) {
            //e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private synchronized static final void serializePool(String name, LinkedList list) {

        if (name == null || list == null) {
            return;
        }
        synchronized (list) {
            FileOutputStream fos = null;
            ObjectOutputStream oos = null;
            try {
                fos = new FileOutputStream(name);
                if (fos == null) {
                    return;
                }
                oos = new ObjectOutputStream(fos);
                if (oos == null) {
                    return;
                }
                oos.writeObject(list);
            } catch (Exception e) {
                //e.printStackTrace();
            } finally {
                try {
                    if (oos != null)
                        oos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (oos != null)
                        oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (fos != null)
                        fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (fos != null)
                        fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
