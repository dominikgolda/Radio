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

import javax.swing.JButton;

import radio.Kontroler;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JCheckBox;

public class MainPanel {

	private JFrame frmRadio;
	private JToggleButton tglBtnPlayPause ;
	private JButton btnStop;
	private Kontroler m_radio;
	private Object m_signal;
	private Thread watekRadia;
	private JToggleButton tglbtnStartRecording;
	private JCheckBox chckbxRecordBuffer;
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
			m_radio = new Kontroler("http://icecast.commedia.org.uk:8000/takeover.mp3",true,m_signal);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
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
		
		tglbtnStartRecording = new JToggleButton("Start Recording");
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
		
		chckbxRecordBuffer = new JCheckBox("Record Buffer");
		chckbxRecordBuffer.setBounds(128, 131, 97, 23);
		frmRadio.getContentPane().add(chckbxRecordBuffer);
		
		JMenuBar menuBar = new JMenuBar();
		frmRadio.setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("New menu");
		menuBar.add(mnNewMenu);
		watekRadia = new Thread(m_radio);
		watekRadia.start();
	}
}
//Testowe adresy stacji:
//http://icecast.linxtelecom.com:8000/mania.mp3
//"http://wroclaw.radio.pionier.net.pl:8000/pl/tuba10-1.mp3"
//http://vipicecast.yacast.net/rmc
//URL urlFormat = new URL("http://icecast.commedia.org.uk:8000/takeover.mp3");