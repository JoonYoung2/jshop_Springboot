package com.example.jshop.controller;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.jshop.dto.LoginLoggerDTO;
import com.example.jshop.dto.MemberDTO;
import com.example.jshop.repository.MemberRepository;
import com.example.jshop.service.KakaoService;
import com.example.jshop.service.MemberService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class KakaoController {
	@Autowired
	private HttpSession session;
	@Autowired
	private KakaoService service;
	@Autowired
	private MemberService memberService;
	@Autowired
	private MemberRepository repo;

	@GetMapping("kakao_login")
	public String redirectkakao(@RequestParam("code") String code, Model model) throws IOException {

		// 접속토큰 get
		String kakaoToken = service.getReturnAccessToken(code);

		// 접속자 정보 get
		MemberDTO result = service.getUserInfo(kakaoToken);

		String user_id = result.getUser_id();
		String user_nm = result.getUser_nm();
		model.addAttribute("msg1", user_id);
		model.addAttribute("msg2", user_nm);

		return "forward:kakao_register?id=" + user_id;
	}

	@GetMapping("kakao_register")
	public String kakao_register(@RequestParam("id") String id) {
		try {
			if (repo.findId(id) != null) {
				MemberDTO check = repo.findId(id);
				session.setAttribute("user_id", check.getUser_id());
				session.setAttribute("loginType", check.getLoginType());
				session.setAttribute("totalCart_cnt", check.getTotalCart_cnt());
				session.setAttribute("result_price", check.getResult_price());
				memberService.loginLogger(check);
				return "redirect:/";

			}
		} catch (Exception e) {
			log.error("error -> {}", e);
		}
		return "signup/kakao_register";
	}

	@PostMapping("kakao_register")
	public String kakao_register(MemberDTO member, Model model) {
		try {
			String msg = service.kakao_register(member);
			if ("이미 등록".equals(msg) || "등록 완료".equals(msg)) {
				return "redirect:/";
			}
			model.addAttribute("msg", msg);
			model.addAttribute("msg1", member.getUser_id());
			model.addAttribute("msg2", member.getUser_nm());
			return "signup/kakao_register";
		} catch (Exception e) {
			log.error("error -> {}", e);
		}
		return "redirect:kakao_register";
	}

	@GetMapping("/kakao_logout")
	public String kakao_logout() {
		String login_time = (String) session.getAttribute("login_time");
		LoginLoggerDTO logger;
		try {
			logger = repo.findLoginLogger(login_time);
			memberService.logoutLogger(logger);
		} catch (Exception e) {
			log.error("error -> {}", e);
		}

		session.invalidate();
		return "redirect:/";
	}

	@GetMapping("/kakao_infoDelete")
	public String kakao_infoDelete() {
		String user_id = (String) session.getAttribute("user_id");
		repo.infoDelete(user_id);
		repo.tmpDelete(user_id);
		repo.creditDelete(user_id);
		session.invalidate();
		return "redirect:/";
	}
}