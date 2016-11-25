package ch.rasc.sec.service;

import ch.rasc.sec.dto.FileDescriptorDto;
import ch.rasc.sec.dto.TokenDto;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;

/**
 * User: NotePad.by
 * Date: 11/21/2016.
 */
public interface GoogleApiService {
    FileDescriptorDto uploadFile(File file, String sessionId, long token, String name) throws Exception;

    List<FileDescriptorDto> getFilesList(TokenDto tokenDto) throws Exception;

    void downloadFile(String fileId, HttpServletResponse response, String sessionId, long token) throws Exception;

    void deleteFile(String fileId, TokenDto tokenDto) throws Exception;
}
