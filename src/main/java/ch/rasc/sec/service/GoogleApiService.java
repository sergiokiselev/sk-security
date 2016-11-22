package ch.rasc.sec.service;

import javax.servlet.ServletOutputStream;
import java.util.List;

/**
 * User: NotePad.by
 * Date: 11/21/2016.
 */
public interface GoogleApiService {
    String uploadFile(java.io.File file) throws Exception;

    List<String> getFilesList() throws Exception;

    void downloadFile(String fileId, ServletOutputStream outputStream) throws Exception;
}
