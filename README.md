
### To launch from .exe you *MUST* run it as administrator or you will get the following error message:

![alt text](https://github.com/Araxeus/ScreenshotZ/blob/master/resources/NoAdminRights.png?raw=true)

#### To do so [on Windows10] - go to the file settings - Compability tab - check run this program as administrator

![alt text](https://github.com/Araxeus/ScreenshotZ/blob/master/resources/RunAsAdmin.png?raw=true)

#### Running .jar as Administrator perform better but isn't mandatory ####
##### To do it you need to open CMD as administrator and go to the location of the jar and run it with java -jar
###### for example:[ G: ---> dir HDD/Downloads/Opera ---> java -jar ScreenshotZ-v1.2.jar ]


#  :sparkles: ScreenshotZ App Features: :sparkles:


     .
     
     - App launch directly to system tray


     - Option to change default screenshot directory


     - When PrintScreen is pressed - it will always save to screenshot directory


     - Option to add a Custom Keybind (Save a combination of 1-3 keys) [Does not replace PrtScn button]
     
     
     - v1.1 added Option to launch Crop UI on Custom Keybind and/or PrtScn
     
     
     - v1.2 added more Crop Options (Save Original , Exit UI onCrop)


     - App save settings to MyUser/.ScreenshotZ/config.XML


     - Default screenshot directory is MyUser/.ScreenshotZ/Screenshots (Default 2nd keybind is null)


    - To show some debugging: run openWithDebug.bat from same directory as jar OR start program from CMD
    
    
    - To add/delete Startup rule run the corresponding bat as Administrator from the same directory as ScreenshotZ-v1.2.exeCom


     - App can directly capture to saved/default directory when launched with @arg[-capture]
     
     it will also launch crop UI if -crop is added ontop of it
     
     for example in cmd: "java -jar ScreenshotZ-v1.2.jar -capture -crop"
     
     (App will just take screenshot without launching main thread)       
    
    
.

- Inner App Functionality:

-> - -    Use Global Hook, Robot().createScreenCapture create entirely new screenshot without clipboard

-> - -    Or if keyboard event is not detected > use Clipboard Listener (listen to type change so have to reset clipboard at program start[if content type was image] and after each screenshot) and grab image from clipboard

.

## Startup Rule Instructions: (Adds startup rule to windows task scheduler)

*	First put bat in same dir as ScreenshotZ-v1.2.exe (rule is for this exact name - can be changed in .bat)

*	Then run the addStartupRule.bat as admin
	
*	to uninstall run deleteStartupRule.bat as admin and press Y

.

## SCHTASKS Batch Syntax:
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
