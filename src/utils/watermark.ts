/**
 * 图片水印工具
 */

/**
 * 图片添加水印
 * @param base64Img 原始 base64 图片
 * @param watermarkText 水印文本
 * @returns 添加水印后的 base64 图片
 */
export async function addWatermarkToImage(
  base64Img: string,
  watermarkText: string
): Promise<string> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onload = () => {
      const canvas = document.createElement('canvas');
      canvas.width = img.width;
      canvas.height = img.height;
      const ctx = canvas.getContext('2d')!;

      // 绘制原图
      ctx.drawImage(img, 0, 0);

      // 设置水印样式
      const fontSize = Math.max(16, img.width / 40);
      ctx.fillStyle = 'rgba(255, 255, 255, 0.6)';
      ctx.strokeStyle = 'rgba(0, 0, 0, 0.3)';
      ctx.font = `${fontSize}px sans-serif`;
      ctx.textAlign = 'right';
      ctx.textBaseline = 'bottom';

      // 绘制描边
      ctx.lineWidth = fontSize / 8;
      const lines = watermarkText.split('\n');
      const lineHeight = fontSize * 1.5;
      const startY = img.height - 20;

      lines.forEach((line, i) => {
        const y = startY - i * lineHeight;
        ctx.strokeText(line, img.width - 20, y);
        ctx.fillText(line, img.width - 20, y);
      });

      resolve(canvas.toDataURL('image/jpeg', 0.9));
    };
    img.onerror = reject;
    img.src = base64Img;
  });
}

/**
 * 下载图片（带水印）
 * @param base64Url 原始 base64 图片
 * @param filename 下载文件名
 * @param watermarkText 水印文本
 */
export async function downloadImageWithWatermark(
  base64Url: string,
  filename: string,
  watermarkText: string
): Promise<void> {
  const watermarkedBase64 = await addWatermarkToImage(base64Url, watermarkText);

  const link = document.createElement('a');
  link.href = watermarkedBase64;
  link.download = filename;
  link.click();
}
