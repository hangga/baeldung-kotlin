package com.baeldung.threadsafe

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Account(val name: String, var balance: Int) {

    private fun deposit(amount: Int) {
        balance += amount
    }

    private fun withdraw(amount: Int) {
        balance -= amount
    }

    fun transfer(to: Account, amount: Int) {
        println("${this.name} tries to transfer $amount to ${to.name}.")
        synchronized(this) {
            Thread.sleep(10) // Simulate processing time
            if (balance >= amount) {
                withdraw(amount)
                synchronized(to) {
                    to.deposit(amount)
                }
            }
        }
    }
}

class ThreadSafeUnitTest {

    private val logger = LoggerFactory.getLogger("")

    @Test
    fun `example of deadlock`() {
        val account1 = Account("Hangga", 1000)
        val account2 = Account("John", 1000)
        val account3 = Account("Alice", 2000)

        // Transfer from account1 to account2
        thread {
            account1.transfer(account2, 100)
        }.join(10) // as a simulation of the time required

        // Transfer from account2 to account1
        thread {
            account2.transfer(account1, 200)
        }.join(20)

        // Transfer from account3 to account1
        thread {
            account3.transfer(account1, 1000)
        }.join(100)

        logger.info("${account1.name}'s actual balance is: ${account1.balance}, expected: 2100")
        logger.info("${account2.name}'s actual balance is: ${account2.balance}, expected: 900")
        logger.info("${account3.name}'s actual balance is: ${account3.balance}, expected: 1000")
    }

    @Test
    fun `test using mutex to prevent deadlock free`() = runBlocking {
        val account1 = Account("Hangga", 1000)
        val account2 = Account("John", 1000)
        val account3 = Account("Alice", 2000)

        val mutex = Mutex()

        // Transfer from account1 to account2
        mutex.withLock {
            account1.transfer(account2, 100)
            Thread.sleep(10) // as a simulation of the time required
        }

        // Transfer from account2 to account1
        mutex.withLock {
            account2.transfer(account1, 200)
            Thread.sleep(20)
        }

        // Transfer from account3 to account1
        mutex.withLock {
            account3.transfer(account1, 1000)
            Thread.sleep(100)
        }

        assertEquals(2100, account1.balance)
        assertEquals(900, account2.balance)
        assertEquals(1000, account3.balance)
    }


    @Test
    fun `example of race condition`() {
        val mutableList = mutableListOf<Int>()

        val thread1 = thread {
            for (i in 1..100) {
                mutableList.add(i)
                Thread.sleep(1) // Add small delay
            }
        }

        val thread2 = thread {
            for (i in 101..200) {
                mutableList.add(i)
                Thread.sleep(1) // Add small delay
            }
        }

        val thread3 = thread {
            for (i in 201..300) {
                mutableList.add(i)
                Thread.sleep(1) // Add small delay
            }
        }

        thread1.join()
        thread2.join()
        thread3.join()

        logger.info("${mutableList.size}")
    }

    @Test
    fun `test using synchronized to prevent race-condition`() {
        val mutableList = mutableListOf<Int>()

        val lock = Any()

        val threads = listOf(thread {
            for (i in 1..100) {
                synchronized(lock) {
                    mutableList.add(i)
                }
            }
        }, thread {
            for (i in 101..200) {
                synchronized(lock) {
                    mutableList.add(i)
                }
            }
        }, thread {
            for (i in 201..300) {
                synchronized(lock) {
                    mutableList.add(i)
                }
            }
        })
        threads.forEach {
            it.join()
        }

        assertEquals(300, mutableList.size)
    }

    @Test
    fun `example of ConcurrentModificationException`() {
        val list = mutableListOf(1, 2, 3, 4, 5)
        assertFailsWith<ConcurrentModificationException> {
            for (item in list) {
                if (item == 3) {
                    list.remove(item)
                }
            }
        }
    }

    @Test
    fun `test using CopyOnWriteArrayList prevent ConcurrentModificationException`() {
        val list = CopyOnWriteArrayList(mutableListOf(1, 2, 3, 4, 5))

        for (item in list) {
            if (item == 3) {
                list.remove(item)
            }
        }
    }

    @Test
    fun `test using AtomicInteger to prevent race-condition`() {
        val list = mutableListOf<Int>()
        val atomInt = AtomicInteger(0)

        thread {
            for (i in 1..100) {
                list.add(i)
                atomInt.incrementAndGet() // Increment the atomic counter
            }
        }.join()

        thread {
            for (i in 101..200) {
                list.add(i)
                atomInt.incrementAndGet()
            }
        }.join()

        thread { list.remove(200) }.join()

        assertEquals(199, list.size)
    }

    @Test
    fun `test using synchronized to prevent race-condition reducing potentially deadlock`() {
        val list = mutableListOf<Int>()

        val threads = listOf(thread {
            for (i in 1..100) {
                synchronized(list) {
                    list.add(i)
                }
            }
        }, thread {
            for (i in 101..200) {
                synchronized(list) {
                    list.add(i)
                }
            }
        }, thread {
            for (i in 201..300) {
                synchronized(list) {
                    list.add(i)
                }
            }
        })

        threads.forEach { it.join() }

        assertEquals(300, list.size)
    }

    @Test
    fun `test using Collections-synchronizedMap to prevent thread-safety issue`() {
        val map = Collections.synchronizedMap(HashMap<Int, String>())

        val thread1 = thread {
            for (i in 1..100) {
                map[i] = "Thread 1 - $i"
                Thread.sleep(10) // simulate delay
            }
        }

        val thread2 = thread {
//            Thread.sleep(50)
            for (i in 101..200) {
                map[i] = "Thread 2 - $i"
            }
        }

        thread1.join()
        thread2.join()

        assertEquals(200, map.size)
    }

    @Test
    fun `test using ConcurrentHashMap for alternative to Collections-synchronizedMap`() {
//        val map = ConcurrentHashMap<Int, String>() // prevent race-condition
        val map = HashMap<Int, String>() // prevent race-condition

        thread {
            for (i in 1..100) {
                map[i] = "Thread 1 - $i"
            }
        }.join() // wait until thread finishes to prevent ConcurrentModificationException

        thread { map.remove(1) }.join()

        thread {
            for (i in 101..200) {
                map[i] = "Thread 2 - $i"
            }
        }.join()

        thread { map.remove(200) }.join()

        assertEquals(198, map.size)
    }

    @AfterEach
    fun detectDeadlock() {
        val threadMXBean = ManagementFactory.getThreadMXBean()
        threadMXBean.findDeadlockedThreads()?.forEach { id ->
            val threadInfo = threadMXBean.getThreadInfo(id, Int.MAX_VALUE)
            logger.warn("Deadlock detected: [id:$id, name:${threadInfo.threadName}, owner:${threadInfo.lockOwnerName}]")
            logger.warn(threadInfo.stackTrace.joinToString("\n"))
        }
    }
}