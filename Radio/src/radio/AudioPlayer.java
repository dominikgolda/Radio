package radio;
import java.io.IOException;




import javax.sound.sampled.*;

import java.io.*;



public class AudioPlayer implements BasicPlayer, Runnable{

	private boolean m_playerPause = false;
	private boolean m_playerStop = false;
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

		//bufor, z kt�rego odczytujemy zdekodowane dane 
		m_buffSize = buffSize;
		m_buffer = new byte[m_buffSize];

		//Tworzenie AudioInputStream, pozwalalaj�cego dekodowa� srumie�
		m_byteArrayStream = new ByteArrayInputStream(m_buffer);
		m_prevModule.read(m_buffer, 0, m_buffSize);

		AudioInputStream auStr1 = AudioSystem.getAudioInputStream(m_byteArrayStream);
		m_audioStream = AudioSystem.getAudioInputStream(m_format,auStr1);
	}

	public void run(){
		try{

			//zmienne pomocnicze
			int dlRamki = 16;						//d�ugo�� ramki
			int maksDlRamki = dlRamki*100;			//d�ugo�� bufora kt�rym odczytujemy 
			byte[] bufor= new byte[maksDlRamki];
			int przeczytane;
			//			int availableData;						//liczba bajt�w danych
			m_odtwarzacz.open();
			m_odtwarzacz.start();
			AudioInputStream auStr1;
			while(!m_playerStop){


				try{
					do{	//czekamy a� player wyjdzie ze stanu PAUSE
						while (  ((przeczytane=m_audioStream.read(bufor))!=-1) && !m_playerStop&&!m_playerPause){ 	
							m_odtwarzacz.write(bufor, 0,przeczytane);
						}
						if(m_playerPause){	//je�eli player zosta� zatrzymany (nie wy��czony) czekamy a� w�tek Kontrolera powiadomi nas, �e nale�y wzonwi� odtwarzanie.
							synchronized(m_notifier){
								try{
									m_notifier.wait();
								}catch(InterruptedException e){}
							}
						}
					}while(m_playerPause);
				}catch(ArrayIndexOutOfBoundsException e){e.printStackTrace();}	//potrzebne, bo biblioteka do obs�ugi mp3 czasem rzuca taki wyj�tek


				int pom = m_prevModule.read(m_buffer, 0, m_buffSize);
				if(pom>0){
					try{
						m_byteArrayStream.reset();
						auStr1 = AudioSystem.getAudioInputStream(m_byteArrayStream);
						m_audioStream = AudioSystem.getAudioInputStream(m_format,auStr1);
					}catch(UnsupportedAudioFileException e){
						e.printStackTrace();
						System.out.println("pom = "+pom);
					}
				}else{
					synchronized(m_notifier){
						try{
							m_notifier.wait();//czekam, a� dojd� nowe dane
						}catch(InterruptedException e){}
					}					
				}
			}
			m_odtwarzacz.drain();
			m_odtwarzacz.stop();


		}catch(Exception e){
			System.out.println("Error");
			System.out.println(e.getMessage());
			e.printStackTrace();

		}

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
	}
}