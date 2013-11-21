package ch.PsYchoss.InfoChat;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

public class MainActivity extends ActionBarActivity {

	TextView textAreaMessage;
	EditText editTextMessage;
	View content;
	DatagramSocket datagramSocket;
	DatagramPacket packet;
	InetAddress local;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment())
					.commit();
		}
		textAreaMessage = (TextView) findViewById(R.id.viewMessage);
		editTextMessage = (EditText) findViewById(R.id.textAreaMessage);
		content = this.findViewById(android.R.id.content);
		try {
			local = getBroadcastAddress();//my broadcast ip
		} catch (IOException e) {
			e.printStackTrace();
			Toast toast = Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
			toast.show();
		}

		try {
			datagramSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast toast = Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
			toast.show();
		}

		byte[] buffer = new byte[10];
		packet = new DatagramPacket(buffer, buffer.length);
		new Thread(new Runnable() {
			public void run() {
				while (true){
					try {
						datagramSocket.receive(packet);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Toast toast = Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
						toast.show();
					}
					final byte[] buff = packet.getData();
					content.post(new Runnable() {
						public void run() {
							receiveMessage("Prova", buff.toString());
						}
					});

					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
			case R.id.action_settings:
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	private InetAddress getBroadcastAddress() throws IOException {
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		// handle null somehow

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) (broadcast >> (k * 8));
		return InetAddress.getByAddress(quads);
	}


	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void sendMessage(String destinatario, String message) {
		textAreaMessage.setText(textAreaMessage.getText() + "\n" + Build.MANUFACTURER + " " + Build.PRODUCT + ": " + message);


		int server_port = 5000; //port that I’m using
		try{
			DatagramSocket s = new DatagramSocket();

			int msg_length=message.length();
			byte[] messageByte = message.getBytes();
			DatagramPacket p = new DatagramPacket(messageByte, msg_length,local,server_port);
			StrictMode.ThreadPolicy policy = new
					StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

			s.send(p);
			Toast toast = Toast.makeText(getApplicationContext(), "message send", Toast.LENGTH_SHORT);
			toast.show();
		}
		catch(Exception e){
			Toast toast = Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
			toast.show();
		}
		editTextMessage.setText("");
	}
	public void receiveMessage(String mittente, String message){
		textAreaMessage.setText(textAreaMessage.getText() + "\n" + mittente + ": " + message);
	}
	public void buttonSend(View view) {
		sendMessage("Prova",editTextMessage.getText().toString());
	}



	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
		                         Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}
	}

}