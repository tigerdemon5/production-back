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

    @Value("${SMTP_ID}")
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
        Object savedAuthNumObj = session.getAttribute(key);

        if (savedAuthNumObj == null) {
            return false;  // 인증번호를 요청한 적 없거나 세션 만료
        }

        int savedAuthNum = (Integer) savedAuthNumObj;
        return savedAuthNum == authNum;
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
        content += "<div align='center' style='border:1px solid #666; border-radius: 8px; font-family:verdana; padding: 20px;'>"; // 패딩 추가
        content += "<h3 style='color:blue;'>회원가입 인증 코드입니다.</h3><br/>";
        content += "<div style='font-size:130%'>";
        content += "CODE : <strong>" + authNumber + "</strong><br/><br/>";
        content += "</div>";
        content += "</div>";

        String sign = "";
        sign += "<div style='margin-top: 30px; border-top: 1px solid #666; padding-top: 20px;'>";
        sign += "<strong style='font-size: 24px; color: #232124;'>SWLUG</strong><br>";
        sign += "<span style='font-size: 16px; color: #666;'>서울여자대학교 정보보호학과 소학회</span><br><br>";
        sign += "<span style='color: #444;'>"
                + "<p style='font-size: 16px;'><strong>SWLUG</strong>는 학부생이 중심이 되어 운영하는 학술 동아리로,<br>"
                + "1999년 대학연합리눅스 유저그룹에 포함된 연합동아리로 시작하였습니다.</p>"
                + "</span><br><br>";
        sign += "<span>"
                + "<strong style='font-size: 20px; color: #232124;'>Contact Us</strong><br>"
                + "✉️ <a href='mailto:swu.swlug@gmail.com' style='font-size: 16px; color: #0056b3; text-decoration: none;' target='_blank'>swu.swlug@gmail.com</a><br>"
                + "⭐️ <a href='https://instagram.com/security_swlug' target='_blank' style='font-size: 16px; color: #0056b3; text-decoration: none;'>@security_swlug</a><br>"
                + "🌐 <a href='https://swlug.com' target='_blank' style='font-size: 16px; color: #0056b3; text-decoration: none;'>swlug.com</a>"
                + "</span>";
        sign += "</div>";

        String allContent = content + "<br><br>" + sign; // content와 sign을 분리하여 추가
        mailSend(setFrom, toMail, title, allContent);

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
            helper.setBcc("swu.swlug@gmail.com");
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
