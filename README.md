AndroidBackgroundVideoRecording
===============================

It is a library project used for background front camera video recording.


Integration Steps:
-------------------
1. Add this project as library project.
2. Write below code to start camera video recorder 

         
         Intent intent = new Intent(YourActivity.this, RecorderService.class);
         intent.putExtra(RecorderService.INTENT_VIDEO_PATH, "/folder-path/"); //eg: "/video/camera/"
         startService(intent);
         

3. Write below code to stop camera video recorder

         stopService(new Intent(YourActivity.this, RecorderService.class));
         


Now you are good to go.  
Enjoy Coding !!!

**Pull Request are welcome.**  
=============================
