package com.boot.swlugweb.v1.blog;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@Service
public class GoogleDriveService {
    private static final String APPLICATION_NAME = "Google Drive API Java with Service Account";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String SERVICE_ACCOUNT_KEY_PATH = "/service_account_key.json";


    private final Drive driveService;

    @Value("${google.drive.folder-id}")
    private String folderId;


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
            fileMetadata.setParents(Collections.singletonList(folderId)); // 폴더 ID

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

            // 7. 공유 가능한 웹 뷰 링크 대신 직접 렌더링 URL 반환
            return "https://lh3.googleusercontent.com/d/" + uploadedFile.getId();

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

}

