package ch.rasc.sec.service;

/**
 * User: NotePad.by
 * Date: 2/22/2016.
 */
public interface MailService {
    void send(String from, String to, String subject, String body);
}
