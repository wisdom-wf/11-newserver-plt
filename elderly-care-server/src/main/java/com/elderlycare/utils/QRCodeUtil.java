package com.elderlycare.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * 二维码工具类（ZXing）— 带品牌标识
 */
public class QRCodeUtil {

    private static final int IMG_WIDTH = 400;
    private static final int IMG_HEIGHT = 480;
    private static final int QR_SIZE = 260;
    private static final int HEADER_HEIGHT = 70;
    private static final int FOOTER_HEIGHT = 50;
    private static final Color BRAND_BLUE = new Color(30, 58, 95);
    private static final Color BG_WHITE = Color.WHITE;
    private static final Color TEXT_GRAY = new Color(120, 120, 120);

    /**
     * 生成纯二维码 PNG（兼容旧接口）
     */
    public static byte[] generatePng(String content, int width, int height) throws WriterException, IOException {
        return generateBrandedPng(content);
    }

    /**
     * 生成带品牌标识的二维码 PNG
     *
     * 布局:
     * ┌─────────────────────┐
     * │  智慧养老 · 预约服务  │  ← 蓝色标题栏 (400×70)
     * ├─────────────────────┤
     * │                     │
     * │     [QR CODE]       │  ← 白色背景区 (400×360)
     * │                     │
     * │   扫码即可在线预约    │  ← 灰色提示文字
     │                     │
     * └─────────────────────┘
     * 总尺寸: 400×480px
     */
    public static byte[] generateBrandedPng(String content) throws WriterException, IOException {
        // 1. 生成二维码矩阵
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);

        // 2. 将 BitMatrix 转为 BufferedImage（黑码白底）
        MatrixToImageConfig config = new MatrixToImageConfig(0xFF000000, 0x00FFFFFF);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(matrix, config);

        // 3. 创建画布
        BufferedImage canvas = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        // 4. 绘制白色背景
        g.setColor(BG_WHITE);
        g.fillRect(0, 0, IMG_WIDTH, IMG_HEIGHT);

        // 5. 绘制蓝色标题栏（上半部分圆角）
        g.setColor(BRAND_BLUE);
        RoundRectangle2D headerBg = new RoundRectangle2D.Float(0, 0, IMG_WIDTH, HEADER_HEIGHT + 20, 0, 0);
        g.fill(headerBg);

        // 6. 绘制标题文字 "智慧养老 · 预约服务"
        g.setColor(Color.WHITE);
        Font titleFont = getChineseFont(Font.BOLD, 24);
        g.setFont(titleFont);
        String title = "智慧养老 · 预约服务";
        FontMetrics fm = g.getFontMetrics();
        int titleX = (IMG_WIDTH - fm.stringWidth(title)) / 2;
        int titleY = HEADER_HEIGHT / 2 + fm.getAscent() / 2 - 4;
        g.drawString(title, titleX, titleY);

        // 7. 绘制二维码（带白色背景卡片 + 阴影效果）
        int qrX = (IMG_WIDTH - QR_SIZE) / 2;
        int qrY = HEADER_HEIGHT + 25;

        // 卡片阴影
        g.setColor(new Color(0, 0, 0, 15));
        g.fillRoundRect(qrX - 8 + 3, qrY - 8 + 3, QR_SIZE + 16, QR_SIZE + 16, 16, 16);
        // 卡片背景
        g.setColor(BG_WHITE);
        g.fillRoundRect(qrX - 8, qrY - 8, QR_SIZE + 16, QR_SIZE + 16, 16, 16);
        // 卡片边框
        g.setColor(new Color(230, 230, 230));
        g.setStroke(new BasicStroke(1));
        g.drawRoundRect(qrX - 8, qrY - 8, QR_SIZE + 16, QR_SIZE + 16, 16, 16);

        // 8. 绘制二维码图片
        g.drawImage(qrImage, qrX, qrY, null);

        // 9. 绘制底部提示文字
        g.setColor(TEXT_GRAY);
        Font tipFont = getChineseFont(Font.PLAIN, 15);
        g.setFont(tipFont);
        String tip = "扫码即可在线预约";
        FontMetrics tipFm = g.getFontMetrics();
        int tipX = (IMG_WIDTH - tipFm.stringWidth(tip)) / 2;
        int tipY = qrY + QR_SIZE + 16 + tipFm.getAscent();
        g.drawString(tip, tipX, tipY);

        g.dispose();

        // 10. 输出 PNG
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(canvas, "PNG", out);
        return out.toByteArray();
    }

    /**
     * 获取中文字体，优先 Noto Sans CJK，fallback 到 SansSerif
     */
    private static Font getChineseFont(int style, int size) {
        // 尝试 Noto Sans CJK SC（服务器有 Noto Serif CJK）
        String[] fontNames = {
            "Noto Sans CJK SC", "Noto Serif CJK SC",
            "WenQuanYi Micro Hei", "WenQuanYi Zen Hei",
            "Microsoft YaHei", "SimHei",
            "PingFang SC", "Hiragino Sans GB"
        };
        for (String name : fontNames) {
            Font f = new Font(name, style, size);
            if (f.canDisplay('智')) {
                return f;
            }
        }
        // fallback
        return new Font(Font.SANS_SERIF, style, size);
    }
}
