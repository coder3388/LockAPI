package org.lockapi;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorAbleThreadUsesLockMethod extends Thread{
    private LocalDateTime threadStartToAcquireTheLockAt;
    private LocalDateTime threadAcquiredTheLockAt;
    private ReentrantLock reentrantLock;

    public MonitorAbleThreadUsesLockMethod( ReentrantLock reentrantLock, String name ) {
        super();
        this.setName( name );
        this.reentrantLock = reentrantLock;
    }

    public void run(){
        try {
            threadStartToAcquireTheLockAt =LocalDateTime.now( );
            LockHelper.lock( reentrantLock );
            System.out.println( Thread.currentThread().getName()+" acquired the lock");
            threadAcquiredTheLockAt =LocalDateTime.now( );
        } finally {
            LockHelper.unLock( reentrantLock );
            System.out.println( Thread.currentThread().getName()+" released the lock");
        }
    }
    public LocalDateTime getThreadAcquiredTheLockAt( ) {
        return threadAcquiredTheLockAt;
    }

    public LocalDateTime getThreadStartToAcquireTheLockAt( ) {
        return threadStartToAcquireTheLockAt;
    }

    public Duration getAcquireLockDuration(){
        return Duration.between( threadStartToAcquireTheLockAt, threadAcquiredTheLockAt );
    }

}
