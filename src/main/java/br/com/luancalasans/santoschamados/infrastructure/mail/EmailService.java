package br.com.luancalasans.santoschamados.infrastructure.mail;

import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    public EmailService(JavaMailSender mailSender, AppProperties appProperties) {
        this.mailSender = mailSender;
        this.appProperties = appProperties;
    }

    public void enviarVerificacaoCadastro(String destinatario, String nome, String link) {
        String assunto = "Confirme seu cadastro — Sanea";
        String corpo = """
                Olá %s,

                Recebemos seu cadastro no Sanea.
                Para ativar sua conta, acesse o link abaixo em até 24 horas:

                %s

                Se você não solicitou este cadastro, ignore este e-mail.

                Sanea
                """.formatted(nome, link);
        enviar(destinatario, assunto, corpo);
    }

    public void enviarVerificacaoAlteracaoEmail(String destinatario, String nome, String link) {
        String assunto = "Confirme a alteração de e-mail — Sanea";
        String corpo = """
                Olá %s,

                Foi solicitada a alteração do e-mail da sua conta no Sanea.
                Para confirmar, acesse o link abaixo em até 24 horas:

                %s

                Se você não solicitou esta alteração, ignore este e-mail e mantenha sua senha segura.

                Sanea
                """.formatted(nome, link);
        enviar(destinatario, assunto, corpo);
    }

    private void enviar(String destinatario, String assunto, String corpo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(appProperties.getMail().getFrom());
            message.setTo(destinatario);
            message.setSubject(assunto);
            message.setText(corpo);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Falha ao enviar e-mail para destinatario configurado: {}", ex.getMessage());
            throw new BusinessException("Não foi possível enviar o e-mail de verificação. Tente novamente mais tarde.");
        }
    }
}
