package datastore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SynchMap {
    ConcurrentMap<String, KVPair> kvMap;

    public SynchMap() {
        this.kvMap = new ConcurrentHashMap<>();
    }

    public Optional<String> get(String k) {
        KVPair kvpair = kvMap.get(k);
        if (kvpair == null)
            return Optional.empty();
        else
            return Optional.of( kvpair.getValue() );
    }

    public Optional<KVPair> getKVPair(String k) {
        KVPair kvpair = kvMap.get(k);
        if (kvpair == null)
            return Optional.empty();
        else
            return Optional.of(kvpair);
    }

    public void put(String k, String v) {
        KVPair kvpair = kvMap.get(k);
        if (kvpair == null) {
            kvpair = new KVPair(k, v);
            kvMap.put(k, kvpair);
        } else {
            kvpair.update(v);
        }
    }

    public boolean commit(String k, String v, int version) {
        KVPair kvpair = kvMap.get(k);
        if (kvpair == null) {
            kvpair = new KVPair(k, v, version);
            kvpair.commit(version);
            kvMap.put(k, kvpair);
            return true;
        } else {
            return kvpair.commit(version);
        }
    }

    public boolean commit(String k, String v) {
        KVPair kvpair = kvMap.get(k);
        if (kvpair == null) {
            kvpair = new KVPair(k, v, 0);
            kvpair.commit(0);
            kvMap.put(k, kvpair);
            return true;
        } else {
            return kvpair.commit();
        }
    }

    public void unCommit(String k, String v) {
        KVPair kvpair = kvMap.get(k);
        if (kvpair != null) {
            if (kvpair.checkVersion(-1)) {
                kvMap.remove(k);
            } else {
                kvpair.unCommit();
            };
        }
    }

    synchronized public void clear() {
        kvMap.clear();
    }
}
