package datastore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SynchMap {
    Map<String, KVPair> kvMap;

    public SynchMap() {
        this.kvMap = new HashMap<>();
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

    synchronized public void put(String k, String v) {
        KVPair kvpair = kvMap.get(k);
        if (kvpair == null) {
            kvpair = new KVPair(k, v);
            kvMap.put(k, kvpair);
        } else {
            kvpair.update(v);
        }
    }

    synchronized public void clear() {
        kvMap.clear();
    }
}
