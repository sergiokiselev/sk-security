package ch.rasc.sec.service.impl;

import ch.rasc.sec.service.DriveService;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.IOUtils;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 * User: NotePad.by
 * Date: 11/21/2016.
 */
@Service
public class DriveServiceImpl implements DriveService {
    private static final String APPLICATION_NAME = "testproject";

    public Drive getDrive(HttpTransport httpTransport) throws GeneralSecurityException,
            IOException {
        JacksonFactory jsonFactory = new JacksonFactory();
        InputStream stream = GoogleApiServiceImpl.class.getResourceAsStream("/key.p12");
        java.io.File tmp = new java.io.File("tmp");
        OutputStream outputStream = new FileOutputStream(tmp);
        IOUtils.copy(stream, outputStream);
        outputStream.close();
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId("owner-669@testproject-150220.iam.gserviceaccount.com")
                .setServiceAccountScopes(Arrays.asList(DriveScopes.DRIVE))
                .setServiceAccountPrivateKeyFromP12File(tmp)
                .build();
        System.out.println(credential);
        //notasecret
        return new Drive.Builder(httpTransport, jsonFactory, null).setApplicationName(APPLICATION_NAME)
                .setHttpRequestInitializer(credential).build();
    }

}
