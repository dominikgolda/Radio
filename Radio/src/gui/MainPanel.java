package gui;

import java.awt.EventQueue;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JToggleButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;

import radio.Kontroler;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JLabel;

public class MainPanel {

	private JFrame frmRadio;
	private JToggleButton tglBtnPlayPause ;
	private JButton btnStop;
	private Kontroler m_radio;
	private Object m_signal;
	private Thread watekRadia;
	private JToggleButton tglbtnStartRecording;
	private JTextField textCurrentDelay;
	private JTextField textMaxDelay;
	private JTextField textSetDelay;
	private long time;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainPanel window = new MainPanel();
					window.frmRadio.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainPanel() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmRadio = new JFrame();
		frmRadio.setTitle("Radio");
		frmRadio.setBounds(100, 100, 378, 223);
		frmRadio.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmRadio.getContentPane().setLayout(null);
		frmRadio.setResizable(false);
		m_signal = new Object();
		try {
			m_radio = new Kontroler("http://wroclaw.radio.pionier.net.pl:8000/pl/tuba10-1.mp3",true,m_signal);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
		tglBtnPlayPause = new JToggleButton("Play");
		tglBtnPlayPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JToggleButton tBtn = (JToggleButton)arg0.getSource();
				try{
					if(tBtn.isSelected()){
						tBtn.setText("Pause");
						m_radio.play();
						synchronized(m_signal){
							m_signal.notify();
						}
					}else{
						tBtn.setText("Play");
						m_radio.pause();
					}
				}catch(Exception wyj){
					wyj.printStackTrace();
				}
			}
		});
		tglBtnPlayPause.setBounds(231, 63, 121, 23);
		frmRadio.getContentPane().add(tglBtnPlayPause);
		
		btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m_radio.stop();
			}
		});
		btnStop.setBounds(231, 97, 121, 23);
		frmRadio.getContentPane().add(btnStop);
		
		tglbtnStartRecording = new JToggleButton("Start recording");
		tglbtnStartRecording.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JToggleButton tBtn = (JToggleButton)arg0.getSource();
				try{
					if(tBtn.isSelected()){
						tBtn.setText("Recording...");
						m_radio.recordBuffer("C:\\Users\\Dominik\\Desktop\\test.mp3", true);
					}else{
						tBtn.setText("Start Recording");
						m_radio.stopRecording();
					}
				}catch(Exception wyj){
					wyj.printStackTrace();
				}
			}
		});
		tglbtnStartRecording.setBounds(231, 131, 121, 23);
		frmRadio.getContentPane().add(tglbtnStartRecording);
		
		textCurrentDelay = new JTextField();
		textCurrentDelay.setEditable(false);
		textCurrentDelay.setText("00:00:00");
		textCurrentDelay.setBounds(12, 30, 70, 20);
		frmRadio.getContentPane().add(textCurrentDelay);
		textCurrentDelay.setColumns(10);
		
		JButton btnSetPosition = new JButton("Set position");
		btnSetPosition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String str = textCurrentDelay.getText();
				double val = Double.parseDouble(str);
				m_radio.setBufferPositionRelative(val);
			}
		});
		btnSetPosition.setBounds(231, 28, 121, 23);
		frmRadio.getContentPane().add(btnSetPosition);
		JLabel lblNewLabel = new JLabel("Delay");
		lblNewLabel.setBounds(12, 12, 70, 15);
		frmRadio.getContentPane().add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Max delay");
		lblNewLabel_1.setBounds(106, 12, 109, 15);
		frmRadio.getContentPane().add(lblNewLabel_1);
		
		textMaxDelay = new JTextField();
		textMaxDelay.setEditable(false);
		textMaxDelay.setText("00:00:00");
		textMaxDelay.setBounds(106, 30, 70, 19);
		frmRadio.getContentPane().add(textMaxDelay);
		textMaxDelay.setColumns(10);
		
		textSetDelay = new JTextField();
		textSetDelay.setText("00:00:00");
		textSetDelay.setColumns(10);
		textSetDelay.setBounds(12, 83, 70, 20);
		frmRadio.getContentPane().add(textSetDelay);
		
		JLabel lblSetDelay = new JLabel("Set delay");
		lblSetDelay.setBounds(12, 63, 70, 15);
		frmRadio.getContentPane().add(lblSetDelay);
		
		JMenuBar menuBar = new JMenuBar();
		frmRadio.setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("New menu");
		menuBar.add(mnNewMenu);
		watekRadia = new Thread(m_radio);
		watekRadia.start();
	}
	
	
	
    private static String formatInterval(final long l)
    {
        final long hr = TimeUnit.MILLISECONDS.toHours(l);
        final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        return String.format("%02d:%02d:%02d", hr, min, sec);
    }
}
//Testowe adresy stacji:
//http://icecast.linxtelecom.com:8000/mania.mp3
//"http://wroclaw.radio.pionier.net.pl:8000/pl/tuba10-1.mp3"
//http://vipicecast.yacast.net/rmc
//URL urlFormat = new URL("http://icecast.commedia.org.uk:8000/takeover.mp3");