package org.buffr.boomkat;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;

public class Command {
    private String TAG = Command.class.getSimpleName();

    AtomicReference<Process> goProcess = new AtomicReference<Process>();
    Context context;
    AssetManager assetManager;

    public Command(Context c, AssetManager am) {
        context = c;
        assetManager = am;
        copyBinary();
    }

    private String binaryPath(String suffix) {
        return context.getFilesDir().getAbsolutePath() + "/" + suffix;
    }

    private void copyBinary() {
        String src = "boomkat-cli";
        maybeCopyFile(src, "boomkat-cli");
    }

    private void maybeCopyFile(String src, String dstSuffix) {
        String fullPath = binaryPath(dstSuffix);
        if (new File(fullPath).exists()) {
            Log.d(TAG, "file " + fullPath + " already exists.");
        }
        try {
            InputStream is = assetManager.open(src);
            FileOutputStream fos = context.openFileOutput(dstSuffix + ".writing", Context.MODE_PRIVATE);
            byte[] buf = new byte[8192];
            int offset;
            while ((offset = is.read(buf)) > 0) {
                fos.write(buf, 0, offset);
            }
            is.close();
            fos.flush();
            fos.close();
            String writingFile = fullPath + ".writing";
            Log.d(TAG, "wrote out " + writingFile);
            Runtime.getRuntime().exec("chmod 0777 " + writingFile);
            Log.d(TAG, "did chmod 0700 on " + writingFile);
            Runtime.getRuntime().exec("mv " + writingFile + " " + fullPath);
            Log.d(TAG, "Moved writin file to " + fullPath);
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public void start(ArrayList<String> commandArgs) {
        final Process p = goProcess.get();
        if (p != null) {
            return;
        }
        commandArgs.add(0, binaryPath("boomkat-cli"));
        final ArrayList<String> commandList = new ArrayList<String>(commandArgs);
        Thread child = new Thread() {
            @Override
            public void run() {
                Process process = null;
                Thread writeThread = null;
                try {
                    process = new ProcessBuilder()
                        //.command(binaryPath("boomkat-cli"))
                        .command(commandList)
                        .redirectErrorStream(false)
                        .start();
                    goProcess.set(process);
                    InputStream in = process.getInputStream();
                    new CopyToAndroidLogThread(process.getErrorStream()).start();
                    // TODO: writeThread?

                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    while (true) {
                        final String line = br.readLine();
                        if (line == null) {
                            Log.d(TAG, "null line from child process.");
                            return;
                        }
                        if (line.startsWith("LOG ")) {
                            Log.d(TAG + "/Child", line.substring(5));
                            continue;
                        }

                        Log.d(TAG + "/command", line);
                        // TODO: process line
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                } finally {
                    if (process != null) {
                        process.destroy();
                    }
                    goProcess.compareAndSet(p, null);
                    if (writeThread != null) {
                        writeThread.interrupt();
                    }
                }
            }
        };
        child.start();
    }

    private class CopyToAndroidLogThread extends Thread {
        private final BufferedReader bufIn;

        public CopyToAndroidLogThread(InputStream in) {
            bufIn = new BufferedReader(new InputStreamReader(in));
        }

        @Override
        public void run() {
            String tag = TAG + "/boomkat-cli";
            while (true) {
                String line = null;
                try {
                    line = bufIn.readLine();
                } catch(IOException e) {
                    Log.d(tag, "Exception: " + e.toString());
                    return;
                }
                if (line == null) {
                    Log.d(tag, "null line from child stderr.");
                    return;
                }
                Log.d(tag, line);
            }
        }
    }
}

