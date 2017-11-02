package com.robc.intellibeat.sounds;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

public class Sound {
	private static final Logger logger = Logger.getInstance(Sound.class);

	private final byte[] bytes;
	private final String name;
	private Clip clip;

	public Sound(byte[] bytes, String name) {
		this.bytes = bytes;
		this.name = name;
	}

	public Sound play() {
		playSoundFromStream(new ByteArrayInputStream(bytes), 0);
		return this;
	}

	public Sound playAndWait() {
		playSoundFromStream(new ByteArrayInputStream(bytes), 0);

		if (clip != null) {
			try {
				Thread.sleep(clip.getMicrosecondLength() / 1000);
			} catch (InterruptedException ignored) {
			}
		}
		return this;
	}

	public Sound playInBackground() {
		playSoundFromStream(new ByteArrayInputStream(bytes), Clip.LOOP_CONTINUOUSLY);
		return this;
	}

	public Sound stop() {
		if (clip != null) {
			clip.stop();
		}
		return this;
	}

	/**
	 * Originally copied from {@link com.intellij.util.ui.UIUtil}.
	 */
	private void playSoundFromStream(final InputStream inputStream, final int loopCount) {
		try {
			final Clip clip = AudioSystem.getClip();
			InputStream stream = inputStream;
			if (!stream.markSupported()) stream = new BufferedInputStream(stream);
			clip.open(AudioSystem.getAudioInputStream(stream));

			final AtomicReference<LineListener> lineListener = new AtomicReference<LineListener>();
			LineListener listener = new LineListener() {
				@Override public void update(@NotNull LineEvent event) {
					if (event.getType() == LineEvent.Type.STOP) {
						clip.close();
						clip.removeLineListener(lineListener.get());
					}
				}
			};
			lineListener.set(listener);
			clip.addLineListener(lineListener.get());

			this.clip = clip;

			// The wrapper thread is unnecessary, unless it blocks on the Clip finishing;
			new Thread(new Runnable() {
				@Override public void run() {
					clip.loop(loopCount);
				}
			}).start();
		} catch (Exception e) {
			logger.warn(e);
		}
	}

	@Override public String toString() {
		return "Sound{name='" + name + '\'' + '}';
	}
}
