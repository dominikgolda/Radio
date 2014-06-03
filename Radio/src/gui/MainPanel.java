package gui;

import java.awt.EventQueue;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JToggleButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;

import radio.Kontroler;

import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JTextField;
import javax.swing.Timer;
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
	private Timer timer;

	private long totalTime = 0;
	private long delayTime = 0;


	private boolean stoped = true;
	private boolean paused = false;
	private JTextField textMaxDelay;
	private JTextField textSetDelay;
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
		} catch (IOException| LineUnavailableException | UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		}

		tglBtnPlayPause = new JToggleButton("Play");

		tglBtnPlayPause.setBounds(231, 63, 121, 23);
		frmRadio.getContentPane().add(tglBtnPlayPause);

		btnStop = new JButton("Stop");
		btnStop.setBounds(231, 97, 121, 23);
		frmRadio.getContentPane().add(btnStop);

		tglbtnStartRecording = new JToggleButton("Start Recording");
		tglbtnStartRecording.setBounds(231, 131, 121, 23);
		frmRadio.getContentPane().add(tglbtnStartRecording);

		textCurrentDelay = new JTextField();
		textCurrentDelay.setText("00:00:00");
		textCurrentDelay.setEditable(false);
		textCurrentDelay.setBounds(10, 30, 86, 20);
		frmRadio.getContentPane().add(textCurrentDelay);
		textCurrentDelay.setColumns(10);

		JButton btnSetPosition = new JButton("Set Position");
		btnSetPosition.setBounds(231, 29, 121, 23);
		frmRadio.getContentPane().add(btnSetPosition);

		textMaxDelay = new JTextField();
		textMaxDelay.setText("00:00:00");
		textMaxDelay.setEditable(false);
		textMaxDelay.setColumns(10);
		textMaxDelay.setBounds(128, 30, 86, 20);
		frmRadio.getContentPane().add(textMaxDelay);

		textSetDelay = new JTextField();
		textSetDelay.setText("00:00:00");
		textSetDelay.setColumns(10);
		textSetDelay.setBounds(10, 81, 86, 20);
		frmRadio.getContentPane().add(textSetDelay);

		JLabel lblNewLabel = new JLabel("Current delay");
		lblNewLabel.setBounds(10, 11, 86, 14);
		frmRadio.getContentPane().add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Max delay");
		lblNewLabel_1.setBounds(128, 11, 86, 14);
		frmRadio.getContentPane().add(lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel("Set position");
		lblNewLabel_2.setBounds(10, 63, 86, 14);
		frmRadio.getContentPane().add(lblNewLabel_2);

		//////////////////////////////////////////////////////
		/////				PLAY / PAUSE				//////
		//////////////////////////////////////////////////////
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
						if(stoped){
							timer.start();
							stoped = false;
						}
						paused = false;
					}else{
						tBtn.setText("Play");
						m_radio.pause();
						paused = true;
					}
				}catch(Exception wyj){
					wyj.printStackTrace();
				}
			}
		});
		//////////////////////////////////////////////////////
		/////					STOP					//////
		//////////////////////////////////////////////////////
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				m_radio.stop();
				stoped = true;
				timer.stop();
				tglBtnPlayPause.setSelected(false);
				totalTime = 0;
				delayTime = 0;
				textCurrentDelay.setText("00:00:00");
				textMaxDelay.setText("00:00:00");
			}
		});
		//////////////////////////////////////////////////////
		/////			  START RECORDING  				//////
		//////////////////////////////////////////////////////
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
		//////////////////////////////////////////////////////
		/////				SET POSITION				//////
		//////////////////////////////////////////////////////
		btnSetPosition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String str = textSetDelay.getText();
				DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
				try {
					Date dt = formatter.parse(str);
					Calendar cal = Calendar.getInstance();
					cal.setTime(dt);
					int hour = cal.get(Calendar.HOUR);
					int minute = cal.get(Calendar.MINUTE);
					int second = cal.get(Calendar.SECOND);
					if(totalTime>0){
						long pom = 1000*(hour*3600+minute*60+second);
						m_radio.setBufferPositionRelative(((double)pom)/totalTime,!(stoped||paused));
						delayTime = totalTime - pom;
					}else{
						m_radio.setBufferPositionRelative(0,!(stoped||paused));
						delayTime = totalTime;
					}
				} catch (ParseException e1) {
					e1.printStackTrace();
				}				
			}
		});
		//////////////////////////////////////////////////////
		/////					TIMETR					//////
		//////////////////////////////////////////////////////
		timer = new Timer(1000,new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(!stoped){
					String hms;
					if(paused){
						delayTime +=1000;
						totalTime +=1000;
					}else{
						totalTime +=1000;
					}
						hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(delayTime),
								TimeUnit.MILLISECONDS.toMinutes(delayTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(delayTime)),
								TimeUnit.MILLISECONDS.toSeconds(delayTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(delayTime)));
						textCurrentDelay.setText(hms);
					hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalTime),
							TimeUnit.MILLISECONDS.toMinutes(totalTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalTime)),
							TimeUnit.MILLISECONDS.toSeconds(totalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalTime)));
					textMaxDelay.setText(hms);
				}


			}
		});

		ImageIcon img = new ImageIcon("C:/Users/Dominik/git/Radio/Radio/src/gui/icon.png");
		frmRadio.setIconImage(img.getImage());
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