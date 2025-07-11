package com.ajaxjs.business.utils.image;


import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class Image {
    /**
     * 图像顺时针旋转90度
     *
     * @param input
     * @param width
     * @param height
     * @param bpp
     */
    public static void rotate90(byte[] input, int width, int height, int bpp) {
        if (input.length != width * height * bpp) {
            throw new IllegalArgumentException("INVALID INPUT SIZE");
        }
        byte[] tmp = new byte[bpp];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int I = height - 1 - j;
                int J = i;
                while ((i * height + j) > (I * width + J)) {
                    int p = I * width + J;
                    int tmp_i = p / height;
                    int tmp_j = p % height;
                    I = height - 1 - tmp_j;
                    J = tmp_i;
                }
                swap(input, (i * height + j) * bpp, (I * width + J) * bpp, bpp, tmp);
            }
        }
    }

    /**
     * 图像顺时针旋转270度 对图像矩阵原地旋转(In-place matrix transposition)的好处就是不用占用额外内存，
     * 所以在一些资源比较紧张的应用场景，原地旋转就显得必要了。
     *
     * @param input
     * @param width
     * @param height
     * @param bpp    每个像素点的字节数，对于RGB就是3
     */
    public static void rotate270(byte[] input, int width, int height, int bpp) {
        if (input.length != width * height * bpp)
            throw new IllegalArgumentException("INVALID INPUT SIZE");

        byte[] tmp = new byte[bpp];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int I = j;
                int J = width - 1 - i;

                while ((i * height + j) > (I * width + J)) {
                    int p = I * width + J;
                    int tmp_i = p / height;
                    int tmp_j = p % height;

                    I = tmp_j;
                    J = width - 1 - tmp_i;
                }

                swap(input, (i * height + j) * bpp, (I * width + J) * bpp, bpp, tmp);
            }
        }
    }

    /**
     * 对数据array中x和y指向的数据交换
     *
     * @param array
     * @param x
     * @param y
     * @param size  数据交换长度
     * @param tmp   用于数据交换的临时缓冲区，长度必须>= size
     */
    private static void swap(byte[] array, int x, int y, int size, byte[] tmp) {
        System.arraycopy(array, x, tmp, 0, size);
        System.arraycopy(array, y, array, x, size);
        System.arraycopy(tmp, 0, array, y, size);
    }

    /**
     * 从 RGB 格式图像矩阵数据创建一个 BufferedImage 从 RGBA 格式转 BufferedImage 的实现如下，注意，这个实现实际只保留了
     * Red,Green,Blue 三个颜色通道数据，删除了 alpha 通道。
     *
     * @param matrixRGBA RGBA 格式图像矩阵数据,为 null 则创建一个指定尺寸的空图像
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage createRGBAImage(byte[] matrixRGBA, int width, int height) {
        // 定义每像素字节数
        int bytePerPixel = 4;

        if (null != matrixRGBA && matrixRGBA.length == width * height * bytePerPixel)
            throw new IllegalArgumentException("invalid image description");

        // 将图像数据byte[]封装为DataBuffer
        DataBufferByte dataBuffer = null == matrixRGBA ? null : new DataBufferByte(matrixRGBA, matrixRGBA.length);
        // 定义色彩空间 sRGB
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] bOffs = {0, 1, 2};

        // 根据色彩空间创建色彩模型(ColorModel实例)，bOffs用于定义R,G,B三个分量在每个像素数据中的位置
        ComponentColorModel colorModel = new ComponentColorModel(cs, false, false, Transparency.OPAQUE,
                DataBuffer.TYPE_BYTE);
        // 从DataBuffer创建光栅对象Raster
        WritableRaster raster = null != dataBuffer
                ? Raster.createInterleavedRaster(dataBuffer, width, height, width * bytePerPixel, bytePerPixel, bOffs,
                null)
                : Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, width * bytePerPixel,
                bytePerPixel, bOffs, null);

        return new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
    }
    // -----------------------------------------------------------------------------------

    /**
     * 根据指定的参数创建一个RGB格式的BufferedImage
     * <a href="https://blog.csdn.net/10km/article/details/51872134">...</a>
     *
     * @param matrixRGB RGB格式的图像矩阵
     * @param width     图像宽度
     * @param height    图像高度
     * @return
     * @see DataBufferByte#DataBufferByte(byte[], int)
     * @see ColorSpace#getInstance(int)
     * @see ComponentColorModel#ComponentColorModel(ColorSpace, boolean, boolean,
     * int, int)
     * @see Raster#createInterleavedRaster(DataBuffer, int, int, int, int, int[],
     * Point)
     * @see BufferedImage#BufferedImage(ColorModel, WritableRaster, boolean,
     * java.util.Hashtable)
     */
    public static BufferedImage createRGBImage(byte[] matrixRGB, int width, int height) {
        // 检测参数合法性
        if (null == matrixRGB || matrixRGB.length != width * height * 3)
            throw new IllegalArgumentException("invalid image description");

        // 将byte[]转为DataBufferByte用于后续创建BufferedImage对象
        DataBufferByte dataBuffer = new DataBufferByte(matrixRGB, matrixRGB.length);
        // sRGB色彩空间对象
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] nBits = {8, 8, 8};
        int[] bOffs = {0, 1, 2};

        ComponentColorModel colorModel = new ComponentColorModel(cs, nBits, false, false, Transparency.OPAQUE,
                DataBuffer.TYPE_BYTE);

        WritableRaster raster = Raster.createInterleavedRaster(dataBuffer, width, height, width * 3, 3, bOffs, null);
        /*
         * try { //写入文件测试查看结果 ImageIO.write(newImg, "bmp", new
         * File(System.getProperty("user.dir"),"test.bmp")); } catch (IOException e) {
         * e.printStackTrace(); }
         */
        return new BufferedImage(colorModel, raster, false, null);
    }

    // -----------------------------------------------------------------------------------

    /**
     * 将一张彩色图你像转换成灰度(gray)
     *
     * @param srcImg
     * @return
     */
    public BufferedImage toGray(BufferedImage srcImg) {
        return new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(srcImg, null);
    }

    /**
     * 将原图压缩生成jpeg格式的数据
     *
     * @param source
     * @return
     */
    public static byte[] wirteJPEGBytes(BufferedImage source) {
        return writeBytes(source, "JPEG");
    }

    /**
     * 将{@link BufferedImage}生成formatName指定格式的图像数据
     * <a href="https://blog.csdn.net/10km/article/details/54584111">...</a>
     *
     * @param source
     * @param formatName 图像格式名，图像格式名错误则抛出异常
     * @return
     */
    public static byte[] writeBytes(BufferedImage source, String formatName) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Graphics2D g = null;

        try {
            for (BufferedImage s = source; !ImageIO.write(s, formatName, output); ) {
                if (null != g)
                    throw new IllegalArgumentException(String.format("not found writer for '%s'", formatName));

                s = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
                g = s.createGraphics();
                g.drawImage(source, 0, 0, null);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (null != g)
                g.dispose();
        }

        return output.toByteArray();
    }

    /**
     * 返回图像的RGB格式字节数组
     * <a href="https://blog.csdn.net/10km/article/details/51866321">...</a>
     *
     * @param image
     * @return
     */
    public static byte[] getMatrixRGB_Raw(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] intArray = new int[w * h];
        byte[] matrixRGB = new byte[w * h * 3];
        image.getRGB(0, 0, w, h, intArray, 0, w);

        // ARGB->RGB
        for (int i = 0, b = 0; i < intArray.length; ++i) {
            matrixRGB[b++] = (byte) (matrixRGB[i] & 0x000000FF);
            matrixRGB[b++] = (byte) ((matrixRGB[i] & 0x0000FF00) >> 8);
            matrixRGB[b++] = (byte) ((matrixRGB[i] & 0x00FF0000) >> 16);
        }

        return matrixRGB;
    }

    /**
     * 获取灰度图像的字节数组
     *
     * @param image
     * @return
     */
    public static byte[] getMatrixGray(BufferedImage image) {
        // 转灰度图像
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(image, grayImage);

        // getData方法返回BufferedImage的raster成员对象
        return (byte[]) grayImage.getData().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
    }

    /**
     * 获取图像RGB格式数据
     *
     * @param image
     * @return
     */
    public static byte[] getMatrixRGB(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            // 转sRGB格式
            BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(),
                    BufferedImage.TYPE_3BYTE_BGR);

            new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB), null).filter(image, rgbImage);
            // 从Raster对象中获取字节数组
            return (byte[]) rgbImage.getData().getDataElements(0, 0, rgbImage.getWidth(), rgbImage.getHeight(), null);
        } else
            return (byte[]) image.getData().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);

    }
//----------------------------------------------

    /*
     * java提供了一个非常方便的图像工具类javax.imageio.ImageIO，用它的javax.imageio.ImageIO.
     * read方法可以很方便的将一个图像文件进行解码。
     * javax.imageio.ImageIO.read方法有多个重载方法，支持File,InputStream,URL等参数，
     * 但这些方法有可能会在解码过程中使用文件系统做cache，具体原因这里不展开讲了，好长，你要研究java源码。
     * 有了磁盘IO势必会影响解码效率，这在性能敏感的应用环境是不能容忍的，
     * 如果要实现完全基于内存的图像解码，就不能简单使用javax.imageio.ImageIO.read方法。
     * 需要利用javax.imageio.stream.MemoryCacheImageInputStream来实现内存cache。
     *
     * https://blog.csdn.net/10km/article/details/52119508
     */

    /**
     * 从内存字节数组中读取图像
     *
     * @param imgBytes 未解码的图像数据
     * @return 返回 {@link BufferedImage}
     * @throws IOException 当读写错误或不识别的格式时抛出
     */
    public static BufferedImage readMemoryImage(byte[] imgBytes) throws IOException {
        if (null == imgBytes || 0 == imgBytes.length)
            throw new NullPointerException("the argument 'imgBytes' must not be null or empty");

        // 将字节数组转为InputStream，再转为MemoryCacheImageInputStream
        ImageInputStream imageInputstream = new MemoryCacheImageInputStream(new ByteArrayInputStream(imgBytes));
        // 获取所有能识别数据流格式的ImageReader对象
        Iterator<ImageReader> it = ImageIO.getImageReaders(imageInputstream);

        // 迭代器遍历尝试用ImageReader对象进行解码
        while (it.hasNext()) {
            ImageReader imageReader = it.next();
            // 设置解码器的输入流
            imageReader.setInput(imageInputstream, true, true);
            // 图像文件格式后缀
            String suffix = imageReader.getFormatName().trim().toLowerCase();
            // 图像宽度
            int width = imageReader.getWidth(0);
            // 图像高度
            int height = imageReader.getHeight(0);
            System.out.printf("format %s,%dx%d\n", suffix, width, height);

            try {
                // 解码成功返回BufferedImage对象
                // 0即为对第0张图像解码(gif格式会有多张图像),前面获取宽度高度的方法中的参数0也是同样的意思
                return imageReader.read(0, imageReader.getDefaultReadParam());
            } catch (Exception e) {
                imageReader.dispose();
                // 如果解码失败尝试用下一个ImageReader解码
            }
        }

        imageInputstream.close();
        // 没有能识别此数据的图像ImageReader对象，抛出异常
        throw new IOException("unsupported image format");
    }

    public static final BufferedImage readMemoryImage1(byte[] imgBytes) throws IOException {
        if (null == imgBytes || 0 == imgBytes.length)
            throw new NullPointerException("the argument 'imgBytes' must not be null or empty");

        // 将字节数组转为InputStream，再转为MemoryCacheImageInputStream
        ImageInputStream imageInputstream = new MemoryCacheImageInputStream(new ByteArrayInputStream(imgBytes));
        // 直接调用ImageIO.read方法解码
        BufferedImage bufImg = ImageIO.read(imageInputstream);

        if (null == bufImg)
            // 没有能识别此数据的图像ImageReader对象，抛出异常
            throw new IOException("unsupported image format");

        return bufImg;
    }

    /**
     * 从{@link InputStream}读取字节数组<br>
     * 结束时会关闭{@link InputStream}<br>
     * {@code in}为{@code null}时抛出{@link NullPointerException}
     *
     * @param in
     * @return 字节数组
     * @throws IOException
     */
    public static final byte[] readBytes(InputStream in) throws IOException {
        if (null == in)
            throw new NullPointerException("the argument 'in' must not be null");

        int buffSize = Math.max(in.available(), 1024 * 8);
        byte[] temp = new byte[buffSize];

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(buffSize)) {
            int size;

            while ((size = in.read(temp)) != -1)
                out.write(temp, 0, size);

            return out.toByteArray();
        }
    }
}
