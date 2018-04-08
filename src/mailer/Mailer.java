package mailer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.Message;

import exceptions.CustomException;
import util.ConfigManager;
import util.SMIRFConstants;

/**
 * Mailer class that sends emails to people. 
 * 
 * Derived from the gmail-quickstart example code from GMAIL API
 * 
 * @author Vivek Venkatraman Krishnan
 *
 */

public class Mailer {
	/** Application name. */
	private static final String APPLICATION_NAME = "SMIRFWeb";

	private static final String credentialLocation = ".credentials/smirf.utmost.gmail.java";

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), credentialLocation);


	/** Global instance of the scopes required by this quickstart.
	 *
	 * If modifying these scopes, delete your previously saved credentials
	 * at credentialLocation -- ~/.credentials/smirf.utmost.gmail.java
	 */
	private static final List<String> SCOPES = GmailScopes.all().stream().collect(Collectors.toList());


	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	private static Map<String, List<InternetAddress>> mailingList = new HashMap<>();

	private static Map<String, String> levelNames = new HashMap<>();


	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);

			levelNames.put("F", "FATAL");
			levelNames.put("W", "WARN");
			levelNames.put("I", "INFO");
			levelNames.put("D", "DEBUG");

			String mailingListFile = ConfigManager.getSmirfMap().get("MAILING_LIST_FILE");

			List<String> mailingListLines = Files.readAllLines(Paths.get(mailingListFile));

			for(String s: mailingListLines){

				String[] chunks = s.split("\\s+");

				if(s.contains("#") || chunks.length < 3) continue;

				String name = chunks[0];
				String email = chunks[1];
				String mailList = chunks[2];

				InternetAddress address = new InternetAddress(email,name);

				for(String i: levelNames.keySet()) {

					if(mailList.contains(i)) {
						List<InternetAddress> list = mailingList.getOrDefault(i, new ArrayList<>());
						list.add(address);
						mailingList.put(i, list);
					}

				}



			}

			System.err.println("Mailing list created:" + mailingList);

		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}



	public static void sendEmail(Object content) {

		MimeMessage message = null;
		InternetAddress from = null;


		try {
			
			from = new InternetAddress("smirf.utmost@gmail.com");


			if(content instanceof CustomException) {

				CustomException e = (CustomException) content;
				List<InternetAddress> to = mailingList.get(e.getLevel());
				
				if(e.getMessage().startsWith("TEST")) {
					to.clear();
					to.add(new InternetAddress("vivekvenkris@gmail.com"));
				}

				System.err.println(e.getLevel() + "  "+ to + " " +  e.getEmailSubject());


				message = createEmailWithAttachmentsAndInline(to, from, "[SMIRF] " + "["+levelNames.get(e.getLevel())+"] " + 
						 e.getEmailSubject(), e.getEmailBody(), e.getEmailAttachments(), e.getEmailInline()); 


			}
			else if (content instanceof java.lang.String ) {
				List<InternetAddress> to = mailingList.get(SMIRFConstants.levelDebug);

				message = createEmailWithAttachmentsAndInline(to, from, "[SMIRF] debug email", (String)content, null, null);
			}
			
			else if (content instanceof java.lang.Exception ) {
				
				List<InternetAddress> to = mailingList.get(SMIRFConstants.levelInfo);

				message = createEmailWithAttachmentsAndInline(to, from, "[SMIRF] exception email:"+ ((java.lang.Exception) content).getMessage(),
						ExceptionUtils.getStackTrace(((java.lang.Exception) content)).replaceAll("\n", "<br/>"), null, null);
			}
			
			sendMessage(getGmailService(), from.getAddress(), message);


		} catch (AddressException e1) {
			e1.printStackTrace();
		} catch (MessagingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	

	}


	public static Message sendMessage(Gmail service,
			String userId,
			MimeMessage emailContent)
					throws MessagingException, IOException {
		Message message = createMessageWithEmail(emailContent);
		message = service.users().messages().send(userId, message).execute();

		System.out.println("Message id: " + message.getId());
		System.out.println(message.toPrettyString());
		return message;
	}	

	/**
	 * 
	 * @param to To address list
	 * @param from sender email
	 * @param subject
	 * @param bodyText
	 * @param attachments Files to add as attachments
	 * @param inlines Files to add inline
	 * @return
	 * @throws MessagingException
	 */
	public static MimeMessage createEmailWithAttachmentsAndInline(List<InternetAddress> to, InternetAddress from, String subject, String bodyText, List<File> attachments, List<InlineAttachment> inlines) throws MessagingException {

		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		email.setFrom(from);
		email.setSubject(subject);

		InternetAddress[] array = new InternetAddress[to.size()];
		email.addRecipients(javax.mail.Message.RecipientType.TO,
				to.toArray(array));

		BodyPart header = new MimeBodyPart();
		String htmlText = "<html><body>";

		htmlText += "<p>" + bodyText + "</p>";

		if(inlines != null) {

			for(InlineAttachment attachment: inlines) {


				htmlText += "<H1>"+ attachment.getTitle() +"</H1> <p>" 
						+ attachment.getText() + "</p> <br/> <br/>"
						+ "<img src=\"cid:"+attachment.getFile().getName()+"\"/> <br/>";


			}
		}

		htmlText += "</html></body>";

		header.setContent(htmlText, "text/html");

		if(inlines !=null || attachments !=null ) { 

			MimeMultipart multipart = new MimeMultipart("related");
			if(inlines != null) {

				for(InlineAttachment attachment: inlines) {


					htmlText += "<H1>"+ attachment.getTitle() +"</H1> <p>" 
							+ attachment.getText() + "</p> <br/> <br/>"
							+ "<img src=\"cid:"+attachment.getFile().getName()+"\"/> <br/>";


				}


				multipart.addBodyPart(header);


				for(InlineAttachment attachment: inlines) {



					DataSource fds = new FileDataSource(attachment.getFile());

					BodyPart image = new MimeBodyPart();
					image.setDataHandler(new DataHandler(fds));
					image.setHeader("Content-ID", "<"+attachment.getFile().getName()+">");
					image.setDescription(MimeBodyPart.INLINE);


					// add image to the multipart
					multipart.addBodyPart(image);


				}
			}


			if(attachments != null) {

				for(File f: attachments) {

					MimeBodyPart attachment = new MimeBodyPart();

					DataSource source = new FileDataSource(f);

					attachment.setDataHandler(new DataHandler(source));
					attachment.setFileName(f.getName());

					multipart.addBodyPart(attachment);

				}
			}


			email.setContent(multipart);
		}
		else {
			email.setContent(htmlText, "text/html; charset=utf-8");
		}


		return email;


	}



	/**
	 * Creates an authorized Credential object.
	 * @return an authorized Credential object.
	 * @throws Exception 
	 */
	public static Credential authorize() throws Exception {

		GoogleClientSecrets clientSecrets =
				GoogleClientSecrets.load(JSON_FACTORY, new FileReader(new File("/home/vivek/SMIRF/mail/client_secret.json")));

		System.err.println("client secrets loaded" + clientSecrets);



		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow =
				new GoogleAuthorizationCodeFlow.Builder(
						HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(DATA_STORE_FACTORY)
				.setAccessType("offline")
				.build();
		Credential credential = new AuthorizationCodeInstalledApp(
				flow, new LocalServerReceiver()).authorize("user");
		System.out.println(
				"Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
		return credential;
	}

	/**
	 * Build and return an authorized Gmail client service.
	 * @return an authorized Gmail client service
	 * @throws Exception 
	 */
	public static Gmail getGmailService() throws Exception {
		Credential credential = authorize();
		System.err.println("authorized");
		return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME)
				.build();
	}

	/**
	 * Create a MimeMessage using the parameters provided.
	 *
	 * @param to email address of the receiver
	 * @param from email address of the sender, the mailbox account
	 * @param subject subject of the email
	 * @param bodyText body text of the email
	 * @return the MimeMessage to be used to send email
	 * @throws MessagingException
	 */
	public static MimeMessage createEmail(String to,
			String from,
			String subject,
			String bodyText)
					throws MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		email.setFrom(new InternetAddress(from));
		email.addRecipient(javax.mail.Message.RecipientType.TO,
				new InternetAddress(to));
		email.setSubject(subject);
		email.setText(bodyText);
		return email;
	}


	/**
	 * Create a MimeMessage using the parameters provided.
	 *
	 * @param to Email address of the receiver.
	 * @param from Email address of the sender, the mailbox account.
	 * @param subject Subject of the email.
	 * @param bodyText Body text of the email.
	 * @param file Path to the file to be attached.
	 * @return MimeMessage to be used to send email.
	 * @throws MessagingException
	 */
	public static MimeMessage createEmailWithInlineAttachment(String to,
			String from,
			String subject,
			String bodyText,
			File file)
					throws MessagingException, IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		email.setFrom(new InternetAddress(from));
		email.addRecipient(javax.mail.Message.RecipientType.TO,
				new InternetAddress(to));
		email.setSubject(subject);

		// This mail has 2 part, the BODY and the embedded image
		MimeMultipart multipart = new MimeMultipart("related");

		// first part (the html)
		BodyPart messageBodyPart = new MimeBodyPart();
		String htmlText = "<H1>Hello</H1><img src=\"cid:image\">";
		messageBodyPart.setContent(htmlText, "text/html; charset=ISO-8859-1");
		// add it
		multipart.addBodyPart(messageBodyPart);

		// second part (the image)
		messageBodyPart = new MimeBodyPart();
		DataSource fds = new FileDataSource(file);

		messageBodyPart.setDataHandler(new DataHandler(fds));
		messageBodyPart.setHeader("Content-ID", "<image>");

		// add image to the multipart
		multipart.addBodyPart(messageBodyPart);

		// put everything together

		email.setContent(multipart);

		return email;
	}



	/**
	 * Create a MimeMessage using the parameters provided.
	 *
	 * @param to Email address of the receiver.
	 * @param from Email address of the sender, the mailbox account.
	 * @param subject Subject of the email.
	 * @param bodyText Body text of the email.
	 * @param file Path to the file to be attached.
	 * @return MimeMessage to be used to send email.
	 * @throws MessagingException
	 */
	public static MimeMessage createEmailWithAttachment(String to,
			String from,
			String subject,
			String bodyText,
			File file)
					throws MessagingException, IOException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		MimeMessage email = new MimeMessage(session);

		email.setFrom(new InternetAddress(from));
		email.addRecipient(javax.mail.Message.RecipientType.TO,
				new InternetAddress(to));
		email.setSubject(subject);

		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setContent(bodyText, "text/plain");

		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(mimeBodyPart);

		mimeBodyPart = new MimeBodyPart();
		DataSource source = new FileDataSource(file);

		mimeBodyPart.setDataHandler(new DataHandler(source));
		mimeBodyPart.setFileName(file.getName());

		multipart.addBodyPart(mimeBodyPart);
		email.setContent(multipart);

		return email;
	}

	/**
	 * Create a message from an email.
	 *
	 * @param emailContent Email to be set to raw of message
	 * @return a message containing a base64url encoded email
	 * @throws IOException
	 * @throws MessagingException
	 */
	public static Message createMessageWithEmail(MimeMessage emailContent)
			throws MessagingException, IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		emailContent.writeTo(buffer);
		byte[] bytes = buffer.toByteArray();
		String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
		Message message = new Message();
		message.setRaw(encodedEmail);
		return message;
	}




	public static void main(String[] args) throws Exception {
		// Build a new authorized API client service.
		Gmail service = getGmailService();

		List<InternetAddress> to = new ArrayList<>();
		to.add(new InternetAddress("vivekvenkris@gmail.com"));
		//to.add(new InternetAddress("viveikvk@gmail.com"));

		File dir = new File("/data/mopsr/results/2018-01-19-05:24:18/");

		List<File> pngs = Arrays.asList(dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.contains("png");
			}
		}));

		List<InlineAttachment> inlines = pngs.subList(0, 2).stream().map(f -> new InlineAttachment("Sample title", f.getAbsolutePath(), f)).collect(Collectors.toList());



		MimeMessage message = createEmailWithAttachmentsAndInline(to, new InternetAddress("smirf.utmost@gmail.com"), "Test everything", "body text blah blah", pngs, inlines);



		sendMessage(service, "smirf.utmost@gmail.com", message);

		System.err.println("service loaded");

		// Print the labels in the user's account.
		//	        String user = "me";
		//	        ListLabelsResponse listResponse =
		//	            service.users().labels().list(user).execute();
		//	        List<Label> labels = listResponse.getLabels();
		//	        if (labels.size() == 0) {
		//	            System.out.println("No labels found.");
		//	        } else {
		//	            System.out.println("Labels:");
		//	            for (Label label : labels) {
		//	                System.out.printf("- %s\n", label.getName());
		//	            }
		//	        }
	}


}