import java.io.IOException;


import javax.sound.sampled.*;

import java.io.*;



public class AudioPlayer implements Runnable{

	boolean m_playerStop;
	AudioFormat m_format;
	BasicAudio m_prevModule;
	int m_buffSize;
	byte[] m_buffer;
	ByteArrayInputStream m_byteArrayStream;
	AudioInputStream m_audioStream;
	SourceDataLine m_odtwarzacz;
	Object m_notifier;
	
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
			long t;
			while(!m_playerStop){
				
			    t= System.currentTimeMillis();
				
				while ((przeczytane=m_audioStream.read(bufor))!=-1){	
					m_odtwarzacz.write(bufor, 0,przeczytane);
				}
				int pom = m_prevModule.read(m_buffer, 0, m_buffSize);
				if(pom>0){
					m_byteArrayStream.reset();
					auStr1 = AudioSystem.getAudioInputStream(m_byteArrayStream);
					m_audioStream = AudioSystem.getAudioInputStream(m_format,auStr1);
					System.out.println("AP: przeczytano "+pom+" czas: "+ (System.currentTimeMillis()-t));
				}else{
					try{
						m_notifier.wait();//czekam, a¿ dojd¹ nowe dane
					}catch(InterruptedException e){}
					Thread.sleep(20);
					System.out.println("AP: przeczytano "+pom+" czas: "+ (System.currentTimeMillis()-t));					
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


	public void stopPlayer(){
		m_playerStop = true;
	}
}