import java.io.IOException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;




public class Kontroler {

	
	public static void main(String[] args)  {
		Object m_notifier = new Object();
		ExchangeBuffer buforInternet = new ExchangeBuffer(16384); //16384
		GetStreamInfo streamInfo = new GetStreamInfo("http://wroclaw.radio.pionier.net.pl:8000/pl/tuba10-1.mp3");
		Buffer buf = new Buffer(buforInternet);
		AudioPlayer odtwarzacz = null;
		InternetReader intRead = null;
		try {
			intRead = new InternetReader(buforInternet,streamInfo.getStream());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		Thread watekInternetowy;
		watekInternetowy = new Thread(intRead);
		watekInternetowy.start();
		
		System.out.println("K :Internet Reader uruchomiony");
		
		//czekamy na nape³nienie bufora
		synchronized(buforInternet){
			if(!buforInternet.getDataAvailableFlag()){	//tu nale¿y dopisaæ jeszcze flagi, stop (exit?)
				try{
					System.out.println("K : Czekam na PIERWSZE dane");					
					buforInternet.wait();
					System.out.println("K : Obudzony PIERWSZY raz");
				}catch (InterruptedException  e){e.printStackTrace();}
			}
		}
		//odczyt z bufora
		buf.write();
		
		try {
			odtwarzacz = new AudioPlayer(streamInfo.getStreamFormat(),buf,m_notifier,16384);
		} catch ( LineUnavailableException| UnsupportedAudioFileException | IOException e1) {
			e1.printStackTrace();
		}
		Thread  watekPlayera;
		watekPlayera = new Thread(odtwarzacz);
		watekPlayera.start();
		System.out.println("K : Odtwarzacz uruchomiony...");

		
		while(true){
			synchronized(buforInternet){
				if(!buforInternet.getDataAvailableFlag()){
					try{
						System.out.println("K : czekam na dane");
						buforInternet.wait();
						System.out.println("K : obudzony");
					}catch (InterruptedException e){e.printStackTrace();}
				}
			}
			if(buforInternet.getDataAvailableFlag()){
				buf.write();
				System.out.println("K : dane przepisane");
				synchronized(m_notifier){
					m_notifier.notify();
				}
			}
		}
	}
	

}


