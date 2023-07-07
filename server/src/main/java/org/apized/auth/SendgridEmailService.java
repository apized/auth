package org.apized.auth;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import io.micronaut.context.annotation.Value;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;

import java.util.Map;

@Singleton
public class SendgridEmailService implements EmailService{

  @Value("${sendgrid.api-key}")
  String apiKey;

  @Value("${sendgrid.from}")
  String from;

  SendGrid sendGrid;

  @PostConstruct
  public void init() {
    sendGrid = new SendGrid(apiKey);
  }

  @Override
  @SneakyThrows
  public void send(String templateId, String name, String email, Map<String, Object> variables) {
    Personalization personalization = new Personalization();
    personalization.addTo(new Email(email, name));

    variables.forEach(personalization::addDynamicTemplateData);

    Mail mail = new Mail();
    mail.setFrom(new Email(from));
    mail.addPersonalization(personalization);
    mail.setTemplateId(templateId);

    Request request = new Request();
    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    request.setBody(mail.build());
    Response response = sendGrid.api(request);
    System.out.println(response);
  }
}
