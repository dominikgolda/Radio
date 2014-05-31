package radio;
import java.util.ArrayList;
import java.util.List;



public class Buffer implements BasicAudio,BasicPlayer {

	private ExchangeBuffer m_exchangeBuf;

	private int listIndex;
	private int arrayPosition;
	private List<byte[]> m_listBufor;
	
	public Buffer(ExchangeBuffer exchangeBuf){
		m_listBufor = new ArrayList<byte[]>();
		m_exchangeBuf = exchangeBuf;
		listIndex = 0;
		arrayPosition = 0;
	}
	
	public synchronized void write(){
		if(m_exchangeBuf.getDataAvailableFlag()){
			byte[] tmp= new byte[m_exchangeBuf.getBufferSize()];
			m_exchangeBuf.getBufferData(tmp, 0);
			m_listBufor.add(tmp);
		}
	}
	@Override
	public synchronized int read(byte[] buff, int startPos, int bytesToRead){
		if(m_listBufor.size()<=listIndex){return -1;} // nie ma dalszych elementów listy
		byte []	tab = m_listBufor.get(listIndex);
		int odczytane = 0;
		int i;
		while(odczytane<bytesToRead){	
			if(arrayPosition<tab.length){
				for(i = 0;arrayPosition<tab.length&&i<bytesToRead;i++,arrayPosition++){
					buff[i+startPos] = tab[arrayPosition];
				}
				odczytane +=i;
			}else{
				listIndex++;
				arrayPosition=0;
				if(m_listBufor.size()<=listIndex){return odczytane;}
				else{
					tab = m_listBufor.get(listIndex);
				}
			}
		}
		return odczytane;
	}


	@Override
	public void stop() {
		m_listBufor = new ArrayList<byte[]>();
		listIndex = 0;
		arrayPosition = 0;
	}

	@Override
	public void play() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * <p> Jest zaimplementowana jako metoda pusta. Buffer nie zmienia swojego stanu w przypadku zatrzymania odtwarzania.
	 */
		@Override
	public void pause() {}



	
}
