package org.lockapi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest {

    @Test
    void givenALockObject_whenLockAndUnlockTheLockObject_thenLockAndUnlockOperationsCanBePerformedAtDifferentMethods(){
        ReentrantLock reentrantLock = new ReentrantLock();
        LockHelper.lock( reentrantLock );
        try{
            assertTrue(reentrantLock.isLocked());
        }finally {
            LockHelper.unLock( reentrantLock );
            assertFalse(reentrantLock.isLocked());
        }
    }

    @Test
    void given3ThreadAndFairnessLockObject_whenThreadsTryToLockTheLockObjectRespectively_thenLongestWaitingThreadShouldBeAcquiredTheLock(){
        ReentrantLock reentrantLock = new ReentrantLock(true);
        Thread firstThreadThatAcquiredTheLock = new Thread("FirstThread") {
            public void run(){
                try {
                    LockHelper.lock( reentrantLock );
                    System.out.println( Thread.currentThread().getName()+" acquired the lock");
                    Thread.sleep( 10*1000 );
                } catch ( InterruptedException e ) {
                    throw new RuntimeException( e );
                }finally {
                    LockHelper.unLock( reentrantLock );
                }
            }
        };
        firstThreadThatAcquiredTheLock.start();

        List<String> lockAcquiredThreads = Collections.synchronizedList( new ArrayList<>( ) );
        for ( int i = 0; i <2; i++ ) {
            Thread thread = new Thread("Thread"+i) {
                public void run(){
                    try {
                        LockHelper.lock( reentrantLock );
                        System.out.println( Thread.currentThread().getName()+" acquired the lock");
                        lockAcquiredThreads.add( Thread.currentThread().getName() );
                    }finally {
                        LockHelper.unLock( reentrantLock );
                    }
                }
            };
            thread.start();

            //--current thread going to sleep as much as 1 second.
            LockHelper.sleepCurrThread( 1000);
        }
        while ( reentrantLock.isLocked() || lockAcquiredThreads.size()<2 ){
            System.out.println("waiting for the all thread to finish their work..." );
            LockHelper.sleepCurrThread(1000 );
        }
        assertEquals( "Thread0", lockAcquiredThreads.get( 0 ) );
    }

    @Test
    void givenALockObject_whenComparingLockAndTryLock_thenTryLockMethodShouldFasterThanLockMethod(){
        final ReentrantLock reentrantLock = new ReentrantLock(true);
        final AtomicBoolean releaseLockCommandForFirstThread = new AtomicBoolean( false );
        WaitAbleThread waitAbleThread = new WaitAbleThread(reentrantLock, "WaitAbleThread", releaseLockCommandForFirstThread);
        waitAbleThread.start();

        MonitorAbleThreadUsesLockMethod secondThreadThatAcquiredTheLock = new MonitorAbleThreadUsesLockMethod(reentrantLock,"SecondThread") ;
        secondThreadThatAcquiredTheLock.start();

        while ( secondThreadThatAcquiredTheLock.getThreadStartToAcquireTheLockAt()==null ){
            LockHelper.sleepCurrThread( 1000 );
        }

        releaseLockCommandForFirstThread.set( true );

        while ( secondThreadThatAcquiredTheLock.getThreadAcquiredTheLockAt()==null ){
            LockHelper.sleepCurrThread( 1000 );
        }
        //--------------------------------------------------
        releaseLockCommandForFirstThread.set( false );
        WaitAbleThread waitAbleThread2 = new WaitAbleThread(reentrantLock, "WaitAbleThread-2", releaseLockCommandForFirstThread);
        waitAbleThread2.start();

        MonitorAbleThreadUsesTryLockMethod thirdThreadThaTryToAcquiredTheLock = new MonitorAbleThreadUsesTryLockMethod(reentrantLock,"ThirdThread") ;
        thirdThreadThaTryToAcquiredTheLock.start();

        while ( thirdThreadThaTryToAcquiredTheLock.getThreadStartToAcquireTheLockAt()==null ){
            LockHelper.sleepCurrThread( 1000 );
        }

        releaseLockCommandForFirstThread.set( true );

        while ( thirdThreadThaTryToAcquiredTheLock.getThreadTriedToAcquireTheLockAt()==null ){
            LockHelper.sleepCurrThread( 1000 );
        }

        assertTrue( secondThreadThatAcquiredTheLock.getAcquireLockDuration().toNanos()>  thirdThreadThaTryToAcquiredTheLock.getTryAcquireLockDuration().toNanos() );

    }



}
