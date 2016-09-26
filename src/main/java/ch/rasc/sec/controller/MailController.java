package ch.rasc.sec.controller;

import ch.rasc.sec.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

@RestController
class MailController {

    @Autowired
    private MailService mailService;

    @RequestMapping("/mail")
    @ResponseStatus(HttpStatus.CREATED)
    SimpleMailMessage send() throws MessagingException {
        mailService.send("sergio.kiselev509@gmail.com", "sergio.kiselev509@gmail.com", "Subject", "Hello kitty");
        return null;
    }
}