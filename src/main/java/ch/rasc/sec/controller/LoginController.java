package ch.rasc.sec.controller;

import ch.rasc.sec.dto.AesKeyDto;
import ch.rasc.sec.dto.UserDto;
import ch.rasc.sec.dto.VerifyDto;
import ch.rasc.sec.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@RestController
public class LoginController {

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public VerifyDto handleLogin(@RequestBody UserDto userDto) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
		return userService.handleLogin(userDto.getLogin(), userDto.getPassword(),userDto.getSessionId());
	}

	@RequestMapping(value = "/verify", method = RequestMethod.POST)
	public String verifyCode(@RequestBody VerifyDto verifyDto) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		// TODO return token here
		return userService.verifyCode(verifyDto.getSessionId(), verifyDto.getCode());
	}

	@RequestMapping(value = "/rsakey", method = RequestMethod.POST)
	public AesKeyDto getAesKey(@RequestBody String rsaKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IOException, NoSuchPaddingException {
		return userService.getAesKey(rsaKey);
	}


}
