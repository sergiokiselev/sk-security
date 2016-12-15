package ch.rasc.sec;

import ch.rasc.sec.cipher.AES;
import ch.rasc.sec.cipher.DiffieHellman;
import ch.rasc.sec.cipher.RSA;
import ch.rasc.sec.dto.*;
import ch.rasc.sec.dto.restresponse.RestResponse;
import ch.rasc.sec.service.TOTPService;
import ch.rasc.sec.service.impl.TOTPServiceImpl;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class TestGoogleAuthApplication {

    private static BASE64Encoder encoder = new BASE64Encoder();
    private static BASE64Decoder decoder = new BASE64Decoder();
    private static RestTemplate restTemplate = new RestTemplate();

    private static SecretKey secretKey;
    private static byte[] ivector;

    public static void main(String[] args)
            throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InterruptedException, InvalidKeySpecException {
        testFullCryptography();
        //testWithoutPostCode();
       // testWithoutAll();
//        totpTest();
        //testFileUpload();
        //testGetFiles();
    }

    private static void totpTest() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        String secret;
        while (true) {
            Scanner sc = new Scanner(System.in);
            secret = sc.nextLine();
            byte[] bytes = new BASE64Decoder().decodeBuffer(secret);
            System.out.println(getCurrentCode(bytes));
        }
    }

    private static void testWithoutAll() throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeyException {
        LoginDto rsaKeyDto = new LoginDto();
        rsaKeyDto.setEncryption(false);
        rsaKeyDto.setPostCode(false);
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter login:");
        //
        String login = scanner.nextLine();
        rsaKeyDto.setLogin(login);
        HttpEntity<LoginDto> encodedEntity = new HttpEntity<>(rsaKeyDto);
        ResponseEntity<RestResponse<AesKeyPartDto>> keyDto = restTemplate
                .exchange(TestUtils.getUrl("logindto"),
                        HttpMethod.POST,
                        encodedEntity,
                        new ParameterizedTypeReference<RestResponse<AesKeyPartDto>>() {
                        });

        UserDto userDto = new UserDto();
        userDto.setSessionId(keyDto.getBody().getData().getSessionId());
        //sergio.kiselev509@gmail.com
        System.out.println("Enter password:");
        //password
        String password = scanner.nextLine();
        userDto.setLogin(encoder.encode(login.getBytes()));
        userDto.setPassword(encoder.encode(password.getBytes()));

        HttpEntity<UserDto> userDtoHttpEntity = new HttpEntity<>(userDto);
        ResponseEntity<RestResponse<VerifyDto>> verifyDto = restTemplate
                .exchange(TestUtils.getUrl("login"),
                        HttpMethod.POST,
                        userDtoHttpEntity,
                        new ParameterizedTypeReference<RestResponse<VerifyDto>>() {
                        });

        System.out.println(verifyDto.getBody().getData());
        byte[] secretBytes = decoder.decodeBuffer(verifyDto.getBody().getData().getSecret());
        verifyToken(verifyDto.getBody().getData().getSessionId(), secretBytes);
    }

    private static void testWithoutPostCode() throws IOException, InterruptedException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidKeySpecException {
        //KeyPair keyPair = RSA.generateKeyPair();
        LoginDto loginDto = new LoginDto();
        //String encoded = encoder.encode(keyPair.getPublic().getEncoded());

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter login:");
        //
        String login = scanner.nextLine();
        loginDto.setLogin(login);
        loginDto.setEncryption(true);
        loginDto.setPostCode(false);
        HttpEntity<LoginDto> encodedEntity = new HttpEntity<>(loginDto);
        ResponseEntity<RestResponse<AesKeyPartDto>> keyDto = restTemplate
                .exchange(TestUtils.getUrl("logindto"),
                        HttpMethod.POST,
                        encodedEntity,
                        new ParameterizedTypeReference<RestResponse<AesKeyPartDto>>() {
                        });

        System.out.println("Enter private key path:");
        //
        String filepath = scanner.nextLine();
        PrivateKey rsaPrivateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(Paths.get(filepath))));


        PublicKey dhServerKey = DiffieHellman.getPublicKeyDecoded(mergeArrays(RSA.decrypt(decoder.decodeBuffer(keyDto.getBody().getData().getDhPublicPart1()),rsaPrivateKey),RSA.decrypt(decoder.decodeBuffer(keyDto.getBody().getData().getDhPublicPart2()),rsaPrivateKey)));
        PublicKey rsaServerKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(mergeArrays(RSA.decrypt(decoder.decodeBuffer(keyDto.getBody().getData().getRsaPublicPart1()),rsaPrivateKey),RSA.decrypt(decoder.decodeBuffer(keyDto.getBody().getData().getRsaPublicPart2()),rsaPrivateKey))));
        DiffieHellman.setDHParamSpec(((DHPublicKey)dhServerKey).getParams());
        KeyPair dhKeyPair = DiffieHellman.generateKeyPair();
        KeyAgreement dhKeyAgreement = DiffieHellman.generateKeyAgreement(dhKeyPair);
        AesKeyPartDto clientDHdto = new AesKeyPartDto();
        clientDHdto.setSessionId(keyDto.getBody().getData().getSessionId());
        clientDHdto.setDhPublicPart1(encoder.encode(RSA.encrypt(Arrays.copyOfRange(dhKeyPair.getPublic().getEncoded(),0,200), rsaServerKey)));
        clientDHdto.setDhPublicPart2(encoder.encode(RSA.encrypt(Arrays.copyOfRange(dhKeyPair.getPublic().getEncoded(),200,dhKeyPair.getPublic().getEncoded().length), rsaServerKey)));
        /*byte[] rsaEncryptedSecretKey = decoder.decodeBuffer(keyDto.getBody().getData().getAesKey());
        byte[] encryptedIvector = decoder.decodeBuffer(keyDto.getBody().getData().getIvector());
        byte[] secretKeyBytes = RSA.decrypt(rsaEncryptedSecretKey, keyPair.getPrivate());
        ivector = RSA.decrypt(encryptedIvector, keyPair.getPrivate());
        secretKey = new SecretKeySpec(secretKeyBytes, "AES");*/
        byte[] sharedSecret = DiffieHellman.getSharedSecret(dhKeyAgreement,dhServerKey);
        secretKey = DiffieHellman.getAESSecretKey(sharedSecret);
        HttpEntity<AesKeyPartDto> dhEncodedEntity = new HttpEntity<>(clientDHdto);
        ResponseEntity<RestResponse<IVectorDto>> ivectorDto = restTemplate
                .exchange(TestUtils.getUrl("dh"),
                        HttpMethod.POST,
                        dhEncodedEntity,
                        new ParameterizedTypeReference<RestResponse<IVectorDto>>() {
                        });
        ivector = RSA.decrypt(decoder.decodeBuffer(ivectorDto.getBody().getData().getIvector()),rsaPrivateKey);
        ResponseEntity<RestResponse<VerifyDto>> verifyDto = login(keyDto, ivector, secretKey, scanner);
        System.out.println(verifyDto.getBody().getData());
        byte[] secretBytes = AES.decrypt(decoder.decodeBuffer(verifyDto.getBody().getData().getSecret()), secretKey, new IvParameterSpec(ivector));
        verifyTokenEncrypted(verifyDto.getBody().getData().getSessionId(), secretBytes);
    }

    private static byte[] mergeArrays(byte[] arr1, byte[] arr2){
        byte[] newArr = new byte[arr1.length+arr2.length];
        System.arraycopy(arr1,0,newArr,0,arr1.length);
        System.arraycopy(arr2,0,newArr,arr1.length,arr2.length);
        return newArr;
    }

    private static void testFullCryptography() throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InterruptedException, InvalidKeySpecException {
        //KeyPair keyPair = RSA.generateKeyPair();
        LoginDto loginDto = new LoginDto();
        //String encoded = encoder.encode(keyPair.getPublic().getEncoded());

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter login:");
        //
        String login = scanner.nextLine();
        loginDto.setLogin(login);
        loginDto.setEncryption(true);
        loginDto.setPostCode(true);
        HttpEntity<LoginDto> encodedEntity = new HttpEntity<>(loginDto);
        ResponseEntity<RestResponse<AesKeyPartDto>> keyDto = restTemplate
                .exchange(TestUtils.getUrl("logindto"),
                        HttpMethod.POST,
                        encodedEntity,
                        new ParameterizedTypeReference<RestResponse<AesKeyPartDto>>() {
                        });

        System.out.println("Enter private key path:");
        //
        String filepath = scanner.nextLine();
        PrivateKey rsaPrivateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Files.readAllBytes(Paths.get(filepath))));


        PublicKey dhServerKey = DiffieHellman.getPublicKeyDecoded(mergeArrays(RSA.decrypt(decoder.decodeBuffer(keyDto.getBody().getData().getDhPublicPart1()),rsaPrivateKey),RSA.decrypt(decoder.decodeBuffer(keyDto.getBody().getData().getDhPublicPart2()),rsaPrivateKey)));
        PublicKey rsaServerKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(mergeArrays(RSA.decrypt(decoder.decodeBuffer(keyDto.getBody().getData().getRsaPublicPart1()),rsaPrivateKey),RSA.decrypt(decoder.decodeBuffer(keyDto.getBody().getData().getRsaPublicPart2()),rsaPrivateKey))));
        DiffieHellman.setDHParamSpec(((DHPublicKey)dhServerKey).getParams());
        KeyPair dhKeyPair = DiffieHellman.generateKeyPair();
        KeyAgreement dhKeyAgreement = DiffieHellman.generateKeyAgreement(dhKeyPair);
        AesKeyPartDto clientDHdto = new AesKeyPartDto();
        clientDHdto.setSessionId(keyDto.getBody().getData().getSessionId());
        clientDHdto.setDhPublicPart1(encoder.encode(RSA.encrypt(Arrays.copyOfRange(dhKeyPair.getPublic().getEncoded(),0,200), rsaServerKey)));
        clientDHdto.setDhPublicPart2(encoder.encode(RSA.encrypt(Arrays.copyOfRange(dhKeyPair.getPublic().getEncoded(),200,dhKeyPair.getPublic().getEncoded().length), rsaServerKey)));
        /*byte[] rsaEncryptedSecretKey = decoder.decodeBuffer(keyDto.getBody().getData().getAesKey());
        byte[] encryptedIvector = decoder.decodeBuffer(keyDto.getBody().getData().getIvector());
        byte[] secretKeyBytes = RSA.decrypt(rsaEncryptedSecretKey, keyPair.getPrivate());
        ivector = RSA.decrypt(encryptedIvector, keyPair.getPrivate());
        secretKey = new SecretKeySpec(secretKeyBytes, "AES");*/
        byte[] sharedSecret = DiffieHellman.getSharedSecret(dhKeyAgreement,dhServerKey);
        secretKey = DiffieHellman.getAESSecretKey(sharedSecret);
        HttpEntity<AesKeyPartDto> dhEncodedEntity = new HttpEntity<>(clientDHdto);
        ResponseEntity<RestResponse<IVectorDto>> ivectorDto = restTemplate
                .exchange(TestUtils.getUrl("dh"),
                        HttpMethod.POST,
                        dhEncodedEntity,
                        new ParameterizedTypeReference<RestResponse<IVectorDto>>() {
                        });
        ivector = RSA.decrypt(decoder.decodeBuffer(ivectorDto.getBody().getData().getIvector()),rsaPrivateKey);
        ResponseEntity<RestResponse<VerifyDto>> verifyDto = login(keyDto, ivector, secretKey, scanner);
        System.out.println(verifyDto.getBody().getData());
        ResponseEntity<RestResponse<TotpSecretDto>> totpSecretDto = verifyCode(ivector, secretKey, scanner, verifyDto);
        byte[] secretBytes = AES.decrypt(decoder.decodeBuffer(totpSecretDto.getBody().getData().getSecret()), secretKey, new IvParameterSpec(ivector));
        System.out.println(secretBytes);
        verifyTokenEncrypted(totpSecretDto.getBody().getData().getSessionId(), secretBytes);
    }

    private static ResponseEntity<RestResponse<TotpSecretDto>> verifyCode(byte[] ivector, SecretKey secretKey, Scanner scanner, ResponseEntity<RestResponse<VerifyDto>> verifyDto)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        System.out.println("Enter code:");
        String code = scanner.nextLine();
        VerifyDto verifyDto1 = new VerifyDto();
        verifyDto1.setSessionId(verifyDto.getBody().getData().getSessionId());
        verifyDto1.setCode(encoder.encode(AES.encrypt(code.getBytes(), secretKey, new IvParameterSpec(ivector))));

        HttpEntity<VerifyDto> verifyDtoHttpEntity = new HttpEntity<>(verifyDto1);
        ResponseEntity<RestResponse<TotpSecretDto>> totpSecretDto = restTemplate
                .exchange(TestUtils.getUrl("verify"),
                        HttpMethod.POST,
                        verifyDtoHttpEntity,
                        new ParameterizedTypeReference<RestResponse<TotpSecretDto>>() {
                        });
        System.out.println(totpSecretDto.getBody().getData());
        return totpSecretDto;
    }

    private static ResponseEntity<RestResponse<VerifyDto>> login(ResponseEntity<RestResponse<AesKeyPartDto>> keyDto, byte[] ivector, SecretKey secretKey, Scanner scanner)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        UserDto userDto = new UserDto();
        userDto.setSessionId(keyDto.getBody().getData().getSessionId());
        System.out.println("Enter login:");
        //
        String login = scanner.nextLine();
        System.out.println("Enter password:");
        //password
        String password = scanner.nextLine();
        userDto.setLogin(encoder.encode(AES.encrypt(login.getBytes(), secretKey, new IvParameterSpec(ivector))));
        userDto.setPassword(encoder.encode(AES.encrypt(password.getBytes(), secretKey, new IvParameterSpec(ivector))));

        HttpEntity<UserDto> userDtoHttpEntity = new HttpEntity<>(userDto);
        return restTemplate
                .exchange(TestUtils.getUrl("login"),
                        HttpMethod.POST,
                        userDtoHttpEntity,
                        new ParameterizedTypeReference<RestResponse<VerifyDto>>() {
                        });
    }

    private static void verifyToken(String sessionId, byte[] secretBytes)
            throws InvalidKeyException, NoSuchAlgorithmException, InterruptedException, IOException {

        long totpCode = getCurrentCode(secretBytes);
        System.out.println("CUrrent CODE: " + totpCode);
        TokenDto tokenDto = new TokenDto();
        tokenDto.setSessionId(sessionId);
        tokenDto.setToken(totpCode);
        System.out.println(tokenDto);
        HttpEntity<TokenDto> tokenDtoHttpEntity = new HttpEntity<>(tokenDto);
        ResponseEntity<RestResponse<String>> responseEntity = restTemplate
                .exchange(TestUtils.getUrl("token"),
                        HttpMethod.POST,
                        tokenDtoHttpEntity,
                        new ParameterizedTypeReference<RestResponse<String>>() {
                        });
        System.out.println(responseEntity.getBody().getData());

        File file = new File("src\\main\\resources\\download\\gopnik.jpeg");
        FileInputStream inputStream = new FileInputStream(file);
        byte[] fileBytes = new byte[(int) file.length()];
        inputStream.read(fileBytes);
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(TestUtils.getUrl("files?name=" + file.getName() +
                "&sessionId=" + sessionId + "&token=" + totpCode));

        FileBody uploadFilePart = new FileBody(file);
        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("file", uploadFilePart);
        httpPost.setEntity(reqEntity);
        HttpResponse response = httpclient.execute(httpPost);

        ResponseEntity<RestResponse<List<FileDescriptorDto>>> keyDto = restTemplate
                .exchange(TestUtils.getUrl("files?sessionId=" + sessionId + "&token=" + totpCode),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<RestResponse<List<FileDescriptorDto>>>() {
                        });
        System.out.println(keyDto.getBody().getData().size());
        System.out.println(keyDto);
        FileDescriptorDto descriptorDto = keyDto.getBody().getData().get(0);

        /*HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(TestUtils.getUrl("files/" + descriptorDto.getGoogleId() +
                "?sessionId=" + sessionId + "&token=" + totpCode));
        HttpResponse response1 = client.execute(get);
        InputStream stream = response1.getEntity().getContent();
        byte[] aaaa = new byte[(int) response1.getEntity().getContentLength()];
        stream.read(aaaa);
        FileOutputStream stream1 = new FileOutputStream("kek2.jpeg");
        stream1.write(aaaa);
        System.out.println(aaaa.length);*/

        ResponseEntity<RestResponse<FileContentDto>> fileContent = restTemplate
                .exchange(TestUtils.getUrl("files/"+descriptorDto.getGoogleId()+/*encoder.encode(AES.encrypt(descriptorDto.getGoogleId().getBytes(),secretKey,new IvParameterSpec(ivector)))*/
                                "?sessionId=" + sessionId + "&token=" + totpCode),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<RestResponse<FileContentDto>>() {
                        });
        byte[] aaaa =decoder.decodeBuffer(fileContent.getBody().getData().getContent());
        FileOutputStream stream1 = new FileOutputStream("kek2.jpeg");
        stream1.write(aaaa);

        /*restTemplate.delete(TestUtils.getUrl("files/" + descriptorDto.getGoogleId() +
                "?sessionId=" + sessionId + "&token=" + totpCode));*/
    }

    private static long getCurrentCode(byte[] secretBytes) throws InvalidKeyException, NoSuchAlgorithmException {
        long timeIndex = System.currentTimeMillis() / 1000 / 30;
        TOTPService totpService = new TOTPServiceImpl();
        long totpCode = totpService.getCode(secretBytes, timeIndex);
        System.out.println(totpCode);
        return totpCode;
    }


    private static void verifyTokenEncrypted(String sessionId, byte[] secretBytes)
            throws InvalidKeyException, NoSuchAlgorithmException, InterruptedException, IOException, InvalidAlgorithmParameterException, NoSuchPaddingException {

        long totpCode = getCurrentCode(secretBytes);
        System.out.println("CUrrent CODE: " + totpCode);
        TokenDto tokenDto = new TokenDto();
        tokenDto.setSessionId(sessionId);
        tokenDto.setToken(totpCode);
        System.out.println(tokenDto);
        HttpEntity<TokenDto> tokenDtoHttpEntity = new HttpEntity<>(tokenDto);
        ResponseEntity<RestResponse<String>> responseEntity = restTemplate
                .exchange(TestUtils.getUrl("token"),
                        HttpMethod.POST,
                        tokenDtoHttpEntity,
                        new ParameterizedTypeReference<RestResponse<String>>() {
                        });
        System.out.println(responseEntity.getBody().getData());

        File file = new File("src\\main\\resources\\download\\gopnik.jpeg");
        File fileTemp = File.createTempFile("tmpfiletosend", file.getName());
        FileInputStream inputStream = new FileInputStream(file);
        byte[] fileBytes = new byte[(int) file.length()];
        inputStream.read(fileBytes);
        FileOutputStream outputStream = new FileOutputStream(fileTemp);
        outputStream.write(AES.encrypt(fileBytes, secretKey, new IvParameterSpec(ivector)));
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(TestUtils.getUrl("files?name=" + file.getName() +
                "&sessionId=" + sessionId + "&token=" + totpCode));

        FileBody uploadFilePart = new FileBody(fileTemp);
        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("file", uploadFilePart);
        httpPost.setEntity(reqEntity);
        HttpResponse response = httpclient.execute(httpPost);

        ResponseEntity<RestResponse<List<FileDescriptorDto>>> keyDto = restTemplate
                .exchange(TestUtils.getUrl("files?sessionId=" + sessionId + "&token=" + totpCode),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<RestResponse<List<FileDescriptorDto>>>() {
                        });
        System.out.println(keyDto.getBody().getData().size());
        System.out.println(keyDto);
        FileDescriptorDto descriptorDto = decryptFileDescriptorDto(keyDto.getBody().getData().get(0));
        System.out.println("FDD "+descriptorDto.getGoogleId()+" "+descriptorDto.getName());

        /*HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(TestUtils.getUrl("files/" + encoder.encode(AES.encrypt(descriptorDto.getGoogleId().getBytes(),secretKey,new IvParameterSpec(ivector))) +
                "?sessionId=" + sessionId + "&token=" + totpCode));
        HttpResponse response1 = client.execute(get);
        InputStream stream = response1.getEntity().getContent();
        System.out.println((int)response1.getEntity().getContentLength());
        byte[] aaaa = new byte[(int) response1.getEntity().getContentLength()];
        stream.read(aaaa);
        FileOutputStream stream1 = new FileOutputStream("kek2.jpeg");
        stream1.write(aaaa);
        stream1.write(AES.decrypt(aaaa,secretKey,new IvParameterSpec(ivector)));
        System.out.println(aaaa.length);*/

        ResponseEntity<RestResponse<FileContentDto>> fileContent = restTemplate
                .exchange(TestUtils.getUrl("files/"+descriptorDto.getGoogleId()+//encoder.encode(AES.encrypt(descriptorDto.getGoogleId().getBytes(),secretKey,new IvParameterSpec(ivector)))
                        "?sessionId=" + sessionId + "&token=" + totpCode),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<RestResponse<FileContentDto>>() {
                        });
        byte[] aaaa =decoder.decodeBuffer(fileContent.getBody().getData().getContent());
        FileOutputStream stream1 = new FileOutputStream("kek2.jpeg");
        stream1.write(AES.decrypt(aaaa,secretKey,new IvParameterSpec(ivector)));

        restTemplate.delete(TestUtils.getUrl("files/" +descriptorDto.getGoogleId()+//encoder.encode(AES.encrypt(descriptorDto.getGoogleId().getBytes(),secretKey,new IvParameterSpec(ivector))) +
              "?sessionId=" + sessionId + "&token=" + totpCode));
    }

    private static FileDescriptorDto decryptFileDescriptorDto(FileDescriptorDto fdd) throws IOException {
        fdd.setGoogleId(new String(AES.decrypt(decoder.decodeBuffer(fdd.getGoogleId()), secretKey,new IvParameterSpec(ivector))));
        fdd.setLink(new String(AES.decrypt(decoder.decodeBuffer(fdd.getLink()), secretKey,new IvParameterSpec(ivector))));;
        fdd.setName(new String(AES.decrypt(decoder.decodeBuffer(fdd.getName()), secretKey,new IvParameterSpec(ivector))));
        return fdd;
    }
}
