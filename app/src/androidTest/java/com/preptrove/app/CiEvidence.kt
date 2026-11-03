package com.preptrove.app

import android.content.Context
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.test.platform.app.InstrumentationRegistry

/** Preserve only disposable-emulator diagnostics before Gradle removes its test app. */
internal fun preserveCiEvidence(context:Context) {
 if(InstrumentationRegistry.getArguments().getString("ephemeralCi")!="true" ||
  BuildConfig.BUILD_TYPE!="debug" || context.packageName!="com.preptrove.app" || Build.VERSION.SDK_INT<31)return
 val automation=InstrumentationRegistry.getInstrumentation().uiAutomation
 fun shell(command:String)=ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).bufferedReader().use {it.readText()}
 if(shell("getprop ro.kernel.qemu").trim()!="1")return
 val folder=context.getExternalFilesDir("acceptance") ?: return
 if(folder.exists()) {
  shell("mkdir -p /data/local/tmp/preptrove-ci-evidence")
  folder.listFiles().orEmpty().filter {it.isFile}.forEach {file->
   require(file.name.matches(Regex("[A-Za-z0-9_.-]+")) && !file.absolutePath.contains("'"))
   val streams=automation.executeShellCommandRw("dd of=/data/local/tmp/preptrove-ci-evidence/"+file.name)
   ParcelFileDescriptor.AutoCloseInputStream(streams[0]).use {output->
    ParcelFileDescriptor.AutoCloseOutputStream(streams[1]).use {input->input.write(file.readBytes())}
    output.readBytes()
   }
  }
 }
}
