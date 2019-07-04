package com.cipher.cloud.logging;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.cipher.cloud.ui.UserInterface;

public class SwingAppender extends AppenderSkeleton {
	private UserInterface userInterface = UserInterface.getInstance();

	public SwingAppender() {

	}

	@Override
	public void append(final LoggingEvent event) {
		String eventText = event.getMessage().toString();

		if (event.getLevel().toString().equals("ERROR")) {
			userInterface.setConsole(eventText, false);
		} else {
			userInterface.setConsole(eventText, true);
		}
	}

	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}
}
