package ch.rasc.sec.service.impl;

import ch.rasc.sec.service.DriveService;
import ch.rasc.sec.service.GoogleApiService;
import ch.rasc.sec.service.UserService;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: NotePad.by
 * Date: 11/21/2016.
 */
@Service
public class GoogleApiServiceImpl implements GoogleApiService {

    @Autowired
    private UserService userService;

    @Autowired
    private DriveService driveService;

    private static final String UPLOAD_FILE_PATH = "src\\main\\resources\\download\\gopnik.jpeg";
    private static final java.io.File UPLOAD_FILE = new java.io.File(UPLOAD_FILE_PATH);

    public String uploadFile(java.io.File file) throws Exception {
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        Drive drive = driveService.getDrive(transport);
        File fileMetadata = new File();
        fileMetadata.setTitle(file.getName());
        FileContent mediaContent = new FileContent("image/jpeg", file);
        Drive.Files.Insert insert = drive.files().insert(fileMetadata, mediaContent);
        MediaHttpUploader uploader = insert.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(true);
        File exec = insert.execute();
        System.out.println(exec);
        return exec.getId();
    }

    @Override
    public List<String> getFilesList() throws Exception {
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        Drive drive = driveService.getDrive(transport);

        Drive.Files.List list = drive.files().list();
        FileList fileList = list.execute();

        List<String> ids = new ArrayList<>();
        for (File i : fileList.getItems()) {
            System.out.println(i);
            ids.add(i.getId());
        }
        return ids;
    }

    @Override
    public void downloadFile(String fileId, ServletOutputStream outputStream) throws Exception {
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        Drive drive = driveService.getDrive(transport);
        Drive.Files.Get get = drive.files().get(fileId);
        File file = get.execute();
        MediaHttpDownloader downloader =
                new MediaHttpDownloader(transport, drive.getRequestFactory().getInitializer());
        downloader.setDirectDownloadEnabled(true);
        downloader.download(new GenericUrl(file.getDownloadUrl()), outputStream);
    }

    /** Updates the name of the uploaded file to have a "drivetest-" prefix. */
    private static File updateFileWithTestSuffix(String id, Drive drive) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setTitle("drivetest-" + UPLOAD_FILE.getName());

        Drive.Files.Update update = drive.files().update(id, fileMetadata);
        return update.execute();
    }

}
