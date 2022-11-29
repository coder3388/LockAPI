package org.lockapi;

import java.util.concurrent.locks.ReentrantLock;

public abstract class LockHelper {
    public static void lock( ReentrantLock lock ){
        lock.lock();
    }

    public static void tryLock( ReentrantLock lock ){
        lock.tryLock();
    }

    public static void unLock( ReentrantLock lock ){
        lock.unlock();
    }

    public static void sleepCurrThread( long milis) {
        try {
            Thread.sleep( milis );
        } catch ( InterruptedException e ) {
            throw new RuntimeException( e );
        }
    }
}
