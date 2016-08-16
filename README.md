#Extension Solarwinds - AppDynamics

Extension AppDynamics to create Alert in console Solarwinds.

#Requeriments
Discovery server AppDynamics into Solarwinds

#Install
Edit the file or create custom.xml into <AppDynamics_HOME>/Controller/custom/action
```
<custom-actions>
...
 <action>
  <type>Solarwinds</type>
  <executable>solar.sh</executable>
 </action>
...
</custom-actions>
```
Copy the files solar.sh, solarwinds.jar, log4j.properties and config.properties into <AppDynamics_HOME>/Controller/custom/action/Solarwinds

#Configure
Edit the file config.properties e put the information of Solarwinds.
```
Solawinds=ip address or hostname
UserSolarwinds=username
PassSolarwinds=password
AlertObject=name_hostname_server_solarwinds
```
Log on the UI Appdynamics and create a Action.
