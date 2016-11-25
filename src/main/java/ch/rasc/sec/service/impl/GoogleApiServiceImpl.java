package ch.rasc.sec.service.impl;

import ch.rasc.sec.dto.FileDescriptorDto;
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
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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

    @Value("${localMachine}")
    private boolean isLocalMachine;

    public FileDescriptorDto uploadFile(java.io.File file, String sessionId, long token, String name) throws Exception {
        userService.verifyToken(new TokenDto(sessionId, token));
        SessionAttributes sessionAttributes = userService.getSessionAttributes(sessionId);
        User user = userRepository.findOne(sessionAttributes.getUserId());
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        Drive drive = driveService.getDrive(transport);
        File fileMetadata = new File();
        fileMetadata.setTitle(file.getName());
        FileContent mediaContent = new FileContent("image/jpeg", file);
        Drive.Files.Insert insert = drive.files().insert(fileMetadata, mediaContent);
        File insertedFile = insert.execute();
        return mapToDto(createFileDescriptor(name, user, insertedFile));
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
    public List<FileDescriptorDto> getFilesList(TokenDto tokenDto) throws Exception {
        userService.verifyToken(tokenDto);
        SessionAttributes sessionAttributes = userService.getSessionAttributes(tokenDto.getSessionId());
        User user = userRepository.getOne(sessionAttributes.getUserId());
        List<FileDescriptor> descriptors = fileDescriptorRepository.getByOwnerId(user);
        return mapToDto(descriptors);
    }

    @Override
    public void downloadFile(String fileId, HttpServletResponse response, String sessionId, long token) throws Exception {
        userService.verifyToken(new TokenDto(sessionId, token));
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        Drive drive = driveService.getDrive(transport);
        Drive.Files.Get get = drive.files().get(fileId);
        File file = get.execute();
        MediaHttpDownloader downloader =
                new MediaHttpDownloader(transport, drive.getRequestFactory().getInitializer());
        downloader.setDirectDownloadEnabled(true);
        String downloadPath = file.getTitle();
        if (!isLocalMachine) {
            downloadPath = "/tmp/" + downloadPath;
        }
        OutputStream out = new FileOutputStream(new java.io.File(downloadPath));
        downloader.download(new GenericUrl(file.getDownloadUrl()), out);
        out.close();
        FileInputStream fis = new FileInputStream(downloadPath);
        byte[] filebytes = new byte[fis.available()];
        fis.read(filebytes);
        OutputStream outputStream = response.getOutputStream();
        response.setContentLength(filebytes.length);
        outputStream.write(filebytes);
        outputStream.close();
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

    private FileDescriptorDto mapToDto(FileDescriptor fileDescriptor) {
        FileDescriptorDto fileDescriptorDto = new FileDescriptorDto();
        fileDescriptorDto.setGoogleId(fileDescriptor.getGoogleId());
        fileDescriptorDto.setLink(fileDescriptor.getLink());
        fileDescriptorDto.setCreated(fileDescriptor.getCreated());
        fileDescriptorDto.setName(fileDescriptor.getName());
        fileDescriptorDto.setLastModified(fileDescriptor.getLastModified());
        fileDescriptorDto.setOwnerId(fileDescriptor.getOwnerId().getId());
        fileDescriptorDto.setSize(fileDescriptor.getSize());
        return fileDescriptorDto;
    }

    private List<FileDescriptorDto> mapToDto(List<FileDescriptor> descriptors) {
        List<FileDescriptorDto> fileDescriptorDtos = new ArrayList<>();
        for (FileDescriptor descriptor: descriptors) {
            fileDescriptorDtos.add(mapToDto(descriptor));
        }
        return fileDescriptorDtos;
    }

}
