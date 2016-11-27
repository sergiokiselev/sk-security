package ch.rasc.sec;

import ch.rasc.sec.cypher.AES;
import ch.rasc.sec.cypher.RSA;
import ch.rasc.sec.dto.*;
import ch.rasc.sec.dto.restresponse.RestResponse;
import ch.rasc.sec.service.TOTPService;
import ch.rasc.sec.service.impl.TOTPServiceImpl;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Scanner;

public class TestGoogleAuthApplication {

    private static BASE64Encoder encoder = new BASE64Encoder();
    private static BASE64Decoder decoder = new BASE64Decoder();
    private static RestTemplate restTemplate = new RestTemplate();

    private static SecretKey secretKey;
    private static byte[] ivector;

    public static void main(String[] args)
            throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InterruptedException {
        //testFullCryptography();
        testWithoutPostCode();
        //testWithoutAll();
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
        RsaKeyDto rsaKeyDto = new RsaKeyDto();
        rsaKeyDto.setEncryption(false);
        rsaKeyDto.setPostCode(false);
        HttpEntity<RsaKeyDto> encodedEntity = new HttpEntity<>(rsaKeyDto);
        ResponseEntity<RestResponse<AesKeyDto>> keyDto = restTemplate
                .exchange(TestUtils.getUrl("rsakey"),
                        HttpMethod.POST,
                        encodedEntity,
                        new ParameterizedTypeReference<RestResponse<AesKeyDto>>() {
                        });

        UserDto userDto = new UserDto();
        userDto.setSessionId(keyDto.getBody().getData().getSessionId());
        System.out.println("Enter login:");
        Scanner scanner = new Scanner(System.in);
        //sergio.kiselev509@gmail.com
        String login = scanner.nextLine();
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

    private static void testWithoutPostCode() throws IOException, InterruptedException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        KeyPair keyPair = RSA.generateKeyPair();
        RsaKeyDto rsaKeyDto = new RsaKeyDto();
        String encoded = encoder.encode(keyPair.getPublic().getEncoded());

        rsaKeyDto.setRsaKey(encoded);
        rsaKeyDto.setEncryption(true);
        rsaKeyDto.setPostCode(false);
        HttpEntity<RsaKeyDto> encodedEntity = new HttpEntity<>(rsaKeyDto);
        ResponseEntity<RestResponse<AesKeyDto>> keyDto = restTemplate
                .exchange(TestUtils.getUrl("rsakey"),
                        HttpMethod.POST,
                        encodedEntity,
                        new ParameterizedTypeReference<RestResponse<AesKeyDto>>() {
                        });

        byte[] rsaEncryptedSecretKey = decoder.decodeBuffer(keyDto.getBody().getData().getAesKey());
        byte[] encryptedIvector = decoder.decodeBuffer(keyDto.getBody().getData().getIvector());
        byte[] secretKeyBytes = RSA.decrypt(rsaEncryptedSecretKey, keyPair.getPrivate());
        ivector = RSA.decrypt(encryptedIvector, keyPair.getPrivate());
        secretKey = new SecretKeySpec(secretKeyBytes, "AES");
        Scanner scanner = new Scanner(System.in);
        ResponseEntity<RestResponse<VerifyDto>> verifyDto = login(keyDto, ivector, secretKey, scanner);
        System.out.println(verifyDto.getBody().getData());
        byte[] secretBytes = AES.decrypt(decoder.decodeBuffer(verifyDto.getBody().getData().getSecret()), secretKey, new IvParameterSpec(ivector));
        verifyTokenEncrypted(verifyDto.getBody().getData().getSessionId(), secretBytes);
    }

    private static void testFullCryptography() throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InterruptedException {
        KeyPair keyPair = RSA.generateKeyPair();
        RsaKeyDto rsaKeyDto = new RsaKeyDto();
        String encoded = encoder.encode(keyPair.getPublic().getEncoded());

        rsaKeyDto.setRsaKey(encoded);
        rsaKeyDto.setEncryption(true);
        rsaKeyDto.setPostCode(true);
        HttpEntity<RsaKeyDto> encodedEntity = new HttpEntity<>(rsaKeyDto);
        ResponseEntity<RestResponse<AesKeyDto>> keyDto = restTemplate
                .exchange(TestUtils.getUrl("rsakey"),
                        HttpMethod.POST,
                        encodedEntity,
                        new ParameterizedTypeReference<RestResponse<AesKeyDto>>() {
                        });

        byte[] rsaEncryptedSecretKey = decoder.decodeBuffer(keyDto.getBody().getData().getAesKey());
        byte[] encryptedIvector = decoder.decodeBuffer(keyDto.getBody().getData().getIvector());
        byte[] secretKeyBytes = RSA.decrypt(rsaEncryptedSecretKey, keyPair.getPrivate());
        ivector = RSA.decrypt(encryptedIvector, keyPair.getPrivate());
        secretKey = new SecretKeySpec(secretKeyBytes, "AES");
        Scanner scanner = new Scanner(System.in);
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

    private static ResponseEntity<RestResponse<VerifyDto>> login(ResponseEntity<RestResponse<AesKeyDto>> keyDto, byte[] ivector, SecretKey secretKey, Scanner scanner)
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
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(TestUtils.getUrl("files/" + descriptorDto.getGoogleId() +
                "?sessionId=" + sessionId + "&token=" + totpCode));
        HttpResponse response1 = client.execute(get);
        InputStream stream = response1.getEntity().getContent();
        byte[] aaaa = new byte[(int) response1.getEntity().getContentLength()];
        stream.read(aaaa);
        FileOutputStream stream1 = new FileOutputStream("kek2.jpeg");
        stream1.write(aaaa);
        System.out.println(aaaa.length);
        restTemplate.delete(TestUtils.getUrl("files/" + descriptorDto.getGoogleId() +
                "?sessionId=" + sessionId + "&token=" + totpCode));
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
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(TestUtils.getUrl("files/" + encoder.encode(AES.encrypt(descriptorDto.getGoogleId().getBytes(),secretKey,new IvParameterSpec(ivector))) +
                "?sessionId=" + sessionId + "&token=" + totpCode));
        HttpResponse response1 = client.execute(get);
        InputStream stream = response1.getEntity().getContent();
        byte[] aaaa = new byte[(int) response1.getEntity().getContentLength()];
        stream.read(aaaa);
        FileOutputStream stream1 = new FileOutputStream("kek2.jpeg");
        stream1.write(aaaa);
        //stream1.write(AES.decrypt(aaaa,secretKey,new IvParameterSpec(ivector)));
        System.out.println(aaaa.length);
        restTemplate.delete(TestUtils.getUrl("files/" + encoder.encode(AES.encrypt(descriptorDto.getGoogleId().getBytes(),secretKey,new IvParameterSpec(ivector))) +
              "?sessionId=" + sessionId + "&token=" + totpCode));
    }

    private static FileDescriptorDto decryptFileDescriptorDto(FileDescriptorDto fdd) throws IOException {
        fdd.setGoogleId(new String(AES.decrypt(decoder.decodeBuffer(fdd.getGoogleId()), secretKey,new IvParameterSpec(ivector))));
        fdd.setLink(new String(AES.decrypt(decoder.decodeBuffer(fdd.getLink()), secretKey,new IvParameterSpec(ivector))));;
        fdd.setName(new String(AES.decrypt(decoder.decodeBuffer(fdd.getName()), secretKey,new IvParameterSpec(ivector))));
        return fdd;
    }
}
