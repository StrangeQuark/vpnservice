package com.strangequark.vpnservice.utility;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class QrCodeUtilityTest {
    @Test
    public void createQrCodeReturnsPngDataUri() throws Exception {
        String configuration = "[Interface]\nPrivateKey = test-key";
        String qrCode = new QrCodeUtility().createQrCode(configuration);
        String encodedImage = qrCode.substring("data:image/png;base64,".length());
        BufferedImage qrCodeImage = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(encodedImage)));

        assertTrue(qrCode.startsWith("data:image/png;base64,"));
        assertNotNull(qrCodeImage);
        assertEquals(configuration, new MultiFormatReader().decode(new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(qrCodeImage)))).getText());
    }
}
