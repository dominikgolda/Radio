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
			System.out.println("K : Obieg pêtli");
			synchronized(m_signal){
				while(STOPED){
					System.out.println("K : zatrzymany - STOPED");
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
						System.out.println("K : Otrzyma³em powiadomienie");
					}catch (InterruptedException e){e.printStackTrace();}
				}
			}

			if(buforInternet.getDataAvailableFlag()){
				System.out.println("K : przepisujê dane");
				buf.write();
				synchronized(m_notifier){			//informacja dla AudioPlayer, ¿e dane s¹ ju¿ dostêpne
					m_notifier.notifyAll();
				}
			}

		}
		System.out.println("K : Koñczê pracê");
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
	public void play() throws MalformedURLException, IOException, LineUnavailableException, UnsupportedAudioFileException{
		if(m_firstStart){

			GetStreamInfo streamInfo = new GetStreamInfo(radioURL);
			buf = new RewindBuffer(buforInternet);
			odtwarzacz = null;
			intRead = null;
			intRead = new InternetReader(buforInternet,streamInfo.getStream());
			Thread watekInternetowy;
			watekInternetowy = new Thread(intRead);
			watekInternetowy.start();
			System.out.println("1");
			//czekamy na nape³nienie bufora
			synchronized(buforInternet){
				if(!buforInternet.getDataAvailableFlag()){	//tu nale¿y dopisaæ jeszcze flagi, stop (exit?)
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
		if(classList!=null){
			for(BasicPlayer au:classList){
				au.exitRadio();
			}
		}
	}


	/**
	 * <p> Ustawia pozycjê od której nast¹pi odtwarzanie. Mo¿na ustawiæ dowolny punkt pomiêdzy chwil¹ obecn¹ a rozpoczêciem odtwarzania lub ostatnim zatrzymaniem (funkcj¹ stop) playera
	 * Zatrzymanie playera funkcja pause() nie wp³ywa na dostêpny odcinek czasu.
	 * @param pos liczba z przedzia³u [0,1) okreœlaj¹ca w którym punkcie nale¿y rozpocz¹æ dalesze odtwarzanie.
	 */
	public void setBufferPositionRelative(double pos,boolean startPlay){
		try{
			RewindBuffer b = (RewindBuffer) buf;
			odtwarzacz.stop();
			b.setBufferPositionRelative(pos);
			Integer a;
			for(Integer i = 1;i<Integer.MAX_VALUE/20;i++){
				a = i;
			}
			boolean pom;
			//czekanie, ¿eby na pewno wy³¹czy³o siê radio, ¿eby nie by³o pomieszanych audycji z ró¿nych momentó
			do{
				pom = false;
				synchronized(m_signal){		
					try{
						m_signal.wait(50);
					}catch(Exception e1){pom = true;}
				}
			}while(pom);
			if(startPlay){
				odtwarzacz.play();
			}
			System.out.println("K : ustalono now¹ pozycjê "+pos);
		}catch(ClassCastException e){}
	}


	/**
	 * <p> Pozwala rozpocz¹æ zapisywanie do pliku.
	 * @param filePath - œcie¿ka do pliku razem z jego nazw¹ i rozszerzeniem.
	 * @throws IOException
	 * @throws FileNotFoundException jest to podklasa IOException
	 */
	public void startRecording(String filePath) throws IOException{
		buf.startRecording(filePath);
	}

	/**
	 * <p> Pozwala zakoñczyæ zapisywanie do pliku.
	 */
	public void stopRecording(){
		buf.stopRecording();
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
	public void recordBuffer(String filePath, boolean czyKontynuowacZapis) throws IOException{
		buf.recordBuffer(filePath, czyKontynuowacZapis);
	}


}


