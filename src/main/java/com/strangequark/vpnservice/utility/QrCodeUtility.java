package com.strangequark.vpnservice.utility;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class QrCodeUtility {
    public String createQrCode(String configuration) {
        try {
            BufferedImage qrCode = MatrixToImageWriter.toBufferedImage(
                    new QRCodeWriter().encode(configuration, BarcodeFormat.QR_CODE, 400, 400));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(qrCode, "PNG", outputStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch(Exception ex) {
            throw new RuntimeException("Unable to generate VPN configuration QR code");
        }
    }
}
