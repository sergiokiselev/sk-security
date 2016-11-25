package ch.rasc.sec.controller;

import ch.rasc.sec.dto.FileDescriptorDto;
import ch.rasc.sec.dto.TokenDto;
import ch.rasc.sec.dto.restresponse.ErrorDto;
import ch.rasc.sec.dto.restresponse.RestResponse;
import ch.rasc.sec.service.GoogleApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.util.List;

/**
 * User: NotePad.by
 * Date: 11/21/2016.
 */
@RestController
public class FileController {

    @Autowired
    private GoogleApiService googleApiService;

    @Value("${localMachine}")
    private boolean isLocalMachine;

    @RequestMapping(method = RequestMethod.POST, value = "/files")
    public RestResponse<FileDescriptorDto> uploadFile(@RequestBody MultipartFile file, @RequestParam String name,
                                                      @RequestParam String sessionId, @RequestParam long token) {
        try {
            String fileLocation = name;
            if (!isLocalMachine) {
                fileLocation = "/tmp/" + name;
            }
            java.io.File file1 = new java.io.File(fileLocation);
            file1.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file1);
            fileOutputStream.write(file.getBytes());
            fileOutputStream.close();
            return new RestResponse<>(googleApiService.uploadFile(file1, sessionId, token, name));
        } catch (Exception e) {
            e.printStackTrace();
            return new RestResponse<>(new ErrorDto(e.getMessage()));
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/files")
    public RestResponse<List<FileDescriptorDto>> getFilesList(@RequestParam String sessionId, @RequestParam long token) {
        try {
            return new RestResponse<>(googleApiService.getFilesList(new TokenDto(sessionId, token)));
        } catch (Exception e) {
            e.printStackTrace();
            return new RestResponse<>(new ErrorDto(e.getMessage()));
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/files/{fileId}")
    public void deleteFile(@PathVariable String fileId, @RequestParam String sessionId, @RequestParam long token) {
        try {
            googleApiService.deleteFile(fileId, new TokenDto(sessionId, token));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/files/{fileId}")
    public void downloadFile(@PathVariable String fileId, @RequestParam String sessionId,
                             @RequestParam long token, HttpServletResponse httpServletResponse) {
        try {
            googleApiService.downloadFile(fileId, httpServletResponse, sessionId, token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
