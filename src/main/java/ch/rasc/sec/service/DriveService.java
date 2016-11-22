package ch.rasc.sec.service;

import com.google.api.client.http.HttpTransport;
import com.google.api.services.drive.Drive;

/**
 * User: NotePad.by
 * Date: 11/21/2016.
 */
public interface DriveService {

    Drive getDrive(HttpTransport httpTransport) throws Exception;
}
