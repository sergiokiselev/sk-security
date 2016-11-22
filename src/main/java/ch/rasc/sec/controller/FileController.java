package ch.rasc.sec.controller;

import ch.rasc.sec.dto.restresponse.ErrorDto;
import ch.rasc.sec.dto.restresponse.RestResponse;
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
    public RestResponse<String> uploadFile(@RequestBody MultipartFile file, @RequestParam String name) {
        try {
            java.io.File file1 = new java.io.File(file.getName());
            file.transferTo(file1);
            return new RestResponse<>(googleApiService.uploadFile(file1));
        } catch (Exception e) {
            e.printStackTrace();
            return new RestResponse<>(new ErrorDto(e.getMessage()));
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/files")
    public RestResponse<List<String>> getFilesList() {
        try {
            return new RestResponse<>(googleApiService.getFilesList());
        } catch (Exception e) {
            e.printStackTrace();
            return new RestResponse<>(new ErrorDto(e.getMessage()));
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
