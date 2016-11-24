package ch.rasc.sec.controller;

import ch.rasc.sec.dto.TokenDto;
import ch.rasc.sec.dto.restresponse.ErrorDto;
import ch.rasc.sec.dto.restresponse.RestResponse;
import ch.rasc.sec.model.FileDescriptor;
import ch.rasc.sec.service.GoogleApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * User: NotePad.by
 * Date: 11/21/2016.
 */
@RestController
public class FileController {

    @Autowired
    private GoogleApiService googleApiService;

    @RequestMapping(method = RequestMethod.POST, value = "/files")
    public RestResponse<FileDescriptor> uploadFile(@RequestBody MultipartFile file, @RequestParam String name,
                                         @RequestParam String sessionId, @RequestParam long token) {
        try {
            java.io.File file1 = new java.io.File(name);
            file.transferTo(file1);
            return new RestResponse<>(googleApiService.uploadFile(file1, sessionId, token, name));
        } catch (Exception e) {
            e.printStackTrace();
            return new RestResponse<>(new ErrorDto(e.getMessage()));
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/files")
    public RestResponse<List<FileDescriptor>> getFilesList(@RequestBody TokenDto tokenDto) {
        try {
            return new RestResponse<>(googleApiService.getFilesList(tokenDto));
        } catch (Exception e) {
            e.printStackTrace();
            return new RestResponse<>(new ErrorDto(e.getMessage()));
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/files/{fileId}")
    public void deleteFile(@PathVariable String fileId, @RequestBody TokenDto tokenDto) {
        try {
            googleApiService.deleteFile(fileId, tokenDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/files/{fileId}")
    public void downloadFile(@PathVariable String fileId, HttpServletResponse httpServletResponse) {
        try {
            googleApiService.downloadFile(fileId, httpServletResponse.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
