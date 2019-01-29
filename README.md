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

## Submission Notes
Want to submit a fix, feature or...? Here is everything, we think you will need to know.

Mycroft.ai is a collaborative, open source project. That means we encourage and expect people to participate. But to make things a bit more clear here are some kind lines if you would like to submit a fix.

### Passthrough (component app)
1. Pull your own fork, work there
2. make a branch of whatever you are working on, makes sure your fork is the latest.
3. Test!!!!
4. merge into your master.
5. make pull request into project master
6. assign a reviewer.
7. check on it, if not reviewed after a week find a new reviewer, we are mostly volunteers so find one that has time.
8. sit back and enjoy your handy work.

#### Coding style... 
We have moved now to Kotlin and therefore will be following the standard coding practices. Also please use descriptive method/function names. And use comments to back up that name when complicated, like a calculation or similar.  Remember, you want to come back 6 months from now and be able to read your code.

Most of all have fun. Ask questions and don't worry about breaking anything, that is why we have a versioning system. 
