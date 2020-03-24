package game2D;

import java.io.*;
import javax.sound.sampled.*;

public class Sound extends Thread {

	public static int NO_EFFECT = 1;
	public static int ECHO_EFFECT = 2;
	public static int FAST_EFFECT = 3;
	private int currentEffect;

	private Clip clip;
	private File file;

	private boolean loop;

	/**
	 * Plays a sound - doesn't loop by default
	 * @param fname String representing the file path of the file to be played
	 * @param effect int representing what sound effect should be applied to the file
	 */
	public Sound(String fname, int effect, boolean loop) {
		this.loop = loop;
		file = new File(fname);
		currentEffect = effect;
	}

	/**
	 * run will play the actual sound but you should not call it directly.
	 * You need to call the 'start' method of your sound object (inherited
	 * from Thread, you do not need to declare your own). 'run' will
	 * eventually be called by 'start' when it has been scheduled by
	 * the process scheduler.
	 */
	public void run() {
		try {
			byte[] soundBytes = applyEffect(file, currentEffect);

			ByteArrayInputStream inputStream = new ByteArrayInputStream(soundBytes);
			AudioFormat format = AudioSystem.getAudioInputStream(file).getFormat();
			AudioInputStream audioStream = new AudioInputStream(inputStream, format, soundBytes.length);

			clip = AudioSystem.getClip();
			clip.open(audioStream);
			clip.start();

			if (loop) {
				clip.loop(Integer.MAX_VALUE);
			}


		} catch (IOException ioe) {
			System.out.println("Error during applying effect to sound file");
			ioe.printStackTrace();
		} catch (Exception e) {
			System.out.println("Error playing sound file");
			e.printStackTrace();
		}
	}

	/**
	 * Applies one of 3 effects (none, echo, speed-up) on a wav file.
	 * @param file - The file to apply the effect to
	 * @param effect - What effect to apply
	 * @return Returns a byte[] representing the transformed sound as bytes
	 * @throws IOException
	 */
	private byte[] applyEffect(File file, int effect) throws IOException {
		// input stream is just the original file's bytes
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
		// output stream will represent the sound with effects applied to it
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int read;
		byte[] tempBuffer = new byte[1024];
		// keep reading the input stream while there are bytes left to read
		while ((read = in.read(tempBuffer)) > 0) {
			switch (effect) {
				// No effect
				case 1:
					// no effect selected, just write input stream bytes to output
					out.write(tempBuffer, 0, read);
					currentEffect = 0;
					break;
				// Echo
				case 2:
					// this is the original sound of the file
					// writing in just this "read" to the output byte[] means no
					// effect was actually applied
					out.write(tempBuffer, 0, read);
					// re-writing the read bytes but making the quieter by dividing
					// creates an echo effect
					out.write(tempBuffer, 0, read / 2);
					currentEffect = 1;
					break;
				// Speed music up
				case 3:
					// divide the
					out.write(tempBuffer, 0, read / 2);
					currentEffect = 2;
					break;
				default:
					throw new RuntimeException("You must specify the effect to be applied");
			}
		}
		// flush the output stream
		out.flush();
		// return the bytes of the stream
		return out.toByteArray();
	}


	/**
	 * Enables switching between no effect / echoing / speeding up sound
	 * @param effect
	 */
	public void switchEffect(int effect) {

		// there is a race condition, clip initialisation is slow and when the game starts
		// the clip could potentially be null when applying the new level "no sound effect"
		// this is a safe guard to avoid an exception on game startup
		if (clip == null) {
			return;
		}
		if (currentEffect == effect) {
			// nothing to do here, trying to switch to already used effect
			return;
		}

		try {
			// get current clip's progress and stop it from playing
			int positionToPlayFrom = clip.getFramePosition();

			// accounts for effect being faster
			if (currentEffect == Sound.FAST_EFFECT) {
				positionToPlayFrom = positionToPlayFrom * 2;
			} else if (currentEffect != Sound.FAST_EFFECT && effect == Sound.FAST_EFFECT) {
				positionToPlayFrom = positionToPlayFrom / 2;
			}

			clip.stop();

			byte[] soundBytes = applyEffect(file, effect);

			ByteArrayInputStream inputStream = new ByteArrayInputStream(soundBytes);
			AudioFormat format = AudioSystem.getAudioInputStream(file).getFormat();
			AudioInputStream audioStream = new AudioInputStream(inputStream, format, soundBytes.length);

			clip = AudioSystem.getClip();
			clip.open(audioStream);
			clip.setFramePosition(positionToPlayFrom);
			clip.start();

			// ensure looping is retained.
			if (loop) {
				clip.loop(Integer.MAX_VALUE);
			}

			// ensure we keep current effect updated
			currentEffect = effect;

		} catch (Exception e) {
			System.out.println("Error switching sound effects");
			e.printStackTrace();
		}
	}
}
