package com.example.mqtt;

import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.example.controller.AlertGPIOPinOutputControlPoint;
import com.example.controller.ControlPoint;
import com.example.controller.ControlPointContainer;
import com.example.controller.GPIOPinOutputControlPoint;
import com.example.controller.PCF8591AnalogIOPoint;
import com.example.controller.TMP102AnalogIOPoint;

public class ThingspeakMQTTMain implements Observer {

	private static String topic = "channels/<channelID>/publish/<write api key>";
	private static String sub_topic = "channels/<channelID>/subscribe/json/<read api key>";
	private static String broker = "tcp://mqtt.thingspeak.com:1883";
	private static MQTTController controller = new MQTTController();
	public static MqttClient subClient = null;
	public static MqttClient sampleClient = null; 
	
	private static ControlPointContainer container = null;
	
	public ThingspeakMQTTMain() {
		System.out.println("Initialize GPIO Devices...");
		container = ControlPointContainer.getInstance();
		container.start();
		
		// Main Class의 객체를 Control Point에 대한 Observer로 등록함.
		System.out.println("Regist Observable to Observer Object...");
		Collection<ControlPoint> points = container.getControlPoints();
		for(ControlPoint point : points)
		{
			point.addObserver(this);
		}
		
		// Subscribe
		sampleClient = controller.createPublisher(broker, topic);
		
		// Subscribe
		subClient = controller.Subscribe(broker,  sub_topic); 
	}

	public static void main(String[] args) {	
		ThingspeakMQTTMain main = new ThingspeakMQTTMain();
		Scanner scanner = new Scanner(System.in);

		// start()함수가 호출되면 cp Container에 들어있던 모든 ControlPoint들이 open()됨.
		// Thingspeak MQTT Broker에 대한 Subscribe 생성
		System.out.println("Enter Q key to quit...");
		while(true)
		{
			String cmd = scanner.nextLine();
			if (cmd.equals("Q")||cmd.equals("q"))
				break;
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (subClient != null)
					try {
						System.out.println("Disconnecting...");
						subClient.disconnect();
						System.out.println("Disconnectd!");
						subClient.close();
						System.out.println("Desubscribed!");
						
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				if (container != null)
				{
					System.out.println("Device closing...");
					container.stop();
					System.out.println("Device closed!");
				}
			}
		});
		scanner.close();
		System.exit(0);
	}

	@Override
	public void update(Observable obs, Object arg) {
		ControlPoint point = (ControlPoint)obs;
		
		// TODO : point의 인스턴스 클래스가 어느것인지를 판단해서 각각 다른 처리를 실행하면 됨.
		if (point instanceof PCF8591AnalogIOPoint)
		{
			// PCF8591 Observable에 의해 변화된 wh도값을 가져와서 Thingspeak으로 Publish
			int photo = point.getPresentValue();
			String content = String.format("field3=%d&status=MQTTPUBLISH", photo);
			
			MqttMessage message = new MqttMessage(content.getBytes());
			message.setQos(0);
			
			try {
				sampleClient.publish(topic, message);
				System.out.println("Publish Success!");
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (point instanceof TMP102AnalogIOPoint)
		{
			// TMP102 Observable에 의해 변화된 온도값을 가져와서 Thingspeak으로 Publish
			float temp = Float.intBitsToFloat(point.getPresentValue());
			String content = "field2="+Float.toString(temp)+"&status=MQTTPUBLISH";
			
			MqttMessage message = new MqttMessage(content.getBytes());
			message.setQos(0);
			
			try {
				sampleClient.publish(topic, message);
				System.out.println("Publish Success!");
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (point instanceof AlertGPIOPinOutputControlPoint)
		{
			AlertGPIOPinOutputControlPoint alert = (AlertGPIOPinOutputControlPoint)point;
			alert.blinkLed();
		}
	}
}
