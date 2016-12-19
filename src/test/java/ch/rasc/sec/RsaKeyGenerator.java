package ch.rasc.sec;

import ch.rasc.sec.cipher.RSA;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

/**
 * Created by 9480 on 12/13/2016.
 */
public class RsaKeyGenerator {

    private static BASE64Encoder encoder = new BASE64Encoder();
    private static BASE64Decoder decoder = new BASE64Decoder();
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPair rsaKP= RSA.generateKeyPair();
        System.out.println(new BigInteger(rsaKP.getPublic().getEncoded())+"\n\n"+encoder.encode(rsaKP.getPrivate().getEncoded()));
        Scanner in = new Scanner(System.in);
        String filePath = in.next();
        FileOutputStream fos = new FileOutputStream(filePath);
        fos.write(rsaKP.getPrivate().getEncoded());
        fos.close();


        /*String publicKey = in.next();
        byte[] encr = RSA.encrypt("Privet".getBytes(), KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(new BigInteger(publicKey).toByteArray())));


        byte[] encodedPrivateKEy = Files.readAllBytes(Paths.get(filePath));
        System.out.println(new String(RSA.decrypt(encr,KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encodedPrivateKEy))/*rsaKP.getPrivate())));
        */in.close();


    }
}
