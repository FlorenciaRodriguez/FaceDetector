package util.email;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendEmail {

	public static void main(String[] args) {

		sendFromGMail("Nada", null, null, null);
	}

	/**
	 * @param folder
	 *            el path de la carpeta a enviar
	 * @param userName
	 *            nombre de usuario de Gmail (del que envía)
	 * @param pass
	 *            contraseña (del que envía)
	 * @param recipient
	 *            email del que recibe
	 * 
	 **/
	public static void sendFromGMail(String folder, String userName, String pass, String recipient) {
		String subject = "Java send mail Eye-Log Project";
		String body = "----";

		String[] to = { recipient };
		Properties props = System.getProperties();
		String host = "smtp.gmail.com";
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.user", userName);
		props.put("mail.smtp.password", pass);
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.auth", "true");

		Session session = Session.getDefaultInstance(props);
		MimeMessage message = new MimeMessage(session);

		try {
			message.setFrom(new InternetAddress(userName));
			InternetAddress[] toAddress = new InternetAddress[to.length];

			// To get the array of addresses
			for (int i = 0; i < to.length; i++) {
				toAddress[i] = new InternetAddress(to[i]);
			}

			for (int i = 0; i < toAddress.length; i++) {
				message.addRecipient(Message.RecipientType.TO, toAddress[i]);
			}

			message.setSubject(subject);
			message.setText(body);

			DataSource source = new FileDataSource(folder);
			message.setDataHandler(new DataHandler(source));
			message.setFileName(folder);

			Transport transport = session.getTransport("smtp");
			transport.connect(host, userName, pass);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		} catch (AddressException ae) {
			ae.printStackTrace();
		} catch (MessagingException me) {
			me.printStackTrace();
		}
	}
}