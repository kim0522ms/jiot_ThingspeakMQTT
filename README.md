# jiot_ThingspeakMQTT

This program is designed to build IoT environment based on Java language on embedded board.

This program requires the following environment.

<pre>
Device : Raspberry Pi, TMP102, PCF8591, LED Pin
OS     : Raspbian
Others : Thingspeak Account
</pre>
***

To control the GPIO in the Java language, we refer to the 'dio.jar' library.
In addition, the program collects sensor data using PCF8591 and TMP102 equipment.

You can easily add other devices by extending the 'ControlPoint.java' code.
<br>

<pre><code>ControlPoint point = new GPIOPinOutputControlPoint(Your Pin Number]);
putControlPoint(point);
</code></pre>

If you want to add a device, add the object defined in the GPIO Pin function to ControlPointContainer.java and add the pin number to the dio.properties file under the lib folder.
***
All devices are registered as one thread in the ScheduledExecutorService and executed sequentially. And All devices extend the Observable class, And if their state changes, propagate the change to the Main class via the 'fireChanged ()' function.

<br>

If you want to take some action when the value of a device changes, you can write an action in the update () function in the Observer class (which is the Main class in this program. You can make another Observer Class).



<pre><code>public void update(Observable obs, Object arg) {
		ControlPoint point = (ControlPoint)obs;

		if (point instanceof [Your ControlPoint Instance Name]){
		}
}</code></pre>

This entry is still being written ... Sorry &#128521;
