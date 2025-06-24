package com.boot.swlugweb.v1.blog;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;

import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class GoogleDriveService {

//    private static final String APPLICATION_NAME = "Spring Boot Google Drive";
//    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
//    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
//    private static final String TOKENS_DIRECTORY_PATH = "tokens";
//    //    private static final String CREDENTIALS_FILE_PATH = "/credentials_service.json";
//    private static final String CREDENTIALS_FILE_PATH = "/service_account_key.json";
////    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
//
//    //credential-service
//    public Drive getDriveService() throws Exception {
//        InputStream in = getClass().getResourceAsStream(CREDENTIALS_FILE_PATH);
//        if (in == null) {
//            throw new RuntimeException("credentials_service.json not found");
//        }
//
//        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(in)
//                .createScoped(Collections.singletonList("drive-uploader@swlugweb1.iam.gserviceaccount.com/auth/drive"));
//
//        return new Drive.Builder(
//                GoogleNetHttpTransport.newTrustedTransport(),
//                JSON_FACTORY,
//                new HttpCredentialsAdapter(credentials)
//        ).setApplicationName(APPLICATION_NAME).build();
//    }}

//test 0621
//    private static final String APPLICATION_NAME = "Spring Boot Google Drive";
//    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
//    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    //service 버전
    //    private static final String CREDENTIALS_FILE_PATH = "/service_account_key.json";
//
//    public Drive getDriveService() throws Exception {
//        InputStream in = getClass().getResourceAsStream(CREDENTIALS_FILE_PATH);
//        if (in == null) {
//            throw new RuntimeException("service_account_key.json not found");
//        }
//
//        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(in)
//                .createScoped(SCOPES);
//
//        return new Drive.Builder(
//                GoogleNetHttpTransport.newTrustedTransport(),
//                JSON_FACTORY,
//                new HttpCredentialsAdapter(credentials)
//        ).setApplicationName(APPLICATION_NAME).build();
//    }

//test0624
private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "credentials1.json";



//    public static String uploadFile(MultipartFile file) throws IOException, GeneralSecurityException {
//        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//
//        getFileList(service);
//        return null;
//    }

    public static String uploadFile(MultipartFile file) throws IOException, GeneralSecurityException {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(file.getOriginalFilename());

        InputStream inputStream = file.getInputStream();
        InputStreamContent mediaContent = new InputStreamContent(
                file.getContentType(), inputStream
        );

        com.google.api.services.drive.model.File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        String fileId = uploadedFile.getId();
        return "https://drive.google.com/file/d/" + fileId + "/view?usp=sharing";
    }


//    public static void getFileList(Drive driveService) throws IOException {
//        FileList result = driveService.files().list().setFields("nextPageToken, files(id, name, createdTime)").execute();
//        List<com.google.api.services.drive.model.File> files = result.getFiles();
//        for (com.google.api.services.drive.model.File file : files) {
//            System.out.println("File Name: " + file.getName() + " File Id: " + file.getId());
//        }
//    }

//    public static void deleteFile(String imageUrl) throws IOException, GeneralSecurityException {
//
//        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//
//        service.files().delete(imageUrl).execute();
//        System.out.println("File with ID " + imageUrl + " has been deleted.");
//    }
//0624
    public void deleteFile(String imageUrl) throws IOException, GeneralSecurityException {
        String fileId = extractFileIdFromUrl(imageUrl); // fileId 추출
        if (fileId == null) {
            throw new IllegalArgumentException("Invalid Google Drive URL: " + imageUrl);
        }

        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        service.files().delete(fileId).execute(); // ✅ 여기 수정
        System.out.println("File with ID " + fileId + " has been deleted.");
    }

    public static String extractFileIdFromUrl(String imageUrl) {
        Pattern pattern = Pattern.compile("/d/([a-zA-Z0-9_-]{25,})");
        Matcher matcher = pattern.matcher(imageUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
//0624

//        public static String extractFileIdFromUrl(String imageurl) {
//        if (imageurl.contains("drive.google.com/file/d/")) {
//            int start = imageurl.indexOf("/d/") + 3;
//            int end = imageurl.indexOf("/", start);
//            if (start > 2 && end > start) {
//                return imageurl.substring(start, end);
//            }
//        }
//        return null;
//    }

//    public void deleteFile(String imageUrl) throws IOException, GeneralSecurityException {
//        String fileId = extractFileIdFromUrl(imageUrl);
//
//        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        com.google.api.services.drive.model.Drive service = new com.google.api.services.drive.model.Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//
//        service.files().delete(fileId).execute();
//        System.out.println("File with ID " + fileId + " has been deleted.");
//    }
//
//    public String extractFileIdFromUrl(String url) {
//        String regex = "/d/([a-zA-Z0-9_-]{25,})";
//        Pattern pattern = Pattern.compile(regex);
//        org.apache.tomcat.util.file.Matcher matcher = pattern.matcher(url);
//        if (matcher.find()) {
//            return matcher.group(1);
//        }
//        throw new IllegalArgumentException("Invalid Google Drive URL: " + url);
//    }


    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }}
//0624




