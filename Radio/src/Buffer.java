import java.util.ArrayList;
import java.util.List;



public class Buffer extends BasicAudio {

	ExchangeBuffer m_exchangeBuf;
	
	
	int listIndex;
	int arrayPosition;
	List<byte[]> m_listBufor = new ArrayList<byte[]>();
	
	public Buffer(ExchangeBuffer exchangeBuf){
		m_exchangeBuf = exchangeBuf;
		listIndex = 0;
		arrayPosition = 0;

	}
	
	public void write(){
		if(m_exchangeBuf.getDataAvailableFlag()){
			byte[] tmp= new byte[m_exchangeBuf.getBufferSize()];
			m_exchangeBuf.getBufferData(tmp, 0);
			m_listBufor.add(tmp);
		}
	}
	@Override
	public int read(byte[] buff, int startPos, int bytesToRead){
		if(m_listBufor.size()<=listIndex){return 0;} // nie ma dalszych elementów listy
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
				if(m_listBufor.size()<=listIndex){return odczytane;}
				else{
					arrayPosition=0;
					tab = m_listBufor.get(listIndex);
				}
			}
		}
		return odczytane;
	}


	
}
