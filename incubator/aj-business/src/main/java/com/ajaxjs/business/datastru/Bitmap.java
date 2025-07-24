package com.ajaxjs.business.datastru;

/**
 * 位图类：
 * 1.最多可以储存（2^37-2^6）个2种状态值的点;
 * 2.理论上有（2^37-2^6）个位空间，实际上可能不能完全使用（见无参构造器中的介绍），
 * 故使用时所选的点的状态值种数（stateNum）最好满足 64% ⌈ log2(stateNum) ⌉ = 0，即stateNum的补码的位数正好是64的因数；（听不明白的话，自行体会）
 * 3.没有删除点的方法，只有增加，更新，查找操作；（问为什么没有删除方法的人不是脑子有病，就是脑子有病。。。）
 * 4.回答3中问题：首先不是实现不了，是没有必要，因为删除一个点需要在数组中通过移位来填充被删除的位置，由于数据量过大可能会极其浪费时间，
 * 而且位图本身存在的意义是表示海量数据中每一条数据的可能性，对于海量数据而言，出现极个别错误并不会影响其最终统计结果（对于偷跑，出错的数据仍然可以在下一层中进行重新过滤，要是听不懂就算了。。。），
 * 这里建议使用update方法将不需要的点置0，用于表示该点被弃用，具体的逻辑需要使用者自己去编辑；
 * 5.该类所花费的理论持久存储空间大约为260MB也就是elementData数组的大小的极限值，实际可能会多一点，但几乎可以忽略不计，最终实现存储2种状态值的点的数量大约20亿个，
 * 也可以自定义每个点的状态值种数stateNum和点的数量length最大可以设置成（2^63-1）个，但最终要满足 length > ⌈ log2(stateNum) ⌉（向上取整） / 64 * Integer.MAX_VALUE，
 * 对于日常使用一定是足够的；（在这里，总有杠精会问超过了怎么办？就算是2种状态值的点有20亿个话，我还是不够用啊？）
 * 6.回答5中的问题：你不会搞个数组吗？你不能多new几个一起用吗！！！
 * <a href="https://blog.csdn.net/weixin_43695916/article/details/126655910">...</a>
 * <a href="https://blog.csdn.net/qq_40100414/article/details/118719748">...</a>
 */
public class Bitmap {
    /**
     * 使用long型数组保存点
     */
    private final long[] elementData;
    /*
     * 位图中能存储的所有点的位数之和的最大值MAX_STATE_SIZE=137,438,953,408；
     * 64（MAX_STATE_SIZE=long类型的位数） * 0x7fffffff（int类型的最大值Integer.MAX_VALUE）= MAX_STATE_SIZE （2^37-2^6）
     */
//    private static final long MAX_STATE_SIZE = 0x1fffffffc0L;//结果没用上，大意了。。。

    /**
     * 位图保存的点的状态种数
     */
    private final long stateNum;

    /**
     * 存储一个位图保存的点的状态值所需要的位数
     */
    private final int stateBitNum;

    /**
     * 位图可以储存的最大点数，初始化Bitmap类时指定
     */
    private final long MAX_SIZE;

    /**
     * 一个long型变量可以保存的点的数量
     */
    private final long numOfOneLong;

    /**
     * 该位图已经保存的点的数量
     */
    private long size;

    /**
     * @param stateNum 位图中点的状态数量,state>1
     * @param length   位图大小,0<length
     *                 如果 length > ⌈ log2(stateNum) ⌉（向上取整） / 64 * Integer.MAX_VALUE，则说明该 Bitmap 类无法储存 length 个 stateNum 种数的点，抛出异常
     */
    public Bitmap(long stateNum, long length) {
        if (stateNum > 1 && length > 0) {
            stateBitNum = 64 - Long.numberOfLeadingZeros(stateNum - 1);// 计算状态值需要多少位来表示
            /*
                计算一个long型里面最多可以存放几个状态值，
                如果不能整除的话，就舍弃余下位数，
                因为在数组中跨元素进行位值的存取计算极其复杂，故舍弃部分位空间，方便位图的各项操作
             */
            numOfOneLong = 64 / stateBitNum;

            /*
                一个long型最多可以存储numOfOneLong个状态值，
                所以该位图最大能存取numOfOneLong*Integer.MAX_VALUE个点，
                如果需要存储的数量length大于位图的最大存储数（numOfOneLong*Integer.MAX_VALUE）就说明该类Bitmap无法满足要求，抛出异常
             */
            if (length > numOfOneLong * Integer.MAX_VALUE)
                throw new RuntimeException("初始化的位图过大，无法存储！");

            this.stateNum = stateNum;
            MAX_SIZE = length;

            if (length % numOfOneLong == 0)
                elementData = new long[(int) (length / numOfOneLong)];
            else
                elementData = new long[((int) (length / numOfOneLong)) + 1];// 如果 length 个点不能被数组正好放下，就需要多添加一个数组元素来存储点
        } else
            throw new RuntimeException("位图类的初始化传入参数值中没有负整数！！！");
    }

    /**
     * 顺序添加点
     *
     * @param state 点的状态
     * @return true表示添加成功，产生false的情况有：位图满了；状态值越界
     */
    public boolean add(long state) {
        if (state > stateNum - 1 || state < 0 || size == MAX_SIZE)
            return false;

        int index = (int) (size / numOfOneLong);
        int left = (int) (size % numOfOneLong);
        elementData[index] |= state << (64 - stateBitNum * (1 + left));
        ++size;

        return true;
    }

    public long find(long index) {
        if (index < 0 || index > size - 1)
            return -1;

        int arrayIndex = (int) (index / numOfOneLong);// 计算数组中哪一个元素保存着 index 索引对应的点
        int elementIndex = (int) (index % numOfOneLong);// 计算元素中从哪一位开始的位保存着 index 索引对应的点

        // 通过左移，清空掉左边无用的位，再通过无符号右移清空掉右边无用的位，最终正好是需要查找的 index 位置对应的 state 值
        return elementData[arrayIndex] << (stateBitNum * elementIndex) >>> (64 - stateBitNum);
    }

    public boolean update(long index, long state) {
        if (index < 0 || index > size - 1 || state > stateNum - 1 || state < 0)
            return false;

        int arrayIndex = (int) (index / numOfOneLong);
        int left = (int) (index % numOfOneLong);
        long bitPre = (1L << stateBitNum) - 1;
        int bitSub = 64 - stateBitNum * (1 + left);
        elementData[arrayIndex] &= ~(bitPre << bitSub);
        elementData[arrayIndex] |= state << (64 - stateBitNum * (1 + left));

        return true;
    }

    /**
     * 返回位图中点的状态数量
     */
    public long getStateNum() {
        return stateNum;
    }

    /**
     * 返回位图可以存储的点的最大数量
     */
    public long getMaxSize() {
        return MAX_SIZE;
    }

    /**
     * 返回位图中实际已使用的数量
     */
    public long getSize() {
        return size;
    }

    /**
     * 辅助toString()方法，主要用于打印位图的具体显示
     */
    private String elementDataToString() {
        StringBuilder result = new StringBuilder("[\n");

        for (long element : elementData) {
            String eleString = Long.toBinaryString(element);
            StringBuilder one = new StringBuilder();

            for (int i = 0; i < 64 - eleString.length(); i++)
                one.append("0");

            one.append(eleString);

            for (int i = 0; i < numOfOneLong + 1; i++)
                one.insert((stateBitNum + 1) * i, ',');

            result.append(one.substring(1, one.lastIndexOf(","))).append(",\n");
        }

        return result.append("]").toString();
    }

    @Override
    public String toString() {
        return "Bitmap{\n" +
                "elementData=" + elementDataToString() +
                ", \nstateNum=" + stateNum +
                ", \nstateBitNum=" + stateBitNum +
                ", \nMAX_SIZE=" + MAX_SIZE +
                ", \nsize=" + size +
                "\n}";
    }

    public static void main(String[] args) {
        Bitmap bitmap = new Bitmap(8, 10);
        for (long i = 0; i < 10; i++)
            bitmap.add(0);

        for (long i = 0; i < 10; i++)
            bitmap.update(i, 7);

        System.out.println(bitmap.find(3));
        System.out.println(bitmap.find(4));
        System.out.println(bitmap.find(5));
        System.out.println("=====");


        bitmap.update(4, 3);
        System.out.println(bitmap.find(3));
        System.out.println(bitmap.find(4));
        System.out.println(bitmap.find(5));
        System.out.println("=====");

        bitmap.update(4, 2);
        System.out.println(bitmap.find(3));
        System.out.println(bitmap.find(4));
        System.out.println(bitmap.find(5));
        System.out.println("=====");

        bitmap.update(4, 1);
        System.out.println(bitmap.find(3));
        System.out.println(bitmap.find(4));
        System.out.println(bitmap.find(5));
        System.out.println("=====");

        bitmap.update(4, 5);
        System.out.println(bitmap.find(3));
        System.out.println(bitmap.find(4));
        System.out.println(bitmap.find(5));
        System.out.println("=====");
    }
}

