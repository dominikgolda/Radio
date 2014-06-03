package radio;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;



public class PauseBuffer extends Buffer implements BasicAudio,BasicPlayer {

	private ExchangeBuffer m_exchangeBuf;

	protected ArrayListWraper m_listBufor;
	
	private OutputStream fileStream;
	private Boolean isRecording = false;

	public PauseBuffer(ExchangeBuffer exchangeBuf){
		m_listBufor = new ArrayListWraper();
		m_exchangeBuf = exchangeBuf;
	}
	@Override
	public synchronized void write(){
		if(m_exchangeBuf.getDataAvailableFlag()){
			byte[] tmp= new byte[m_exchangeBuf.getBufferSize()];
			m_exchangeBuf.getBufferData(tmp, 0);
			m_listBufor.add(tmp);
			if(isRecording){
				try {
					fileStream.write(tmp);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	@Override
	public synchronized int read(byte[] buff, int startPos, int bytesToRead){
		return m_listBufor.read(buff, startPos, bytesToRead);
	}


	@Override
	public void stop() {
		m_listBufor.clear();
	}

	@Override
	public void play() {

	}

	/**
	 * <p> Jest zaimplementowana jako metoda pusta. Buffer nie zmienia swojego stanu w przypadku zatrzymania odtwarzania.
	 */
	@Override
	public void pause() {}

	@Override
	public void exitRadio() {
		stopRecording();
	}

	/**
	 * <p> Pozwala rozpocz¹æ zapisywanie do pliku.
	 * @param filePath - œcie¿ka do pliku razem z jego nazw¹ i rozszerzeniem.
	 * @throws IOException
	 * @throws FileNotFoundException jest to podklasa IOException
	 */
	@Override
	public synchronized  void startRecording(String filePath) throws IOException{
		if(!isRecording){
			fileStream = new FileOutputStream(filePath);
			isRecording = true;
		}else{
			fileStream.flush();
			fileStream.close();
			fileStream = new FileOutputStream(filePath);
		}
	}

	/**
	 * <p> Pozwala zakoñczyæ zapisywanie do pliku.
	 */
	@Override
	public synchronized void stopRecording(){
		try{
			fileStream.flush();
			fileStream.close();
			isRecording = false;
		}catch(IOException e){
			//e.printStackTrace();
		}catch(Exception e1){}
	}

	/**
	 * <p> Pozwala zapisaæ ca³y bufor (od rozpoczêcia odtwarzania).
	 * <p>Je¿eli parametr <b>czyKontynuowacZapis</b> jest równy <b>true</b>
	 * zapis dokonywany jest do pliku podanego w argumencie <b>filePath</b>, po czym kontynuowany jest zapis do wczeœniej otwartego pliku.
	 * <p>Je¿eli parametr <b>czyKontynuowacZapis</b> jest równy <b>false</b> zapis dokonywany jest do pliku podanego w w argumencie <b>filePath</b>,
	 * po czym plik ten jest zamykany.
	 * @param filePath œcie¿ka do pliku razem z jego nazw¹ i rozszerzeniem.
	 * @param czyKontynuowacZapis okreœla, czy nale¿y po zapisaniu ca³ego bufora kontynuowaæ zapis do tego samego pliku
	 * @throws IOException
	 * @throws FileNotFoundException jest to podklasa IOException
	 */
	@Override
	public synchronized  void recordBuffer(String filePath, boolean czyKontynuowacZapis) throws IOException{
		List<byte[]> list = m_listBufor.getData();
		if(czyKontynuowacZapis){
			startRecording(filePath);
			for(byte [] ar:list){
				fileStream.write(ar, 0, ar.length);
			}
		}else{
			OutputStream outStr = new FileOutputStream(filePath);
			for(byte [] ar:list){
				outStr.write(ar, 0, ar.length);
			}
			outStr.flush();
			outStr.close();
		}
	}

}
