package radio;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public interface BasicPlayer {
	public void pause();
	public void stop();
	public void play() throws MalformedURLException, IOException, LineUnavailableException, UnsupportedAudioFileException;
	public void exitRadio();
}
