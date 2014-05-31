package radio;
import java.io.*;


public class InternetReader  implements Runnable, BasicPlayer {
	private int m_rozmiarBufora;
	private ExchangeBuffer m_exchangeBuffer;
	private InputStream m_strumien;
	private int m_wskZapisu=0;
	private byte[] m_buffer;
	private boolean STOP = false;
	public InternetReader(ExchangeBuffer buffer, InputStream inStream){
		m_exchangeBuffer = buffer;
		m_rozmiarBufora = buffer.getBufferSize();
		m_buffer = new byte[m_rozmiarBufora];
		m_strumien = inStream;
	}

	@Override
	public void run(){
		int len;
		while(true){
			try{
				//////////////////////////////////////////////////////
				////		ODBIÓR DANYCH Z INTERNETU			//////
				//////////////////////////////////////////////////////
				while((len = m_strumien.read(m_buffer,m_wskZapisu,m_rozmiarBufora-m_wskZapisu))>0){
					m_wskZapisu = m_wskZapisu + len;
				}

				//////////////////////////////////////////////////////
				////	PRZESY£ANIE DANYCH DO W¥TKU G£ÓWNEGO      ////
				//////////////////////////////////////////////////////
				if(!STOP){
					synchronized(m_exchangeBuffer){
						while(m_exchangeBuffer.getDataAvailableFlag()){//czekamy, a¿ bufor bêdzie wolny
							try{
								m_exchangeBuffer.wait();
							}catch (InterruptedException  e){e.printStackTrace();}
						}
					}
					m_exchangeBuffer.fillBuffer(m_buffer);
					m_wskZapisu = 0;
					synchronized(m_exchangeBuffer){
						m_exchangeBuffer.notifyAll();
					}
				}
			}catch(IOException e){
				System.out.println(e.getMessage());
			}catch(IndexOutOfBoundsException e){
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}

	}

	@Override
	public void stop(){
		STOP = true;
	}

	@Override
	public void play(){
		STOP = false;
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
}