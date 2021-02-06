#            :sparkles: ScreenshotZ :sparkles:

############# REGISTRY INSTEAD OF TXT SETTINGS? #############


     App can directly capture to default directory when launched with @param -capture (doesnt open the whole program)


-> - -    Use Global Hook, Robot().createScreenCapture create entirely new screenshot without clipboard, if it doesnt launch for some reason (keyboard event not detected)

-> - -    Or if keyboard event not detected us Clipboard Listener (listen to type change so have to reset clipboard at program start[if content type was image] and after each screenshot) and grab image from clipboard

# TODO

- Find way to load icon from jar

- Add option to launch 'crop jpanel' (on screenshot, launch window with crop ui)

- Add option to 'remap' button ?

- ? Fix memory leak ?

- Make/Add .REG file "ScreenshotZ" (Add to explorer context menu - launch with @param -capture) [almost done]

- Make installScript (use also contextMenu.REG and onStartup.bat)

- launch4j wrapper escalate privilege to Admin

- Expand Readme

.

.

.

.


# Startup Rule Instructions: (Adds startup rule to windows task scheduler)

*	First put bat in same dir as ScreenshotZ.exe (rule is for this exact name - can be changed in .bat)

*	Then run the addStartupRule.bat as admin
	
*	to uninstall run deleteStartupRule.bat as admin and press Y



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
