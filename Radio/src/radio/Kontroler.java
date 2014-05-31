package radio;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;




public class Kontroler implements Runnable, BasicPlayer{

	private boolean m_firstStart = true;
	private boolean PAUSED;
	private boolean STOPED;
	private boolean EXIT = false;
	private final int BUFFER_SIZE = 32768;//32768
	Object m_notifier = new Object();
	Object m_signal;
	ExchangeBuffer buforInternet;
	AudioPlayer odtwarzacz;
	InternetReader intRead;
	Buffer buf;
	List<BasicPlayer> classList;
	String radioURL;

	public Kontroler(String radioURL,boolean stoped,Object signal) throws MalformedURLException, IOException, LineUnavailableException, UnsupportedAudioFileException{
		STOPED = stoped;
		m_signal = signal;
		buforInternet = new ExchangeBuffer(BUFFER_SIZE); //16384
		this.radioURL = radioURL;

	}

	public  void run(){


		while(!EXIT){

			synchronized(m_signal){
				while(STOPED){
					try{
						m_signal.wait();
					}catch(InterruptedException e){}
				}
			}


			synchronized(buforInternet){
				if(!buforInternet.getDataAvailableFlag()){
					try{
						System.out.println("K : Zasypiam");
						buforInternet.wait();
						System.out.println("K : Otrzyma�em powiadomienie");
					}catch (InterruptedException e){e.printStackTrace();}
				}
			}

			if(buforInternet.getDataAvailableFlag()){
				System.out.println("K : przepisuj� dane");
				buf.write();
				synchronized(m_notifier){			//informacja dla AudioPlayer, �e dane s� ju� dost�pne
					m_notifier.notifyAll();
				}
			}
		}
	}

	@Override
	public void pause() {
		PAUSED = true;
		for(BasicPlayer au:classList){
			au.pause();
		}
	}

	@Override
	public void stop() {
		STOPED = true;
		for(BasicPlayer au:classList){
			au.stop();
		}
	}



	@Override
	public void play(){
		if(m_firstStart){
			try{
				GetStreamInfo streamInfo = new GetStreamInfo(radioURL);
				buf = new Buffer(buforInternet);
				odtwarzacz = null;
				intRead = null;
				intRead = new InternetReader(buforInternet,streamInfo.getStream());
				Thread watekInternetowy;
				watekInternetowy = new Thread(intRead);
				watekInternetowy.start();
				System.out.println("1");
				//czekamy na nape�nienie bufora
				synchronized(buforInternet){
					if(!buforInternet.getDataAvailableFlag()){	//tu nale�y dopisa� jeszcze flagi, stop (exit?)
						try{				
							buforInternet.wait();
						}catch (InterruptedException  e){e.printStackTrace();}
					}
				}
				System.out.println("2");
				//odczyt z bufora
				buf.write();
				odtwarzacz = new AudioPlayer(streamInfo.getStreamFormat(),buf,m_notifier,BUFFER_SIZE);
				intRead.stop();
				System.out.println("3");

				classList = new ArrayList<BasicPlayer>();
				classList.add(intRead);
				classList.add(buf);
				classList.add(odtwarzacz);
				m_firstStart = false;
				System.out.println("4");
				Thread  watekPlayera;
				watekPlayera = new Thread(odtwarzacz);
				watekPlayera.start();
				intRead.play();
				this.play();
				System.out.println("5");
				STOPED = false;

			}catch(Exception e){
				m_firstStart = true;
				e.printStackTrace();
			}
		}else{
			if(STOPED==true && PAUSED == true){
				STOPED = false;
				PAUSED = false;
			}else if(STOPED==true){
				STOPED = false;
			}else if(PAUSED==true){
				PAUSED = false;
			}
			synchronized(m_notifier){
				m_notifier.notifyAll();
			}
			synchronized(buforInternet){
				buforInternet.notifyAll();
			}
			for(BasicPlayer au:classList){
				au.play();
			}
		}
	}

	@Override
	public void exitRadio() {
		EXIT = true;
		for(BasicPlayer au:classList){
			au.exitRadio();
		}
	}


	
	
	
	
	/**
	 * <p> Pozwala rozpocz�� zapisywanie do pliku.
	 * @param filePath - �cie�ka do pliku razem z jego nazw� i rozszerzeniem.
	 * @throws IOException
	 * @throws FileNotFoundException jest to podklasa IOException
	 */
	public void startRecording(String filePath) throws IOException{
		buf.startRecording(filePath);
	}

	/**
	 * <p> Pozwala zako�czy� zapisywanie do pliku.
	 */
	public void stopRecording(){
		buf.stopRecording();
	}

	/**
	 * <p> Pozwala zapisa� ca�y bufor (od rozpocz�cia odtwarzania).
	 * <p>Je�eli parametr <b>czyKontynuowacZapis</b> jest r�wny <b>true</b>
	 * zapis dokonywany jest do pliku podanego w argumencie <b>filePath</b>, po czym kontynuowany jest zapis do wcze�niej otwartego pliku.
	 * <p>Je�eli parametr <b>czyKontynuowacZapis</b> jest r�wny <b>false</b> zapis dokonywany jest do pliku podanego w w argumencie <b>filePath</b>,
	 * po czym plik ten jest zamykany.
	 * @param filePath �cie�ka do pliku razem z jego nazw� i rozszerzeniem.
	 * @param czyKontynuowacZapis okre�la, czy nale�y po zapisaniu ca�ego bufora kontynuowa� zapis do tego samego pliku
	 * @throws IOException
	 * @throws FileNotFoundException jest to podklasa IOException
	 */
	public void recordBuffer(String filePath, boolean czyKontynuowacZapis) throws IOException{
		buf.recordBuffer(filePath, czyKontynuowacZapis);
	}


}


