package radio;




public class RewindBuffer extends PauseBuffer {

	public RewindBuffer(ExchangeBuffer exchangeBuf) {
		super(exchangeBuf);
	}
	
	/**
	 * <p> Ustawia pozycj� w buforze, od kt�rej nast�pi odtwarzanie.
	 * @param pos liczba z przedzia�u [0,1) okre�laj�ca w kt�rym punkcie nale�y rozpocz�� dalesze odtwarzanie
	 */
	public void setBufferPositionRelative(double pos){
		super.m_listBufor.setPosition((int) (pos*m_listBufor.getTotalLength()));
	}


}
