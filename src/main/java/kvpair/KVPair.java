package kvpair;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class KVPair {
    private String key;
    volatile private String value;
    volatile private int version;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public KVPair(String key, String initialValue) {
        this.key = key;
        this.value = initialValue;
        this.version = 1;
    }

    public void update(String newValue) {
        this.lock.writeLock().lock();
        this.value = newValue;
        this.version++;
        this.lock.writeLock().unlock();
    }

    public String getValue() {
        this.lock.readLock().lock();
        String returnValue = value;
        this.lock.readLock().unlock();
        return returnValue;
    }
}
