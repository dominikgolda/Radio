package radio;




public class RewindBuffer extends PauseBuffer {

	public RewindBuffer(ExchangeBuffer exchangeBuf) {
		super(exchangeBuf);
	}
	
	/**
	 * <p> Ustawia pozycjê w buforze, od której nast¹pi odtwarzanie.
	 * @param pos liczba z przedzia³u [0,1) okreœlaj¹ca w którym punkcie nale¿y rozpocz¹æ dalesze odtwarzanie
	 */
	public void setBufferPositionRelative(double pos){
		super.m_listBufor.setPosition((int) (pos*m_listBufor.getTotalLength()));
	}


}
