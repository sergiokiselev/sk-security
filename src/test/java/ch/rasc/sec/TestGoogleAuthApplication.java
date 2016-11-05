package ch.rasc.sec;

import ch.rasc.sec.cypher.AES;
import ch.rasc.sec.cypher.RSA;
import ch.rasc.sec.dto.AesKeyDto;
import ch.rasc.sec.dto.restresponse.RestResponse;
import ch.rasc.sec.dto.TokenDto;
import ch.rasc.sec.dto.TotpSecretDto;
import ch.rasc.sec.dto.UserDto;
import ch.rasc.sec.dto.VerifyDto;
import ch.rasc.sec.service.TOTPService;
import ch.rasc.sec.service.impl.TOTPServiceImpl;
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
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class TestGoogleAuthApplication {

    private static BASE64Encoder encoder = new BASE64Encoder();
    private static BASE64Decoder decoder = new BASE64Decoder();
    private static RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args)
            throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InterruptedException {
        KeyPair keyPair = RSA.generateKeyPair();
        String encoded = encoder.encode(keyPair.getPublic().getEncoded());

        HttpEntity<String> encodedEntity = new HttpEntity<>(encoded);
        ResponseEntity<RestResponse<AesKeyDto>> keyDto = restTemplate
                .exchange("http://127.0.0.1:8084/google/rsakey",
                        HttpMethod.POST,
                        encodedEntity,
                        new ParameterizedTypeReference<RestResponse<AesKeyDto>>() {});

        byte[] rsaEncryptedSecretKey = decoder.decodeBuffer(keyDto.getBody().getData().getAesKey());
        byte[] encryptedIvector = decoder.decodeBuffer(keyDto.getBody().getData().getIvector());
        byte[] secretKeyBytes = RSA.decrypt(rsaEncryptedSecretKey, keyPair.getPrivate());
        byte[] ivector = RSA.decrypt(encryptedIvector, keyPair.getPrivate());
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "AES");
        Scanner scanner = new Scanner(System.in);
        ResponseEntity<RestResponse<VerifyDto>> verifyDto = login(keyDto, ivector, secretKey, scanner);
        ResponseEntity<RestResponse<TotpSecretDto>> totpSecretDto = verifyCode(ivector, secretKey, scanner, verifyDto);
        byte[] secretBytes = AES.decrypt(decoder.decodeBuffer(totpSecretDto.getBody().getData().getSecret()), secretKey, new IvParameterSpec(ivector));
        verifyToken(totpSecretDto, secretBytes);
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
                .exchange("http://127.0.0.1:8084/google/verify",
                        HttpMethod.POST,
                        verifyDtoHttpEntity,
                        new ParameterizedTypeReference<RestResponse<TotpSecretDto>>() {});
        System.out.println(totpSecretDto.getBody().getData());
        return totpSecretDto;
    }

    private static ResponseEntity<RestResponse<VerifyDto>> login(ResponseEntity<RestResponse<AesKeyDto>> keyDto, byte[] ivector, SecretKey secretKey, Scanner scanner)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        UserDto userDto = new UserDto();
        userDto.setSessionId(keyDto.getBody().getData().getSessionId());
        System.out.println("Enter login:");
        //sergio.kiselev509@gmail.com
        String login = scanner.nextLine();
        System.out.println("Enter password:");
        //password
        String password = scanner.nextLine();
        userDto.setLogin(encoder.encode(AES.encrypt(login.getBytes(), secretKey, new IvParameterSpec(ivector))));
        userDto.setPassword(encoder.encode(AES.encrypt(password.getBytes(), secretKey, new IvParameterSpec(ivector))));

        HttpEntity<UserDto> userDtoHttpEntity = new HttpEntity<>(userDto);
        return restTemplate
                .exchange("http://127.0.0.1:8084/google/login",
                        HttpMethod.POST,
                        userDtoHttpEntity,
                        new ParameterizedTypeReference<RestResponse<VerifyDto>>() {});
    }

    private static void verifyToken(ResponseEntity<RestResponse<TotpSecretDto>> totpSecretDto, byte[] secretBytes)
            throws InvalidKeyException, NoSuchAlgorithmException, InterruptedException {
        for (int i = 0; i < 10; i++) {
            long totpCode = getCurrentCode(secretBytes);
            TokenDto tokenDto = new TokenDto();
            tokenDto.setSessionId(totpSecretDto.getBody().getData().getSessionId());
            tokenDto.setToken(totpCode);

            HttpEntity<TokenDto> tokenDtoHttpEntity = new HttpEntity<>(tokenDto);
            ResponseEntity<RestResponse<String>> responseEntity = restTemplate
                    .exchange("http://127.0.0.1:8084/google/token",
                            HttpMethod.POST,
                            tokenDtoHttpEntity,
                            new ParameterizedTypeReference<RestResponse<String>>() {});
            System.out.println(responseEntity.getBody().getData());
            Thread.sleep(10000);
        }
    }

    private static long getCurrentCode(byte[] secretBytes) throws InvalidKeyException, NoSuchAlgorithmException {
        long timeIndex = System.currentTimeMillis() / 1000 / 30;
        TOTPService totpService = new TOTPServiceImpl();
        long totpCode = totpService.getCode(secretBytes, timeIndex);
        System.out.println(totpCode);
        return totpCode;
    }
}
