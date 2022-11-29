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
        final ReentrantLock reentrantLock = new ReentrantLock(true);
        final AtomicBoolean releaseLockCommandForThread = new AtomicBoolean( false );
        Thread firstThreadThatAcquiredTheLock = new Thread("FirstThread") {
            public void run(){
                try {
                    LockHelper.lock( reentrantLock );
                    System.out.println( Thread.currentThread().getName()+" acquired the lock");
                    while (  !releaseLockCommandForThread.get() ){
                        System.out.println( Thread.currentThread().getName()+" waiting unlock command");
                        LockHelper.sleepCurrThread( 10 );
                    }
                } finally {
                    LockHelper.unLock( reentrantLock );
                }
            }
        };
        firstThreadThatAcquiredTheLock.start();

        List<String> lockAcquiredThreads = Collections.synchronizedList( new ArrayList<>( ) );
        //--create a thread every 1 second
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
        //--allow first thread to release the lock
        releaseLockCommandForThread.set( true );

        while ( reentrantLock.isLocked() || lockAcquiredThreads.size()<2 ){
            System.out.println("waiting for the all thread to finish their work..." );
            LockHelper.sleepCurrThread(1000 );
        }
        assertEquals( "Thread0", lockAcquiredThreads.get( 0 ) );
    }

    @Test
    void givenALockObject_whenComparingLockAndTryLock_thenTryLockMethodShouldFasterThanLockMethod(){
        final ReentrantLock reentrantLock = new ReentrantLock(true);
        final AtomicBoolean releaseLockCommandForThread = new AtomicBoolean( false );

        //----------------calculate lock() duration-----------------------
        WaitAbleThread waitAbleThread = new WaitAbleThread(reentrantLock, "WaitAbleThread", releaseLockCommandForThread);
        waitAbleThread.start();

        MonitorAbleThreadUsesLockMethod firstThreadThatAcquiredTheLock = new MonitorAbleThreadUsesLockMethod(reentrantLock,"FirstThread") ;
        firstThreadThatAcquiredTheLock.start();

        //--wait for the current thread to be ready for the first thread to get the lock.
        while ( firstThreadThatAcquiredTheLock.getThreadStartToAcquireTheLockAt()==null ){
            LockHelper.sleepCurrThread( 10 );
        }

        //--allow first thread to get the lock
        releaseLockCommandForThread.set( true );

        //--suspend the current thread until the first thread gets the lock.
        while ( firstThreadThatAcquiredTheLock.getThreadAcquiredTheLockAt()==null ){
            LockHelper.sleepCurrThread( 10 );
        }
        //----------------calculate tryLock() duration-----------------------
        releaseLockCommandForThread.set( false );
        WaitAbleThread waitAbleThread2 = new WaitAbleThread(reentrantLock, "WaitAbleThread-2", releaseLockCommandForThread);
        waitAbleThread2.start();

        MonitorAbleThreadUsesTryLockMethod secondThreadThaTryToAcquiredTheLock = new MonitorAbleThreadUsesTryLockMethod(reentrantLock,"SecondThread") ;
        secondThreadThaTryToAcquiredTheLock.start();

        //--wait for the current thread to be ready for the second thread to get the lock.
        while ( secondThreadThaTryToAcquiredTheLock.getThreadStartToAcquireTheLockAt()==null ){
            LockHelper.sleepCurrThread( 10 );
        }

        //--allow second thread to get the lock
        releaseLockCommandForThread.set( true );

        //--suspend the current thread until the second thread gets the lock.
        while ( secondThreadThaTryToAcquiredTheLock.getThreadTriedToAcquireTheLockAt()==null ){
            LockHelper.sleepCurrThread( 10 );
        }

        assertTrue( firstThreadThatAcquiredTheLock.getAcquireLockDuration().toNanos()>  secondThreadThaTryToAcquiredTheLock.getTryAcquireLockDuration().toNanos() );

    }



}
