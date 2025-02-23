package com.boot.swlugweb.v1.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class EmailService {
    //이메일 수정
    @Autowired
    private JavaMailSender mailSender;
    private int authNumber;
    private final HttpSession session;

    @Value("${SMTP_ID")
    private String smtpId;

    //세션 초기화
    public EmailService(HttpSession session) {
        this.session = session;
    }

    //인증번호 저장
    public void saveAuthCode(String email, int authCode) {
        session.setAttribute("auth_code:" + email, authCode);
    }

    //인증번호 확인
    public Boolean checkAuthNumber(String email, int authNum) {
        String key = "auth_code:" + email;
        String savedAuthNum = (String) session.getAttribute(key).toString();
        return savedAuthNum != null && savedAuthNum.equals(String.valueOf(authNum)); //저장된 수가 null이 아니고 입력값과 같다면 true
    }

    //임의의 6자리 양수 생성
    public void makeNumberRend() {
        Random rand = new Random();
        String randNum = "";

        for (int i = 0; i < 6; i++) {
            randNum += Integer.toString(rand.nextInt(10));
        }

        authNumber = Integer.parseInt(randNum);



    }

    //mail을 어디서 보내는지, 어디로 보내는지, 인증 번호를 html 형식으로 어떻게 보내는지 작성
    public String joinEmail(String email){
        makeNumberRend();
        String setFrom = smtpId; //emailConfig에 설정한 자신의 이메일 주소
        String toMail = email; //controller에서 전달한 사용자 이메일
        String title = "SWLUG 회원가입 이메일 인증 관련 메일입니다.";

        String content = "";
        content += "<div style='margin:100px;'>";
        content += "<h1> 서울여자대학교 SWLUG 인증번호 안내 메일입니다.</h1>";
        content += "<br>";
        content += "<p>안녕하세요, SWLUG 입니다.<p>";
        content += "<br>";
        content += "<p>해당 이메일은 회원가입을 위한 인증번호 안내 메일입니다.<p>";
        content += "<br>";
        content += "<p>하단 인증번호를 '이메일 인증번호' 칸에 입력하여 가입을 완료해주세요..<p>";
        content += "<br>";
        content += "<div align='center' style='border:1px solid black; font-family:verdana';></br>";
        content += "<h3 style='color:blue;'>회원가입 인증 코드입니다.</h3></br>";
        content += "<div style='font-size:130%'>";
        content += "CODE : <strong>";
        content += authNumber + "</strong><div><br/> "; // 인증번호 넣기
        content += "</div>";

        mailSend(setFrom, toMail, title, content);

        return Integer.toString(authNumber);

    }

    //이메일 전송
    public void mailSend(String setFrom, String toMail, String title, String content){
        //Config에서 정의한 JavaMailSender객체 사용, MimeMessage 객체 생성
        MimeMessage message = mailSender.createMimeMessage();

        try{
            //이메일 메시지 관련 설정 수행
            // true를 전달, multipart 형식의 메시지 지원 + utf-8 전달, 문자 인코딩 설정
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom(setFrom); //이메일 발신자 주소 설정
            helper.setTo(toMail); //이메일 수신자 주소 설정
            helper.setSubject(title); //이메일 제목 설정
            helper.setText(content, true); // 이메일 내용 설정
            mailSender.send(message); // 이메일 전송

        }
        catch(MessagingException e){
            //이메일 서버 연결 X, 잘못된 메일 주소 설정, 인증 오류 발생 등
            e.printStackTrace();
        }

        //세션에 저장
        saveAuthCode(toMail, authNumber);

    }
}
