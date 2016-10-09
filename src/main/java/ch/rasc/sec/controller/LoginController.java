package ch.rasc.sec.controller;

import ch.rasc.sec.dto.UserDto;
import ch.rasc.sec.dto.VerifyDto;
import ch.rasc.sec.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
public class LoginController {

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public VerifyDto handleLogin(@RequestBody UserDto userDto) throws NoSuchAlgorithmException, InvalidKeyException {
		return userService.handleLogin(userDto.getLogin(), userDto.getPassword());
	}

	@RequestMapping(value = "/verify", method = RequestMethod.POST)
	public String verifyCode(@RequestBody VerifyDto verifyDto) throws InvalidKeyException, NoSuchAlgorithmException {
		// TODO return token here
		return userService.verifyCode(verifyDto.getSessionId(), verifyDto.getCode());
	}
}
