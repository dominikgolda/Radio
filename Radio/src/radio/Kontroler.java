package radio;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;




public class Kontroler implements Runnable, BasicPlayer{
	
	private boolean PAUSED;
	private boolean STOPED;
	Object m_notifier = new Object();
	Object m_signal;
	ExchangeBuffer buforInternet;
	AudioPlayer odtwarzacz;
	InternetReader intRead;
	Buffer buf;
	List<BasicPlayer> classList;
	
	public Kontroler(String radioURL,boolean stoped,Object signal) throws MalformedURLException, IOException, LineUnavailableException, UnsupportedAudioFileException{
		STOPED = stoped;
		m_signal = signal;
		buforInternet = new ExchangeBuffer(88200); //16384
		GetStreamInfo streamInfo = new GetStreamInfo("http://wroclaw.radio.pionier.net.pl:8000/pl/tuba10-1.mp3");
		buf = new Buffer(buforInternet);
		odtwarzacz = null;
		intRead = null;
		intRead = new InternetReader(buforInternet,streamInfo.getStream());
		Thread watekInternetowy;
		watekInternetowy = new Thread(intRead);
		watekInternetowy.start();
		
		//czekamy na nape³nienie bufora
		synchronized(buforInternet){
			if(!buforInternet.getDataAvailableFlag()){	//tu nale¿y dopisaæ jeszcze flagi, stop (exit?)
				try{				
					buforInternet.wait();
				}catch (InterruptedException  e){e.printStackTrace();}
			}
		}
		//odczyt z bufora
		buf.write();
		odtwarzacz = new AudioPlayer(streamInfo.getStreamFormat(),buf,m_notifier,88200);
		intRead.stop();
		
		classList = new ArrayList<BasicPlayer>();
		classList.add(intRead);
		classList.add(buf);
		classList.add(odtwarzacz);
	}
	
	public  void run(){

		Thread  watekPlayera;
		watekPlayera = new Thread(odtwarzacz);
		watekPlayera.start();
		intRead.play();
		
		while(true){
			
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
						System.out.println("K : Otrzyma³em powiadomienie");
					}catch (InterruptedException e){e.printStackTrace();}
				}
			}
			
			if(buforInternet.getDataAvailableFlag()){
				System.out.println("K : przepisujê dane");
				buf.write();
				synchronized(m_notifier){			//informacja dla AudioPlayer, ¿e dane s¹ ju¿ dostêpne
					m_notifier.notify();
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
	public void play() {
		if(STOPED==true && PAUSED == true){
			STOPED = false;
			PAUSED = true;
		}else if(STOPED==true){
			STOPED = false;
		}else if(PAUSED==true){
			PAUSED = true;
		}
		for(BasicPlayer au:classList){
			au.play();
		}
		
	}
	
	

}


