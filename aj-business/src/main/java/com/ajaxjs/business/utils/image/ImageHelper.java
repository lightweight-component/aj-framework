package com.ajaxjs.business.utils.image;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


/***
 * 对图片进行操作  *
 *
 * @author youqiang.xiong
 * @since 2016.11.16
 * */
public class ImageHelper {
    /**
     * 根据RGB值判断 深色与浅色
     *
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static boolean isDark(Double r, Double g, Double b) {
        if (r * 0.299 + g * 0.578 + b * 0.114 >= 192)  //浅色
            return false;
        else //深色
            return true;
    }

    protected static Logger log = LoggerFactory.getLogger(ImageHelper.class);

    private static final ImageHelper imageHelper = new ImageHelper();

    public static ImageHelper getImageHelper() {
        return imageHelper;
    }

    /***
     * * 按指定的比例缩放图片
     *
     * @param sourceImagePath 源地址
     * @param destinationPath  改变大小后图片的地址
     * @param scale  缩放比例，如1.2
     */
    public static void scaleImage(String sourceImagePath, String destinationPath, double scale, String format) throws Exception {
        log.info("图片原路径：" + sourceImagePath + "\t 图片目录路径:" + destinationPath);
        File file = new File(sourceImagePath);

        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            width = parseDoubleToInt(width * scale);
            height = parseDoubleToInt(height * scale);

            Image image = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = outputImage.getGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();
            ImageIO.write(outputImage, format, new File(destinationPath));
        } catch (IOException e) {
            log.error("{}", e);
            throw new Exception("放入目标路径报错,Temp目录未生成。" + e.getMessage());
        }

    }

    /***
     * * 将图片缩放到指定的高度或者宽度 *
     *
     * @param sourceImagePath
     *            图片源地址
     * @param destinationPath
     *            压缩完图片的地址
     * @param width
     *            缩放后的宽度
     * @param height
     *            缩放后的高度
     * @param auto
     *            是否自动保持图片的原高宽比例
     * @param format
     *            图片格式 例如 jpg
     */
    public static void scaleImageWithParams(String sourceImagePath, String destinationPath, int width, int height, boolean auto, String format) {
        try {
            File file = new File(sourceImagePath);
            BufferedImage bufferedImage;
            bufferedImage = ImageIO.read(file);

            if (auto) {
                ArrayList<Integer> paramsArrayList = getAutoWidthAndHeight(bufferedImage, width, height);
                width = paramsArrayList.get(0);
                height = paramsArrayList.get(1);
                log.info("自动调整比例，width=" + width + " height=" + height);
            }

            Image image = bufferedImage.getScaledInstance(width, height, Image.SCALE_DEFAULT);
            BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = outputImage.getGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();
            ImageIO.write(outputImage, format, new File(destinationPath));
        } catch (Exception e) {
            log.info("scaleImageWithParams方法压缩图片时出错了");
            log.error("{}", e);
        }
    }

    /***
     * * 将图片缩放到大小等于对应宽度和高度的图片
     *
     * @param sourceImagePath
     *            图片源地址
     * @param destinationPath
     *            压缩完图片的地址
     * @param width
     *            缩放后的宽度
     * @param height
     *            缩放后的高度
     * @param format
     *            图片格式 例如 jpg
     */
    public static void scaleImageToVIPByHeight(String sourceImagePath, String destinationPath, int width, int height, String format) throws Exception {

        File file = new File(sourceImagePath);
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(file);
        } catch (IOException e) {
            log.error("{}", e);
        }

        assert bufferedImage != null;
        double scale = height * 1.0d / bufferedImage.getHeight();
        log.info("缩放比例：" + scale);
        scaleImage(sourceImagePath, destinationPath, scale, format);

    }

    /***
     * * 将图片缩放到大小等于对应宽度和高度的图片
     *
     * @param sourceImagePath
     *            图片源地址
     * @param destinationPath
     *            压缩完图片的地址
     * @param width
     *            缩放后的宽度
     * @param height
     *            缩放后的高度
     * @param format
     *            图片格式 例如 jpg
     */
    public static void scaleImageToVIPByWidth(String sourceImagePath, String destinationPath, int width, int height, String format) {
        try {
            File file = new File(sourceImagePath);
            BufferedImage bufferedImage = ImageIO.read(file);
            double scale = bufferedImage.getWidth() * 1.0d / width < 1 ? bufferedImage.getWidth() * 1.0d / width : width * 1.0d / bufferedImage.getWidth();
            log.info("缩放比例：" + scale);
            scaleImage(sourceImagePath, destinationPath, scale, format);

        } catch (Exception e) {
            log.info("scaleImageWithParams方法压缩图片时出错了");
            log.error("{}", e);
        }
    }

    /**
     * * 将double类型的数据转换为int，四舍五入原则
     */
    private static int parseDoubleToInt(double sourceDouble) {
        return (int) sourceDouble;
    }

    /***
     * * @param bufferedImage 要缩放的图片对象 *
     *
     * @param width_scale
     *            要缩放到的宽度 *
     * @param height_scale
     *            要缩放到的高度 *
     * @return 一个集合，第一个元素为宽度，第二个元素为高度
     */
    private static ArrayList<Integer> getAutoWidthAndHeight(BufferedImage bufferedImage, int width_scale, int height_scale) {
        ArrayList<Integer> arrayList = new ArrayList<Integer>();
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        double scale_w = getDot2Decimal(width_scale, width);
        log.info("getAutoWidthAndHeight width=" + width + "scale_w=" + scale_w);
        double scale_h = getDot2Decimal(height_scale, height);

        if (scale_w < scale_h) {
            arrayList.add(parseDoubleToInt(scale_w * width));
            arrayList.add(parseDoubleToInt(scale_w * height));
        } else {
            arrayList.add(parseDoubleToInt(scale_h * width));
            arrayList.add(parseDoubleToInt(scale_h * height));
        }

        return arrayList;
    }

    /***
     * * 返回两个数a/b的小数点后三位的表示
     *
     * @param a
     * @param b
     * @return
     */
    public static double getDot2Decimal(int a, int b) {
        BigDecimal bigDecimal_1 = new BigDecimal(a);
        BigDecimal bigDecimal_2 = new BigDecimal(b);
        BigDecimal bigDecimal_result = bigDecimal_1.divide(bigDecimal_2, new MathContext(4));
        Double double1 = Double.parseDouble(bigDecimal_result.toString());
        log.info("相除后的double为：" + double1);

        return double1;
    }

    public static BufferedImage resize(BufferedImage source, int targetW, int targetH) {
        // targetW，targetH分别表示目标长和宽
        int type = source.getType();
        BufferedImage target;
        double sx = (double) targetW / source.getWidth();
        double sy = (double) targetH / source.getHeight();

        // 这里想实现在targetW，targetH范围内实现等比缩放。如果不需要等比缩放
        // 则将下面的if else语句注释即可
        if (sx > sy) {
            sx = sy;
            targetW = (int) (sx * source.getWidth());
        } else {
            sy = sx;
            targetH = (int) (sy * source.getHeight());
        }
        if (type == BufferedImage.TYPE_CUSTOM) { // handmade
            ColorModel cm = source.getColorModel();
            WritableRaster raster = cm.createCompatibleWritableRaster(targetW, targetH);
            boolean alphaPremultiplied = cm.isAlphaPremultiplied();
            target = new BufferedImage(cm, raster, alphaPremultiplied, null);
        } else target = new BufferedImage(targetW, targetH, type);

        Graphics2D g = target.createGraphics();
        // smoother than exlax:
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
        g.dispose();

        return target;
    }

    public static boolean isPic(File file) {
        String imgeArray[] = {"bmp", "dib", "gif", "jfif", "jpe", "jpeg", "jpg", "png", "tif", "tiff", "ico"};
        String fileName = file.getName();
        fileName = fileName.substring(fileName.lastIndexOf(".") + 1);

        for (String suffix : imgeArray) {
            if (fileName.equals(suffix))
                return true;
        }

        return false;
    }

    public static void createNewPic(File f, String backGroundPicPath, String targetPicPath) throws IOException {
        if (f.isFile() && isPic(f)) {

            File file = new File(backGroundPicPath);
            Image img = ImageIO.read(file);
            int width = img.getWidth(null);
            int height = img.getHeight(null);
            // create target image
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);// get a graphics pen
            Graphics g = image.createGraphics();// draw source image
            g.drawImage(img, 0, 0, width, height, null);
            // draw target logo
            BufferedImage doPic = ImageIO.read(f);

            if (doPic.getWidth() > width || doPic.getHeight() > height)
                resize(doPic, width, height);

            int lw = doPic.getWidth(null);
            int lh = doPic.getHeight(null);
            int px_x = (width - lw) / 2;
            int px_y = (height - lh) / 2;

            g.drawImage(doPic, px_x, px_y, lw, lh, null);
            // 这里是添加水印文字// String str = "http://www.hongliang.net";
            // g.setColor(Color.red);
            // g.setFont(new Font("Courier", Font.PLAIN, 36));
            // 水印文字对准// g.drawString(str, width-400, height-60);

            g.dispose();

//            try (FileOutputStream os = new FileOutputStream(targetPicPath + f.getName())) {
//                JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
//                encoder.encode(image);
//                log.info("完成：" + f.getName());
//            }
        }
    }

    /**
     * 给图片添加水印
     *
     * @param iconPath   水印图片路径
     * @param srcImgPath 源图片路径
     * @param targetPath 目标图片路径
     */
    public static void markImageByIcon(String iconPath, String srcImgPath, String targetPath) {
        markImageByIcon(iconPath, srcImgPath, targetPath, null);
    }

    /**
     * 给图片添加水印、可设置水印图片旋转角度
     *
     * @param iconPath   水印图片路径
     * @param srcImgPath 源图片路径
     * @param targerPath 目标图片路径
     * @param degree     水印图片旋转角度
     */
    public static void markImageByIcon(String iconPath, String srcImgPath, String targerPath, Integer degree) {

        try {
            Image srcImg = ImageIO.read(new File(srcImgPath));
            BufferedImage buffImg = new BufferedImage(srcImg.getWidth(null), srcImg.getHeight(null), BufferedImage.TYPE_INT_RGB);

            // 得到画笔对象
            // Graphics g= buffImg.getGraphics();
            Graphics2D g = buffImg.createGraphics();

            // 设置对线段的锯齿状边缘处理
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g.drawImage(srcImg.getScaledInstance(srcImg.getWidth(null), srcImg.getHeight(null), Image.SCALE_SMOOTH), 0, 0, null);

            if (null != degree)
                // 设置水印旋转
                g.rotate(Math.toRadians(degree), (double) buffImg.getWidth() / 2, (double) buffImg.getHeight() / 2);

            // 水印图象的路径 水印一般为gif或者png的，这样可设置透明度
            ImageIcon imgIcon = new ImageIcon(iconPath);

            // 得到Image对象。
            Image img = imgIcon.getImage();

            float alpha = 1f; // 透明度
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            g.drawImage(img, 0, 0, null);  // 表示水印图片的位置
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.dispose();

            try (OutputStream os = Files.newOutputStream(Paths.get(targerPath))) {
                // 生成图片
                ImageIO.write(buffImg, "JPG", os);
                log.info("图片完成添加Icon印章。。。。。。");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createNewPic(String dir, String backGroundPicPath, String targetPicPath) throws IOException {
        File ML = new File(dir);

        if (ML.isDirectory()) {
            File[] files = ML.listFiles();

            for (File f : files)
                createNewPic(f, backGroundPicPath, targetPicPath);
        } else if (ML.isFile())
            createNewPic(ML, backGroundPicPath, targetPicPath);
        else
            log.info("原路径格式有误");
    }

    /***
     * 图片类型格式转换
     * @param sourceImagePath 图片源路径
     * @param desImagePath 图片目标路径
     */
    public static void imageTypeConvert(String sourceImagePath, String desImagePath) {
        try {
            File sourceFile = new File(sourceImagePath);
            BufferedImage bufferedImage = ImageIO.read(sourceFile);
            // create a blank, RGB, same width and height, and a white
            // background
            BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
            // write to jpeg file
            ImageIO.write(newBufferedImage, "jpg", new File(desImagePath));

            // 转换成功后，删除源文件
            if (sourceFile.isFile())
                sourceFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String sourcePath = "E:\\test\\652KJ1820000F44\\target\\6.png";
        String destPath = "E:\\test\\652KJ1820000F44\\target\\6.jpg";
        imageTypeConvert(sourcePath, destPath);
    }

}