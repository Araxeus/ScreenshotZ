# :sparkles: ScreenshotZ :sparkles:
- App Version Differences:
*	V1 use global hook, grab image from clipboard
*	V2 using clipboard listener (listen to type change so have to reset clipboard at start and after each screenshot) #probably the lightest && fastest and most reliable
*	V3 use global hook, Robot().createScreenCapture create entirely new screenshot without clipboard (lowest memory consuption)

- Startup Rule Instructions:
*	Put bat in same dir as ScreenshotZ.exe (rule is for this exact name - can be changed in .bat)
*	And then run the addStartupRule.bat as admin
*	(Adds startup rule to windows task scheduler)
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
