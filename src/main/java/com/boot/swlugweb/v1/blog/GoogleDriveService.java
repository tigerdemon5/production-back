package com.boot.swlugweb.v1.blog;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.google.api.client.googleapis.media.MediaHttpUploader.UploadState.INITIATION_STARTED;


@Service
public class GoogleDriveService {
//    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
//    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
//    private static final String TOKENS_DIRECTORY_PATH = "tokens";
//    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
//    private static final String CREDENTIALS_FILE_PATH = "/credentials1.json";

    //서비스 코드
    private static final String APPLICATION_NAME = "Google Drive API Java with Service Account";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String SERVICE_ACCOUNT_KEY_PATH = "/service_account_key.json";
    //


    private final Drive driveService;

    //서비스 코드
    public GoogleDriveService() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleCredentials credentials = getCredentials();
        this.driveService = new Drive.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(APPLICATION_NAME).build();
    }
    private GoogleCredentials getCredentials() throws IOException {
        InputStream in = GoogleDriveService.class.getResourceAsStream(SERVICE_ACCOUNT_KEY_PATH);
        if (in == null) {
            throw new FileNotFoundException("Service account key not found at " + SERVICE_ACCOUNT_KEY_PATH);
        }
        return ServiceAccountCredentials.fromStream(in)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/drive"));
    }

    public String uploadFileToDrive(MultipartFile file) {
        try {
            // 1. 파일 메타데이터 설정
            com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
            fileMetadata.setName(file.getOriginalFilename());

            // 2. 파일 내용 설정 (application/octet-stream은 범용이지만 필요시 "image/jpeg" 등으로 교체 가능)
            InputStreamContent mediaContent = new InputStreamContent(
                    "application/octet-stream", file.getInputStream()
            );

            // 3. 업로드 요청 생성
            Drive.Files.Create createRequest = driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id, webViewLink");

            // 4. 업로드 상태 로깅 리스너 등록
            createRequest.getMediaHttpUploader().setProgressListener(
                    uploader -> {
                        switch (uploader.getUploadState()) {
                            case INITIATION_STARTED:
                                System.out.println("✅ Upload Initiation Started");
                                break;
                            case INITIATION_COMPLETE:
                                System.out.println("✅ Upload Initiation Complete");
                                break;
                            case MEDIA_IN_PROGRESS:
                                System.out.printf("🔄 Upload Progress: %.2f%%\n", uploader.getProgress() * 100);
                                break;
                            case MEDIA_COMPLETE:
                                System.out.println("✅ Upload Complete");
                                break;
                            default:
                                System.out.println("⚠️ Upload State: " + uploader.getUploadState());
                                break;
                        }
                    }
            );

            // 5. 실제 업로드 수행
            com.google.api.services.drive.model.File uploadedFile = createRequest.execute();
            String fileId = uploadedFile.getId();

            System.out.println("✅ File Uploaded: " + fileId);

            // 6. 업로드된 파일 공개 권한 설정
            setFilePublic(fileId);

            // 7. 공유 가능한 웹 뷰 링크 반환
            return uploadedFile.getWebViewLink();

        } catch (Exception e) {
            System.err.println("❌ 파일 업로드 실패: " + file.getOriginalFilename());
            e.printStackTrace();
            throw new RuntimeException("Google Drive upload failed", e);
        }
    }

    private void setFilePublic(String fileId) throws IOException {
        Permission permission = new Permission()
                .setType("anyone")               // 누구나 접근 가능
                .setRole("reader")               // 읽기 권한만
                .setAllowFileDiscovery(false);   // 검색엔진에서 노출 방지

        driveService.permissions().create(fileId, permission)
                .setFields("id")
                .execute();

        System.out.println("🌍 File is now public: " + fileId);
    }



    //서비스

//    public GoogleDriveService() throws GeneralSecurityException, IOException {
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        this.driveService = new Drive.Builder(
//                HTTP_TRANSPORT,
//                JSON_FACTORY,
//                getCredentials(HTTP_TRANSPORT)
//        ).setApplicationName(APPLICATION_NAME).build();
//    }

//    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
//        InputStream in = GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
//        if (in == null) {
//            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
//        }
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
//                .setAccessType("offline")
//                .build();
//        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8080).build();
//        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//    }


    public String uploadFile(MultipartFile file) throws IOException {
        java.io.File convFile = new java.io.File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        file.transferTo(convFile);

        com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();
        String randomFileName = UUID.randomUUID().toString() + ".jpg";
        fileMetaData.setName(randomFileName);

        FileContent fileContent = new FileContent("image/jpeg", convFile);
        com.google.api.services.drive.model.File uploadedFile = driveService.files().create(fileMetaData, fileContent).execute();

        return "https://drive.google.com/uc?id=" + uploadedFile.getId();
    }







    private Path saveTempFile(MultipartFile multipartFile) throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path tempFile = tempDir.resolve(multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            fos.write(multipartFile.getBytes());
        }
        return tempFile;
    }


    public void deleteFile(String fileId) throws IOException, GeneralSecurityException {
        Drive service = driveService;
        service.files().delete(fileId).execute();
    }




    private java.io.File convertMultipartFileToFile(MultipartFile file) throws IOException {
        java.io.File convFile = new java.io.File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }


//    public String uploadFileToDrive(MultipartFile file) throws IOException {
//        // Google Drive에 업로드할 파일 메타데이터 설정
//        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
//        fileMetadata.setName(file.getOriginalFilename());
//
//        // 업로드할 파일 내용 설정
//        InputStreamContent mediaContent = new InputStreamContent(
//                "application/octet-stream", file.getInputStream()
//        );
//
//        // 파일을 구글 드라이브에 업로드
//        Drive.Files.Create createRequest = driveService.files().create(fileMetadata, mediaContent);
//
//        // 업로드 진행 상태를 모니터링
//        createRequest.getMediaHttpUploader().setProgressListener(
//                new MediaHttpUploaderProgressListener() {
//                    public void progressChanged(MediaHttpUploader uploader) throws IOException {
//                        switch (uploader.getUploadState()) {
//                            case INITIATION_STARTED:
//                                System.out.println("Initiation Started");
//                                break;
//                            case INITIATION_COMPLETE:
//                                System.out.println("Initiation Complete");
//                                break;
//                            case MEDIA_IN_PROGRESS:
//                                System.out.println("Upload In Progress");
//                                break;
//                            case MEDIA_COMPLETE:
//                                System.out.println("Upload Complete");
//                                break;
//                        }
//                    }
//                }
//        );
//
//        // 파일 업로드 실행
//        com.google.api.services.drive.model.File uploadedFile = createRequest.execute();
//        String fileId = uploadedFile.getId();
//
//        // 파일 공개 권한 설정 (필수)
//        setFilePublic(fileId);
//
//        // 이미지 원본 URL 반환
//        return driveService.files().get(fileId).execute().getWebViewLink();
//
//        // 업로드된 파일의 ID를 반환 (WebViewLink로 파일을 찾을 수 있음)
//        //return "https://drive.google.com/file/d/" + uploadedFile.getId() + "/view";
//    }
//
//    // 파일을 "공개"로 설정하는 메서드 수정
//    private void setFilePublic(String fileId) throws IOException {
//        Permission permission = new Permission()
//                .setType("anyone")  // 모든 사용자 접근 허용
//                .setRole("reader")  // 읽기 권한 부여
//                .setAllowFileDiscovery(false); // 검색 엔진에서 노출되지 않도록 설정
//
//        driveService.permissions().create(fileId, permission)
//                .setFields("id")
//                .execute();
//
//        System.out.println("File is now public: " + fileId);
//    }


}

