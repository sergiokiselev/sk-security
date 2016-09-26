package ch.rasc.sec.controller;

import ch.rasc.sec.dto.UserDto;
import ch.rasc.sec.dto.VerifyDto;
import ch.rasc.sec.service.UserService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Controller
@Log4j
public class LoginController {

	@Autowired
	private UserService userService;

	@RequestMapping("/login")
	public String home(Model model) {
		model.addAttribute("userDto", new UserDto());
		return "login";
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String handleLogin(@ModelAttribute UserDto userDto, Model model) {
		boolean isLogged = false;
		try {
			isLogged = userService.handleLogin(userDto.getLogin(), userDto.getPassword());
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			log.warn(e);
		}
		if (isLogged) {
			long userId = userService.findIdByEmail(userDto.getLogin());
			VerifyDto verifyDto = new VerifyDto();
			verifyDto.setUserId(userId);
			model.addAttribute("verifyDto", verifyDto);
			return "login_second";
		} else {
			return "login";
		}
	}

	@RequestMapping(value = "/verify", method = RequestMethod.POST)
	public String verifyCode(@ModelAttribute VerifyDto verifyDto, Model model) {
		boolean verified = false;
		try {
			 verified = userService.verifyCode(verifyDto.getUserId(), verifyDto.getCode());
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			log.warn(e);
		}
		if (verified) {
			return "home";
		} else {
			model.addAttribute("error", "Wrong code");
			return "login_second";
		}
	}
}
