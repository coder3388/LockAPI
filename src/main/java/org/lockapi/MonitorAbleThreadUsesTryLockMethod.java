package org.lockapi;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorAbleThreadUsesTryLockMethod extends Thread{
    private LocalDateTime threadStartToAcquireTheLockAt;
    private LocalDateTime threadTriedToAcquireTheLockAt;
    private ReentrantLock reentrantLock;

    public MonitorAbleThreadUsesTryLockMethod( ReentrantLock reentrantLock, String name ) {
        super();
        this.setName( name );
        this.reentrantLock = reentrantLock;
    }

    public void run(){
        try {
            threadStartToAcquireTheLockAt =LocalDateTime.now( );
            LockHelper.tryLock( reentrantLock );
            System.out.println( Thread.currentThread().getName()+" acquired the lock");
            threadTriedToAcquireTheLockAt =LocalDateTime.now( );
        } finally {
            if ( reentrantLock.isHeldByCurrentThread() ){
                LockHelper.unLock( reentrantLock );
            }
            System.out.println( Thread.currentThread().getName()+" released the lock");
        }
    }

    public LocalDateTime getThreadStartToAcquireTheLockAt( ) {
        return threadStartToAcquireTheLockAt;
    }

    public LocalDateTime getThreadTriedToAcquireTheLockAt( ) {
        return threadTriedToAcquireTheLockAt;
    }

    public Duration getTryAcquireLockDuration(){
        return Duration.between( threadStartToAcquireTheLockAt, threadTriedToAcquireTheLockAt );
    }
}
