package ch.rasc.sec.controller;

import ch.rasc.sec.model.User;
import ch.rasc.sec.repository.UserRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;

@Controller
public class QRCodeController {

	@Autowired
	private UserRepository userRepository;

	@RequestMapping(value = "/qrcode/{username}.png", method = RequestMethod.GET)
	public void qrcode(HttpServletResponse response,
			@PathVariable("username") String username)
					throws WriterException, IOException {

		User user = userRepository.findByEmail(username);
		if (user != null) {
			response.setContentType("image/png");
			String contents = "otpauth://totp/" + username + ":" + user.getEmail()
					+ "?secret=" + user.getSecret() + "&issuer=SpringSecurityTOTP";
			//String contents = String.valueOf(new SecureRandom().nextBoolean());
			QRCodeWriter writer = new QRCodeWriter();
			BitMatrix matrix = writer.encode(contents, BarcodeFormat.QR_CODE, 200, 200);
			MatrixToImageWriter.writeToStream(matrix, "PNG", response.getOutputStream());
			response.getOutputStream().flush();
		}
	}


}
