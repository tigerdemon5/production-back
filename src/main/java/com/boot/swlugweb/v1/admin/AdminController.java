package com.boot.swlugweb.v1.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
//테스트
@RestController
@RequestMapping("/api/admin/users")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserResponseDto>> getUsers() {
        return ResponseEntity.ok(adminService.getUsers());
    }

    @GetMapping("/detail")
    public ResponseEntity<AdminUserInfoResponseDto> getUserInfo(@RequestParam String userId) {

        AdminUserInfoResponseDto userInfo = adminService.getUserInfo(userId);

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/create")
    public RedirectView createUser(@RequestBody AdminCreateUserRequestDto requestDto) {

        String redirect = adminService.createUser(requestDto);

        return new RedirectView(redirect);
    }


    @PostMapping("/update")
    public RedirectView updateUser(@RequestBody AdminUserUpdateRequestDto requestDto) {
        String redirect = adminService.updateUser(requestDto);

        return new RedirectView(redirect);
    }

    @GetMapping("/delete")
    public RedirectView deleteUser(@RequestParam String userId) {
        String redirect = adminService.deleteUser(userId);

        return new RedirectView(redirect);
    }

}
