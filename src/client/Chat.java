package client;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet.ColorAttribute;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import communication.ChatList;
import communication.Command;
import communication.CommandType;

public class Chat extends JTextPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8584074616264236671L;

	private final Style err;
	private final Style chat;
	private final Style bold;
	private final Style light;
	private final StyledDocument doc = getStyledDocument();
	
	public Chat() {
		super();
		err = doc.addStyle("err", null);
		StyleConstants.setForeground(err, Color.RED);
		chat = doc.addStyle("chat", null);
		bold = doc.addStyle("bold", null);
		StyleConstants.setBold(bold, true);
		light = doc.addStyle("light", null);
		StyleConstants.setForeground(light, Color.LIGHT_GRAY);
		StyleConstants.setItalic(light, true);
	}
	
	public void exec(Command com) {
		try {
			switch(CommandType.get(com.context, ChatList.values())) {
			case ChatList.ERR:
				doc.insertString(doc.getLength(), "\n"+com.body, err);
				break;
			case ChatList.CHAT:
				Command mess = Command.decode(com.body);
				doc.insertString(doc.getLength(), "\n"+mess.context+":", bold);
				doc.insertString(doc.getLength(), mess.body, chat);
				break;
			case ChatList.SERVER:
				doc.insertString(doc.getLength(), "\n"+com.body, light);
				break;
			default:
				doc.insertString(doc.getLength(), "\n"+com.body, null);
				break;
			
			}
		} catch(BadLocationException e) {
		}
	}

	public void clear() {
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
}
