package ace.fw.dal.sharding.hash;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.LongAdder;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author Caspar
 * @contract 279397942@qq.com
 * @create 2020/4/9 17:53
 * @description 一致性hash(带虚拟节点)
 */
@AllArgsConstructor
public class StringConsistentHashCoding implements HashCoding {

    private int realNodeCount;

    private int virtualNodeCount;

    private final HashFunction hash;

    private final SortedMap<Integer, Integer> bucketMap;

    public StringConsistentHashCoding(int realNodeCount, int virtualNodeCount) {
        checkArgument(realNodeCount > 0, "realNodeCount must be positive: %s", realNodeCount);
        checkArgument(virtualNodeCount > 0, "virtualNodeCount must be positive: %s", realNodeCount);

        this.realNodeCount = realNodeCount;
        this.virtualNodeCount = virtualNodeCount;

        this.bucketMap = new TreeMap<>();
        this.hash = Hashing.murmur3_32();

        String virtualNodeNameFormat = "VIRTUAL-%s-NODE-%s";
        for (int i = 0; i < realNodeCount; i++) {
            for (int n = 0; n < virtualNodeCount; n++) {
                String virtualNodeName = String.format(virtualNodeNameFormat, i, n);
                this.bucketMap.put(this.hash.hashUnencodedChars(virtualNodeName).asInt(), i);
            }
        }
    }

    @Override
    public int hash(String v) {
        SortedMap<Integer, Integer> tail = bucketMap.tailMap(hash.hashUnencodedChars(v).asInt());
        if (tail.isEmpty()) {
            return bucketMap.get(bucketMap.firstKey());
        }
        return tail.get(tail.firstKey());
    }

    public static void main(String[] args) throws Exception {
        StringConsistentHashCoding hashCoding = new StringConsistentHashCoding(32, 64);
        int count = 100000;
        Map<String, LongAdder> map = getMap();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            int v = hashCoding.hash(Integer.toString(i));
            LongAdder longAdder = map.get(Integer.toString(v));
            longAdder.increment();
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);

        long c = 0;
        for (Map.Entry<String, LongAdder> entry : map.entrySet()) {
            System.out.println(String.format("%s=%s", entry.getKey(), entry.getValue().longValue()));
            c += entry.getValue().longValue();
        }
        System.out.println(c);
    }

    static Map<String, LongAdder> getMap() {
        Map<String, LongAdder> map = new HashMap<>();
        for (int i = 0; i < 32; i++) {
            map.put(Integer.toString(i), new LongAdder());
        }
        return map;
    }
}
