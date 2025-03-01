[![Maven Central](https://img.shields.io/maven-central/v/com.ajaxjs/aj-qrcode?label=Latest%20Release)](https://central.sonatype.com/artifact/com.ajaxjs/aj-qrcode)
[![Javadoc](https://img.shields.io/badge/javadoc-1.0-brightgreen.svg?)](https://dev.ajaxjs.com/docs/javadoc/aj-qrcode/)
[![License](https://img.shields.io/badge/license-Apache--2.0-green.svg?longCache=true&style=flat)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[![Email](https://img.shields.io/badge/Contact--me-Email-orange.svg)](mailto:frank@ajaxjs.com)
[![QQ群](https://framework.ajaxjs.com/static/qq.svg)](https://shang.qq.com/wpa/qunwpa?idkey=3877893a4ed3a5f0be01e809e7ac120e346102bd550deb6692239bb42de38e22)

# QR Code Generator
Forks from https://github.com/nayuki/QR-Code-generator. Puts normal version and fast version in one project.

Tutorial: https://zhangxin.blog.csdn.net/article/details/139821806.

Java Documents: https://dev.ajaxjs.com/docs/javadoc/aj-qrcode/.

## Install
```xml
<dependency>
    <groupId>com.ajaxjs</groupId>
    <artifactId>aj-qrcode</artifactId>
    <version>1.0</version>
</dependency>
```

## Usage


```java
String text = "Hello, world!";          // User-supplied Unicode text
Ecc errCorLvl = Ecc.LOW;  // Error correction level
QrCode qr = QrCode.encodeText(text, errCorLvl);  // Make the QR Code symbol

BufferedImage img = Utils.toImage(qr, 10, 4);          // Convert to bitmap image
File imgFile = new File("hello-world-QR.png");   // File path for output
ImageIO.write(img, "png", imgFile);              // Write image to file

String svg = Utils.toSvgString(qr, 4, "#FFFFFF", "#000000");  // Convert to SVG XML code
File svgFile = new File("c:\\temp\\hello-world-QR.svg");          // File path for output
Files.write(svgFile.toPath(), svg.getBytes(StandardCharsets.UTF_8)); // Write image to file
```

