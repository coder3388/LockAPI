package org.lockapi;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class WaitAbleThread extends Thread{
    private ReentrantLock reentrantLock;
    private AtomicBoolean releaseLockCommandForFirstThread;

    public WaitAbleThread( ReentrantLock reentrantLock, String name, AtomicBoolean releaseLockCommandForFirstThread ) {
        super();
        this.setName( name );
        this.reentrantLock = reentrantLock;
        this.releaseLockCommandForFirstThread = releaseLockCommandForFirstThread;
    }
    public void run(){
        try {
            LockHelper.lock( reentrantLock );
            System.out.println( Thread.currentThread().getName()+" acquired the lock");
            while (  !releaseLockCommandForFirstThread.get() ){
                System.out.println( Thread.currentThread().getName()+" waiting unlock command");
                LockHelper.sleepCurrThread( 1000 );
            }
        } finally {
            LockHelper.unLock( reentrantLock );
            System.out.println( Thread.currentThread().getName()+" released the lock");
        }
    }
}
