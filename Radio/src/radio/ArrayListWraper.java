/**
 * Pozwla na obs³ugê ArrayList tak jak ma to byæ na
 */
package radio;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dominik
 *
 */
public class ArrayListWraper {
	private List<byte[]> lista;
	private int listIndex = 0;
	private int arrayPosition = 0;
	private int totalLength = 0;
	private List<Integer> listaDlugosci;

	public ArrayListWraper(){
		lista = new ArrayList<byte[]>();
		listaDlugosci = new ArrayList<Integer>();
	}

	/**
	 * <p> Pozwala dodaæ tablicê bajtów do koñca listy.
	 * @param tab tablica bajtów.
	 */
	public void add(byte[] tab){
		lista.add(tab);
		listaDlugosci.add(tab.length);
		totalLength +=tab.length;		
	}

	public  int read(byte[] buff, int startPos, int bytesToRead){
		if(lista.size()<=listIndex){return -1;} // nie ma dalszych elementów listy
		byte []	tab = lista.get(listIndex);
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
				if(lista.size()<=listIndex){return odczytane;}
				else{
					tab = lista.get(listIndex);
				}
			}
		}
		return odczytane;
	}

	public void setPosition(int arrayStartPos){
		if(arrayStartPos>=totalLength){return;} //sprawdzam, czy nie zarz¹dano elementu, którego nie ma

		////	znajdowanie indeksu pierwszego elementu do odczytania ///
		int pom = 0;
		int i=-1;
		/// 	znajdowanie tablicy w której znajduje siê rz¹dany element ///
		while(pom<arrayStartPos){
			i++;
			pom += listaDlugosci.get(i);
		}
		if(i==-1){
			i=0;
			arrayPosition = 0;
		}
		listIndex = i;
		arrayPosition = pom - arrayStartPos;

	}

	public int getTotalLength(){
		return totalLength;
	}

	/**
	 * Kasuje dane i doprowadza obiekt do stanu pocz¹tkowego.
	 */
	public void clear(){
		listIndex = 0;
		arrayPosition = 0;
		totalLength = 0;
		lista = new ArrayList<byte[]>();
		listaDlugosci = new ArrayList<Integer>();
	}
	
	public List<byte[]> getData(){
		return lista;
	}
}
