package ch.rasc.sec.controller;

import ch.rasc.sec.dto.AesKeyDto;
import ch.rasc.sec.dto.restresponse.ErrorDto;
import ch.rasc.sec.dto.restresponse.RestResponse;
import ch.rasc.sec.dto.TokenDto;
import ch.rasc.sec.dto.TotpSecretDto;
import ch.rasc.sec.dto.UserDto;
import ch.rasc.sec.dto.VerifyDto;
import ch.rasc.sec.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LoginController {

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public RestResponse<VerifyDto> handleLogin(@RequestBody UserDto userDto) {
		try {
			return new RestResponse<>(userService.handleLogin(userDto.getLogin(), userDto.getPassword(),userDto.getSessionId()));
		} catch (Exception e) {
			log.debug(e.getLocalizedMessage());
			return new RestResponse<>(new ErrorDto(e.getLocalizedMessage()));
		}
	}

	@RequestMapping(value = "/verify", method = RequestMethod.POST)
	public RestResponse<TotpSecretDto> verifyCode(@RequestBody VerifyDto verifyDto) {
		try {
			return new RestResponse<>(userService.verifyCode(verifyDto.getSessionId(), verifyDto.getCode()));
		} catch (Exception e) {
			log.debug(e.getLocalizedMessage());
			return new RestResponse<>(new ErrorDto(e.getLocalizedMessage()));
		}
	}

	@RequestMapping(value = "/rsakey", method = RequestMethod.POST)
	public RestResponse<AesKeyDto> getAesKey(@RequestBody String rsaKey) {
		try {
			return new RestResponse<>(userService.getAesKey(rsaKey));
		} catch (Exception e) {
			log.debug(e.getLocalizedMessage());
			return new RestResponse<>(new ErrorDto(e.getLocalizedMessage()));
		}
	}

	@RequestMapping(value = "/token", method = RequestMethod.POST)
	public RestResponse<String> verifyToken(@RequestBody TokenDto token) {
		try {
			return new RestResponse<>(userService.verifyToken(token));
		} catch (Exception e) {
			log.debug(e.getLocalizedMessage());
			return new RestResponse<>(new ErrorDto(e.getLocalizedMessage()));
		}
	}


}
