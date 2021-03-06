package radio;
import java.io.*;


public class InternetReader  implements Runnable, BasicPlayer {
	private int m_rozmiarBufora;
	private ExchangeBuffer m_exchangeBuffer;
	private InputStream m_strumien;
	private int m_wskZapisu=0;
	private byte[] m_buffer;
	private boolean STOP = false;
	private boolean EXIT =false;
	public InternetReader(ExchangeBuffer buffer, InputStream inStream){
		m_exchangeBuffer = buffer;
		m_rozmiarBufora = buffer.getBufferSize();
		m_buffer = new byte[m_rozmiarBufora];
		m_strumien = inStream;
	}

	@Override
	public void run(){
		int len;
		try{
			while(!EXIT){
				try{
					//////////////////////////////////////////////////////
					////		ODBI�R DANYCH Z INTERNETU			//////
					//////////////////////////////////////////////////////
					while((len = m_strumien.read(m_buffer,m_wskZapisu,m_rozmiarBufora-m_wskZapisu))>0){
						m_wskZapisu = m_wskZapisu + len;
					}

					//////////////////////////////////////////////////////
					////	PRZESY�ANIE DANYCH DO W�TKU G��WNEGO      ////
					//////////////////////////////////////////////////////
					if(!STOP){
						synchronized(m_exchangeBuffer){
							while(m_exchangeBuffer.getDataAvailableFlag()){//czekamy, a� bufor b�dzie wolny
								try{
									System.out.println("IR: Czekam na wolny bufor");
									m_exchangeBuffer.wait();
								}catch (InterruptedException  e){e.printStackTrace();}
							}
						}
						m_exchangeBuffer.fillBuffer(m_buffer);
						System.out.println("IR: przepisa�em dane do bufora");
						m_wskZapisu = 0;
					}else{
						synchronized (m_exchangeBuffer) {
							try{
								System.out.println("IR: zatrzymany");
								m_exchangeBuffer.wait();
							}catch (InterruptedException  e){e.printStackTrace();}
						}
					}
				}catch(IOException e){
					System.out.println(e.getMessage());
				}catch(IndexOutOfBoundsException e){
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
				System.out.println("IR: Jeden obieg petli");
			}
		}finally{
			try{
				m_strumien.close();
			}catch(IOException e){}
		}
		Thread.currentThread().interrupt();
		return;

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
	public void pause() {}

	@Override
	public void exitRadio() {
		EXIT = true;

	}
}