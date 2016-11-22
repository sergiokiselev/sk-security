package ch.rasc.sec.service;

import ch.rasc.sec.dto.TokenDto;
import ch.rasc.sec.model.FileDescriptor;
import ch.rasc.sec.util.exception.AuthenticationException;

import javax.servlet.ServletOutputStream;
import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * User: NotePad.by
 * Date: 11/21/2016.
 */
public interface GoogleApiService {
    FileDescriptor uploadFile(File file, String sessionId, long token, String name) throws Exception;

    List<FileDescriptor> getFilesList(TokenDto tokenDto) throws Exception;

    void downloadFile(String fileId, ServletOutputStream outputStream) throws Exception;

    void deleteFile(String fileId, TokenDto tokenDto) throws Exception;
}
