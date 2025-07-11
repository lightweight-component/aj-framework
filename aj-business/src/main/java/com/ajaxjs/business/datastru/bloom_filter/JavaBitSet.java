package com.ajaxjs.business.datastru.bloom_filter;


import java.util.BitSet;

/**
 * 使用Java内置的BitSet实现布隆过滤器
 */
public class JavaBitSet implements BaseBitSet {
    private final BitSet bitSet;

    public JavaBitSet() {
        this.bitSet = new BitSet();
    }

    public JavaBitSet(BitSet bitSet) {
        if (bitSet == null) {
            this.bitSet = new BitSet();
        } else {
            this.bitSet = bitSet;
        }
    }

    public void set(int bitIndex) {
        bitSet.set(bitIndex);
    }

    public void set(int bitIndex, boolean value) {
        bitSet.set(bitIndex, value);
    }

    public boolean get(int bitIndex) {
        return bitSet.get(bitIndex);
    }

    @Override
    public void clear(int bitIndex) {
        bitSet.clear(bitIndex);
    }

    @Override

    public void clear() {
        bitSet.clear();
    }

    @Override
    public long size() {
        return bitSet.size();
    }

    @Override
    public boolean isEmpty() {
        return this.isEmpty();
    }
}