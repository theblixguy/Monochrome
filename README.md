# Monochrome
Monochrome turns on monochrome (or black & white) mode when your battery is low, to further reduce display power consumption when your battery level is low. This means your device can last longer when its battery is low.

Monochrome is triggered when your device's battery level hits the LOW level as defined by the OEM (usually 15%) and Monochrome is triggered again when your battery level hits the OKAY level (usually 30%) as defined by the OEM. When your battery level reaches the LOW state, Monochrome is turned on and when your battery level reaches the OKAY state, Monochrome is turned off. This is all done automatically so you don't have to manage anything. 

Monochrome also works on the GPU level, so Monochrome does not implement a hidden/invisible view on top of apps to enforce black and white colors. This means Monochrome does not run in the background and/or constantly monitor your battery level, it's only activated when your battery reaches LOW or OKAY states and after enabling/disabling B/W mode on the GPU level, Monochrome exits

# License

This code is licensed under GPL v2
