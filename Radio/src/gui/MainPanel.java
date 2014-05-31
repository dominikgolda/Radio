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

public class MainPanel {

	private JFrame frame;
	private JToggleButton tglBtnPlayPause ;
	private JButton btnStop;
	private Kontroler m_radio;
	private Object m_signal;
	private Thread watekRadia;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainPanel window = new MainPanel();
					window.frame.setVisible(true);
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
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		m_signal = new Object();
		try {
			m_radio = new Kontroler("http://wroclaw.radio.pionier.net.pl:8000/pl/tuba10-1.mp3",true,m_signal);
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
		tglBtnPlayPause.setBounds(10, 11, 121, 23);
		frame.getContentPane().add(tglBtnPlayPause);
		
		btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m_radio.stop();
			}
		});
		btnStop.setBounds(10, 42, 121, 23);
		frame.getContentPane().add(btnStop);
		watekRadia = new Thread(m_radio);
		watekRadia.start();
	}
}
