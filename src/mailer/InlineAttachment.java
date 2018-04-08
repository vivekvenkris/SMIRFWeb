package mailer;

import java.io.File;

public class InlineAttachment {

	private String title;
	private String text;
	private File file;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public InlineAttachment(String title, String text, File file) {
		super();
		this.title = title;
		this.text = text;
		this.file = file;
	}
	
	
	
}
