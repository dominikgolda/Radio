package radio;
class ExchangeBuffer{
	
	/**
	 *  Rozmiar bufora.
	 */
	private int m_bufferSize;
	/**
	 *   Bufor przechowywuj¹cy dane.
	 */
	private volatile byte [] m_buffer;
	private volatile boolean m_newDataAvailable = false;
	
	
	public ExchangeBuffer(int bufferSize){
		this.m_bufferSize = bufferSize;
		this.m_buffer = new byte[bufferSize];
	}
	
	
	/**
	 * <p> Przepisuje dane z tablicy tab podanej jako argument do bufora.
	 * @param tab Tablica 
	 * @throws ArrayIndexOutOfBoundsException wyj¹tek zostanie rzucony, je¿eli tablica tab bêdize mniejsza ni¿ rozmiar bufora ExchangeBuffer
	 */
	public synchronized void fillBuffer(byte [] tab) throws ArrayIndexOutOfBoundsException{
		for(int i = 0;i<m_bufferSize;i++){
			m_buffer[i] = tab[i];
		}
		m_newDataAvailable = true;
		notifyAll();
	}
	
	/**
	 *  <p> Funkcja kopiuje zawartoœæ bufora do  zmiennej przekazanej do funkcji i kasuje flagê m_bufferSize
	 * @param tab - tablica do której kopiowana jest zawartoœæ bufora.
	 * @throws ArrayIndexOutOfBoundsException wyj¹tek zostanie rzucony, je¿eli tablica tab bêdize mniejsza ni¿ rozmiar bufora ExchangeBuffer
	 */
	public synchronized void getBufferData(byte [] tab,int pos)  throws ArrayIndexOutOfBoundsException{
		for(int i = 0;i<m_bufferSize;i++){
			tab[i+pos] = m_buffer[i];
		}
		m_newDataAvailable = false;
		notifyAll();
	}

	/**
	 * 
	 * @return - aktualny rozmiar bufora
	 */
	public  int getBufferSize(){
		return m_bufferSize;
	}
	
	public synchronized boolean getDataAvailableFlag(){
		return m_newDataAvailable;
	}
}