package radio;
import java.io.IOException;






import javax.sound.sampled.*;

import java.io.*;



public class AudioPlayer implements BasicPlayer, Runnable{

	private boolean m_playerPause = false;
	private boolean m_playerStop = true;
	private boolean m_exitPlayer = false;
	private AudioFormat m_format;
	private BasicAudio m_prevModule;
	private int m_buffSize;
	private byte[] m_buffer;
	private ByteArrayInputStream m_byteArrayStream;
	private AudioInputStream m_audioStream;
	private SourceDataLine m_odtwarzacz;
	private Object m_notifier;


	AudioPlayer(AudioFormat streamFormat,BasicAudio prevModule, Object notifier, int buffSize) throws LineUnavailableException, UnsupportedAudioFileException, IOException{
		m_format = streamFormat;
		m_prevModule = prevModule;
		m_notifier = notifier;
		//Tworzenie obiektu SourceDataLine dla formatu odczytanego przez GetStreamInfo 
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, m_format);
		m_odtwarzacz = (SourceDataLine) AudioSystem.getLine(info);
		m_odtwarzacz.open(m_format); //otwarcie linii danych

		//bufor, z którego odczytujemy zdekodowane dane 
		m_buffSize = buffSize;
		m_buffer = new byte[m_buffSize];

		//Tworzenie AudioInputStream, pozwalalaj¹cego dekodowaæ srumieñ
		m_byteArrayStream = new ByteArrayInputStream(m_buffer);
		m_prevModule.read(m_buffer, 0, m_buffSize);

		AudioInputStream auStr1 = AudioSystem.getAudioInputStream(m_byteArrayStream);
		m_audioStream = AudioSystem.getAudioInputStream(m_format,auStr1);
	}

	public void run(){
		try{
			//zmienne pomocnicze
			int dlRamki = 16;						//d³ugoœæ ramki
			int maksDlRamki = dlRamki*100;			//d³ugoœæ bufora którym odczytujemy 
			byte[] bufor= new byte[maksDlRamki];
			int przeczytane;
			//			int availableData;						//liczba bajtów danych
			m_odtwarzacz.open();
			m_odtwarzacz.start();
			AudioInputStream auStr1;
			while(!m_exitPlayer){
				try{
					//////	czekamy a¿ player wyjdzie ze stanu PAUSE	/////
					do{
						while (  ((przeczytane=m_audioStream.read(bufor))!=-1) && !m_playerStop&&!m_playerPause&!m_exitPlayer){ 	
							m_odtwarzacz.write(bufor, 0,przeczytane);
						}
						//////je¿eli player zosta³ zatrzymany (nie wy³¹czony) czekamy a¿ w¹tek Kontrolera powiadomi nas, ¿e nale¿y wzonwiæ odtwarzanie.	/////
						if(m_playerPause){	
							System.out.println("AP: odtwarzanie wstrzymane");
							synchronized(m_notifier){
								try{
									m_notifier.wait();
								}catch(InterruptedException e){}
							}
						}
					}while(m_playerPause);
				}catch(ArrayIndexOutOfBoundsException e){e.printStackTrace();}	//potrzebne, bo biblioteka do obs³ugi mp3 czasem rzuca taki wyj¹tek

				//////	czekamy na wznowieniu playera po zatrzymaniu	/////
				while(m_playerStop){
					m_audioStream = null;
					synchronized(m_notifier){
						try{
							m_notifier.wait();
						}catch(InterruptedException e){}
					}
				}

				//////	wyjœcie z programu	/////
				if(m_exitPlayer){
					m_odtwarzacz.drain();
					m_odtwarzacz.stop();
					Thread.currentThread().interrupt();
					return;
				}
				int pom;
				do{
					//////	Odczyt danych od poprzedniego modu³u	/////
					pom = m_prevModule.read(m_buffer, 0, m_buffSize);
					//////	Je¿eli s¹ dane tworzymy strumieñ dekoduj¹cy mp3	/////

					if(pom>0){
						try{
							m_byteArrayStream.reset();
							auStr1 = AudioSystem.getAudioInputStream(m_byteArrayStream);
							m_audioStream = AudioSystem.getAudioInputStream(m_format,auStr1);
						}catch(UnsupportedAudioFileException e){
							e.printStackTrace();
							System.out.println("pom = "+pom);
						}
						//////	Je¿eli nie ma danych czekamy napowiadomienie od Kontroler'a	/////
					}else{
						synchronized(m_notifier){
							try{
								System.out.println("AP: oczekujê na dane");
								m_notifier.wait();//czekam, a¿ dojd¹ nowe dane
							}catch(InterruptedException e){}
						}					
					}
				}while(pom<=0);
			}

		}catch(Exception e){
			System.out.println("Error");
			System.out.println(e.getMessage());
			e.printStackTrace();

		}
		//////	drugie wyjœcie z programu	/////
		m_odtwarzacz.drain();
		m_odtwarzacz.stop();
		Thread.currentThread().interrupt();
		return;

	}


	@Override
	public void pause() {
		m_playerPause = true;
	}

	@Override
	public void stop() {
		m_playerStop = true;	
	}


	@Override
	public void play() {
		m_playerPause = false;
		m_playerStop = false;
	}

	public void exitRadio(){
		m_exitPlayer = true;
	}
}