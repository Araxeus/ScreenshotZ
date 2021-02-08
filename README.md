###### Running Jar/Exe as Administrator perform better ######


#  :sparkles: ScreenshotZ App Features: :sparkles:


     .
     
     - App launch directly to system tray


     - Option to change default screenshot directory


     - When PrintScreen is pressed - it will always save to screenshot directory


     - Option to add a Custom Keybind (Save a combination of 1-3 keys) [Does not replace PrtScn button]
     
     
     - Option to launch Crop UI on Custom Keybind and/or PrtScn


     - App save settings to MyUser/.ScreenshotZ/config.XML


     - Default screenshot directory is MyUser/.ScreenshotZ/Screenshots (Default 2nd keybind is null)


    - To show some debugging: run openWithDebug.bat from same directory as jar OR start program from CMD


     - App can directly capture to saved/default directory when launched with @arg[-capture] (doesnt open the whole program)
     
     Adding @arg[-crop] ontop of it will also launch the crop UI (-crop is bugged in v1.1)
     
     for example in cmd: "java -jar ScreenshotZ-v1-jar-with-dependencies.jar -capture -crop"
         
###### Known Issues: ######

###### - Crop tool will often not automatically gain focus ######

###### - @arg [-crop] cant launch crop tool if program isn't running ######
    
.

- Inner App Functionality:

-> - -    Use Global Hook, Robot().createScreenCapture create entirely new screenshot without clipboard

-> - -    Or if keyboard event is not detected > use Clipboard Listener (listen to type change so have to reset clipboard at program start[if content type was image] and after each screenshot) and grab image from clipboard

# TODO

- ? Fix memory leak ?

- Make/Add .REG file "ScreenshotZ" (Add to explorer context menu - launch with @arg -capture) [almost done]

- Make installScript (use also contextMenu.REG and onStartup.bat)

- fix @arg -crop (change getKeyboardHook() to create new hook if null)

- fix focus on crop

- change crop to save on same filename? [need external feedback]

.

.

# Startup Rule Instructions: (Adds startup rule to windows task scheduler)

*	First put bat in same dir as ScreenshotZ.exe (rule is for this exact name - can be changed in .bat)

*	Then run the addStartupRule.bat as admin
	
*	to uninstall run deleteStartupRule.bat as admin and press Y

.

# SCHTASKS Batch Syntax:
* HEAD:

'/DELETE - {/TN ...}'

'/CREATE - {/TN /TR /SC ...}'

'/CHANGE {/TN /DISABLE /ENABLE ...}'

* ARG:

'/SC {MINUTE, HOURLY, DAILY, WEEKLY, MONTHLY, ONCE, ONSTART, ONLOGON, ONIDLE, ONEVENT}' #[task schedule]

'/ST {HH:mm}' #[specify time to run task in 24hour format]

'/D {MON, TUE, WED, THU, FRI, SAT, SUN}' [OR if SC=monthly can use 1-31 representing date OR * representing all days] #[day to execute]

'/TN {FOLDER\TASKNAME}' #[specifies task name and folder (no folder if no \)]

'/TR {PATH}' #[specifies full path to app/script to run on schedule]

'/DELAY {mmmm:ss}' #[specify delay before starting, available only for ONSTART, ONLOGON, and ONEVENT]
