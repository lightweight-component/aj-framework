package com.ajaxjs.business.datastru.bloom_filter;


import java.io.Serializable;
import java.util.Collection;

/**
 * 采用Redis、Java的BitSet布隆过滤器去重
 * BloomFilter算法及其适用场景
 * <p>
 * BloomFilter是利用类似位图或者位集合数据结构来存储数据，利用位数组来简洁的表示一个集合，并且能够快速的判断一个元素是不是已经存在于这个集合。因为基于Hash来计算数据所在位置，所以BloomFilter的添加和查询操作都是O(1)的。因为存储简洁，这种数据结构能够利用较少的内存来存储海量的数据。那么，还有这种时间和空间两全其美的算法？当然不是，BloomFilter正是它的高效（使用Hash）带来了它的判断不一定是正确的，也就是说准确率不是100%。因为再好的Hash都是存在冲突的，这样的话同一个位置可能被多次置1。这样再判断的时候，有可能一个不存在的数据就会误判成存在。但是判断存在的数据一定是存在的。这里需要注意的是这里的Hash和HashMap不同，HashMap可以使用开放定址发、链地址法来解决冲突，因为HashMap是有Key-Value结构的，是可逆的，可以定位。但是Hash是不可逆的，所以不能够解决冲突。虽然BloomFilter不是100%准确，但是可以通过调节参数，使用Hash函数的个数，位数组的大小来降低失误率。这样调节完全可以把失误率降低到接近于0。可以满足大部分场景了。
 * 适用场景：BloomFilter一般适用于大数据量的对精确度要求不是100%的去重场景。
 * <p>
 * 　　爬虫链接的去重：大的爬虫系统有成千上万的链接需要去爬，而且需要保证爬虫链接不能循环。这样就需要链接列表的去重。把链接Hash后存放在BitSet中，然后在爬取之前判断是否存在。
 * 　　网站UV统计：一般同一个用户的多次访问是要过滤掉的，一般大型网站的UV是巨大的，这样使用BloomFilter就能较高效的实现。
 * 缓存击穿：将已存在的缓存放到布隆中，当黑客访问不存在的缓存时迅速返回避免缓存及DB挂掉。
 * 垃圾邮件识别(反垃圾邮件)：从数十亿个垃圾邮件列表中判断某邮箱是否垃圾邮箱（同理，垃圾短信）
 * 集合判重
 * <a href="https://blog.csdn.net/ycb1689/article/details/89850260">...</a>
 * This program refers to the java-bloomfilter,you can get its details form <a href="https://github.com/MagnusS/Java-BloomFilter">...</a>.
 * You have any questions about this program please put issues on github to
 *
 * @param <E> Element type
 */
public class BloomFilter<E> implements Cloneable, Serializable {

    private BaseBitSet bitSet;
    private final int bitSetSize;
    private final double bitsPerElement;
    private final int expectedNumberOfFilterElements; // expected (maximum) number of elements to be added
    private int numberOfAddedElements; // number of elements actually added to the Bloom filter
    private final int k; // number of hash functions


    /**
     * Bind a bit set for Bloom filter. It can be any data structure that implements the BaseBitSet interface.
     */
    public void bind(BaseBitSet bitSet) {
        this.bitSet = bitSet;
    }

    /**
     * Constructs an empty Bloom filter. The total length of the Bloom filter will be
     * c*n.
     *
     * @param c is the number of bits used per element.
     * @param n is the expected number of elements the filter will contain.
     * @param k is the number of hash functions used.
     */
    public BloomFilter(double c, int n, int k) {
        this.expectedNumberOfFilterElements = n;
        this.k = k;
        this.bitsPerElement = c;
        this.bitSetSize = (int) Math.ceil(c * n);
        numberOfAddedElements = 0;
    }

    /**
     * Constructs an empty Bloom filter. The optimal number of hash functions (k) is estimated from the total size of the Bloom
     * and the number of expected elements.
     *
     * @param bitSetSize              defines how many bits should be used in total for the filter.
     * @param expectedNumberOElements defines the maximum number of elements the filter is expected to contain.
     */
    public BloomFilter(int bitSetSize, int expectedNumberOElements) {
        this(bitSetSize / (double) expectedNumberOElements,
                expectedNumberOElements,
                (int) Math.round((bitSetSize / (double) expectedNumberOElements) * Math.log(2.0)));
    }

    /**
     * Constructs an empty Bloom filter with a given false positive probability. The number of bits per
     * element and the number of hash functions is estimated
     * to match the false positive probability.
     *
     * @param falsePositiveProbability is the desired false positive probability.
     * @param expectedNumberOfElements is the expected number of elements in the Bloom filter.
     */
    public BloomFilter(double falsePositiveProbability, int expectedNumberOfElements) {
        this(Math.ceil(-(Math.log(falsePositiveProbability) / Math.log(2.0))) / Math.log(2.0), // c = k / ln(2)
                expectedNumberOfElements,
                (int) Math.ceil(-(Math.log(falsePositiveProbability) / Math.log(2.0)))); // k = ceil(-log_2(false prob.))
    }

    /**
     * Construct a new Bloom filter based on existing Bloom filter data.
     *
     * @param bitSetSize                     defines how many bits should be used for the filter.
     * @param expectedNumberOfFilterElements defines the maximum number of elements the filter is expected to contain.
     * @param actualNumberOfFilterElements   specifies how many elements have been inserted into the <code>filterData</code> BitSet.
     * @param filterData                     a BitSet representing an existing Bloom filter.
     */
    public BloomFilter(int bitSetSize, int expectedNumberOfFilterElements, int actualNumberOfFilterElements, BaseBitSet filterData) {
        this(bitSetSize, expectedNumberOfFilterElements);
        this.bitSet = filterData;
        this.numberOfAddedElements = actualNumberOfFilterElements;
    }


    /**
     * Compares the contents of two instances to see if they are equal.
     *
     * @param obj is the object to compare to.
     * @return True if the contents of the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BloomFilter<E> other = (BloomFilter<E>) obj;
        if (this.expectedNumberOfFilterElements != other.expectedNumberOfFilterElements) {
            return false;
        }
        if (this.k != other.k) {
            return false;
        }
        if (this.bitSetSize != other.bitSetSize) {
            return false;
        }
        if (this.bitSet != other.bitSet && (this.bitSet == null || !this.bitSet.equals(other.bitSet))) {
            return false;
        }
        return true;
    }

    /**
     * Calculates a hash code for this class.
     *
     * @return hash code representing the contents of an instance of this class.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (this.bitSet != null ? this.bitSet.hashCode() : 0);
        hash = 61 * hash + this.expectedNumberOfFilterElements;
        hash = 61 * hash + this.bitSetSize;
        hash = 61 * hash + this.k;
        return hash;
    }


    /**
     * Calculates the expected probability of false positives based on
     * the number of expected filter elements and the size of the Bloom filter.
     * <p>
     * The value returned by this method is the <i>expected</i> rate of false
     * positives, assuming the number of inserted elements equals the number of
     * expected elements. If the number of elements in the Bloom filter is less
     * than the expected value, the true probability of false positives will be lower.
     *
     * @return expected probability of false positives.
     */
    public double expectedFalsePositiveProbability() {
        return getFalsePositiveProbability(expectedNumberOfFilterElements);
    }

    /**
     * Calculate the probability of a false positive given the specified
     * number of inserted elements.
     *
     * @param numberOfElements number of inserted elements.
     * @return probability of a false positive.
     */
    public double getFalsePositiveProbability(double numberOfElements) {
        // (1 - e^(-k * n / m)) ^ k
        return Math.pow((1 - Math.exp(-k * (double) numberOfElements
                / (double) bitSetSize)), k);

    }

    /**
     * Get the current probability of a false positive. The probability is calculated from
     * the size of the Bloom filter and the current number of elements added to it.
     *
     * @return probability of false positives.
     */
    public double getFalsePositiveProbability() {
        return getFalsePositiveProbability(numberOfAddedElements);
    }


    /**
     * Returns the value chosen for K.
     * <p>
     * K is the optimal number of hash functions based on the size
     * of the Bloom filter and the expected number of inserted elements.
     *
     * @return optimal k.
     */
    public int getK() {
        return k;
    }

    /**
     * Sets all bits to false in the Bloom filter.
     */
    public void clear() {
        bitSet.clear();
        numberOfAddedElements = 0;
    }

    /**
     * Adds an object to the Bloom filter. The output from the object's
     * toString() method is used as input to the hash functions.
     *
     * @param element is an element to register in the Bloom filter.
     */
    public void add(E element) {
        add(element.toString().getBytes(MessageDigestUtils.CHARSET));
    }

    /**
     * Adds an array of bytes to the Bloom filter.
     *
     * @param bytes array of bytes to add to the Bloom filter.
     */
    public void add(byte[] bytes) {
        int[] hashes = MessageDigestUtils.createHashes(bytes, k);

        for (int hash : hashes)
            bitSet.set(Math.abs(hash % bitSetSize), true);

        numberOfAddedElements++;
    }

    /**
     * Adds all elements from a Collection to the Bloom filter.
     *
     * @param c Collection of elements.
     */
    public void addAll(Collection<? extends E> c) {
        for (E element : c)
            add(element);
    }

    /**
     * Returns true if the element could have been inserted into the Bloom filter.
     * Use getFalsePositiveProbability() to calculate the probability of this
     * being correct.
     *
     * @param element element to check.
     * @return true if the element could have been inserted into the Bloom filter.
     */
    public boolean contains(E element) {
        return contains(element.toString().getBytes(MessageDigestUtils.CHARSET));
    }

    /**
     * Returns true if the array of bytes could have been inserted into the Bloom filter.
     * Use getFalsePositiveProbability() to calculate the probability of this
     * being correct.
     *
     * @param bytes array of bytes to check.
     * @return true if the array could have been inserted into the Bloom filter.
     */
    public boolean contains(byte[] bytes) {
        int[] hashes = MessageDigestUtils.createHashes(bytes, k);

        for (int hash : hashes) {
            if (!bitSet.get(Math.abs(hash % bitSetSize)))
                return false;
        }

        return true;
    }

    /**
     * Returns true if all the elements of a Collection could have been inserted
     * into the Bloom filter. Use getFalsePositiveProbability() to calculate the
     * probability of this being correct.
     *
     * @param c elements to check.
     * @return true if all the elements in c could have been inserted into the Bloom filter.
     */
    public boolean containsAll(Collection<? extends E> c) {
        for (E element : c)
            if (!contains(element))
                return false;
        return true;
    }

    /**
     * Read a single bit from the Bloom filter.
     *
     * @param bit the bit to read.
     * @return true if the bit is set, false if it is not.
     */
    public boolean getBit(int bit) {
        return bitSet.get(bit);
    }

    /**
     * Set a single bit in the Bloom filter.
     *
     * @param bit   is the bit to set.
     * @param value If true, the bit is set. If false, the bit is cleared.
     */
    public void setBit(int bit, boolean value) {
        bitSet.set(bit, value);
    }

    /**
     * Return the bit set used to store the Bloom filter.
     *
     * @return bit set representing the Bloom filter.
     */
    public BaseBitSet getBitSet() {
        return bitSet;
    }

    /**
     * Returns the number of bits in the Bloom filter. Use count() to retrieve
     * the number of inserted elements.
     *
     * @return the size of the bitSet used by the Bloom filter.
     */
    public int size() {
        return this.bitSetSize;
    }

    /**
     * Returns the number of elements added to the Bloom filter after it
     * was constructed or after clear() was called.
     *
     * @return number of elements added to the Bloom filter.
     */
    public int count() {
        return this.numberOfAddedElements;
    }

    /**
     * Returns is the bit set empty, bit set is empty means no any elements added to bloom filter.
     *
     * @return is the bit set empty
     */
    public boolean isEmpty() {
        return count() <= 0;
    }

    /**
     * Returns the expected number of elements to be inserted into the filter.
     * This value is the same value as the one passed to the constructor.
     *
     * @return expected number of elements.
     */
    public int getExpectedNumberOfElements() {
        return expectedNumberOfFilterElements;
    }

    /**
     * Get expected number of bits per element when the Bloom filter is full. This value is set by the constructor
     * when the Bloom filter is created. See also getBitsPerElement().
     *
     * @return expected number of bits per element.
     */
    public double getExpectedBitsPerElement() {
        return this.bitsPerElement;
    }

    /**
     * Get actual number of bits per element based on the number of elements that have currently been inserted and the length
     * of the Bloom filter. See also getExpectedBitsPerElement().
     *
     * @return number of bits per element.
     */
    public double getBitsPerElement() {
        return this.bitSetSize / (double) numberOfAddedElements;
    }

}