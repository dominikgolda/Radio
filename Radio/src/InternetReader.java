import java.io.*;


public class InternetReader implements Runnable {
	int m_rozmiarBufora;
	ExchangeBuffer m_exchangeBuffer;
	InputStream m_strumien;
	int m_wskZapisu=0;
	int m_wskOdczytu=0;
	byte[] m_buffer;
	public InternetReader(ExchangeBuffer buffer, InputStream inStream){
		m_exchangeBuffer = buffer;
		m_rozmiarBufora = buffer.getBufferSize();
		m_buffer = new byte[m_rozmiarBufora];
		m_strumien = inStream;
	}
	
	@Override
	public void run(){
		int len;
		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		while(true){
			try{
				while((len = m_strumien.read(m_buffer,m_wskZapisu,m_rozmiarBufora-m_wskZapisu))>0){
					m_wskZapisu = m_wskZapisu + len;
				}
				synchronized(m_exchangeBuffer){
					while(m_exchangeBuffer.getDataAvailableFlag()){//czekamy, a¿ bufor bêdzie wolny
						try{
							System.out.println("IR: zasypiam");
							m_exchangeBuffer.wait();
							System.out.println("IR: obudzony");
						}catch (InterruptedException  e){e.printStackTrace();}
					}
				}
				m_exchangeBuffer.fillBuffer(m_buffer);
				m_wskZapisu = 0;
				System.out.println("IR: Bufor napelniony");
				synchronized(m_exchangeBuffer){
					m_exchangeBuffer.notifyAll();
					System.out.println("IR: Wyslano powiadomienie");
				}
			}catch(IOException e){
				System.out.println(e.getMessage());
			}catch(IndexOutOfBoundsException e){
				System.out.println("Watek poboczny");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		
	}
	
}
