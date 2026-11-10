package com.example.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;

public class CodigoQR {

    // Generar un código QR
    public static byte[] generarQR(String texto) throws Exception {

        Map<EncodeHintType, Object> opciones = new HashMap<>();
        opciones.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix matriz = new MultiFormatWriter().encode(
            texto,
            BarcodeFormat.QR_CODE,
            300,
            300,
            opciones
        );

        BufferedImage imagen = MatrixToImageWriter.toBufferedImage(matriz);

        ByteArrayOutputStream salida = new ByteArrayOutputStream();

        ImageIO.write(imagen, "PNG", salida);

        return salida.toByteArray();
    }

    // Leer un código QR desde una imagen
    public static String leerQR(InputStream archivo) throws Exception {

        BufferedImage imagen = ImageIO.read(archivo);

        if (imagen == null) {
            throw new Exception("No se pudo leer la imagen");
        }

        var fuente = new BufferedImageLuminanceSource(imagen);

        var binario = new HybridBinarizer(fuente);

        var bitmap = new BinaryBitmap(binario);

        Result resultado = new MultiFormatReader().decode(bitmap);

        return resultado.getText();
    }
}
