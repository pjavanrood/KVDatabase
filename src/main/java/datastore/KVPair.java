package datastore;

import replication.KVPairResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class KVPair {
    private final String key;
    volatile private String value;
    volatile private int version;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    volatile private boolean committed;

    public KVPair(String key, String initialValue) {
        this.key = key;
        this.value = initialValue;
        this.version = 0;
        this.committed = false;
    }

    public KVPair(String key, String initialValue, int version) {
        this.key = key;
        this.value = initialValue;
        this.version = version;
    }

    public void update(String newValue) {
        this.lock.writeLock().lock();
        this.value = newValue;
        this.version++;
        this.committed = false;
        this.lock.writeLock().unlock();
    }

    public boolean update(String newValue, int version) {
        this.lock.writeLock().lock();
        boolean result = true;
        if (version > this.version) {
            this.value = newValue;
            this.version = version;
            this.committed = false;
        } else {
            result = false;
        }
        this.lock.writeLock().unlock();
        return result;
    }

    public boolean commit(int version) {
        this.lock.writeLock().lock();
        boolean result = true;
        if (!this.committed && version == this.version) {
            this.committed = true;
        } else {
            result = false;
        }
        this.lock.writeLock().unlock();
        return result;
    }

    public boolean commit() {
        this.lock.writeLock().lock();
        boolean result = true;
        if (!this.committed) {
            this.committed = true;
        } else {
            result = false;
        }
        this.lock.writeLock().unlock();
        return result;
    }

    public void unCommit(int version) {
        this.lock.writeLock().lock();
        if (this.committed && version == this.version) {
            this.committed = false;
        }
        this.lock.writeLock().unlock();
    }

    public void unCommit() {
        this.lock.writeLock().lock();
        this.committed = false;
        this.lock.writeLock().unlock();
    }

    public List<String> get() {
        List<String> result = new ArrayList<>();
        this.lock.readLock().lock();
        result.add(this.key);
        result.add(this.value);
        result.add(String.valueOf(this.version));
        this.lock.readLock().unlock();
        return result;
    }

    public String getValue() {
        this.lock.readLock().lock();
        String returnValue = value;
        this.lock.readLock().unlock();
        return returnValue;
    }

    public int getVersion() {
        this.lock.readLock().lock();
        int result = this.version;
        this.lock.readLock().unlock();
        return result;
    }

    public boolean equalsKVPair(String key, String value, int version) {
        this.lock.readLock().lock();
        boolean result = key.equals(this.key) && value.equals(this.value) && version == this.version;
        this.lock.readLock().unlock();
        return result;
    }

    public boolean checkVersion(int version) {
        this.lock.readLock().lock();
        boolean result = this.version == version;
        this.lock.readLock().unlock();
        return result;
    }

    public boolean checkCommitted() {
        this.lock.readLock().lock();
        boolean result = this.committed;
        this.lock.readLock().unlock();
        return result;
    }
}
