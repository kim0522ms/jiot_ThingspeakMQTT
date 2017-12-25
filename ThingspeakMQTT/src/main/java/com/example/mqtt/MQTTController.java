package com.example.mqtt;
import java.awt.Point;
import java.util.Collection;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.controller.AlertGPIOPinOutputControlPoint;
import com.example.controller.ControlPoint;
import com.example.controller.ControlPointContainer;
import com.example.controller.GPIOPinOutputControlPoint;
import com.example.controller.OutputControlPoint;
import com.example.controller.TMP102AnalogIOPoint;

public class MQTTController  {
	
	// ControlPointContainer에서 가지고 있는 모든 ControlPoint를 points에 저장해둠.
	public Collection<ControlPoint> points = ControlPointContainer.getInstance().getControlPoints();
	 
	public MqttClient createPublisher(String broker, String topic)
	{
		MqttClient client = null;
		MemoryPersistence persistence = new MemoryPersistence();
		try {
			client = new MqttClient(broker, MqttClient.generateClientId(), persistence);
		
		    MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("-------------------------------------------------");
            System.out.println("Connecting to broker...");
            client.connect(connOpts);
            System.out.println("-------------------------------------------------");
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
		
		return client;
	}
	
	public MqttClient Subscribe(String broker, String topic) {		
		MqttClient sampleClient = null;
		
		MemoryPersistence persistence = new MemoryPersistence();
		try {
			MqttConnectOptions connOpt = new MqttConnectOptions();
			//connOpt.setCleanSession(true);
			
			String id = MqttClient.generateClientId();
			
			sampleClient = new MqttClient(broker, id, persistence);
			
			// MQTT API KEY. Thingspeak에 연결하기 위해서는 중복되지 않는 userid와 MQTT APIKEY를 비밀번호로 입력해야한다.
			char[] passwd = "<Your MQTT API KEY>".toCharArray();
			
			connOpt.setUserName(id);
			connOpt.setPassword(passwd);
			
			sampleClient.setCallback(new MqttCallback() {
				
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					// TODO ControlPoint 장치를 제어할 Command 작성할 것
					// Observable이 Observer에게 Update()문을 실행하도록 notify를 보내야 함.
					// System.out.println(String.format("Message Arrived : %s", message));
					
					//String[] commands = message.toString().split(" ");
					String jsonMessage = message.toString();
					
					
					// Broker로부터 전송받은 메세지를 처리하는 함수
					JSONObject json = new JSONObject(jsonMessage);
					String[] commands = null;
					
					try {
						// 받은 json의 field값으로 Command를 구분
						if (json.getString("field1") != null)
						{
							commands = json.get("field1").toString().split(" ");
							executeCommand(commands);
						}
					}catch (JSONException je){
			            System.out.println("-------------------------------------------------");
						System.out.println("Message arrived But invalid value");
						return;
					}
				}
				
				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void connectionLost(Throwable cause) {
					// TODO Auto-generated method stub
					cause.printStackTrace();
					System.exit(-1);
				}
			});
			System.out.println("Connecting to broker: "+ broker);
			sampleClient.connect(connOpt);
			
			// Thingspeak Subscribe의 경우 QoS가 항상 0. setCleanSession()을 호출할 경우 1.
			System.out.println("Prepare to subscribe..");
			sampleClient.subscribe(topic, 0);
			
			System.out.println("Create Subscribe Success!");
		} catch (MqttException me) {
			// TODO Auto-generated catch block
			System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
		}
		return sampleClient;
	}
	
	public void executeCommand(String[] commands) throws MqttPersistenceException, MqttException
	{
		// USAGE: led_list
		if (commands[0].equals("led_list"))
		{
			Collection<ControlPoint> points
            = ControlPointContainer.getInstance().getControlPoints();
			for (ControlPoint point : points)
			{
				// ControlPointContainer에 LED ControlPoin가 있을 경우 상태 출력.
				if (point instanceof GPIOPinOutputControlPoint || point instanceof AlertGPIOPinOutputControlPoint)
				{
					System.out.println(String.format("LED %s is enabled and status = %s",
							point.getId(), point.getPresentValue() == 1 ? "ON" : "OFF"));
				}
			}
		}
		
		// USAGE: set <deviceid> <value>
		else if (commands[0].equals("set"))
		{
			if (commands.length != 3)
				System.out.println("Invalied Set Parameter!");
			else {
				// deviceid가 high_limit일 경우
				if (commands[1].equals("high_limit"))
				{
					for (ControlPoint point : points)
					{
						// ControlPointContainer에 LED ControlPoin가 있을 경우 상태 출력.
						if (point instanceof TMP102AnalogIOPoint)
						{
							int temp = Integer.parseInt(commands[2]);
							System.out.println("HIGH 테스트 : " + Integer.toString(temp));
							TMP102AnalogIOPoint tmp102 = (TMP102AnalogIOPoint)point;
							tmp102.setHighLimitTemperature(temp);
							System.out.println("Notice : High Limit Temperature set by " + Integer.toString(temp));
						}
					}
				}
				// deviceid가 low_limit일 경우
				else if (commands[1].equals("low_limit"))
				{
					for (ControlPoint point : points)
					{
						// ControlPointContainer에 LED ControlPoin가 있을 경우 상태 출력.
						if (point instanceof TMP102AnalogIOPoint)
						{
							int temp = Integer.parseInt(commands[2]);
							System.out.println("Low 테스트 : " + Integer.toString(temp));
							TMP102AnalogIOPoint tmp102 = (TMP102AnalogIOPoint)point;
							tmp102.setLowLimitTemerature(temp);
							System.out.println("Notice : Low Limit Temperature set by " + Integer.toString(temp));
						}
					}
				}
				
				// deviceid가 숫자일 경우
				else {
					ControlPoint point = ControlPointContainer.getInstance().getControlPoint(Integer.parseInt(commands[1]));
					
					if (point == null)
						System.out.println(String.format("Control Point %s not found.", commands[1]));
					else
					{
						OutputControlPoint op = (OutputControlPoint)point;
						op.setPresentValue(Integer.parseInt(commands[2]));
					}
				}
			}
		}
		else if (commands[0].equals("viewLimit"))
		{
			for (ControlPoint point : points)
			{
				// ControlPointContainer에 LED ControlPoin가 있을 경우 상태 출력.
				if (point instanceof TMP102AnalogIOPoint)
				{
					TMP102AnalogIOPoint tmp102 = (TMP102AnalogIOPoint)point;
					String high_limit = tmp102.getHighLimitTemperature();
					String low_limit = tmp102.getLowLimitTemperature();
					
					System.out.println("High Limit = " + high_limit);
					System.out.println("Low Limit = " + low_limit);
				}
			}
		}
		
		else
		{
			System.out.println("Invalied command!");
		}
	}
}
