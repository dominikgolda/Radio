import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.*;




public class GetStreamInfo{

	String m_adresURL;
	
	GetStreamInfo(String adresURL){
		m_adresURL = adresURL;
	}
	
	
	public AudioFormat getStreamFormat() throws MalformedURLException{
		return getStreamFormat(m_adresURL);
	}
	
	public AudioFormat getStreamFormat(String adresURL) throws MalformedURLException{
		URL urlFormat = new URL(adresURL);
		
		AudioFormat format;
		AudioFormat decodedFormat;
		try{
			format = AudioSystem.getAudioFileFormat(urlFormat).getFormat();
			System.out.println(format.toString());
			System.out.println(format.getFrameRate());

			decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, //kodowanie
					format.getSampleRate(),											 //sample rate
					16,															 //rozmiar ramki(bits)
					format.getChannels(),												 //liczba kana³ów
					format.getChannels()*2,											 //rozmiar ramki (bytes)
					format.getSampleRate(),											 //frame rate
					false); //format danych w pliku, po dodaniu informacji, których nie (nie da siê odzyskaæ??) ma w mp3
		}catch(Exception e){
			e.printStackTrace();
			decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, //kodowanie
			 44100,											 //sample rate
			 16,											 //rozmiar ramki(bits)
			 2,												 //liczba kana³ów
			 4,											     //rozmiar ramki (bytes)
			 44100,											 //frame rate
			 false); //format danych w pliku, po dodaniu informacji, których nie (nie da siê odzyskaæ??) ma w mp3				
		}		
		return decodedFormat;
	}

	
	public InputStream getStream() throws MalformedURLException, IOException{
		return getStream(m_adresURL);
	}
	
	public InputStream getStream (String adresURL) throws MalformedURLException, IOException{
		
		URLConnection conn = new URL(adresURL).openConnection();
		InputStream is = conn.getInputStream();
		return is;
		
	}

	//Testowe adresy stacji:
	//http://icecast.linxtelecom.com:8000/mania.mp3
	//"http://wroclaw.radio.pionier.net.pl:8000/pl/tuba10-1.mp3"
	//http://vipicecast.yacast.net/rmc
	//URL urlFormat = new URL("http://icecast.commedia.org.uk:8000/takeover.mp3");
}