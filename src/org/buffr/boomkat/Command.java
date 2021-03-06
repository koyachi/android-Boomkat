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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.buffr.boomkat.data.Record;

public class Command {
    private static final String TAG = Command.class.getSimpleName();

    AtomicReference<Process> goProcess = new AtomicReference<Process>();
    static Context context;
    static AssetManager assetManager;
    BoomkatService service;

    public static void init(Context c, AssetManager am) {
        context = c;
        assetManager = am;
        copyBinary();
    }

    public Command(BoomkatService service) {
        this.service = service;
    }

    static private String binaryPath(String suffix) {
        return context.getFilesDir().getAbsolutePath() + "/" + suffix;
    }

    static private void copyBinary() {
        String src = "boomkat-cli";
        maybeCopyFile(src, "boomkat-cli");
    }

    static private void maybeCopyFile(String src, String dstSuffix) {
        String fullPath = binaryPath(dstSuffix);
        if (new File(fullPath).exists()) {
            Log.d(TAG, "file " + fullPath + " already exists.");
            return;
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
        commandArgs.add(0, Command.binaryPath("boomkat-cli"));
        final ArrayList<String> commandList = new ArrayList<String>(commandArgs);
        Thread child = new Thread() {
            @Override
            public void run() {
                Process process = null;
                Thread writeThread = null;
                try {
                    process = new ProcessBuilder()
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
                        parseLine(line);
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

    private void parseLine(String line) {
        if (line.startsWith("CMD:SEARCH:")) {
            processSearchResponse(line);
            return;
        }
        if (line.startsWith("CMD:RecordInfo:")) {
            processRecordInfoResponse(line);
            return;
        }
        /*
        if (line.startsWith("CMD:xxx:")) {
            processXXXResponse(line);
            return;
        }
        */
    }

    private String recrodInfoRegexpPattern = "\\{id = (.*?), artist = (.*?), title = (.*?), label = (.*?), genre = \\[(.*?)\\], url = (.*?), thumbnail = (.*?)\\}";
    private Pattern commandSearchStartPattern = Pattern.compile("^CMD:SEARCH:(.*?):START$");
    private Pattern commandSearchRecordsPattern = Pattern.compile("^CMD:SEARCH:(.*?):RES:\\[(\\d+)\\] = " + recrodInfoRegexpPattern + "$");
    private Pattern commandSearchEndPattern = Pattern.compile("^CMD:SEARCH:(.*?):END$");

    private Record recordFromMatcher(Matcher m, int offset) {
        Record record = new Record();
        record.id = m.group(offset + 0);
        record.artist = m.group(offset + 1);
        record.title = m.group(offset + 2);
        record.label = m.group(offset + 3);
        record.genre = m.group(offset + 4);
        record.url = m.group(offset + 5);
        record.thumbnailUrl = m.group(offset + 6);
        return record;
    }

    private void processSearchResponse(String line) {
        Log.d(TAG, "processSearchResponse");
        Matcher m1 = commandSearchStartPattern.matcher(line);
        if (m1.find()) {
            String searchWord = m1.group(1);
            // TODO: remove count argument.
            service.onSearchResponseStart(0);
            return;
        }

        Matcher m2 = commandSearchRecordsPattern.matcher(line);
        if (m2.find()) {
            String searchWord = m2.group(1);
            int index = Integer.parseInt(m2.group(2));
            // TODO: LTSV?
            /*
            Record record = new Record();
            record.id = m2.group(3);
            record.artist = m2.group(4);
            record.title = m2.group(5);
            record.label = m2.group(6);
            record.genre = m2.group(7);
            record.url = m2.group(8);
            record.thumbnailUrl = m2.group(9);
            service.onSearchResponseEachRecord(index, record);
            */
            service.onSearchResponseEachRecord(index, recordFromMatcher(m2, 3));
            return;
        }
        Matcher m3 = commandSearchEndPattern.matcher(line);
        if (m3.find()){
            String searchWord = m3.group(1);
            service.onSearchResponseEnd();
            return;
        }
    }

    private Pattern commandRecordInfoStartPattern = Pattern.compile("^CMD:RecordInfo:(.*?):START$");
    private Pattern commandRecordInfoBodyPattern = Pattern.compile("^CMD:RecordInfo:(.*?):RECORD_INFO:\\[(\\d+)\\] = " + recrodInfoRegexpPattern + "$");
    private Pattern commandRecordInfoRecordsByTheSameLabelPattern = Pattern.compile("^CMD:RecordInfo:(.*?):BY_THE_SAME_LABEL:\\[(\\d+)\\] = " + recrodInfoRegexpPattern + "$");
    private Pattern commandRecordInfoRecordsAlsoBoughtPattern = Pattern.compile("^CMD:RecordInfo:(.*?):ALSO_BOUGHT:\\[(\\d+)\\] = " + recrodInfoRegexpPattern + "$");
    private Pattern commandRecordInfoRecordsByTheSameArtistPattern = Pattern.compile("^CMD:RecordInfo:(.*?):BY_THE_SAME_ARTIST:\\[(\\d+)\\] = " + recrodInfoRegexpPattern + "$");
    private Pattern commandRecordInfoRecordsYouMightLikePattern = Pattern.compile("^CMD:RecordInfo:(.*?):YOU_MIGHT_LIKE:\\[(\\d+)\\] = " + recrodInfoRegexpPattern + "$");
    private Pattern commandRecordInfoEndPattern = Pattern.compile("^CMD:RecordInfo:(.*?):END$");

    private void processRecordInfoResponse(String line) {
        Log.d(TAG, "processRecordInfoResponse");
        Matcher m1 = commandRecordInfoStartPattern.matcher(line);
        if (m1.find()) {
            String recordId = m1.group(1);
            service.onRecordInfoResponseStart();
            return;
        }

        Matcher m2 = commandRecordInfoBodyPattern.matcher(line);
        if (m2.find()) {
            String recordId = m2.group(1);
            int index = Integer.parseInt(m2.group(2));
            service.onRecordInfoResponseBody(index, recordFromMatcher(m2, 3));
            return;
        }
        Matcher m3 = commandRecordInfoRecordsByTheSameLabelPattern.matcher(line);
        if (m3.find()) {
            String recordId = m3.group(1);
            int index = Integer.parseInt(m3.group(2));
            service.onRecordInfoResponseRecordsByTheSameLabel(index, recordFromMatcher(m3, 3));
            return;
        }
        Matcher m4 = commandRecordInfoRecordsAlsoBoughtPattern.matcher(line);
        if (m4.find()) {
            String recordId = m4.group(1);
            int index = Integer.parseInt(m4.group(2));
            service.onRecordInfoResponseRecordsAlsoBought(index, recordFromMatcher(m4, 3));
            return;
        }
        Matcher m5 = commandRecordInfoRecordsByTheSameArtistPattern.matcher(line);
        if (m5.find()) {
            String recordId = m5.group(1);
            int index = Integer.parseInt(m5.group(2));
            service.onRecordInfoResponseRecordsByTheSameArtist(index, recordFromMatcher(m5, 3));
            return;
        }
        Matcher m6 = commandRecordInfoRecordsYouMightLikePattern.matcher(line);
        if (m6.find()) {
            String recordId = m6.group(1);
            int index = Integer.parseInt(m6.group(2));
            service.onRecordInfoResponseRecordsYouMightLike(index, recordFromMatcher(m6, 3));
            return;
        }

        Matcher m7 = commandRecordInfoEndPattern.matcher(line);
        if (m7.find()) {
            String recordId = m7.group(1);
            service.onRecordInfoResponseEnd();
            return;
        }
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

