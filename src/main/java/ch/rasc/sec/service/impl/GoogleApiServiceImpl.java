package ch.rasc.sec.service.impl;

import ch.rasc.sec.GoogleAuth;
import ch.rasc.sec.cypher.AES;
import ch.rasc.sec.dto.FileDescriptorDto;
import ch.rasc.sec.dto.TokenDto;
import ch.rasc.sec.model.*;
import ch.rasc.sec.repository.FileDescriptorRepository;
import ch.rasc.sec.repository.GrantsRepository;
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
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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

    @Autowired
    private GrantsRepository grantsRepository;


    @Autowired
    private BASE64Encoder encoder;

    @Autowired
    private BASE64Decoder decoder;

    @Value("${localMachine}")
    private boolean isLocalMachine;

    public FileDescriptorDto uploadFile(byte[] bytes, String sessionId, long token, String name) throws Exception {
        userService.verifyToken(new TokenDto(sessionId, token));
        SessionAttributes sessionAttributes = userService.getSessionAttributes(sessionId);
        String fileLocation = name;
        if (!isLocalMachine) {
            fileLocation = "/tmp/" + name;
        }
        java.io.File file1 = new java.io.File(fileLocation);
        file1.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(file1);

        if (sessionAttributes.isEncryption()) {
            bytes = AES.decrypt(bytes, sessionAttributes.getAesKey(), new IvParameterSpec(sessionAttributes.getIvector()));
            bytes = AES.encrypt(bytes, GoogleAuth.serverGoogleKey, GoogleAuth.ivectorGoogle);


        }
        fileOutputStream.write(bytes);
        fileOutputStream.close();

        User user = userRepository.findOne(sessionAttributes.getUserId());
        HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
        Drive drive = driveService.getDrive(transport);
        File fileMetadata = new File();
        fileMetadata.setTitle(name);
        FileContent mediaContent = new FileContent("image/jpeg", file1);
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
        createGrants(user, fileDescriptor);
        return fileDescriptorRepository.save(fileDescriptor);
    }

    private FileDescriptorDto encryptFileDescriptorDto(FileDescriptorDto fdd, SessionAttributes sessionAttributes) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        fdd.setGoogleId(encoder.encode(AES.encrypt(fdd.getGoogleId().getBytes(), sessionAttributes.getAesKey(), new IvParameterSpec(sessionAttributes.getIvector()))));
        fdd.setLink(encoder.encode(AES.encrypt(fdd.getLink().getBytes(), sessionAttributes.getAesKey(), new IvParameterSpec(sessionAttributes.getIvector()))));
        fdd.setName(encoder.encode(AES.encrypt(fdd.getName().getBytes(), sessionAttributes.getAesKey(), new IvParameterSpec(sessionAttributes.getIvector()))));
        return fdd;
    }

    private void createGrants(User user, FileDescriptor fileDescriptor) {
        for (UserGroup userGroup : user.getUserGroups()) {
            grantsRepository.save(new Grants(Grants.AccessLevel.READWRITE, userGroup, fileDescriptor));
        }
    }


    @Override
    public List<FileDescriptorDto> getFilesList(TokenDto tokenDto) throws Exception {
        userService.verifyToken(tokenDto);
        SessionAttributes sessionAttributes = userService.getSessionAttributes(tokenDto.getSessionId());
        User user = userRepository.getOne(sessionAttributes.getUserId());
        //List<FileDescriptor> descriptors = fileDescriptorRepository.getByOwnerId(user);
        Set<FileDescriptor> descriptors = new HashSet<>();
        Set<UserGroup> userGroups = user.getUserGroups();
        for (UserGroup ug : userGroups) {
            for (Grants grant : ug.getGrants()) {
                descriptors.add(grant.getFileDescriptor());
            }
        }


        return mapToDto(new ArrayList<FileDescriptor>(descriptors), sessionAttributes);
    }

    private boolean checkAccess(Long fileId, User user) {
        for (UserGroup group : user.getUserGroups()) {
            for (Grants grant : group.getGrants()) {
                if (grant.getFileDescriptor().getId().equals(fileId)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void downloadFile(String fileId, HttpServletResponse response, String sessionId, long token) throws Exception {
        userService.verifyToken(new TokenDto(sessionId, token));
        SessionAttributes sessionAttributes = userService.getSessionAttributes(sessionId);
        User user = userRepository.findOne(sessionAttributes.getUserId());

        if (sessionAttributes.isEncryption())
            fileId = new String(AES.decrypt(decoder.decodeBuffer(fileId), sessionAttributes.getAesKey(), new IvParameterSpec(sessionAttributes.getIvector())));

        FileDescriptor fd = fileDescriptorRepository.getByGoogleId(fileId);
        if (!checkAccess(fd.getId(), user))
            return;

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

        if (sessionAttributes.isEncryption()) {
            filebytes = AES.decrypt(filebytes, GoogleAuth.serverGoogleKey, GoogleAuth.ivectorGoogle);
            //filebytes = AES.encrypt(filebytes, sessionAttributes.getAesKey(), new IvParameterSpec(sessionAttributes.getIvector()));
        }
        response.setContentLength(filebytes.length);
        outputStream.write(filebytes);
        outputStream.close();
    }

    @Override
    public void deleteFile(String fileId, TokenDto tokenDto) throws Exception {
        userService.verifyToken(tokenDto);
        SessionAttributes sessionAttributes = userService.getSessionAttributes(tokenDto.getSessionId());
        User user = userRepository.findOne(sessionAttributes.getUserId());

        if (sessionAttributes.isEncryption())
            fileId = new String(AES.decrypt(decoder.decodeBuffer(fileId), sessionAttributes.getAesKey(), new IvParameterSpec(sessionAttributes.getIvector())));
        FileDescriptor fd = fileDescriptorRepository.getByGoogleId(fileId);
        if (!checkAccess(fd.getId(), user))
            return;

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

    private List<FileDescriptorDto> mapToDto(List<FileDescriptor> descriptors, SessionAttributes sessionAttributes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        List<FileDescriptorDto> fileDescriptorDtos = new ArrayList<>();
        for (FileDescriptor descriptor : descriptors) {
            if (sessionAttributes.isEncryption()) {
                fileDescriptorDtos.add(encryptFileDescriptorDto(mapToDto(descriptor), sessionAttributes));
            } else {
                fileDescriptorDtos.add(mapToDto(descriptor));
            }
        }
        return fileDescriptorDtos;
    }

}
