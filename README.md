# Mycroft-Android

This is the Android companion app to Mycroft-core. It works by opening a websocket connection to the Mycroft-core messagebus 
and sending and receiving messages from there.

It implements voice recognition and Text To Speech (TTS) via Google API's at the moment, but that may change soon.

## To Install

Import the repo into Android Studio, or your IDE of choice.
Build and deploy to a device

Once the app is running on a device (Lollipop or later SDK 24), you will need to set the IP address of your Mycroft-core instance
in the Settings -> General Options menu. That will then create a websocket connection to your Mycroft and off you go!

## To help out
If you would like to help out on this project, please join Mattermost at https://chat.mycroft.ai/login and 
ask where you can contribute! Currently, design and UI/UX is most needed, but any and all help is greatly appreciated!

## Beacon Scanning - Optional!
Beacon scanning is optional. But the idea was to permit a user to interact with beacons (uses for beacons are numerous). 
Currently, there is no tts for beacons; only scanning is possible. We will build out the functionality to include
 more interaction with Mycroft specific use cases. 
#### How to use 
To beacon scan you will have to set the string res (resource) value for beacon_layout in the build configs. 
Below are some layouts. Please note, we are currently are getting eddystone to work with Mycroft. The first
release will support ibeacon and AltBeacon.  Please let us know if there are any issues.

ALTBEACON 	m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25
EDDYSTONE  TLM 	x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15
EDDYSTONE  UID 	s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19
EDDYSTONE  URL 	s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v
IBEACON 	m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24