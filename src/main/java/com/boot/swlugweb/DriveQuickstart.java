package com.boot.swlugweb;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class DriveQuickstart {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    //사용자의 토큰을 어디에 저장할지 경로를 지정
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    //어플리케이션이 요청하는 권한의 범위를 지정
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    //비밀키 경로
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static String realFileId = "";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException{
        //credentials.json 파일을 in에 저장함
        InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {   // credentials이 빈값이면
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8080).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        return credential;
    }

    public static void main(String... args) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();

        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
//        uploadFile(service);
//        upadateFile(service);
//        fileDownload(service);
//        DeleteFile(service);
    }

    //파일 업로드
//    public static void uploadFile(Drive service) throws IOException{
//        System.out.println("\n\n 파일 업로드 시작..");
//        Path currentWorkingDir = Paths.get("").toAbsolutePath();
//        File fileMetaData = new File();
//        fileMetaData.setName("test5.jpg"); //업로드 파일 이름
//        java.io.File f = new java.io.File(currentWorkingDir + "/files/test1.jpg");
//        FileContent fileContent = new FileContent("image/jpeg", f);
//        service.files().create(fileMetaData, fileContent).execute();
//    }

     // 업로드 파일 이름 랜덤
    public static void uploadFile(Drive service) throws IOException{
        System.out.println("\n\n 파일 업로드 시작..");
        Path currentWorkingDir = Paths.get("").toAbsolutePath();
        File fileMetaData = new File();
        String randomFileName = UUID.randomUUID().toString() + ".jpg"; // 랜덤 파일명 생성
        fileMetaData.setName(randomFileName); //업로드 파일 이름
        java.io.File f = new java.io.File(currentWorkingDir + "/files/test1.jpg");
        FileContent fileContent = new FileContent("image/jpeg", f);
        service.files().create(fileMetaData, fileContent).execute();
    }

//    public static void uploadFile(Drive service) throws IOException{
//        System.out.println("\n\n 파일 업로드 시작..");
//        Path currentWorkingDir = Paths.get("").toAbsolutePath();
//        File fileMetaData = new File();
//        fileMetaData.setName("test1.jpg"); //업로드 파일 이름
//        java.io.File f = new java.io.File(currentWorkingDir + "/files/test1.jpg");
//        FileContent fileContent = new FileContent("image/jpeg", f);
//        service.files().create(fileMetaData, fileContent).execute();
//    }


    //파일 업데이트
    public static void upadateFile(Drive service) throws IOException{
        System.out.println("파일 덮어씌우기");
        //파일경로설정
        Path currentWorkingDir = Paths.get("").toAbsolutePath();
        File fileMetaData2 = new File();
        fileMetaData2.setName("test6.jpg");
        java.io.File f2 = new java.io.File(currentWorkingDir + "/files/test2.jpg");
        FileContent fileContent1 = new FileContent("image/jpg", f2);
        service.files().update("1NB6_LohUjyfyitR7pGGxc2OF-Y6IWbgZ", fileMetaData2, fileContent1).execute();
    }

    //파일 다운로드
    //다운로드 할 파일 이름 : downFileName에 설정
    //파일 내 저장 경로 : 내 PC에 있는 다운로드 폴더로 설정해 놓았음
    //변경하고 싶다면 downloadFolderPath 수정
    public static void fileDownload(Drive service) throws IOException{
        FileList result = service.files().list()
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        //파일 다운로드
        String downFileName = "test6.jpg";
        //파일 저장경로
        String downloadFolderPath = System.getProperty("user.home") + java.io.File.separator + "Downloads" + java.io.File.separator;
        String fullPath = downloadFolderPath + downFileName;
        System.out.println("\n\n 다운로드 시작...");
        try{
            File downloadFile = files.stream()
                    .filter(downFile -> downFile.getName().equals(downFileName))
                    .findAny().orElseThrow(()-> new FileNotFoundException(downFileName+"not found?!?!"));
            String downloadFileId = downloadFile.getId();
            OutputStream outputStream = new ByteArrayOutputStream();
            service.files().get(downloadFileId)
                    .executeMediaAndDownloadTo(outputStream);
            var byteArrayOutputStream = (ByteArrayOutputStream) outputStream;
            try(OutputStream writeStream = new FileOutputStream(fullPath)){
                System.out.println("success");
                byteArrayOutputStream.writeTo(writeStream);
            }
        } catch (GoogleJsonResponseException e){
            System.err.println("Unable to move file : " + e.getDetails());
            throw e;
        }
    }

    //파일 삭제
    public static void DeleteFile(Drive service){
        try{
            service.files().delete("1NB6_LohUjyfyitR7pGGxc2OF-Y6IWbgZ").execute();
            System.out.println("성공적으로 삭제했습니다.");
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("삭제 실패");
        }
    }
}