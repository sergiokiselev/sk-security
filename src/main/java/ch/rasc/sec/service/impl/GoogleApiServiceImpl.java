package ch.rasc.sec.service.impl;

import ch.rasc.sec.dto.TokenDto;
import ch.rasc.sec.model.FileDescriptor;
import ch.rasc.sec.model.SessionAttributes;
import ch.rasc.sec.model.User;
import ch.rasc.sec.repository.FileDescriptorRepository;
import ch.rasc.sec.repository.UserRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

/**
 * User: NotePad.by
 * Date: 11/21/2016.
 */
@Service
@Transactional
public class GoogleApiServiceImpl implements GoogleApiService {

    @Autowired
    private UserService userService;

    @Autowired
    private DriveService driveService;

    @Autowired
    private FileDescriptorRepository fileDescriptorRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_FILE_PATH = "src\\main\\resources\\download\\gopnik.jpeg";
    private static final java.io.File UPLOAD_FILE = new java.io.File(UPLOAD_FILE_PATH);

    public FileDescriptor uploadFile(java.io.File file, String sessionId, long token, String name) throws Exception {
        userService.verifyToken(new TokenDto(sessionId, token));
        SessionAttributes sessionAttributes = userService.getSessionAttributes(sessionId);
        User user = userRepository.findOne(sessionAttributes.getUserId());
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        Drive drive = driveService.getDrive(transport);
        File fileMetadata = new File();
        fileMetadata.setTitle(file.getName());
        FileContent mediaContent = new FileContent("image/jpeg", file);
        Drive.Files.Insert insert = drive.files().insert(fileMetadata, mediaContent);
        MediaHttpUploader uploader = insert.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(true);
        File insertedFile = insert.execute();
        return createFileDescriptor(name, user, insertedFile);
    }

    private FileDescriptor createFileDescriptor(String name, User user, File insertedFile) {
        FileDescriptor fileDescriptor = new FileDescriptor();
        fileDescriptor.setName(name);
        fileDescriptor.setOwnerId(user);
        fileDescriptor.setGoogleId(insertedFile.getId());
        fileDescriptor.setCreated(new Date(insertedFile.getCreatedDate().getValue()));
        fileDescriptor.setLink(insertedFile.getDownloadUrl());
        fileDescriptor.setSize(insertedFile.size());
        return fileDescriptorRepository.save(fileDescriptor);
    }

    @Override
    public List<FileDescriptor> getFilesList(TokenDto tokenDto) throws Exception {
        userService.verifyToken(tokenDto);
        SessionAttributes sessionAttributes = userService.getSessionAttributes(tokenDto.getSessionId());
        User user = userRepository.getOne(sessionAttributes.getUserId());
        List<FileDescriptor> descriptors = fileDescriptorRepository.getByOwnerId(user);
//        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
//        Drive drive = driveService.getDrive(transport);
//
//        Drive.Files.List list = drive.files()
//        FileList fileList = list.execute();
//
//        List<String> ids = new ArrayList<>();
//        for (File i : fileList.getItems()) {
//            System.out.println(i);
//            ids.add(i.getId());
//        }
//        return ids;
        return descriptors;
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

    @Override
    public void deleteFile(String fileId, TokenDto tokenDto) throws Exception {
        userService.verifyToken(tokenDto);
        SessionAttributes sessionAttributes = userService.getSessionAttributes(tokenDto.getSessionId());
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        Drive drive = driveService.getDrive(transport);
        FileDescriptor descriptor = fileDescriptorRepository.getByGoogleId(fileId);
        if (descriptor == null) {
            throw new Exception("Descriptor was not found");
        }
        if (descriptor.getOwnerId().getId() != sessionAttributes.getUserId()) {
            throw new Exception("You are not owner of this file");
        }
        Drive.Files.Delete delete = drive.files().delete(fileId);
        delete.execute();
        fileDescriptorRepository.delete(descriptor);
    }

    /** Updates the name of the uploaded file to have a "drivetest-" prefix. */
    private static File updateFileWithTestSuffix(String id, Drive drive) throws IOException {
        File fileMetadata = new File();
        fileMetadata.setTitle("drivetest-" + UPLOAD_FILE.getName());

        Drive.Files.Update update = drive.files().update(id, fileMetadata);
        return update.execute();
    }

}
