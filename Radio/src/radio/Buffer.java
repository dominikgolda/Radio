package radio;

import java.io.IOException;

public abstract class Buffer implements BasicAudio, BasicPlayer {

	@Override
	public void pause() {

	}

	@Override
	public void stop() {

	}

	@Override
	public void play() {

	}

	@Override
	public void exitRadio() {

	}

	@Override
	public int read(byte[] buff, int startPos, int bytesToRead) {
		return -1;
	}

	public void write() {
		
	}

	public void startRecording(String filePath) throws IOException {
		
	}

	public void stopRecording() {
		
	}

	public void recordBuffer(String filePath, boolean czyKontynuowacZapis) throws IOException {
		
	}

}
