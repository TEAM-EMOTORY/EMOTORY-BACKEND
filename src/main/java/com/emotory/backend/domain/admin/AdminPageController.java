package com.emotory.backend.domain.admin;

import com.emotory.backend.domain.authoring.dto.AuthoringDtos.CreateRequest;
import com.emotory.backend.domain.authoring.service.AuthoringService;
import com.emotory.backend.global.exception.ErrorResponse;
import com.emotory.backend.global.exception.authoring.AuthoringException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/** 관리자 화면만 담당하며, 인증과 동화 제작 처리는 기존 기능을 재사용한다. */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminPageController {
    private final AdminSessionController sessions;
    private final AuthoringService authoring;

    @ModelAttribute
    void preventCaching(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
    }

    @GetMapping({"", "/"})
    public String home() {
        return "redirect:/admin/stories";
    }

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute AdminSessionController.PasswordRequest form,
                        BindingResult binding, HttpServletRequest request, HttpServletResponse response,
                        Model model) {
        if (binding.hasErrors()) {
            response.setStatus(400);
            model.addAttribute("error", "비밀번호를 입력하세요. 최대 72바이트까지 사용할 수 있습니다.");
            return "admin/login";
        }
        var result = sessions.authenticate(form, request, response);
        if (result.getStatusCode().is2xxSuccessful()) {
            return "redirect:/admin/stories";
        }
        response.setStatus(result.getStatusCode().value());
        model.addAttribute("error", ((ErrorResponse) result.getBody()).getMessage());
        return "admin/login";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        sessions.logout(request, response);
        return "redirect:/admin/login";
    }

    @GetMapping("/stories")
    public String stories(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("drafts", authoring.list(page));
        return "admin/stories";
    }

    @PostMapping("/stories")
    public String create(@Valid @ModelAttribute CreateRequest form, BindingResult binding,
                         HttpServletResponse response, Model model) {
        if (binding.hasErrors()) {
            response.setStatus(400);
            model.addAttribute("drafts", authoring.list(0));
            model.addAttribute("error", "제목은 255자 이내로 입력하세요.");
            return "admin/stories";
        }
        return "redirect:/admin/stories/" + authoring.create(form).id();
    }

    @GetMapping("/stories/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("draft", authoring.get(id));
        return "admin/editor";
    }

    @ExceptionHandler(AuthoringException.class)
    public String authoringError(AuthoringException exception, HttpServletResponse response, Model model) {
        response.setStatus(exception.getErrorCode().getStatus());
        model.addAttribute("error", exception.getErrorCode().getMessage());
        return "admin/error";
    }
}
